import org.formasaloido.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

public final class VoiceChecks {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  GennyVoice voice;try(InputStream in=new FileInputStream(args[0])){voice=new GennyVoice(in);}
  check(voice.sampleRate==16000,"native bank sample rate");
  for(String word:"cero uno dos tres cuatro cinco seis siete ocho nueve diez menos más elevado sobre equis coma raíz paréntesis millón veintiún deletreo".split(" "))check(voice.contains(word),"math word present: "+word);
  String text="x^2 = 1/2. Menos 0.25.";
  List<byte[]> chunks=voice.chunks(text);long bytes=0;int peak=0;long energy=0;
  for(byte[] chunk:chunks){check(chunk.length%2==0,"aligned PCM");bytes+=chunk.length;for(int i=0;i<chunk.length;i+=2){int sample=(short)((chunk[i]&255)|(chunk[i+1]<<8));peak=Math.max(peak,Math.abs(sample));energy+=(long)sample*sample;}}
  check(bytes>16000,"mathematical explanation produces audio");check(bytes<16000*2*60,"bounded short explanation");check(peak>1000&&peak<=24000&&energy>1000000,"nonempty audio with playback headroom");
  check(voice.chunks("qxzñ").size()>4,"unknown words are spelled, not silently dropped");
  check(voice.chunks("").isEmpty(),"empty speech is empty");
  try{new GennyVoice(new ByteArrayInputStream(new byte[12]));throw new AssertionError("bad bank accepted");}catch(IOException expected){checks++;}
  if(args.length>1){ByteArrayOutputStream out=new ByteArrayOutputStream();for(byte[] chunk:chunks)out.write(chunk);byte[] pcm=out.toByteArray();AudioFormat format=new AudioFormat(16000,16,1,true,false);try(AudioInputStream audio=new AudioInputStream(new ByteArrayInputStream(pcm),format,pcm.length/2)){AudioSystem.write(audio,AudioFileFormat.Type.WAVE,new File(args[1]));}}
  System.out.println("PASS "+checks+" bundled Genny voice checks; PCM generated without SAPI, Android TTS, Python or network.");
 }
}
