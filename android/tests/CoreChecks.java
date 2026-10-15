import org.formasaloido.*;
import java.io.*;
import java.util.*;

public final class CoreChecks {
 static int checks=0;
 static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  for(int family=1;family<=5;family++)for(int seed=0;seed<500;seed++){
   Geometry g=new Geometry(seed,family),repeat=new Geometry(seed,family);
   check(g.closed()==(family!=3),"closure");check(g.elements(1).size()==new int[]{0,3,4,4,0,7}[family],"vertex count");check(g.elements(2).size()==(family==3?0:family==5?2:1),"regions");check(g.elements(0).size()==(family==4?1:family==5?7:family==1?3:family==2?4:3),"curve is one element");
   for(int j=0;j<g.shapes.size();j++)for(int i=0;i<g.shapes.get(j).points.size();i++){Geometry.Point p=g.shapes.get(j).points.get(i),q=repeat.shapes.get(j).points.get(i);check(p.x>0&&p.x<600&&p.y>0&&p.y<450,"canvas bounds");check(p.x==q.x&&p.y==q.y,"reproducibility");}
  }
  check(Geometry.segmentDistance(new Geometry.Point(5,4),new Geometry.Point(0,0),new Geometry.Point(10,0))==4,"hit distance");
  Sound.HRIR h;try(InputStream in=new FileInputStream(args[0])){h=new Sound.HRIR(in);}
  float[] l=new float[128],r=new float[128],ml=new float[128],mr=new float[128];h.filter(80,0,l,r);h.filter(-80,0,ml,mr);double le=0,re=0;for(int i=0;i<128;i++){check(Math.abs(l[i]-mr[i])<.000001&&Math.abs(r[i]-ml[i])<.000001,"left right mirror");le+=l[i]*l[i];re+=r[i]*r[i];}check(re>le,"right HRIR reaches right ear more strongly");h.filter(0,0,l,r);for(int i=0;i<128;i++)check(Float.isFinite(l[i])&&Float.isFinite(r[i]),"finite taps");
  Sound.Settings s=new Sound.Settings();Geometry.Point a=new Geometry.Point(100,350),b=new Geometry.Point(500,90);Sound.Program longer=new Sound.Program(s).move(a,b),shorter=new Sound.Program(s).move(a,new Geometry.Point(150,320));check(longer.duration>shorter.duration,"length proportional to duration");
  for(int family=1;family<=5;family++){Geometry g=new Geometry(42,family);Sound.Program p=Sound.Program.contour(g,s);check(p.duration>0&&p.duration<30,"finite contour");check(p.spans.get(0).kind.equals("start"),"start marker");check(p.spans.get(p.spans.size()-1).kind.equals(g.closed()?"closed":"end"),"terminal marker");}
  for(int lesson=0;lesson<Curriculum.LESSONS.size();lesson++)for(int variant=0;variant<16;variant++)for(double gap:new double[]{8,40,120}){
   Geometry scene=Curriculum.scene(lesson,variant,gap);check(!scene.shapes.isEmpty(),"lesson has geometry");
   for(Geometry.Shape shape:scene.shapes)for(Geometry.Point point:shape.points)check(point.x>=0&&point.x<=600&&point.y>=0&&point.y<=450,"lesson bounds: "+Curriculum.LESSONS.get(lesson).id);
   Sound.Program plan=Sound.Program.contour(scene,s);check(!plan.spans.isEmpty()&&plan.duration<60,"finite guided scene");double end=0;for(Sound.Span span:plan.spans){check(span.start>=end-.000001&&span.duration>0,"non overlapping scan spans");end=span.start+span.duration;}
   check(Curriculum.ordered(scene,0,4,null).size()==scene.shapes.size(),"all components restored");
   List<Geometry.Shape> focused=Curriculum.ordered(scene,10,3,new Geometry.Point(300,225));check(focused.size()==1&&focused.get(0)==scene.shapes.get(0),"component focus preserves original geometry");
   for(int filter=1;filter<4;filter++)for(Geometry.Shape shape:Curriculum.ordered(scene,filter,1,null))check(filter==1?shape.points.size()==1:filter==2?!shape.curve&&shape.points.size()>1:shape.curve,"filter consistency");
  }
  Geometry point=Curriculum.scene(1,0,70);TouchMap map=new TouchMap();
  check(map.sample(point,new Geometry.Point(300,225),0,0,10,false).kind.equals("vertex"),"point found");
  check(map.sample(point,new Geometry.Point(314,225),0,0,10,false).kind.equals("vertex"),"contact hysteresis");
  check(map.sample(point,new Geometry.Point(325,225),0,0,10,false).kind.equals("silent"),"contact released");
  check(map.sample(point,new Geometry.Point(314,225),0,0,10,false).kind.equals("silent"),"no reentry at outer hysteresis limit");
  check(map.sample(point,new Geometry.Point(50,50),0,0,10,true).kind.equals("air"),"optional background");
  check(map.sample(point,new Geometry.Point(5,225),0,0,10,true).frame,"canvas warning");
  check(map.sample(point,new Geometry.Point(-5,225),0,0,10,true).kind.equals("silent"),"outside canvas");
  Geometry square=null,gapped=null;for(int i=0;i<Curriculum.LESSONS.size();i++){if(Curriculum.LESSONS.get(i).id.equals("square"))square=Curriculum.scene(i,0,70);if(Curriculum.LESSONS.get(i).id.equals("gap"))gapped=Curriculum.scene(i,0,70);}
  map.reset();check(map.sample(square,new Geometry.Point(300,225),1,0,10,false).kind.equals("region"),"fill inside");
  check(map.sample(square,new Geometry.Point(70,225),1,0,10,false).kind.equals("silent"),"fill outside");
  check(map.sample(square,new Geometry.Point(70,225),2,0,10,false).kind.equals("region"),"negative outside");
  check(map.sample(square,new Geometry.Point(300,225),2,0,10,false).kind.equals("silent"),"negative inside");
  check(map.sample(gapped,new Geometry.Point(300,225),0,0,6,false).kind.equals("silent"),"gap remains absent under contact tolerance");
  for(int i=0;i<Curriculum.LESSONS.size();i++)if(Curriculum.LESSONS.get(i).id.equals("gap"))for(double gap:new double[]{8,15,40,120}){map.reset();Geometry g=Curriculum.scene(i,0,gap);check(map.sample(g,new Geometry.Point(300,225),0,0,35,false).kind.equals("silent"),"tolerance never fills even a small missing segment");}
  for(Sound.Span span:Sound.Program.contour(gapped,s).spans)if(span.kind.equals("line"))check(!(span.a.x<300&&span.b.x>300),"scan never invents a bridge across gap");
  check(Geometry.description(square.elements(2).get(0),2).startsWith("Centro de la región"),"region description remains region");
  double[] energy=new double[2];long started=System.nanoTime();for(int profile=0;profile<3;profile++)for(int spatial=0;spatial<3;spatial++){
   s.profile=profile;s.spatial=spatial;Sound.Renderer renderer=new Sound.Renderer(h);renderer.program(Sound.Program.guide(s));short[] buffer=new short[512];double e=0;for(int block=0;block<350;block++){renderer.render(buffer,256,s);for(short value:buffer){check(Math.abs((int)value)<28000,"bounded PCM");e+=(double)value*value;}}check(e>100000,"non silent PCM");
  }
  for(int side=0;side<2;side++){s.spatial=1;Sound.Renderer renderer=new Sound.Renderer(h);renderer.touch(new Geometry.Point(side==0?0:600,225),"region");short[] buffer=new short[512];double el=0,er=0;for(int block=0;block<60;block++){renderer.render(buffer,256,s);for(int i=0;i<256;i++){el+=(double)buffer[i*2]*buffer[i*2];er+=(double)buffer[i*2+1]*buffer[i*2+1];}}check(side==0?el>er:er>el,"audible channel orientation");}
  // Mono survives channel summation; disabling lateral code removes all X information.
  for(int profile=0;profile<3;profile++)for(boolean enabled:new boolean[]{false,true})for(double amount:new double[]{0,.7,1}){
   Sound.Settings ms=new Sound.Settings();ms.spatial=2;ms.profile=profile;ms.monoLateral=enabled;ms.monoAmount=amount;
   Sound.Settings copied=ms.copy();check(copied.monoLateral==enabled&&copied.monoAmount==amount,"mono settings copied");
   short[][] samples=new short[3][Sound.RATE*4];
   for(int side=0;side<3;side++){Sound.Renderer monoRenderer=new Sound.Renderer(h);monoRenderer.touch(new Geometry.Point(side*300,225),"line");monoRenderer.render(samples[side],Sound.RATE*2,ms);for(int j=0;j<samples[side].length;j+=2){check(samples[side][j]==samples[side][j+1],"mono channels identical");check(Math.abs((int)samples[side][j])<28000,"mono bounded");}}
   check(enabled&&amount>0?!Arrays.equals(samples[0],samples[2]):Arrays.equals(samples[0],samples[2]),"mono lateral code switches off completely");
   if(enabled&&amount>0)check(!Arrays.equals(samples[0],samples[1])&&!Arrays.equals(samples[1],samples[2]),"three distinct lateral codes");
  }
  // Changing spatial controls while rendering must stay finite and bounded.
  Sound.Renderer stress=new Sound.Renderer(h);stress.touch(new Geometry.Point(600,0),"line");short[] buffer=new short[512];for(int i=0;i<50;i++){s.spatial=i%3;s.volume=.6;s.width=i%2==0?90:15;s.pitch=1;s.elevation=40;s.swap=i%3==0;stress.render(buffer,256,s);for(short value:buffer)check(Math.abs((int)value)<28000,"settings changes bounded");}
  System.out.println("PASS "+checks+" assertions: 2500 geometries, HRTF mirror/orientation, path markers, PCM levels and settings. Audio checks elapsed "+((System.nanoTime()-started)/1_000_000)+" ms.");
 }
}
