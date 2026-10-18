package org.formasaloido;

import java.io.*;
import java.util.*;

/** PCM synthesis, path grammar and measured HRIR convolution; no Android dependency. */
public final class Sound {
 public static final int RATE=44100,TAPS=128;
 public static final class Settings {
  public int profile=0,spatial=1;public double monoAmount=.7,volume=.28,width=65,elevation=35,pitch=.65,speed=1,pause=.18,brightness=.6,lowHz=180,highHz=720;public boolean monoLateral=true,markers=true,swap=false;
  public Settings copy(){Settings s=new Settings();s.profile=profile;s.spatial=spatial;s.monoAmount=monoAmount;s.monoLateral=monoLateral;s.volume=volume;s.width=width;s.elevation=elevation;s.pitch=pitch;s.speed=speed;s.pause=pause;s.brightness=brightness;s.markers=markers;s.swap=swap;s.lowHz=lowHz;s.highHz=highHz;return s;}
 }
 public static final class HRIR {
  static final class Measurement {final double elevation,azimuth;final float[] left=new float[TAPS],right=new float[TAPS];Measurement(double e,double a){elevation=e;azimuth=a;}}
  final List<Measurement> data=new ArrayList<>();
  public HRIR(InputStream source)throws IOException{DataInputStream in=new DataInputStream(source);if(in.readInt()!=0x48524952||in.readInt()!=RATE)throw new IOException("Formato HRTF no válido");int n=in.readInt();if(n<10||n>1000)throw new IOException("Número HRTF no válido");for(int i=0;i<n;i++){Measurement m=new Measurement(in.readInt(),in.readInt());for(int j=0;j<TAPS;j++){m.left[j]=in.readShort()/32768f;m.right[j]=in.readShort()/32768f;}data.add(m);}}
  public void filter(double az,double el,float[] left,float[] right){
   double a=Math.min(90,Math.abs(az)),e=Math.max(-40,Math.min(40,el)),e0=Math.floor(e/10)*10,e1=Math.min(40,e0+10),t=e1==e0?0:(e-e0)/(e1-e0);Arrays.fill(left,0);Arrays.fill(right,0);
   addRow(a,e0,1-t,az<0,left,right);if(t>0)addRow(a,e1,t,az<0,left,right);
  }
  private void addRow(double a,double e,double weight,boolean mirror,float[] l,float[] r){Measurement lo=null,hi=null;for(Measurement m:data)if(m.elevation==e){if(m.azimuth<=a&&(lo==null||m.azimuth>lo.azimuth))lo=m;if(m.azimuth>=a&&(hi==null||m.azimuth<hi.azimuth))hi=m;}if(lo==null)lo=hi;if(hi==null)hi=lo;if(lo==null)return;double t=hi.azimuth==lo.azimuth?0:(a-lo.azimuth)/(hi.azimuth-lo.azimuth);for(int k=0;k<TAPS;k++){float x=(float)(weight*((1-t)*lo.left[k]+t*hi.left[k])),y=(float)(weight*((1-t)*lo.right[k]+t*hi.right[k]));l[k]+=mirror?y:x;r[k]+=mirror?x:y;}}
 }
 public static final class Span {
  public final Geometry.Point a,b;public final double start,duration;public final String kind;public final double frequency,gain;
  Span(Geometry.Point a,Geometry.Point b,double start,double duration,String kind,double frequency){this.a=a;this.b=b;this.start=start;this.duration=duration;this.kind=kind;this.frequency=frequency;this.gain=1;}
  Span(Geometry.Point p,double start,double duration,double gain){a=b=p;this.start=start;this.duration=duration;kind="raster";frequency=0;this.gain=gain;}
 }
 public static final class Program {
  public final List<Span> spans=new ArrayList<>();public double duration=0;final Settings settings;
  public Program(Settings s){settings=s.copy();}
  public Program silence(double seconds){duration+=seconds;return this;}
  public Program move(Geometry.Point a,Geometry.Point b){double d=Math.max(.012,a.distance(b)/160*settings.speed);spans.add(new Span(a,b,duration,d,"line",0));duration+=d;return this;}
  public Program moveTimed(Geometry.Point a,Geometry.Point b,double seconds){double d=Math.max(.012,Math.min(5,seconds));spans.add(new Span(a,b,duration,d,"line",0));duration+=d;return this;}
  public Program sample(Geometry.Point p,double seconds,double gain){double d=Math.max(.012,Math.min(.5,seconds));spans.add(new Span(p,duration,d,Math.max(0,Math.min(1,gain))));duration+=d;return this;}
  public Program append(Program p){for(Span v:p.spans){if(v.kind.equals("raster"))spans.add(new Span(v.a,duration+v.start,v.duration,v.gain));else spans.add(new Span(v.a,v.b,duration+v.start,v.duration,v.kind,v.frequency));}duration+=p.duration;return this;}
  public Program limited(double seconds){Program out=new Program(settings);for(Span v:spans){if(v.start>=seconds)break;double d=Math.min(v.duration,seconds-v.start);if(v.kind.equals("raster"))out.spans.add(new Span(v.a,v.start,d,v.gain));else{double t=d/v.duration;Geometry.Point b=new Geometry.Point(v.a.x+(v.b.x-v.a.x)*t,v.a.y+(v.b.y-v.a.y)*t,v.a.z+(v.b.z-v.a.z)*t);out.spans.add(new Span(v.a,b,v.start,d,v.kind,v.frequency));}}out.duration=Math.min(duration,seconds);return out;}
  public Program pulse(Geometry.Point p,double hz,double seconds,String kind){spans.add(new Span(p,p,duration,seconds,kind,hz));duration+=seconds;return this;}
  public Program marker(Geometry.Point p,String kind){if(!settings.markers)return this;double[] notes;
   switch(kind){case "start":notes=new double[]{600};break;case "corner":notes=new double[]{1300};break;case "turn":notes=new double[]{1000,750};break;case "bend":notes=new double[]{850};break;case "closed":notes=new double[]{660,880,660};break;default:notes=new double[]{440,330};}
   for(double hz:notes){pulse(p,hz,.065,kind);silence(.025);}return this;
  }
  public Program path(List<Geometry.Point> points,boolean curve,boolean closed){if(points.isEmpty())return this;marker(points.get(0),"start");silence(.08);double turn=0;
   for(int i=1;i<points.size();i++){Geometry.Point a=points.get(i-1),b=points.get(i);move(a,b);if(i<points.size()-1){Geometry.Point c=points.get(i+1);double ux=b.x-a.x,uy=b.y-a.y,vx=c.x-b.x,vy=c.y-b.y;if(curve){turn+=Math.abs(Math.atan2(ux*vy-uy*vx,ux*vx+uy*vy));boolean reversal=ux*vx<0||uy*vy<0;if(reversal||turn>=Math.PI/6){marker(b,reversal?"turn":"bend");turn=0;}}else{marker(b,"corner");silence(settings.pause);}}}
   silence(.04);marker(points.get(points.size()-1),closed?"closed":"end");return this;
  }
  public static Program element(Geometry.Element el,int level,Settings s){Program p=new Program(s);if(level==0&&el.points.size()>1)p.path(el.points,el.curve,el.curve&&el.points.get(0).distance(el.points.get(el.points.size()-1))<.01);else p.pulse(el.center(),0,.4,level==2?"region":"vertex");return p;}
  public static Program contour(Geometry g,Settings s){return contour(g,s,0,4,null);}
  public static Program contour(Geometry g,Settings s,int filter,int anchor,Geometry.Point local){Program p=new Program(s);Geometry.Point last=null;for(Geometry.Shape shape:Curriculum.ordered(g,filter,anchor,local)){List<Geometry.Point> points=new ArrayList<>(shape.points);if(last!=null)p.silence(Math.max(.025,last.distance(points.get(0))/160*s.speed));if(shape.points.size()==1){p.pulse(points.get(0),0,.3,"vertex");last=points.get(0);continue;}if(shape.closed)points.add(points.get(0));p.path(points,shape.curve,shape.closed);last=points.get(points.size()-1);}if(!p.spans.isEmpty())p.silence(.1);return p;}
  public static Program guide(Settings s){Program p=new Program(s);for(Geometry.Point q:Arrays.asList(new Geometry.Point(70,225),new Geometry.Point(530,225),new Geometry.Point(300,65),new Geometry.Point(300,385)))p.pulse(q,0,.4,"vertex").silence(.4);return p.path(Arrays.asList(new Geometry.Point(100,350),new Geometry.Point(500,90)),false,false);}
 }
 public static final class Renderer {
  public double azimuth=0,elevation=0;
  final HRIR hrir;final float[] history=new float[TAPS],left=new float[TAPS],right=new float[TAPS],targetL=new float[TAPS],targetR=new float[TAPS];int cursor=0,sampleCounter=0,spanIndex=0;double phase=0,amplitude=0,pan=0,monoPosition=0,tremoloPhase=0;long noise=7;
  Program program;long frame=0,touchAge=0;Geometry.Point touch;boolean touchActive=false;String touchKind="line";
  public Renderer(HRIR h){hrir=h;}
  boolean paused=false;long bookmark=-1,rangeEnd=Long.MAX_VALUE;
  public void program(Program p){program=p;frame=0;spanIndex=0;touchActive=false;paused=false;bookmark=-1;rangeEnd=Long.MAX_VALUE;}
  public void touch(Geometry.Point p,String kind){if(!touchActive||touch==null||touch.distance(p)>.1||!touchKind.equals(kind))touchAge=0;program=null;paused=false;bookmark=-1;touch=p;touchKind=kind;touchActive=true;}
  public void quiet(){program=null;touchActive=false;paused=false;bookmark=-1;}
  public boolean paused(){return paused;}
  public double seconds(){return program==null?0:Math.min(program.duration,frame/(double)RATE);}
  public double remaining(){return program==null?0:Math.max(0,program.duration-seconds());}
  private void seek(double seconds){frame=(long)(Math.max(0,Math.min(program.duration,seconds))*RATE);spanIndex=0;amplitude=0;Arrays.fill(history,0);rangeEnd=Long.MAX_VALUE;}
  /** Called on the audio thread (or under its lock), never from a UI thread. */
  public String control(String action){
   if(program==null)return "No hay un recorrido guardado. Inicia una reproducción; el contacto directo y Detener cancelan el recorrido.";
   switch(action){
    case "pause":paused=true;return "En pausa. "+location();
    case "resume":if(remaining()<=0)return "Recorrido terminado. Retrocede o vuelve al marcador para repetir.";paused=false;rangeEnd=Long.MAX_VALUE;return "Continuando recorrido.";
    case "back":seek(seconds()-5);paused=true;return "Retroceso de hasta cinco segundos. En pausa. "+location();
    case "mark":bookmark=frame;paused=true;return "Marcador guardado. En pausa. "+location();
    case "return":if(bookmark<0)return "Todavía no has guardado un marcador en este recorrido.";seek(bookmark/(double)RATE);paused=true;return "En el marcador. Pulsa Continuar. "+location();
    case "fragment":{
     if(program.duration<=0)return "Este recorrido está vacío.";
     double end=seconds();if(end<1.0/RATE)end=Math.min(5,program.duration);
     seek(Math.max(0,end-5));rangeEnd=(long)Math.ceil(end*RATE);paused=false;return "Repitiendo hasta cinco segundos; quedará en pausa al terminar. Continuar prosigue el recorrido.";
    }
    case "position":paused=true;return "En pausa. "+location();
    default:throw new IllegalArgumentException("Control desconocido");
   }
  }
  private String location(){double time=seconds();Span found=null;int n=0;for(Span s:program.spans){n++;if(time<s.start+s.duration){found=s;break;}}String prefix="Segundo "+Math.round(time)+" de "+Math.round(program.duration)+". ";if(found==null)return prefix+"Final del recorrido.";if(time<found.start)return prefix+"Pausa entre señales; la siguiente está "+Geometry.position(found.a)+".";double t=Math.max(0,Math.min(1,(time-found.start)/found.duration));Geometry.Point p=new Geometry.Point(found.a.x+(found.b.x-found.a.x)*t,found.a.y+(found.b.y-found.a.y)*t);return prefix+"Segmento sonoro "+n+" de "+program.spans.size()+", "+Geometry.position(p)+". "+(found.kind.equals("line")?"Trazo.":found.kind.equals("raster")?"Muestra de luminosidad.":"Señal de referencia o cambio.");}
  public boolean done(){return !touchActive&&(program==null||frame>=Math.ceil(program.duration*RATE));}
  public void render(short[] out,int frames,Settings s){for(int i=0;i<frames;i++){
   if(program!=null&&frame>=rangeEnd){paused=true;rangeEnd=Long.MAX_VALUE;}
   if(paused&&!touchActive){out[2*i]=out[2*i+1]=0;continue;}
   double x=300,y=225,level=0,hz=0;String kind="line";
   double depth=0;if(touchActive&&touch!=null){x=touch.x;y=touch.y;depth=touch.z;level=touchKind.equals("air")?.018:touchKind.equals("near")?.05:touchKind.equals("frame")?.12:.19;kind=touchKind;if(kind.equals("vertex"))level*=Math.exp(-touchAge/(.12*RATE));touchAge++;}
   else if(program!=null){double time=frame/(double)RATE;while(spanIndex<program.spans.size()&&time>=program.spans.get(spanIndex).start+program.spans.get(spanIndex).duration)spanIndex++;if(spanIndex<program.spans.size()){Span a=program.spans.get(spanIndex);if(time>=a.start){double t=Math.max(0,Math.min(1,(time-a.start)/a.duration));x=a.a.x+(a.b.x-a.a.x)*t;y=a.a.y+(a.b.y-a.a.y)*t;kind=a.kind;hz=a.frequency;double ramp=a.kind.equals("line")?.008:.012;boolean joinedBefore=spanIndex>0&&joins(program.spans.get(spanIndex-1),a),joinedAfter=spanIndex+1<program.spans.size()&&joins(a,program.spans.get(spanIndex+1));double attack=joinedBefore?1:(time-a.start)/ramp,release=joinedAfter?1:(a.start+a.duration-time)/ramp;level=.24*a.gain*Math.max(0,Math.min(1,Math.min(attack,release)));}}frame++;}
   // Smooth output control, retaining phase across all segments.
   if(program!=null&&spanIndex<program.spans.size()){Span a=program.spans.get(spanIndex);double progress=Math.max(0,Math.min(1,(frame/(double)RATE-a.start)/a.duration));depth=a.a.z+(a.b.z-a.a.z)*progress;}level/=1+Math.max(0,depth);
   amplitude+=(level-amplitude)*.012;double base=Math.sqrt(s.lowHz*s.highHz),pitch=hz>0?hz:base*Math.pow(s.highHz/s.lowHz,(.5-y/450)*s.pitch);phase+=2*Math.PI*pitch/RATE;if(phase>Math.PI*8)phase-=Math.PI*8;
   // A learned lateral code, identical in both channels; never claims virtual stereo.
   double amount=s.spatial==2&&s.monoLateral?Math.max(0,Math.min(1,s.monoAmount)):0;
   double desired=(Math.max(0,Math.min(600,x))/300-1)*amount;
   monoPosition+=(desired-monoPosition)*.002;
   double code=amount==0?0:monoPosition;
   double bright=Math.max(0,Math.min(1,s.brightness+code*.5));
   double mono=wave(phase,s.profile,bright,kind)*amplitude;
   tremoloPhase+=2*Math.PI*(4+code*2)/RATE;if(tremoloPhase>2*Math.PI)tremoloPhase-=2*Math.PI;
   // Preserve short corner/start/end cues; modulate only sustained material.
   if(code!=0&&(kind.equals("line")||kind.equals("raster")||kind.equals("region")))mono*=1-.28*Math.abs(code)+.28*Math.abs(code)*Math.cos(tremoloPhase);
   if(sampleCounter++%64==0){double az=(x/300-1)*s.width,el=(.5-y/450)*2*s.elevation;azimuth=s.swap?-az:az;elevation=el;if(hrir!=null&&s.spatial==1)hrir.filter(az,el,targetL,targetR);pan=Math.max(-1,Math.min(1,az/90));}
   double l=0,r=0;if(s.spatial==1&&hrir!=null){history[cursor]=(float)mono;int at=cursor;for(int k=0;k<TAPS;k++){left[k]+=(targetL[k]-left[k])*.002;right[k]+=(targetR[k]-right[k])*.002;l+=history[at]*left[k];r+=history[at]*right[k];at=(at-1)&(TAPS-1);}cursor=(cursor+1)&(TAPS-1);}else if(s.spatial==3){l=r=mono;}else if(s.spatial==2){l=r=mono*Math.sqrt(.5);}else{l=mono*Math.sqrt((1-pan)*.5);r=mono*Math.sqrt((1+pan)*.5);}
   double gain=s.volume*2;if(s.swap){double a=l;l=r;r=a;}out[2*i]=(short)(Math.tanh(l*gain)*28000);out[2*i+1]=(short)(Math.tanh(r*gain)*28000);
  }}
  private boolean joins(Span a,Span b){return a.kind.equals("line")&&b.kind.equals("line")&&Math.abs(a.start+a.duration-b.start)<.00001;}
  double wave(double p,int profile,double brightness,String kind){if(kind.equals("air")||kind.equals("near")){noise=(noise*1664525+1013904223)&0xffffffffL;return .7*Math.sin(p)+.3*((noise/2147483648.0)-1);}if(!kind.equals("line")&&!kind.equals("raster")&&!kind.equals("region")&&!kind.equals("vertex"))return .8*Math.sin(p)+.2*Math.sin(3*p);double v=0,weight=0;int count=3+(int)(brightness*10);for(int k=1;k<=count;k++){double a;if(profile==0)a=1.0/(k*k);else if(profile==1)a=Math.exp(-k*.28)/k;else{if(k%2==0)continue;a=1.0/k;}v+=a*Math.sin(k*p);weight+=a;}if(kind.equals("region"))v=.7*v+.15*Math.sin(p*1.25)+.15*Math.sin(p*1.5);return weight==0?0:v/weight;}
 }
}
