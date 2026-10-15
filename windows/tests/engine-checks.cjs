'use strict';
const fs=require('fs'),path=require('path'),assert=require('assert'),{spawn}=require('child_process'),readline=require('readline');
const root=path.resolve(__dirname,'..');
function machine(file){const b=fs.readFileSync(file);return b.readUInt16LE(b.readUInt32LE(0x3c)+4);}
async function run(arch){
 const folder=path.join(root,'build','Formas-al-oido-Windows-'+arch),java=path.join(folder,'runtime','bin','java.exe');
 assert.equal(machine(path.join(folder,'Formas-al-oido.exe')),arch==='x86'?0x14c:0x8664);
 assert.equal(machine(java),arch==='x86'?0x14c:0x8664);
 const child=spawn(java,['-Xmx160m','-cp',path.join(folder,'formas-engine.jar'),'DesktopEngine',path.join(folder,'kemar.bin'),'--silent'],{windowsHide:true});
 const queue=[],waiters=[];let lessonCount=0;const lines=readline.createInterface({input:child.stdout});let errors='';child.stderr.on('data',s=>errors+=s);
 lines.on('line',line=>{if(line.startsWith('LESSON|'))lessonCount++;if(waiters.length)waiters.shift()(line);else queue.push(line);});
 function next(){return new Promise((resolve,reject)=>{if(queue.length)return resolve(queue.shift());let callback;const timeout=setTimeout(()=>{const i=waiters.indexOf(callback);if(i>=0)waiters.splice(i,1);reject(Error('Engine timeout '+errors));},6000);callback=x=>{clearTimeout(timeout);resolve(x);};waiters.push(callback);});}
 async function until(prefix){for(let i=0;i<100;i++){const line=await next();assert(!line.startsWith('ERROR|'),line);if(line.startsWith(prefix))return line;}throw Error('Response not found '+prefix);}
 function send(s){child.stdin.write(s+'\n');}
 function scene(line){let count=0,holes=0;for(const s of line.split('|')[2].split('/').filter(Boolean)){let [flags,data]=s.split(':');if(flags.split(',')[1]==='1')holes++;for(const point of data.split(';').filter(Boolean)){const [x,y]=point.split(',').map(Number);assert(Number.isFinite(x)&&Number.isFinite(y));assert(x>=0&&x<=600&&y>=0&&y<=450);count++;}}return {count,holes};}
 try{await until('READY');assert.equal(lessonCount,29);for(let lesson=0;lesson<29;lesson++){send('GUIDE|'+lesson+'|0|70');assert(scene(await until('SCENE|')).count>0);}
  for(let mode=0;mode<3;mode++){send('SET|1|'+mode+'|0.28|true|0.7|1|0.6|65|35|true|false|0|true|18');send('TOUCH|300|225');await until('FEEDBACK|');send('HOLD');send('STOP');send('CONTOUR');send('STOP');send('PING');await until('PONG');}
  send('DEMO|1');assert.equal(scene(await until('SCENE|')).holes,1);
  send('ELEMENT|0|0');await until('DESCRIPTION|');send('STOP');send('QUIT');
  await new Promise((resolve,reject)=>{const timer=setTimeout(()=>{child.kill();reject(Error('Shutdown timeout'));},5000);child.once('exit',code=>{clearTimeout(timer);code===0?resolve():reject(Error('Exit '+code+' '+errors));});});
  console.log('PASS '+arch+': PE architecture, bundled Java, 29 lesson scenes, bounds, 3 audio settings, touch/hold/stop protocol, image hole, element description and clean shutdown.');
 }finally{if(child.exitCode===null)child.kill();}
}
(async()=>{for(const arch of ['x86','x64'])await run(arch);})().catch(e=>{console.error(e);process.exitCode=1;});
