import java.io.*;
import org.formasaloido.*;

/** Real-time OpenAL queue on the null device; no physical speakers. */
public final class StreamChecks {
 public static void main(String[] args)throws Exception{
  PrintStream console=System.out;ByteArrayOutputStream capture=new ByteArrayOutputStream();
  DesktopEngine engine=new DesktopEngine(args[0]);
  try{
   System.setOut(new PrintStream(capture,true,"UTF-8"));
   engine.settings.spatial=3;engine.startAudio();
   engine.command("GUIDE|20|0|70");engine.command("CONTOUR");Thread.sleep(350);
   engine.command("CONTROL|pause");Thread.sleep(60);engine.command("CONTROL|resume");Thread.sleep(100);
   engine.command("STOP");engine.command("TOUCH|300|225");Thread.sleep(60);
   engine.command("QUIT");engine.audioThread.join(3000);
   if(engine.audioThread.isAlive())throw new AssertionError("El hilo de audio no terminó");
   String log=capture.toString("UTF-8");
   if(log.contains("AUDIOERROR|")||!log.contains("STATUS|"+DesktopEngine.text("OpenAL Soft HRTF: Built-In HRTF")))throw new AssertionError("No se mantuvo la salida OpenAL: "+log);
  }finally{engine.running=false;System.setOut(console);}
  System.out.println("PASS OpenAL streaming real: inicio, cola acotada, pausa, continuación, detener, contacto y cierre del hilo (dispositivo null).");
 }
}
