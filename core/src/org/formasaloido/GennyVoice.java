package org.formasaloido;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/** Offline Genny-generated Spanish word bank. No OS TTS, Python or network at runtime. */
public final class GennyVoice {
 public final int sampleRate;
 private final Map<String,byte[]> words=new HashMap<>();
 private final byte[] gap, pause;
 public GennyVoice(InputStream input)throws IOException{
  DataInputStream in=new DataInputStream(new BufferedInputStream(input));
  if(in.readInt()!=0x474e5631)throw new IOException("Banco de voz no compatible");
  sampleRate=in.readInt();int count=in.readInt();
  if(sampleRate!=16000||count<27||count>10000)throw new IOException("Cabecera de voz no válida");
  long total=0;
  for(int i=0;i<count;i++){String word=in.readUTF();int length=in.readInt();total+=length;if(word.length()>64||length<0||length>sampleRate*20||length%2!=0||total>100000000)throw new IOException("Banco de voz fuera de límites");byte[] pcm=new byte[length];in.readFully(pcm);if(words.put(word,pcm)!=null)throw new IOException("Entrada duplicada");}
  for(char c:"abcdefghijklmnñopqrstuvwxyz".toCharArray())if(!words.containsKey("@"+c))throw new IOException("Falta el alfabeto de respaldo");
  gap=new byte[sampleRate*2/25];pause=new byte[sampleRate*2/5];
 }
 public boolean contains(String word){return words.containsKey(word.toLowerCase(Locale.ROOT));}
 public List<byte[]> chunks(String text){
  if(text==null||text.length()>65536)throw new IllegalArgumentException("Texto de voz fuera de límites");
  List<byte[]> result=new ArrayList<>();Matcher tokens=Pattern.compile("[\\p{L}]+|[.,;:!?]").matcher(MathSpeech.normalize(text));
  while(tokens.find()){
   String token=tokens.group().toLowerCase(Locale.ROOT);
   if(token.length()==1&&".,;:!?".contains(token)){result.add(pause);continue;}
   byte[] pcm=words.get(token);
   if(pcm!=null)result.add(pcm);
   else{
    // Make unknown text recoverable instead of silently skipping it.
    byte[] cue=words.get("deletreo");if(cue!=null){result.add(cue);result.add(gap);}
    String plain=token.replace('á','a').replace('é','e').replace('í','i').replace('ó','o').replace('ú','u').replace('ü','u');
    for(char letter:plain.toCharArray()){byte[] name=words.get("@"+letter);if(name!=null){result.add(name);result.add(gap);}}
   }
   result.add(gap);
  }
  return result;
 }
}
