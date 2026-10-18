package org.formasaloido;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.util.ArrayDeque;

/** One mono emitter, streamed on its owning audio thread. No second KEMAR filter. */
public final class OpenALOutput implements AutoCloseable {
 public interface AL extends Library {
  Pointer alcOpenDevice(String name);byte alcCloseDevice(Pointer d);
  Pointer alcLoopbackOpenDeviceSOFT(String name);
  Pointer alcCreateContext(Pointer d,int[] attributes);
  byte alcSetThreadContext(Pointer c);void alcDestroyContext(Pointer c);
  void alcGetIntegerv(Pointer d,int key,int size,int[] value);
  String alcGetString(Pointer d,int key);
  void alcRenderSamplesSOFT(Pointer d,short[] samples,int frames);
  void alGenSources(int count,int[] ids);void alDeleteSources(int count,int[] ids);
  void alGenBuffers(int count,int[] ids);void alDeleteBuffers(int count,int[] ids);
  void alSourcei(int s,int key,int value);void alSourcef(int s,int key,float value);
  void alSource3f(int s,int key,float x,float y,float z);
  void alSourcePlay(int s);void alSourceStop(int s);
  void alGetSourcei(int s,int key,int[] value);
  void alBufferData(int b,int format,short[] pcm,int bytes,int rate);
  void alSourceQueueBuffers(int s,int count,int[] buffers);
  void alSourceUnqueueBuffers(int s,int count,int[] buffers);
  void alDistanceModel(int mode);void alDopplerFactor(float factor);int alGetError();
 }
 private final AL al;private Pointer device,context;private final int[] source={0},buffers=new int[4],value=new int[1];
 private final ArrayDeque<Integer> free=new ArrayDeque<>();private short[] mono=new short[256];private final boolean loopback;
 public final String hrtf;
 public OpenALOutput(String library)throws IOException{this(library,false);}
 public OpenALOutput(String library,boolean offline)throws IOException{
  al=Native.load(library,AL.class);loopback=offline;
  try{
   device=offline?al.alcLoopbackOpenDeviceSOFT(null):al.alcOpenDevice(null);
   if(device==null)throw new IOException("OpenAL: dispositivo no disponible");
   int[] attrs=offline?new int[]{0x1992,1,0x1007,Sound.RATE,0x1990,0x1501,0x1991,0x1402,0}:new int[]{0x1992,1,0x1007,Sound.RATE,0};
   context=al.alcCreateContext(device,attrs);
   if(context==null||al.alcSetThreadContext(context)==0)throw new IOException("OpenAL: contexto no disponible");
   al.alcGetIntegerv(device,0x1992,1,value);
   if(value[0]==0)throw new IOException("OpenAL: HRTF no activada por el dispositivo");
   hrtf=al.alcGetString(device,0x1995);
   al.alDistanceModel(0);al.alDopplerFactor(0);
   al.alGenSources(1,source);al.alGenBuffers(buffers.length,buffers);
   al.alSourcei(source[0],0x202,1); // listener-relative
   al.alSourcei(source[0],0x1214,1); // AL_SOURCE_SPATIALIZE_SOFT
   al.alSourcef(source[0],0x1021,0); // no additional distance attenuation
   check();for(int b:buffers)free.add(b);
  }catch(IOException|RuntimeException|LinkageError e){close();throw e;}
 }
 private void current()throws IOException{if(context==null||al.alcSetThreadContext(context)==0)throw new IOException("OpenAL cerrado");}
 private void check()throws IOException{int e=al.alGetError();if(e!=0)throw new IOException("OpenAL error "+Integer.toHexString(e));}
 private void reclaim(){al.alGetSourcei(source[0],0x1016,value);int n=value[0];for(int i=0;i<n;i++){int[] b={0};al.alSourceUnqueueBuffers(source[0],1,b);free.add(b[0]);}}
 public void write(short[] stereo,double azimuth,double elevation)throws IOException,InterruptedException{
  current();long deadline=System.nanoTime()+2000000000L;
  while(free.isEmpty()){reclaim();if(!free.isEmpty())break;if(System.nanoTime()>deadline)throw new IOException("OpenAL: salida sin respuesta");Thread.sleep(2);}
  int frames=stereo.length/2;if(mono.length!=frames)mono=new short[frames];for(int i=0;i<frames;i++)mono[i]=stereo[2*i];
  double az=Math.toRadians(azimuth),el=Math.toRadians(elevation);
  al.alSource3f(source[0],0x1004,(float)(Math.sin(az)*Math.cos(el)),(float)Math.sin(el),(float)(-Math.cos(az)*Math.cos(el)));
  int[] b={free.remove()};al.alBufferData(b[0],0x1101,mono,frames*2,Sound.RATE);al.alSourceQueueBuffers(source[0],1,b);
  al.alGetSourcei(source[0],0x1010,value);if(value[0]!=0x1012)al.alSourcePlay(source[0]);check();
 }
 /** Offline regression tests exercise the actual mixer, not an audio device. */
 public short[] renderOffline(short[] stereo,double azimuth,double elevation)throws Exception{
  if(!loopback)throw new IllegalStateException("Sólo loopback");write(stereo,azimuth,elevation);short[] out=new short[stereo.length];al.alcRenderSamplesSOFT(device,out,out.length/2);check();return out;
 }
 public void flush()throws IOException{
  current();al.alSourceStop(source[0]);al.alGetSourcei(source[0],0x1015,value);int n=value[0];for(int i=0;i<n;i++){int[] b={0};al.alSourceUnqueueBuffers(source[0],1,b);}free.clear();for(int b:buffers)free.add(b);check();
 }
 public void close(){if(context!=null){al.alcSetThreadContext(context);if(source[0]!=0){al.alSourceStop(source[0]);al.alDeleteSources(1,source);}if(buffers[0]!=0)al.alDeleteBuffers(buffers.length,buffers);al.alcSetThreadContext(null);al.alcDestroyContext(context);context=null;}if(device!=null){al.alcCloseDevice(device);device=null;}}
}
