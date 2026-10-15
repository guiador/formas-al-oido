// Repack original MIT KEMAR stereo WAVs without changing their 44.1 kHz samples.
const fs=require('fs'),path=require('path'),assert=require('assert/strict');
const root=path.join(__dirname,'kemar'),records=[];
for(const dir of fs.readdirSync(root))for(const file of fs.readdirSync(path.join(root,dir))){
 const m=file.match(/^H(-?\d+)e(\d+)a\.wav$/i);if(!m)continue;
 const elevation=+m[1],azimuth=+m[2];if(elevation < -40||elevation>40||azimuth>90)continue;
 const wav=fs.readFileSync(path.join(root,dir,file));assert.equal(wav.toString('ascii',0,4),'RIFF');let pcm;
 for(let pos=12;pos+8<=wav.length;){const id=wav.toString('ascii',pos,pos+4),n=wav.readUInt32LE(pos+4);if(id==='fmt '){assert.equal(wav.readUInt16LE(pos+8),1);assert.equal(wav.readUInt16LE(pos+10),2);assert.equal(wav.readUInt32LE(pos+12),44100);assert.equal(wav.readUInt16LE(pos+22),16);}if(id==='data')pcm=wav.subarray(pos+8,pos+8+n);pos+=8+n+(n&1);}
 assert.equal(pcm?.length,512);records.push({elevation,azimuth,pcm});
}
records.sort((a,b)=>a.elevation-b.elevation||a.azimuth-b.azimuth);
const out=Buffer.alloc(12+records.length*(8+512));out.write('HRIR',0);out.writeInt32BE(44100,4);out.writeInt32BE(records.length,8);let pos=12;
for(const r of records){out.writeInt32BE(r.elevation,pos);out.writeInt32BE(r.azimuth,pos+4);pos+=8;for(let i=0;i<256;i++,pos+=2)out.writeInt16BE(r.pcm.readInt16LE(i*2),pos);}
fs.writeFileSync(path.join(__dirname,'../assets/kemar.bin'),out);console.log(`${records.length} HRIR positions, ${out.length} bytes, 128 stereo taps at 44100 Hz.`);
