import org.formasaloido.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/** Local pipe bridge. UI is native Windows; geometry and audio share the Android core. */
public final class DesktopEngine {
 TraceDrawing drawing=new TraceDrawing();ImageScan.Raster raster;int layer=0,focusedTrace=0;boolean heightMode=false;int scanCenter=128,scanWindow=255;boolean scanInvert=false;
 final Object gate=new Object();
 final Sound.Settings settings=new Sound.Settings();
 final TouchMap map=new TouchMap();
 final Sound.Renderer renderer;
 Geometry geometry=Curriculum.scene(0,0,70);
 BufferedImage sourceImage;ImageTrace.Result imageExtraction;ImageScene imageScene=new ImageScene();long visionEpoch=0;
 void vision(ImageScene s){if(imageExtraction==null)return;stop();imageScene=s;geometry=imageExtraction.geometry;raster=new ImageScan.Raster(sourceImage.getRGB(0,0,sourceImage.getWidth(),sourceImage.getHeight(),null,0,sourceImage.getWidth()),sourceImage.getWidth(),sourceImage.getHeight());s.addUnknown(geometry,imageExtraction.left,imageExtraction.top,imageExtraction.width*imageExtraction.scale,imageExtraction.height*imageExtraction.scale);send("VISION|"+text(s.json())+"|reset");scene("Vista general de la imagen. "+s.regions.size()+" regiones explorables. La interpretación opcional está en Imagen inteligente.");}
 volatile boolean running=true;
 long audibleUntil=0;boolean touching=false;
 int surface=0;double tolerance=18;boolean air=true;
 String lastFeedback="";
 SourceDataLine output;Thread audioThread;
 DesktopEngine(String data)throws Exception{openalLibrary=new File(new File(data).getAbsoluteFile().getParentFile(),"soft_oal.dll").getPath();try(InputStream in=new FileInputStream(data)){renderer=new Sound.Renderer(new Sound.HRIR(in));}}
 static String text(String s){return Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
 static void send(String s){System.out.println(s);System.out.flush();}
 volatile boolean flushRequested=false;
 final String openalLibrary;
 void startAudio(){Thread t=new Thread(()->{OpenALOutput openal=null;boolean failed=false;try{
  short[] pcm=new short[512];byte[] bytes=new byte[1024];int previous=-1;
  while(running){Sound.Settings effective;boolean active,flush;
   synchronized(gate){effective=settings.copy();active=touching||(!renderer.paused()&&System.nanoTime()<audibleUntil);flush=flushRequested;flushRequested=false;}
   if(effective.spatial==3&&!failed&&openal==null){try{openal=new OpenALOutput(openalLibrary);send("STATUS|"+text("OpenAL Soft HRTF: "+openal.hrtf));if(output!=null){output.stop();output.close();output=null;}}catch(Exception|LinkageError ex){failed=true;send("AUDIOERROR|"+text("OpenAL no disponible. Se usa HRTF KEMAR."));}}
   if(effective.spatial!=3&&openal!=null){openal.close();openal=null;}
   if(effective.spatial==3&&openal==null)effective.spatial=1;
   if(flush||previous!=effective.spatial){if(openal!=null)openal.flush();else if(output!=null)output.flush();}previous=effective.spatial;
   if(!active){Thread.sleep(10);continue;}
   if(openal==null&&output==null){AudioFormat format=new AudioFormat(Sound.RATE,16,2,true,false);output=AudioSystem.getSourceDataLine(format);output.open(format,4096);output.start();}
   double az,el;synchronized(gate){renderer.render(pcm,256,effective);az=renderer.azimuth;el=renderer.elevation;}
   if(openal!=null){openal.write(pcm,az,el);}else{for(int i=0;i<pcm.length;i++){bytes[i*2]=(byte)pcm[i];bytes[i*2+1]=(byte)(pcm[i]>>8);}output.write(bytes,0,bytes.length);}
  }
 }catch(Exception ex){send("AUDIOERROR|"+text("No se pudo abrir o mantener la salida de audio. Revisa el dispositivo predeterminado y vuelve a abrir la aplicación."));}finally{if(openal!=null)openal.close();if(output!=null){output.stop();output.close();}}},"audio-local");t.setDaemon(true);audioThread=t;t.start();}
 void stop(){touching=false;renderer.quiet();audibleUntil=System.nanoTime()+100000000L;flushRequested=true;}
 void play(Sound.Program p){stop();renderer.program(p);audibleUntil=System.nanoTime()+(long)((p.duration+.2)*1e9);}
 void scene(String description){map.reset();lastFeedback="";StringBuilder b=new StringBuilder("SCENE|").append(text(description)).append('|');
  for(Geometry.Shape s:geometry.shapes){b.append(s.closed?'1':'0').append(',').append(s.hole?'1':'0').append(':');for(Geometry.Point p:s.points)b.append(p.x).append(',').append(p.y).append(';');b.append('/');}send(b.toString());}
 Geometry activeGeometry(){if(layer==0)return geometry;if(layer==1)return drawing.geometry();if(layer==3){Geometry d=drawing.geometry();return new Geometry(0,focusedTrace>=0&&focusedTrace<d.shapes.size()?Arrays.asList(d.shapes.get(focusedTrace)):new ArrayList<Geometry.Shape>());}List<Geometry.Shape> shapes=new ArrayList<>(geometry.shapes);shapes.addAll(drawing.geometry().shapes);return new Geometry(0,shapes);}
 void traces(){StringBuilder shapes=new StringBuilder(),labels=new StringBuilder();for(TraceDrawing.Stroke s:drawing.strokes()){shapes.append(s.closed?'1':'0').append(",0:");for(TraceDrawing.Sample q:s.samples())shapes.append(q.point.x).append(',').append(q.point.y).append(';');shapes.append('/');labels.append(s.label.replace('\n',' ')).append('\n');}send("TRACES|"+text(labels.toString())+"|"+shapes+"|"+drawing.pointCount());}
 void command(String line)throws Exception{String[] a=line.split("\\|",-1);synchronized(gate){switch(a[0]){
  case "GUIDE":{stop();visionEpoch++;sourceImage=null;imageExtraction=null;raster=null;heightMode=false;int n=Curriculum.clamp(Integer.parseInt(a[1]));Curriculum.Lesson l=Curriculum.LESSONS.get(n);geometry=Curriculum.scene(n,Integer.parseInt(a[2]),Double.parseDouble(a[3]));scene(l.title+". "+l.intro.replace("teléfono","dispositivo").replace("con el dedo","con el dedo, ratón o flechas"));break;}
  case "RANDOM":stop();visionEpoch++;sourceImage=null;imageExtraction=null;raster=null;heightMode=false;geometry=new Geometry(Long.parseLong(a[1]),Integer.parseInt(a[2]));scene("Exploración libre. Figura nueva. Escucha el contorno antes de recorrerla.");break;
  case "SET":settings.profile=Integer.parseInt(a[1]);settings.spatial=Integer.parseInt(a[2]);settings.volume=Double.parseDouble(a[3]);settings.monoLateral=Boolean.parseBoolean(a[4]);settings.monoAmount=Double.parseDouble(a[5]);settings.speed=Double.parseDouble(a[6]);settings.brightness=Double.parseDouble(a[7]);settings.width=Double.parseDouble(a[8]);settings.elevation=Double.parseDouble(a[9]);settings.markers=Boolean.parseBoolean(a[10]);settings.swap=Boolean.parseBoolean(a[11]);surface=Integer.parseInt(a[12]);air=Boolean.parseBoolean(a[13]);tolerance=Double.parseDouble(a[14]);break;
  case "STOP":stop();map.reset();lastFeedback="";break;
  case "VISION_DATA":{stop();visionEpoch++;String json=new String(Base64.getDecoder().decode(a[1]),java.nio.charset.StandardCharsets.UTF_8);Map<?,?> parsed=(Map<?,?>)Json.parse(json);vision(ImageScene.fromJson(json,ImageScene.clean(parsed.get("source"),100)));break;}
  case "VISION_API":{if(sourceImage==null)throw new IllegalArgumentException("Falta imagen");stop();final long request=++visionEpoch;final BufferedImage snapshot=sourceImage;final String provider=a[1],url=new String(Base64.getDecoder().decode(a[2]),java.nio.charset.StandardCharsets.UTF_8),model=new String(Base64.getDecoder().decode(a[3]),java.nio.charset.StandardCharsets.UTF_8),key=new String(Base64.getDecoder().decode(a[4]),java.nio.charset.StandardCharsets.UTF_8);Thread job=new Thread(()->{try{ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(snapshot,"png",out);ImageScene response=VisionApi.analyze(out.toByteArray(),provider,url,model,key);synchronized(gate){if(request==visionEpoch&&sourceImage==snapshot)vision(response);}}catch(Exception error){synchronized(gate){if(request==visionEpoch)send("VISIONERROR|"+text("No se pudo consultar la API. Revisa dirección, clave, modelo y conexión. Los contornos siguen disponibles."));}}},"vision-api");job.setDaemon(true);job.start();break;}
  case "IMAGE_FOCUS":{stop();int n=Integer.parseInt(a[1]);if(imageExtraction==null||n<0||n>=imageScene.regions.size())throw new IllegalArgumentException("Región inexistente");ImageScene.Region r=imageScene.regions.get(n);geometry=ImageScene.focus(imageExtraction.geometry,r,imageExtraction.left,imageExtraction.top,imageExtraction.width*imageExtraction.scale,imageExtraction.height*imageExtraction.scale);int l=(int)(r.left*sourceImage.getWidth()),t=(int)(r.top*sourceImage.getHeight()),w=Math.max(1,(int)Math.ceil(r.right*sourceImage.getWidth())-l),h=Math.max(1,(int)Math.ceil(r.bottom*sourceImage.getHeight())-t);BufferedImage crop=sourceImage.getSubimage(l,t,Math.min(w,sourceImage.getWidth()-l),Math.min(h,sourceImage.getHeight()-t));raster=new ImageScan.Raster(crop.getRGB(0,0,crop.getWidth(),crop.getHeight(),null,0,crop.getWidth()),crop.getWidth(),crop.getHeight());layer=0;scene("Primer plano del componente "+(n+1)+". Las posiciones son ahora locales a esta región. Su caja no es la silueta del objeto.");break;}
  case "VISION_CORRECT":{visionEpoch++;int n=Integer.parseInt(a[1]);ImageScene.Region r=imageScene.regions.get(n);r.label=ImageScene.clean(new String(Base64.getDecoder().decode(a[2]),java.nio.charset.StandardCharsets.UTF_8),160);r.analogy=ImageScene.clean(new String(Base64.getDecoder().decode(a[3]),java.nio.charset.StandardCharsets.UTF_8),240);r.source="Anotación del usuario";send("VISION|"+text(imageScene.json()));break;} case "IMAGE_RESTORE":if(imageExtraction!=null){stop();geometry=imageExtraction.geometry;raster=new ImageScan.Raster(sourceImage.getRGB(0,0,sourceImage.getWidth(),sourceImage.getHeight(),null,0,sourceImage.getWidth()),sourceImage.getWidth(),sourceImage.getHeight());scene("Vista general restaurada; la imagen conserva su ubicación original.");}break;
  case "CONTROL":{String message=renderer.control(a[1]);touching=false;flushRequested=true;audibleUntil=renderer.paused()?0:System.nanoTime()+(long)((renderer.remaining()+.2)*1e9);send((a[1].equals("resume")||a[1].equals("fragment")?"STATUS|":"ORIENTATION|")+text(message));break;}
  case "OVERVIEW":renderer.control("pause");touching=false;audibleUntil=0;flushRequested=true;send("ORIENTATION|"+text((layer==0?"Fondo: ":layer==2?"Fondo y trazos: ":"Trazos aportados: ")+Orientation.overview(activeGeometry())));break;
  case "WHERE":renderer.control("pause");touching=false;audibleUntil=0;flushRequested=true;send("ORIENTATION|"+text(Orientation.at(activeGeometry(),new Geometry.Point(Double.parseDouble(a[1]),Double.parseDouble(a[2])))));break;
  case "TOUCH":{if(heightMode&&raster!=null){Geometry.Point q=new Geometry.Point(Double.parseDouble(a[1]),Double.parseDouble(a[2]));if(!raster.contains(q))stop();else{renderer.touch(new Geometry.Point(q.x,q.y,4*(1-raster.value(q,scanCenter,scanWindow,scanInvert))),"line");touching=true;}break;}TouchMap.Feedback f=map.sample(activeGeometry(),new Geometry.Point(Double.parseDouble(a[1]),Double.parseDouble(a[2])),surface,0,tolerance,air);if(f.kind.equals("silent")){stop();}else{renderer.touch(f.position,f.kind);touching=true;}String next=f.kind+"|"+f.component;if(!next.equals(lastFeedback)){send("FEEDBACK|"+next);lastFeedback=next;}break;}
  case "HOLD":touching=false;audibleUntil=System.nanoTime()+220000000L;break;
  case "CONTOUR":play(Sound.Program.contour(activeGeometry(),settings));break;
  case "ELEMENT":{int level=Integer.parseInt(a[1]);List<Geometry.Element> es=activeGeometry().elements(level);if(es.isEmpty()){send("STATUS|"+text("No hay elementos en este nivel."));break;}int n=Math.floorMod(Integer.parseInt(a[2]),es.size());Geometry.Element e=es.get(n);send("DESCRIPTION|"+text(Geometry.description(e,level)));play(Sound.Program.element(e,level,settings));break;}
  case "REFERENCE":{Sound.Program p=new Sound.Program(settings).pulse(new Geometry.Point(300,225),0,.4,"vertex").silence(.4);for(Geometry.Point q:Curriculum.points(0,0,600,0,600,450,0,450))p.pulse(q,0,.3,"vertex").silence(.3);play(p);break;}
  case "CALIBRATE":{Sound.Program p=new Sound.Program(settings);for(int x:new int[]{0,300,600})p.pulse(new Geometry.Point(x,225),0,2,"line").silence(.5);play(p);break;}
  case "IMAGE":{stop();String filename=new String(Base64.getDecoder().decode(a[1]),java.nio.charset.StandardCharsets.UTF_8);try{BufferedImage image=ImageIO.read(new File(filename));if(image==null||image.getWidth()>768||image.getHeight()>768)throw new IOException("Imagen no compatible");int w=image.getWidth(),h=image.getHeight();ImageTrace.Options o=new ImageTrace.Options();o.mode=Integer.parseInt(a[2]);o.maxDimension=Integer.parseInt(a[3]);o.offset=Integer.parseInt(a[4]);ImageTrace.Result r=ImageTrace.analyze(image.getRGB(0,0,w,h,null,0,w),w,h,o);visionEpoch++;sourceImage=image;imageExtraction=r;imageScene=new ImageScene();imageScene.addUnknown(r.geometry,r.left,r.top,r.width*r.scale,r.height*r.scale);send("VISION|"+text(imageScene.json())+"|reset");geometry=r.geometry;raster=new ImageScan.Raster(image.getRGB(0,0,w,h,null,0,w),w,h);heightMode=false;scene("Imagen: "+geometry.shapes.size()+" contornos conservados. "+r.dropped+" contornos descartados. La extracción por contraste no reconoce objetos ni texto.");}finally{send("IMAGEDONE|"+a[1]);}break;}
  case "DEMO":{stop();visionEpoch++;sourceImage=null;imageExtraction=null;raster=null;heightMode=false;int n=Integer.parseInt(a[1]);ImageTrace.Result r=ImageTrace.analyze(ImageTrace.demo(n,480,360),480,360,new ImageTrace.Options());geometry=r.geometry;scene(new String[]{"Ejemplo: círculo","Ejemplo: cuadrado con hueco","Ejemplo: silueta de casa"}[n]);break;}
  case "DRAW_BEGIN":drawing.begin(Double.parseDouble(a[1]),Double.parseDouble(a[2]),Long.parseLong(a[3]),new String(Base64.getDecoder().decode(a[4]),java.nio.charset.StandardCharsets.UTF_8));renderer.touch(new Geometry.Point(Double.parseDouble(a[1]),Double.parseDouble(a[2])),"line");touching=true;break;
  case "DRAW_MOVE":drawing.add(Double.parseDouble(a[1]),Double.parseDouble(a[2]),Long.parseLong(a[3]));renderer.touch(new Geometry.Point(Double.parseDouble(a[1]),Double.parseDouble(a[2])),"line");touching=true;break;
  case "DRAW_END":if(a.length>1)drawing.finish(Long.parseLong(a[1]));else drawing.end();stop();traces();break;
  case "DRAW_UNDO":stop();drawing.undo();traces();break;
  case "DRAW_POINT":stop();drawing.coordinate(Double.parseDouble(a[1]),Double.parseDouble(a[2]),Boolean.parseBoolean(a[3]),new String(Base64.getDecoder().decode(a[4]),java.nio.charset.StandardCharsets.UTF_8));traces();break;
  case "DRAW_CLEAR":stop();drawing.clear();traces();break;
  case "DRAW_CLOSE":stop();if(!drawing.strokes().isEmpty())drawing.strokes().get(drawing.strokes().size()-1).closed=Boolean.parseBoolean(a[1]);traces();break;
  case "DRAW_PLAY":play(drawing.playback(settings,Integer.parseInt(a[1]),Integer.parseInt(a[2])));break;
  case "DRAW_EXPORT":drawing.end();send("PROJECT|"+(a.length>1?a[1]+"|":"")+drawing.encode());break;
  case "DRAW_IMPORT":{TraceDrawing incoming=TraceDrawing.decode(a[1]);stop();drawing=incoming;traces();break;}
  case "DRAW_DEMO":stop();drawing=TraceDrawing.demo(Integer.parseInt(a[1]));traces();break;
  case "LAYER":stop();layer=Integer.parseInt(a[1]);if(layer<0||layer>3)throw new IllegalArgumentException();map.reset();break;
  case "SELECT_TRACE":if(layer==3)stop();focusedTrace=Integer.parseInt(a[1]);break;
  case "HEIGHT":heightMode=Boolean.parseBoolean(a[1]);if(heightMode&&raster==null){heightMode=false;throw new IllegalArgumentException("Falta imagen");}break;
  case "SCAN":{stop();int scanMode=Integer.parseInt(a[1]);Geometry.Point origin=new Geometry.Point(Double.parseDouble(a[2]),Double.parseDouble(a[3]));scanCenter=Math.max(0,Math.min(255,Integer.parseInt(a[6])));scanWindow=Math.max(1,Math.min(255,Integer.parseInt(a[7])));scanInvert=Boolean.parseBoolean(a[8]);if(scanMode==4)play(ImageScan.contour(activeGeometry(),settings,origin));else{ImageScan scan=new ImageScan(activeGeometry(),raster,settings,scanMode,origin,Integer.parseInt(a[4]),scanCenter,scanWindow,scanInvert);int selected=Integer.parseInt(a[5]);send("STATUS|"+text(selected<0?"Lectura completa: "+scan.sections.size()+" secciones. Límite de 180 segundos.":scan.sections.get(selected).name));play(scan.program(settings,selected));}break;}
  case "PING":send("PONG");break;
  case "QUIT":stop();running=false;break;
  default:throw new IllegalArgumentException("Comando desconocido");
 }}}
 public static void main(String[] args)throws Exception{Locale.setDefault(Locale.ROOT);DesktopEngine engine=new DesktopEngine(args[0]);if(args.length<2||!args[1].equals("--silent"))engine.startAudio();
  for(Curriculum.Lesson l:Curriculum.LESSONS)send("LESSON|"+text(l.title)+"|"+text(l.hint));send("READY");
  BufferedReader input=new BufferedReader(new InputStreamReader(System.in,java.nio.charset.StandardCharsets.UTF_8));String line;while(engine.running&&(line=input.readLine())!=null)try{engine.command(line);}catch(Exception e){send("ERROR|"+text("No se pudo completar la operación: "+e.getClass().getSimpleName()));}engine.running=false;if(engine.audioThread!=null)engine.audioThread.join(3000);
 }
}
