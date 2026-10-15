const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const source=fs.readFileSync(require('path').join(__dirname,'app.js'),'utf8').split('let session;')[0];const box={};vm.createContext(box);vm.runInContext(source,box);
for(const family of ['triangle','quad','open','curve','nested'])for(let seed=0;seed<500;seed++){
 const f=box.makeFigure(seed,family);assert.equal(JSON.stringify(f),JSON.stringify(box.makeFigure(seed,family)));
 for(const shape of f.shapes)for(const [x,y]of shape.points){assert(x>0&&x<600);assert(y>0&&y<450);}
 assert.equal(f.closed,family!=='open');assert.equal(f.vertices.length,{triangle:3,quad:4,open:4,curve:0,nested:7}[family]);
 assert.equal(f.regions.length,family==='open'?0:family==='nested'?2:1);
}
assert.equal(box.distance([5,4],[0,0],[10,0]),4);assert.equal(box.distance([0,0],[1,0],[2,0]),1);
console.log('PASS: 2500 reproducible figures; boundaries, closure, vertices, regions and hit distance.');
