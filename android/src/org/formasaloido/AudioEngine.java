package org.formasaloido;

import android.content.Context;
import android.media.*;
import android.os.Process;
import java.io.*;

public final class AudioEngine implements AutoCloseable {
 private final Sound.HRIR hrir;private volatile Sound.Settings settings;private volatile boolean running=true;private final java.util.ArrayDeque<Object> commands=new java.util.ArrayDeque<>();private Thread thread;public final boolean hasHrtf;public volatile String error="";
 private static final Object STOP=new Object();
 public AudioEngine(Context context,Sound.Settings s){Sound.HRIR h=null;try(InputStream in=context.getAssets().open("kemar.bin")){h=new Sound.HRIR(in);}catch(Exception e){error="No se pudieron cargar las mediciones HRTF.";}hrir=h;hasHrtf=h!=null;settings=s.copy();}
 public synchronized void settings(Sound.Settings s){settings=s.copy();}
 private synchronized void startThread(){if(thread==null){thread=new Thread(this::run,"FormasAudio");thread.start();}}
 public synchronized void play(Sound.Program p,Runnable onComplete){commands.clear();commands.add(new Playback(p,onComplete));startThread();notifyAll();}
 public synchronized void touch(Geometry.Point p,String kind){commands.clear();commands.add(new Touch(p,kind));startThread();notifyAll();}
 public synchronized void stop(){commands.clear();commands.add(STOP);notifyAll();}
 public synchronized void control(String action,java.util.function.Consumer<String> report){commands.add(new Control(action,report));startThread();notifyAll();}
 private synchronized boolean hasCommands(){return !commands.isEmpty();}
 private static class Control{final String action;final java.util.function.Consumer<String> report;Control(String a,java.util.function.Consumer<String> r){action=a;report=r;}}
 private static class Playback{final Sound.Program program;final Runnable completed;Playback(Sound.Program p,Runnable c){program=p;completed=c;}}
 private static class Touch{final Geometry.Point p;final String kind;Touch(Geometry.Point p,String k){this.p=p;kind=k;}}
 private void run(){Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);AudioTrack track=null;
  try{int minimum=AudioTrack.getMinBufferSize(Sound.RATE,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);int buffer=Math.max(4096,minimum*2);
   track=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(Sound.RATE).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()).setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(buffer).setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY).build();
   if(track.getState()!=AudioTrack.STATE_INITIALIZED)throw new IOException("Salida de audio no disponible");track.play();Sound.Renderer renderer=new Sound.Renderer(hrir);short[] chunk=new short[512];boolean playing=false;Runnable completed=null;
   while(running){Object next; synchronized(this){next=commands.poll();}if(next==STOP){renderer.quiet();track.pause();track.flush();track.play();playing=false;completed=null;}else if(next instanceof Playback){Playback playback=(Playback)next;renderer.program(playback.program);completed=playback.completed;track.pause();track.flush();track.play();playing=true;}else if(next instanceof Touch){Touch t=(Touch)next;renderer.touch(t.p,t.kind);playing=false;completed=null;}
    if(next instanceof Control){Control c=(Control)next;String report=renderer.control(c.action);track.pause();track.flush();track.play();if(c.report!=null)c.report.accept(report);}
    renderer.render(chunk,256,settings);int offset=0;while(offset<chunk.length&&running){int n=track.write(chunk,offset,chunk.length-offset,AudioTrack.WRITE_BLOCKING);if(n<0)throw new IOException("Error de reproducción: "+n);offset+=n;}
    if(playing&&renderer.done()&&!renderer.paused()){playing=false;Runnable callback=completed;completed=null;if(callback!=null&&!hasCommands())callback.run();}
    if(renderer.done()||renderer.paused()){synchronized(this){if(!hasCommands()&&running){track.pause();wait();if(running)track.play();}}}
   }
  }catch(Exception e){error="Audio: "+e.getMessage();}finally{if(track!=null){try{track.pause();track.flush();}catch(Exception ignored){}track.release();}synchronized(this){thread=null;}}
 }
 public void close(){running=false;stop();Thread t=thread;if(t!=null)t.interrupt();}
}
