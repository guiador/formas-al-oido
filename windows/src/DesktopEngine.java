import org.formasaloido.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/** Local pipe bridge. UI is native Windows; geometry and audio share the Android core. */
public final class DesktopEngine {
 final Object gate=new Object();
 final Sound.Settings settings=new Sound.Settings();
 final TouchMap map=new TouchMap();
 final Sound.Renderer renderer;
 Geometry geometry=Curriculum.scene(0,0,70);
 volatile boolean running=true;
 long audibleUntil=0;boolean touching=false;
 int surface=0;double tolerance=18;boolean air=true;
 String lastFeedback="";
 SourceDataLine output;
 DesktopEngine(String data)throws Exception{try(InputStream in=new FileInputStream(data)){renderer=new Sound.Renderer(new Sound.HRIR(in));}}
 static String text(String s){return Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
 static void send(String s){System.out.println(s);System.out.flush();}
 void startAudio(){Thread t=new Thread(()->{try{
  AudioFormat format=new AudioFormat(Sound.RATE,16,2,true,false);
  output=AudioSystem.getSourceDataLine(format);output.open(format,4096);output.start();
  short[] pcm=new short[512];byte[] bytes=new byte[1024];
  while(running){boolean active;synchronized(gate){active=touching||System.nanoTime()<audibleUntil;if(active)renderer.render(pcm,256,settings);}
   if(!active){Thread.sleep(15);continue;}
   for(int i=0;i<pcm.length;i++){bytes[i*2]=(byte)pcm[i];bytes[i*2+1]=(byte)(pcm[i]>>8);}output.write(bytes,0,bytes.length);
  }
 }catch(Exception ex){send("AUDIOERROR|"+text("No se pudo abrir la salida de audio. Revisa el dispositivo predeterminado y vuelve a abrir la aplicación."));}finally{if(output!=null){output.stop();output.close();}}},"audio-local");t.setDaemon(true);t.start();}
 void stop(){touching=false;renderer.quiet();audibleUntil=System.nanoTime()+100000000L;if(output!=null)output.flush();}
 void play(Sound.Program p){stop();renderer.program(p);audibleUntil=System.nanoTime()+(long)((p.duration+.2)*1e9);}
 void scene(String description){map.reset();lastFeedback="";StringBuilder b=new StringBuilder("SCENE|").append(text(description)).append('|');
  for(Geometry.Shape s:geometry.shapes){b.append(s.closed?'1':'0').append(',').append(s.hole?'1':'0').append(':');for(Geometry.Point p:s.points)b.append(p.x).append(',').append(p.y).append(';');b.append('/');}send(b.toString());}
 void command(String line)throws Exception{String[] a=line.split("\\|",-1);synchronized(gate){switch(a[0]){
  case "GUIDE":{stop();int n=Curriculum.clamp(Integer.parseInt(a[1]));Curriculum.Lesson l=Curriculum.LESSONS.get(n);geometry=Curriculum.scene(n,Integer.parseInt(a[2]),Double.parseDouble(a[3]));scene(l.title+". "+l.intro.replace("teléfono","dispositivo").replace("con el dedo","con el dedo, ratón o flechas"));break;}
  case "RANDOM":stop();geometry=new Geometry(Long.parseLong(a[1]),Integer.parseInt(a[2]));scene("Exploración libre. Figura nueva. Escucha el contorno antes de recorrerla.");break;
  case "SET":settings.profile=Integer.parseInt(a[1]);settings.spatial=Integer.parseInt(a[2]);settings.volume=Double.parseDouble(a[3]);settings.monoLateral=Boolean.parseBoolean(a[4]);settings.monoAmount=Double.parseDouble(a[5]);settings.speed=Double.parseDouble(a[6]);settings.brightness=Double.parseDouble(a[7]);settings.width=Double.parseDouble(a[8]);settings.elevation=Double.parseDouble(a[9]);settings.markers=Boolean.parseBoolean(a[10]);settings.swap=Boolean.parseBoolean(a[11]);surface=Integer.parseInt(a[12]);air=Boolean.parseBoolean(a[13]);tolerance=Double.parseDouble(a[14]);break;
  case "STOP":stop();map.reset();lastFeedback="";break;
  case "TOUCH":{TouchMap.Feedback f=map.sample(geometry,new Geometry.Point(Double.parseDouble(a[1]),Double.parseDouble(a[2])),surface,0,tolerance,air);if(f.kind.equals("silent")){stop();}else{renderer.touch(f.position,f.kind);touching=true;}String next=f.kind+"|"+f.component;if(!next.equals(lastFeedback)){send("FEEDBACK|"+next);lastFeedback=next;}break;}
  case "HOLD":touching=false;audibleUntil=System.nanoTime()+220000000L;break;
  case "CONTOUR":play(Sound.Program.contour(geometry,settings));break;
  case "ELEMENT":{int level=Integer.parseInt(a[1]);List<Geometry.Element> es=geometry.elements(level);if(es.isEmpty()){send("STATUS|"+text("No hay elementos en este nivel."));break;}int n=Math.floorMod(Integer.parseInt(a[2]),es.size());Geometry.Element e=es.get(n);send("DESCRIPTION|"+text(Geometry.description(e,level)));play(Sound.Program.element(e,level,settings));break;}
  case "REFERENCE":{Sound.Program p=new Sound.Program(settings).pulse(new Geometry.Point(300,225),0,.4,"vertex").silence(.4);for(Geometry.Point q:Curriculum.points(0,0,600,0,600,450,0,450))p.pulse(q,0,.3,"vertex").silence(.3);play(p);break;}
  case "CALIBRATE":{Sound.Program p=new Sound.Program(settings);for(int x:new int[]{0,300,600})p.pulse(new Geometry.Point(x,225),0,2,"line").silence(.5);play(p);break;}
  case "IMAGE":{stop();String filename=new String(Base64.getDecoder().decode(a[1]),java.nio.charset.StandardCharsets.UTF_8);try{BufferedImage image=ImageIO.read(new File(filename));if(image==null||image.getWidth()>768||image.getHeight()>768)throw new IOException("Imagen no compatible");int w=image.getWidth(),h=image.getHeight();ImageTrace.Options o=new ImageTrace.Options();o.mode=Integer.parseInt(a[2]);o.maxDimension=Integer.parseInt(a[3]);o.offset=Integer.parseInt(a[4]);ImageTrace.Result r=ImageTrace.analyze(image.getRGB(0,0,w,h,null,0,w),w,h,o);geometry=r.geometry;scene("Imagen: "+geometry.shapes.size()+" contornos conservados. "+r.dropped+" contornos descartados. La extracción por contraste no reconoce objetos ni texto.");}finally{send("IMAGEDONE|"+a[1]);}break;}
  case "DEMO":{stop();int n=Integer.parseInt(a[1]);ImageTrace.Result r=ImageTrace.analyze(ImageTrace.demo(n,480,360),480,360,new ImageTrace.Options());geometry=r.geometry;scene(new String[]{"Ejemplo: círculo","Ejemplo: cuadrado con hueco","Ejemplo: silueta de casa"}[n]);break;}
  case "PING":send("PONG");break;
  case "QUIT":stop();running=false;break;
  default:throw new IllegalArgumentException("Comando desconocido");
 }}}
 public static void main(String[] args)throws Exception{Locale.setDefault(Locale.ROOT);DesktopEngine engine=new DesktopEngine(args[0]);if(args.length<2||!args[1].equals("--silent"))engine.startAudio();
  for(Curriculum.Lesson l:Curriculum.LESSONS)send("LESSON|"+text(l.title)+"|"+text(l.hint));send("READY");
  BufferedReader input=new BufferedReader(new InputStreamReader(System.in,java.nio.charset.StandardCharsets.UTF_8));String line;while(engine.running&&(line=input.readLine())!=null)try{engine.command(line);}catch(Exception e){send("ERROR|"+text("No se pudo completar la operación: "+e.getClass().getSimpleName()));}engine.running=false;
 }
}
