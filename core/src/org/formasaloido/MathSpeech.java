package org.formasaloido;

import java.util.regex.*;

/** Plain Spanish for any TTS backend; display text and authored notes stay intact. */
public final class MathSpeech {
 private static final Pattern TOKENS=Pattern.compile("[0-9]+(?:[.,][0-9]+)?|(?<![\\p{L}])(?:sqrt|sin|sen|cos|tan)(?=\\s*\\()|(?<![\\p{L}])[xX](?![\\p{L}])|[+\\-−*/×÷^=≈≤≥<>%°π√²³()]");
 public static String normalize(String text){
  if(text==null)return "";
  Matcher matcher=TOKENS.matcher(text);StringBuffer out=new StringBuffer();
  while(matcher.find()){
   String token=matcher.group(),word;
   switch(token){
    case "x":case "X":word="equis";break;
    case "+":word="más";break;
    case "-":case "−":word="menos";break;
    case "*":case "×":word="por";break;
    case "/":word="sobre";break;
    case "÷":word="dividido entre";break;
    case "^":word="elevado a";break;
    case "²":word="al cuadrado";break;
    case "³":word="al cubo";break;
    case "=":word="igual a";break;
    case "≈":word="aproximadamente";break;
    case "≤":word="menor o igual que";break;
    case "≥":word="mayor o igual que";break;
    case "<":word="menor que";break;
    case ">":word="mayor que";break;
    case "%":word="por ciento";break;
    case "°":word="grados";break;
    case "π":word="pi";break;
    case "√":case "sqrt":word="raíz cuadrada de";break;
    case "sin":case "sen":word="seno de";break;
    case "cos":word="coseno de";break;
    case "tan":word="tangente de";break;
    case "(":word="abre paréntesis";break;
    case ")":word="cierra paréntesis";break;
    default:word=MathLesson.number(token);
   }
   matcher.appendReplacement(out,Matcher.quoteReplacement(" "+word+" "));
  }
  matcher.appendTail(out);return out.toString().replaceAll("\\s+"," ").replaceAll(" +([.,;:])","$1").trim();
 }
 private MathSpeech(){}
}
