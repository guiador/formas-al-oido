package org.formasaloido;

import java.io.*;
import java.util.*;

/** Explicitly recorded paths; never infers pen order from a photograph. */
public final class TraceDrawing {
 public static final int MAX_STROKES=128,MAX_POINTS=20000;
 public static final class Sample {
  public final Geometry.Point point;public final long time;
  Sample(double x,double y,long time){if(!Double.isFinite(x)||!Double.isFinite(y)||x<0||x>600||y<0||y>450||time<0||time>86400000)throw new IllegalArgumentException("Muestra fuera del lienzo o del tiempo permitido");point=new Geometry.Point(x,y);this.time=time;}
 }
 public static final class Stroke {
  public String label;public boolean closed;private final List<Sample> samples=new ArrayList<>();
  Stroke(String label){this.label=label==null?"":label.substring(0,Math.min(120,label.length()));}
  public List<Sample> samples(){return Collections.unmodifiableList(samples);}
 }
 private final List<Stroke> strokes=new ArrayList<>();private Stroke current;private int count;private Geometry cachedGeometry;private boolean[] cachedClosed;
 public List<Stroke> strokes(){return Collections.unmodifiableList(strokes);}public int pointCount(){return count;}
 public void begin(double x,double y,long time,String label){if(strokes.size()>=MAX_STROKES||count>=MAX_POINTS)throw new IllegalStateException("Límite de trazos alcanzado");Sample p=new Sample(x,y,time);end();cachedGeometry=null;current=new Stroke(label);current.samples.add(p);strokes.add(current);count++;}
 public void add(double x,double y,long time){if(current==null)throw new IllegalStateException("Inicia un trazo");Sample p=new Sample(x,y,time),last=current.samples.get(current.samples.size()-1);if(time<last.time)throw new IllegalArgumentException("Tiempo fuera de orden");if(p.point.distance(last.point)<.4&&time-last.time<100)return;if(count>=MAX_POINTS)throw new IllegalStateException("Límite de puntos alcanzado");cachedGeometry=null;current.samples.add(p);count++;}
 public void finish(long time){if(current!=null&&count<MAX_POINTS){Sample last=current.samples.get(current.samples.size()-1);add(last.point.x,last.point.y,time);}end();}
 public void end(){current=null;}
 public void undo(){cachedGeometry=null;end();if(!strokes.isEmpty())count-=strokes.remove(strokes.size()-1).samples.size();}
 public void clear(){cachedGeometry=null;end();strokes.clear();count=0;}
 public Geometry geometry(){if(cachedGeometry!=null){boolean same=cachedClosed.length==strokes.size();for(int i=0;same&&i<cachedClosed.length;i++)same=cachedClosed[i]==strokes.get(i).closed;if(same)return cachedGeometry;}cachedClosed=new boolean[strokes.size()];for(int i=0;i<cachedClosed.length;i++)cachedClosed[i]=strokes.get(i).closed;List<Geometry.Shape> shapes=new ArrayList<>();for(Stroke s:strokes){List<Geometry.Point> points=new ArrayList<>();for(Sample p:s.samples)points.add(p.point);shapes.add(new Geometry.Shape(points,s.closed,true));}cachedGeometry=new Geometry(0,shapes);return cachedGeometry;}
 public Sound.Program playback(Sound.Settings settings,int selected,int fragment){Sound.Program out=new Sound.Program(settings);int first=selected<0?0:selected,last=selected<0?strokes.size():selected+1;if(first<0||last>strokes.size())throw new IllegalArgumentException("Trazo inexistente");
  for(int n=first;n<last;n++){Stroke s=strokes.get(n);int from=fragment<0?0:fragment*20,to=fragment<0?s.samples.size():Math.min(s.samples.size(),from+21);if(from>=s.samples.size())throw new IllegalArgumentException("No hay más fragmentos");Sample a=s.samples.get(from);out.marker(a.point,"start");
   if(to-from==1)out.pulse(a.point,0,.25,"vertex");
   for(int i=from+1;i<to&&out.duration<180;i++){Sample b=s.samples.get(i);double seconds=Math.max(.012,Math.min(2,(b.time-a.time)/1000.0))*settings.speed;out.moveTimed(a.point,b.point,seconds);if(i+1<to){Geometry.Point c=s.samples.get(i+1).point;double ux=b.point.x-a.point.x,uy=b.point.y-a.point.y,vx=c.x-b.point.x,vy=c.y-b.point.y;double angle=Math.abs(Math.atan2(ux*vy-uy*vx,ux*vx+uy*vy));if(angle>Math.PI/3)out.marker(b.point,"turn");}a=b;}
   if(s.closed&&fragment<0&&a.point.distance(s.samples.get(0).point)>.01){out.move(a.point,s.samples.get(0).point);a=s.samples.get(0);}out.marker(a.point,s.closed&&fragment<0?"closed":"end");if(n+1<last){long pause=strokes.get(n+1).samples.get(0).time-a.time;out.silence(Math.max(.15,Math.min(2,pause/1000.0))*settings.speed);}if(out.duration>=180)break;
  }return out.limited(180);
 }
 public String encode()throws IOException{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);out.writeInt(0x46415452);out.writeInt(1);out.writeInt(strokes.size());for(Stroke s:strokes){out.writeUTF(s.label);out.writeBoolean(s.closed);out.writeInt(s.samples.size());for(Sample p:s.samples){out.writeDouble(p.point.x);out.writeDouble(p.point.y);out.writeLong(p.time);}}out.flush();return Base64.getEncoder().encodeToString(bytes.toByteArray());}
 public static TraceDrawing decode(String payload)throws IOException{if(payload.length()>1500000)throw new IOException("Proyecto demasiado grande");try{DataInputStream in=new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(payload)));if(in.readInt()!=0x46415452||in.readInt()!=1)throw new IOException("Formato de proyecto incompatible");int n=in.readInt();if(n<0||n>MAX_STROKES)throw new IOException("Demasiados trazos");TraceDrawing result=new TraceDrawing();for(int i=0;i<n;i++){String label=in.readUTF();if(label.length()>120)throw new IOException("Etiqueta demasiado larga");boolean closed=in.readBoolean();int points=in.readInt();if(points<1||points+result.count>MAX_POINTS)throw new IOException("Demasiados puntos");long last=-1;Stroke s=new Stroke(label);s.closed=closed;for(int j=0;j<points;j++){double x=in.readDouble(),y=in.readDouble();long time=in.readLong();if(time<last)throw new IOException("Tiempo fuera de orden");s.samples.add(new Sample(x,y,time));last=time;}result.strokes.add(s);result.count+=points;}if(in.read()!=-1)throw new IOException("Datos adicionales no admitidos");return result;}catch(IllegalArgumentException e){throw new IOException("Datos de trazo no válidos",e);}}
 public static TraceDrawing demo(int type){TraceDrawing d=new TraceDrawing();long time=0;List<Geometry.Shape> shapes;String[] labels;
  if(type==0){List<Geometry.Shape> f=new ArrayList<>();f.add(ellipse(300,225,130,180));f.add(ellipse(250,190,25,13));f.add(ellipse(350,190,25,13));f.add(new Geometry.Shape(Curriculum.points(300,200,280,270,315,270),false,false));f.add(new Geometry.Shape(Curriculum.points(245,310,275,325,325,325,355,310),false,true));shapes=f;labels=new String[]{"Óvalo de cara esquemática","Ojo a la izquierda del lienzo","Ojo a la derecha del lienzo","Nariz esquemática","Boca esquemática"};}
  else if(type==1){shapes=Arrays.asList(new Geometry.Shape(Curriculum.points(80,370,180,370,180,230,400,230,400,90,520,90),false,false));labels=new String[]{"Ruta esquemática con cuatro giros"};}
  else{List<Geometry.Point> p=new ArrayList<>();for(int i=0;i<=80;i++){double t=i/80.0;p.add(new Geometry.Point(100+400*t+45*Math.sin(t*Math.PI*4),225-140*Math.sin(t*Math.PI*2)));}shapes=Arrays.asList(new Geometry.Shape(p,false,true));labels=new String[]{"Gesto cursivo sintético; no es una letra reconocida"};}
  for(int i=0;i<shapes.size();i++){List<Geometry.Point> p=shapes.get(i).points;d.begin(p.get(0).x,p.get(0).y,time,labels[i]);for(int j=1;j<p.size();j++){time+=40;d.add(p.get(j).x,p.get(j).y,time);}d.end();time+=350;}return d;
 }
 private static Geometry.Shape ellipse(double x,double y,double rx,double ry){List<Geometry.Point> p=new ArrayList<>();for(int i=0;i<=64;i++){double t=i*Math.PI/32;p.add(new Geometry.Point(x+rx*Math.cos(t),y+ry*Math.sin(t)));}return new Geometry.Shape(p,false,true);}
}
