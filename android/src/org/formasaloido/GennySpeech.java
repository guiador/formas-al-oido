package org.formasaloido;

import android.content.Context;
import android.media.*;
import android.os.*;
import java.io.*;
import java.util.concurrent.*;

/** Interruptible native playback of the bundled Genny bank. */
public final class GennySpeech {
 private static volatile GennyVoice bank;
 private final Context context;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private volatile long generation;
 private volatile boolean busy;
 public GennySpeech(Context context){this.context=context.getApplicationContext();}
 public boolean isSpeaking(){return busy;}
 public void stop(){generation++;busy=false;}
 public void close(){stop();worker.shutdownNow();}
 public void speak(String text,float rate,Runnable done,Runnable error){
  stop();final long request=generation;busy=true;
  worker.execute(()->{
   AudioTrack track=null;
   try{
    if(bank==null)synchronized(GennySpeech.class){if(bank==null)try(InputStream in=context.getAssets().open("voice/genny-es.bin")){bank=new GennyVoice(in);}}
    if(request!=generation)return;
    int size=Math.max(4096,AudioTrack.getMinBufferSize(bank.sampleRate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT));
    track=new AudioTrack(AudioManager.STREAM_MUSIC,bank.sampleRate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,size,AudioTrack.MODE_STREAM);
    track.setPlaybackRate((int)(bank.sampleRate*Math.max(.5,Math.min(2,rate))));track.play();long frames=0;
    for(byte[] chunk:bank.chunks(text))for(int at=0;at<chunk.length&&request==generation;){int n=track.write(chunk,at,Math.min(1024,chunk.length-at));if(n<=0)throw new IOException("No se pudo reproducir la voz");at+=n;frames+=n/2;}
    while(request==generation&&Integer.toUnsignedLong(track.getPlaybackHeadPosition())<frames)Thread.sleep(15);
    if(request==generation)main.post(()->{if(request==generation){busy=false;if(done!=null)done.run();}});
   }catch(Exception e){if(request==generation)main.post(()->{if(request==generation){busy=false;if(error!=null)error.run();}});}
   finally{if(track!=null){try{track.pause();track.flush();}catch(Exception ignored){}track.release();}}
  });
 }
}
