import org.formasaloido.*;
import javax.sound.sampled.*;
import java.io.*;

/** Bundled Spanish voice, independent of SAPI. PCM is streamed in interruptible blocks. */
final class GennySpeech {
 private final File file;
 private GennyVoice bank;
 private volatile long generation;
 GennySpeech(File file){this.file=file;}
 void stop(){generation++;}
 void speak(String text,double rate,Runnable done,Runnable error){
  stop();final long request=generation;
  Thread thread=new Thread(()->{
   SourceDataLine line=null;
   try{
    synchronized(this){if(bank==null)try(InputStream in=new FileInputStream(file)){bank=new GennyVoice(in);}}
    if(request!=generation)return;
    AudioFormat format=new AudioFormat((float)(bank.sampleRate*Math.max(.5,Math.min(2,rate))),16,1,true,false);
    line=AudioSystem.getSourceDataLine(format);line.open(format,4096);line.start();
    for(byte[] chunk:bank.chunks(text))for(int at=0;at<chunk.length&&request==generation;){int n=line.write(chunk,at,Math.min(1024,chunk.length-at));if(n<=0)throw new IOException("No se pudo reproducir la voz");at+=n;}
    if(request==generation){line.drain();if(request==generation)done.run();}
   }catch(Exception e){if(request==generation)error.run();}
   finally{if(line!=null){line.stop();line.flush();line.close();}}
  },"genny-voice");thread.setDaemon(true);thread.start();
 }
}
