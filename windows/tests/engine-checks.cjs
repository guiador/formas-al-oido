'use strict';
const fs=require('fs'),path=require('path'),assert=require('assert'),{spawn}=require('child_process'),readline=require('readline');
const root=path.resolve(__dirname,'..');
function pngFixture(){const zlib=require('zlib');function crc(b){let c=0xffffffff;for(const x of b){c^=x;for(let k=0;k<8;k++)c=(c>>>1)^((c&1)?0xedb88320:0);}return (c^0xffffffff)>>>0;}function chunk(type,data){const t=Buffer.from(type),length=Buffer.alloc(4),sum=Buffer.alloc(4);length.writeUInt32BE(data.length);sum.writeUInt32BE(crc(Buffer.concat([t,data])));return Buffer.concat([length,t,data,sum]);}const header=Buffer.alloc(13);header.writeUInt32BE(64,0);header.writeUInt32BE(64,4);header[8]=8;header[9]=6;const rows=[];for(let y=0;y<64;y++){const row=Buffer.alloc(1+64*4);for(let x=0;x<64;x++){const black=x>10&&x<54&&y>10&&y<54&&!(x>25&&x<39&&y>25&&y<39);const i=1+x*4;row[i]=row[i+1]=row[i+2]=black?0:255;row[i+3]=255;}rows.push(row);}return Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',header),chunk('IDAT',zlib.deflateSync(Buffer.concat(rows))),chunk('IEND',Buffer.alloc(0))]);}
function machine(file){const b=fs.readFileSync(file);return b.readUInt16LE(b.readUInt32LE(0x3c)+4);}
async function run(arch){
 const folder=path.join(root,'build','Formas-al-oido-Windows-'+arch),java=path.join(folder,'runtime','bin','java.exe');
 assert.equal(machine(path.join(folder,'Formas-al-oido.exe')),arch==='x86'?0x14c:0x8664);
 assert.equal(machine(java),arch==='x86'?0x14c:0x8664);
 const child=spawn(java,['-Xmx160m','-cp',path.join(folder,'formas-engine.jar')+';'+path.join(folder,'jna.jar'),'DesktopEngine',path.join(folder,'kemar.bin'),'--silent'],{windowsHide:true});
 const queue=[],waiters=[];let lessonCount=0;const lines=readline.createInterface({input:child.stdout});let errors='';child.stderr.on('data',s=>errors+=s);
 lines.on('line',line=>{if(line.startsWith('LESSON|'))lessonCount++;if(waiters.length)waiters.shift()(line);else queue.push(line);});
 function next(){return new Promise((resolve,reject)=>{if(queue.length)return resolve(queue.shift());let callback;const timeout=setTimeout(()=>{const i=waiters.indexOf(callback);if(i>=0)waiters.splice(i,1);reject(Error('Engine timeout '+errors));},6000);callback=x=>{clearTimeout(timeout);resolve(x);};waiters.push(callback);});}
 async function until(prefix){for(let i=0;i<100;i++){const line=await next();assert(!line.startsWith('ERROR|'),line);if(line.startsWith(prefix))return line;}throw Error('Response not found '+prefix);}
 function send(s){child.stdin.write(s+'\n');}
 function scene(line){let count=0,holes=0;for(const s of line.split('|')[2].split('/').filter(Boolean)){let [flags,data]=s.split(':');if(flags.split(',')[1]==='1')holes++;for(const point of data.split(';').filter(Boolean)){const [x,y]=point.split(',').map(Number);assert(Number.isFinite(x)&&Number.isFinite(y));assert(x>=0&&x<=600&&y>=0&&y<=450);count++;}}return {count,holes};}
 try{await until('READY');assert.equal(lessonCount,29);const mathEncode=s=>Buffer.from(s).toString('base64');
  for(const [input,expected] of [['1/2+1/3','5/6'],['2x+3=11','x = 4'],['rad: sin(pi/2)','≈ 1']]){send('MATH|'+mathEncode(input)+'|41');const line=await until('MATHDATA|');assert.equal(line.split('|')[2],'41');const data=JSON.parse(Buffer.from(line.split('|')[1],'base64').toString());assert.equal(data.result,expected);send('MATH_LOAD|'+mathEncode(data.project)+'|42');assert.equal((await until('MATHDATA|')).split('|')[2],'42');}
  send('MATH|'+mathEncode('1/0')+'|43');assert(Buffer.from((await until('MATHERROR|')).split('|')[1],'base64').toString().includes('cero'));send('PING');await until('PONG');
  for(let n=0;n<8;n++){send('MATH_PLANE|'+n);assert(scene(await until('MATHPLANE|')).count>4);}
  send('MATH_TRACE|2|999');let point=JSON.parse(Buffer.from((await until('MATHPOINT|')).split('|')[1],'base64').toString());assert.equal(point.count,5);assert.equal(point.index,4);send('MATH_PROJECT|5|0');point=JSON.parse(Buffer.from((await until('MATHPOINT|')).split('|')[1],'base64').toString());assert.equal(point.y,0);
  send('MATH_LOAD|'+mathEncode(JSON.stringify({schema:'formas-math-plane-v1',plane:2,x:-3,y:-2}))+'|44');const restored=JSON.parse(Buffer.from((await until('MATHRESTORE|')).split('|')[1],'base64').toString());assert.equal(restored.plane,2);assert(restored.description.includes('Esquina'));for(let lesson=0;lesson<29;lesson++){send('GUIDE|'+lesson+'|0|70');assert(scene(await until('SCENE|')).count>0);}
  for(let mode=0;mode<4;mode++){send('SET|1|'+mode+'|0.28|true|0.7|1|0.6|65|35|true|false|0|true|18');send('TOUCH|300|225');await until('FEEDBACK|');send('HOLD');send('STOP');send('CONTOUR');send('STOP');send('PING');await until('PONG');}
  send('DEMO|1');assert.equal(scene(await until('SCENE|')).holes,1);
  send('ELEMENT|0|0');await until('DESCRIPTION|');send('STOP');
  send('OVERVIEW');assert(Buffer.from((await until('ORIENTATION|')).split('|')[1],'base64').toString().includes('geometría disponible'));
  send('WHERE|300|225');assert(Buffer.from((await until('ORIENTATION|')).split('|')[1],'base64').toString().includes('puntero'));
  send('CONTOUR');for(const action of ['pause','mark','back','return','position']){send('CONTROL|'+action);const report=Buffer.from((await until('ORIENTATION|')).split('|')[1],'base64').toString();assert(!report.includes('No hay un recorrido'),report);}
  send('CONTROL|resume');await until('STATUS|');send('CONTROL|fragment');await until('STATUS|');send('STOP');send('CONTROL|return');assert(Buffer.from((await until('ORIENTATION|')).split('|')[1],'base64').toString().includes('No hay un recorrido'));
  send('DRAW_POINT|20|30|true|'+Buffer.from('Coordenadas').toString('base64'));await until('TRACES|');send('DRAW_POINT|120|30|false|');assert.equal(scene((await until('TRACES|')).replace(/^TRACES\|[^|]+/,'SCENE|text')).count,2);send('DRAW_CLEAR');await until('TRACES|');
  send('DRAW_BEGIN|100|100|0|'+Buffer.from('ruta').toString('base64'));send('DRAW_MOVE|180|120|200');send('DRAW_MOVE|180|200|500');send('DRAW_END');assert.equal(scene((await until('TRACES|')).replace(/^TRACES\|[^|]+/,'SCENE|text')).count,3);
  send('DRAW_EXPORT');const payload=(await until('PROJECT|')).split('|')[1];assert(payload.length>20);
  send('DRAW_CLEAR');await until('TRACES|');send('DRAW_IMPORT|'+payload);await until('TRACES|');send('DRAW_CLOSE|true');await until('TRACES|');send('LAYER|1');send('DRAW_PLAY|0|0');send('STOP');
  for(let mode=0;mode<5;mode++){send('SCAN|'+mode+'|600|450|40|0|128|255|false');if(mode<4)await until('STATUS|');send('STOP');send('PING');await until('PONG');}
  const fixture=path.join(folder,'vision-fixture.png');fs.writeFileSync(fixture,pngFixture());send('IMAGE|'+Buffer.from(fixture).toString('base64')+'|0|192|0');await until('VISION|');const original=scene(await until('SCENE|'));assert.equal(original.holes,1);await until('IMAGEDONE|');
  const vision={overview:'Prueba de una hipótesis',source:'Prueba de protocolo',regions:[{box:[0,0,1,1],label:'Podría ser un marco',analogy:'recuerda a un cuadrado'}]};send('VISION_DATA|'+Buffer.from(JSON.stringify(vision)).toString('base64'));await until('VISION|');await until('SCENE|');send('IMAGE_FOCUS|0');assert.equal(scene(await until('SCENE|')).holes,1);send('VISION_CORRECT|0|'+Buffer.from('Anotación propia').toString('base64')+'|');const corrected=JSON.parse(Buffer.from((await until('VISION|')).split('|')[1],'base64').toString());assert.equal(corrected.regions[0].source,'Anotación del usuario');send('IMAGE_RESTORE');assert.equal(scene(await until('SCENE|')).count,original.count);fs.unlinkSync(fixture);
  send('DRAW_DEMO|0');let traces=await until('TRACES|');assert.equal(Buffer.from(traces.split('|')[1],'base64').toString().trim().split('\n').length,5);send('QUIT');
  await new Promise((resolve,reject)=>{const timer=setTimeout(()=>{child.kill();reject(Error('Shutdown timeout'));},5000);child.once('exit',code=>{clearTimeout(timer);code===0?resolve():reject(Error('Exit '+code+' '+errors));});});
  console.log('PASS '+arch+': PE architecture, bundled Java, 29 lesson scenes, bounds, 4 audio settings, touch/hold/stop protocol, image hole, element description and clean shutdown.');
 }finally{if(child.exitCode===null)child.kill();}
}
(async()=>{for(const arch of ['x86','x64'])await run(arch);})().catch(e=>{console.error(e);process.exitCode=1;});
