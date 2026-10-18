import org.formasaloido.*;
import java.util.Arrays;

public final class OpenALChecks {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 static short[] render(String library,double az,double el)throws Exception{
  try(OpenALOutput output=new OpenALOutput(library,true)){
   short[] all=new short[512*40];
   for(int n=0;n<40;n++){short[] pcm=new short[512];for(int i=0;i<256;i++)pcm[i*2]=pcm[i*2+1]=(short)(9000*Math.sin((n*256+i)*2*Math.PI*440/Sound.RATE));short[] block=output.renderOffline(pcm,az,el);System.arraycopy(block,0,all,n*512,512);}
   output.flush();for(int n=0;n<10;n++)output.renderOffline(new short[512],az,el);
   short[] silent=output.renderOffline(new short[512],az,el);for(short v:silent)check(Math.abs(v)<3,"Audio antiguo después de vaciar y agotar la cola");
   System.out.println("HRTF: "+output.hrtf);return all;
  }
 }
 static double energy(short[] pcm,int channel){double v=0;for(int i=4096+channel;i<pcm.length;i+=2)v+=(double)pcm[i]*pcm[i];return v;}
 public static void main(String[] args)throws Exception{
  short[] left=render(args[0],-65,0),right=render(args[0],65,0),high=render(args[0],65,35);
  check(energy(left,0)>energy(left,1)*1.2,"Posición izquierda no lateralizada");
  check(energy(right,1)>energy(right,0)*1.2,"Posición derecha no lateralizada");
  check(!Arrays.equals(right,high),"La elevación no cambia el filtro");
  Sound.Settings s=new Sound.Settings();s.spatial=3;s.swap=true;
  Sound.Renderer renderer=new Sound.Renderer(null);renderer.touch(new Geometry.Point(0,0),"line");short[] pcm=new short[512];renderer.render(pcm,256,s);
  for(int i=0;i<pcm.length;i+=2)check(pcm[i]==pcm[i+1],"La fuente de OpenAL debe ser mono sin KEMAR/pan previo");
  check(renderer.azimuth>0&&renderer.elevation>0,"Intercambio de oídos o altura incorrecto");
  java.util.concurrent.ExecutorService workers=java.util.concurrent.Executors.newFixedThreadPool(2);
  try{
   java.util.concurrent.Future<short[]> a=workers.submit(()->render(args[0],-65,0));
   java.util.concurrent.Future<short[]> b=workers.submit(()->render(args[0],65,0));
   short[] one=a.get(),two=b.get();
   check(energy(one,0)>energy(one,1)*1.2&&energy(two,1)>energy(two,0)*1.2,"Los contextos de dos hilos interfieren entre sí");
  }finally{workers.shutdownNow();}
  System.out.println("PASS OpenAL: izquierda/derecha, elevación, fuente mono, inversión, vaciado y cierre de contextos reales.");
 }
}
