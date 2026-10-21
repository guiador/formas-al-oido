"""Build an offline word bank from the user's chosen Genny checkout (not bundled).

Usage: python tools/Build-Genny-Voice.py path/to/genny-checkout
Requires numpy and scipy. Only synthesized PCM, project vocabulary and provenance ship.
"""
import concurrent.futures
import argparse
import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
import time

ROOT = Path(__file__).resolve().parent.parent
RATE = 16000
PARAMS = {"lang": "es", "voice": "female", "rate": 1.12, "seed": 0}


def initialize(source, fork):
    sys.path.insert(0, str(source))
    global synthesize
    if fork:
        from genny_bridge import render as bridge_render
        synthesize = lambda text, raw=False: bridge_render(text, PARAMS, {}, RATE, raw)
    else:
        from genny.speech import render_speech
        synthesize = lambda text, raw=False: render_speech(
            None if raw else text, sr=RATE, phonemes=text if raw else None, **PARAMS)


def source_signature(checkout):
    """Hash actual synthesis sources and loaded native binary, not just upstream HEAD."""
    fork = (checkout/"app/src/main/python/genny_bridge.py").is_file()
    source = checkout/"app/src/main/python" if fork else checkout
    initialize(source, fork)
    import numpy as np
    import scipy
    files = sorted((source/"genny").glob("*.py"))
    revision_file = checkout/"revision.txt"
    if fork:
        files += [source/"genny_bridge.py", source/"spanish_r.py"]
        metadata = json.loads((checkout/"app/src/main/assets/upstream.json").read_text(encoding="utf-8-sig"))
        revision = metadata["revision"]
        from genny_bridge import _init_native_dsp
        native = _init_native_dsp()
        if native is not None:
            files.append(Path(native._name).resolve())
    else:
        native = None
        revision = revision_file.read_text().strip() if revision_file.exists() else subprocess.check_output(
            ["git", "-C", str(checkout), "rev-parse", "HEAD"], text=True).strip()
    hashes = {p.relative_to(source).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
    signature = {"revision": revision, "files": hashes, "fork_spanish_r": fork,
                 "native_dsp": native is not None, "sample_rate": RATE, "params": PARAMS,
                 "numpy": np.__version__, "scipy": scipy.__version__,
                 "generator_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest()}
    return source, fork, signature

def render(entry):
    import numpy as np
    key, text = entry
    if key == "y":
        signal = synthesize("I1", raw=True)
    else:
        signal = synthesize(text)
    if not len(signal) or not np.all(np.isfinite(signal)):
        raise ValueError(f"Invalid audio for {key}")
    # Remove render padding, retain quiet consonants and add short click-free edges.
    active = np.flatnonzero(np.abs(signal) > .001)
    if active.size:
        signal = signal[max(0, active[0]-80):min(len(signal), active[-1]+81)].copy()
    fade = min(64, len(signal)//2)
    if fade:
        signal[:fade] *= np.linspace(0, 1, fade)
        signal[-fade:] *= np.linspace(1, 0, fade)
    return key, (np.clip(signal, -1, 1)*24000).astype("<i2").tobytes()

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("checkout", type=Path)
    parser.add_argument("--workers", type=int, default=4)
    args = parser.parse_args()
    if not 1 <= args.workers <= 16:
        parser.error("--workers must be between 1 and 16")
    source, fork, signature = source_signature(args.checkout.resolve())
    words = set()
    for folder in ("core/src", "android/src", "windows/src"):
        for path in (ROOT/folder).rglob("*"):
            if path.suffix not in (".java", ".cs"):
                continue
            for literal in re.findall(r'"((?:[^"\\]|\\.)*)"', path.read_text(encoding="utf-8")):
                words.update(re.findall("[a-záéíóúüñ]+", literal.lower()))
    words.update(re.findall("[a-záéíóúüñ]+", (ROOT/"core/assets/vision/labels.tsv").read_text(encoding="utf-8").lower()))
    words.update("veintiún millón millones mil billón billones dieciséis veintidós veintitrés veintiséis".split())
    entries = [(word, word) for word in sorted(words) if len(word) <= 30]
    entries += [("@"+letter, letter.upper()) for letter in "abcdefghijklmnñopqrstuvwxyz"]
    # cache entries so adding UI vocabulary does not rebuild the whole voice.
    fingerprint = hashlib.sha256(json.dumps(signature, sort_keys=True).encode()).hexdigest()
    cache = ROOT/"tools/tts-cache/words"/fingerprint
    cache.mkdir(parents=True, exist_ok=True)
    pending = [entry for entry in entries if not (cache/(entry[0]+".pcm")).exists()]
    start = time.monotonic()
    with concurrent.futures.ProcessPoolExecutor(max_workers=args.workers,
            initializer=initialize, initargs=(source, fork)) as pool:
        for count, (key, pcm) in enumerate(pool.map(render, pending), 1):
            (cache/(key+".pcm")).write_bytes(pcm)
            if count % 100 == 0:
                print(f"Genny: {count}/{len(pending)} palabras, {time.monotonic()-start:.0f}s", flush=True)
    target = ROOT/"core/assets/voice"
    target.mkdir(parents=True, exist_ok=True)
    bank = target/"genny-es.bin"
    temporary = target/"genny-es.bin.tmp"
    with temporary.open("wb") as output:
        output.write(b"GNV1"+struct.pack(">ii", RATE, len(entries)))
        for key, _ in entries:
            encoded = key.encode("utf-8")
            pcm = (cache/(key+".pcm")).read_bytes()
            output.write(struct.pack(">H", len(encoded))+encoded+struct.pack(">i", len(pcm))+pcm)
    # Avoid replacing an identical bank (also avoids Windows reader locks).
    if bank.exists() and hashlib.sha256(temporary.read_bytes()).digest() == hashlib.sha256(bank.read_bytes()).digest():
        temporary.unlink()
    else:
        for attempt in range(6):
            try:
                temporary.replace(bank)
                break
            except PermissionError:
                if attempt == 5:
                    raise
                time.sleep(.2 * 2**attempt)
    provenance = {"source": "https://github.com/ogomez92/genny", "revision": signature["revision"], "speech_sha256": signature["files"]["genny/speech.py"], "sample_rate": RATE, "voice": "female", "language": "es", "entries": len(entries), "bank_sha256": hashlib.sha256(bank.read_bytes()).hexdigest(), "fallback": "Unknown words are spelled using Spanish letter names.", "synthesis": signature, "cache_fingerprint": fingerprint}
    (target/"genny-es.json").write_text(json.dumps(provenance, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
    print(f"Genny: {bank.stat().st_size} bytes, {len(entries)} entradas", flush=True)

if __name__ == "__main__":
    main()
