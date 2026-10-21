"""Verify the generated bank against the fork; measure host DSP, never device latency.

Run with the fork's host Python/dependencies: python tools/tests/check_genny_fork.py ../genny-tts
"""
import hashlib
import importlib.util
import json
from pathlib import Path
import statistics
import struct
import sys
import time
import wave

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location("bank_builder", ROOT/"tools/Build-Genny-Voice.py")
builder = importlib.util.module_from_spec(spec)
spec.loader.exec_module(builder)
source, fork, signature = builder.source_signature(Path(sys.argv[1]).resolve())
assert fork and signature["native_dsp"], "This check requires the fork and its compiled DSP"
import numpy as np
import genny_bridge as bridge

metadata = json.loads((ROOT/"core/assets/voice/genny-es.json").read_text(encoding="utf-8"))
assert metadata["synthesis"] == signature, "Bank provenance differs from the current source"
raw = (ROOT/"core/assets/voice/genny-es.bin").read_bytes()
assert hashlib.sha256(raw).hexdigest() == metadata["bank_sha256"]
magic, rate, count = struct.unpack_from(">4sii", raw)
assert magic == b"GNV1" and rate == builder.RATE and count == metadata["entries"]
words = {}
offset = 12
for _ in range(count):
    size, = struct.unpack_from(">H", raw, offset)
    offset += 2
    key = raw[offset:offset+size].decode("utf-8")
    offset += size
    size, = struct.unpack_from(">i", raw, offset)
    offset += 4
    words[key] = raw[offset:offset+size]
    offset += size
assert offset == len(raw)
for word in ("tres", "raíz", "cuatro", "sobre", "@r", "y"):
    assert builder.render((word, word[1:].upper() if word.startswith("@") else word))[1] == words[word], word

text = "Pero, perro. Caro, carro. Tres, comer."
params = dict(builder.PARAMS)
measurements = {}
signals = {}
for name, function in (("python_scipy", bridge._ORIGINAL_TV_FILTER), ("native_c", bridge._fast_tv_filter)):
    bridge.speech._tv_filter = function
    bridge.render(text, params, {}, rate)  # warm-up outside the measurement
    timings = []
    for _ in range(5):
        start = time.perf_counter()
        signal = bridge.render(text, params, {}, rate)
        timings.append((time.perf_counter()-start)*1000)
    measurements[name] = round(statistics.median(timings), 3)
    signals[name] = signal
np.testing.assert_allclose(signals["python_scipy"], signals["native_c"], rtol=1e-8, atol=1e-9)
out = ROOT/"core/tests/out"
out.mkdir(parents=True, exist_ok=True)
old = bridge.speech.render_speech(text, sr=rate, **params)
for name, signal in (("original", old), ("fork", signals["native_c"])):
    with wave.open(str(out/f"genny-r-{name}.wav"), "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(rate)
        wav.writeframes((np.clip(signal, -1, 1)*24000).astype("<i2").tobytes())
report = {"bank_entries": count, "bank_matches_fork": True, "host_median_ms": measurements,
          "speedup": round(measurements["python_scipy"]/measurements["native_c"], 2),
          "max_sample_error": float(np.max(np.abs(signals["python_scipy"]-signals["native_c"]))),
          "scope": "Host synthesis only; not Android latency or perceptual intelligibility."}
(out/"genny-fork-checks.json").write_text(json.dumps(report, indent=2)+"\n", encoding="utf-8")
print(json.dumps(report, indent=2))
