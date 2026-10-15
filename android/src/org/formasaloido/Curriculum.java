package org.formasaloido;

import java.util.*;

/** Explicit teaching scenes. Variant changes only the advertised property. */
public final class Curriculum {
 public static final String[] MODULES={"1. Lienzo y punto","2. Vecindad y continuidad","3. Relaciones entre líneas","4. Curvas y figuras","5. Variaciones y profundidad explícita"};
 public static final String[] DIRECTIONS={"derecha","arriba a la derecha","arriba","arriba a la izquierda","izquierda","abajo a la izquierda","abajo","abajo a la derecha"};
 public static final class Lesson {
  public final int module;public final String id,title,intro,hint;public final boolean variants;
  Lesson(int module,String id,String title,String intro,String hint,boolean variants){this.module=module;this.id=id;this.title=title;this.intro=intro;this.hint=hint;this.variants=variants;}
 }
 public static final List<Lesson> LESSONS=Collections.unmodifiableList(Arrays.asList(
  l(0,"frame","Conoce tu lienzo","Este rectángulo es el marco de trabajo. Escucha sus cuatro lados. Después búscalos con el dedo. El marco no es el borde físico del teléfono.","Pulsa Recuperar referencia para escuchar el centro y las cuatro esquinas.",false),
  l(0,"point","Encuentra un punto","Hay un punto en el centro. Muévete despacio hasta encontrarlo. Puedes salir y volver tantas veces como quieras.","Empieza en el centro del lienzo. Activa el aire espacial si el silencio te desorienta.",false),
  l(1,"pair","Dos puntos, una dirección","Escucha dos puntos. Después intenta encontrarlos por separado. El segundo cambia de dirección únicamente cuando tú pides otra variante.","Repite el recorrido para escuchar primero un punto y después el otro. La separación puede ajustarse sin cambiar la dirección.",true),
  l(1,"chain","Una cadena de puntos","Hay cinco puntos alineados. Busca sus separaciones. La app conserva cada punto: no los convierte automáticamente en línea.","Compara una separación amplia con una más pequeña. Tu impresión es un resultado de esta tarea, no un diagnóstico.",true),
  l(1,"horizontal","Una recta horizontal","La línea va de izquierda a derecha a la misma altura. Sigue su continuidad.","El tono de altura permanece estable; la posición cambia entre los oídos.",false),
  l(1,"vertical","Una recta vertical","La línea sube desde abajo hasta arriba, en el centro.","El refuerzo tonal sube. Con HRTF también cambia el filtro de elevación.",false),
  l(1,"diagonal","Una recta diagonal","La dirección cambia solo al pedir otra variante. Escucha el desplazamiento horizontal y vertical juntos.","Busca primero un extremo; después sigue el trazo hacia el otro.",true),
  l(1,"dashed","Una línea discontinua","Son cuatro segmentos separados por huecos reales. El recorrido deja silencio en esos huecos.","Cuenta interrupciones si te resulta útil. El aire de fondo es opcional y suena más tenue.",false),
  l(1,"gap","Encuentra una interrupción","Una recta tiene un hueco central. Busca dónde termina y vuelve a comenzar.","La separación controla el tamaño del hueco. No cambies a la vez el volumen ni la velocidad.",false),
  l(2,"parallel","Dos líneas paralelas","Escucha dos líneas separadas. Mantienen la misma dirección y distancia. Las variantes rotan ambas juntas.","Puedes filtrar un componente y después recuperar el conjunto. Cada línea se escucha por separado.",true),
  l(2,"converge","Dos líneas que se acercan","Las líneas están más separadas arriba y se acercan abajo sin tocarse.","Compara la posición de sus extremos superiores y después los inferiores.",false),
  l(2,"cross","Una intersección","Dos rectas se cruzan en el centro. Explora cada una y vuelve al cruce.","La intersección es compartida. Usa el centro como referencia.",false),
  l(2,"zigzag","Cambios de dirección","Un trazo zigzaguea con segmentos cortos y largos. Los clics marcan esquinas.","Repite un tramo. No necesitas contar todas las esquinas de una vez.",false),
  l(2,"mixed","Recta y curva","Hay una recta y una curva separadas. Puedes escuchar todo o filtrar por tipo.","El filtro cambia lo que escuchas y tocas; el dibujo original se conserva.",false),
  l(3,"circle","Un círculo finito","Recorre un círculo. Las señales de giro no son esquinas. Tres notas indican que volviste al inicio.","Encuentra un punto del borde, sal y vuelve a él. No necesitas recorrerlo entero.",false),
  l(3,"thick","El grosor del contorno","Este círculo tiene un trazo más ancho. La zona que responde al dedo también es más ancha.","El grosor es distinto de la tolerancia adicional de contacto, que puedes ajustar en Sonido.",false),
  l(3,"arc","Un círculo abierto","Falta un segmento del círculo. Sus dos extremos no se conectan.","Dos notas descendentes indican final abierto. Busca el espacio entre los extremos.",false),
  l(3,"half","Medio círculo","La curva termina en dos extremos. La variante cambia qué mitad está presente.","Explora sus extremos y luego el tramo curvo que los une.",true),
  l(3,"crescent","Una media luna","Dos arcos delimitan una región en forma de media luna.","Compara contorno, relleno y negativo. El silencio interior depende del modo que elijas.",true),
  l(3,"ellipse","Una elipse","Una figura ovalada. Las variantes cambian su inclinación en el lienzo.","Una elipse en 2D no prueba que un objeto se incline en profundidad.",true),
  l(3,"triangle","Tres lados","Un triángulo con lados de distinta longitud. Puedes escuchar un borde cada vez en el laboratorio.","Escucha los clics de esquina y el cierre de tres notas.",false),
  l(3,"square","Un cuadrado","Cuatro lados iguales y cuatro esquinas rectas.","Puedes empezar por una esquina o por el componente más próximo al centro.",false),
  l(3,"rectangle","Un rectángulo","Dos lados son más largos que los otros dos.","Con la misma velocidad, los lados más largos duran más.",false),
  l(3,"rhombus","Un rombo","Cuatro lados iguales; sus esquinas alternan entre más abiertas y más cerradas.","La trayectoria comunica el cambio de dirección. El clic por sí solo no mide un ángulo.",false),
  l(3,"trapezoid","Un trapecio","Dos lados paralelos de distinta longitud.","Compara primero la base inferior con la superior.",false),
  l(3,"parallelogram","Un romboide","Dos pares de lados paralelos. La figura está inclinada.","Sigue un lado y busca el opuesto antes de examinar los otros dos.",false),
  l(3,"nested","Una figura dentro de otra","Un triángulo está dentro de un cuadrado. Puedes enfocar un componente y recuperar el conjunto.","La región interior sigue siendo otra figura, no un agujero inventado.",false),
  l(4,"organic","Un contorno irregular","El borde se curva y se abomba de manera desigual. Las variantes aumentan esa irregularidad.","La irregularidad modifica la trayectoria. No se añade ruido arbitrario que cambie el significado del sonido.",true),
  l(4,"depth","Profundidad definida","Una línea mantiene la altura del dibujo mientras su profundidad declarada cambia de lejana a cercana. La app representa esa cercanía aumentando la intensidad.","Esta profundidad está escrita en la escena. No se deduce de una foto ni del grosor de la línea.",false)
 ));
 private static Lesson l(int m,String id,String title,String intro,String hint,boolean variants){return new Lesson(m,id,title,intro,hint,variants);}
 public static int clamp(int index){return Math.max(0,Math.min(LESSONS.size()-1,index));}
 public static List<Geometry.Point> points(double... xy){List<Geometry.Point> p=new ArrayList<>();for(int i=0;i<xy.length;i+=2)p.add(new Geometry.Point(xy[i],xy[i+1]));return p;}
 static Geometry.Shape shape(boolean closed,boolean curve,double... xy){return new Geometry.Shape(points(xy),closed,curve);}
 static Geometry.Shape arc(double cx,double cy,double rx,double ry,double from,double to,boolean closed,int variant,double irregular,double width){List<Geometry.Point> p=new ArrayList<>();boolean whole=Math.abs(to-from)>=Math.PI*2-.001;int n=72;for(int i=0;i<(whole?n:n+1);i++){double t=from+(to-from)*i/n,scale=1+irregular*(.6*Math.sin(3*t)+.4*Math.cos(5*t)),x=rx*Math.cos(t)*scale,y=ry*Math.sin(t)*scale,a=variant*Math.PI/8;p.add(new Geometry.Point(cx+x*Math.cos(a)-y*Math.sin(a),cy+x*Math.sin(a)+y*Math.cos(a)));}return new Geometry.Shape(p,closed,true,width);}
 public static Geometry scene(int lesson,int variant,double separation){String id=LESSONS.get(clamp(lesson)).id;List<Geometry.Shape> s=new ArrayList<>();double gap=Math.max(8,Math.min(120,separation));double a=Math.floorMod(variant,8)*Math.PI/4;
  switch(id){
   case "frame":s.add(shape(true,false,0,0,600,0,600,450,0,450));break;
   case "point":s.add(shape(false,false,300,225));break;
   case "pair":s.add(shape(false,false,300-gap/2*Math.cos(a),225+gap/2*Math.sin(a)));s.add(shape(false,false,300+gap/2*Math.cos(a),225-gap/2*Math.sin(a)));break;
   case "chain":gap=Math.min(gap,90);for(int i=-2;i<=2;i++)s.add(shape(false,false,300+i*gap*Math.cos(a),225-i*gap*Math.sin(a)));break;
   case "horizontal":s.add(shape(false,false,80,225,520,225));break;
   case "vertical":s.add(shape(false,false,300,390,300,60));break;
   case "diagonal":s.add(shape(false,false,300-180*Math.cos(a+Math.PI/4),225+150*Math.sin(a+Math.PI/4),300+180*Math.cos(a+Math.PI/4),225-150*Math.sin(a+Math.PI/4)));break;
   case "dashed":for(int i=0;i<4;i++)s.add(shape(false,false,70+i*125,225,150+i*125,225));break;
   case "gap":s.add(shape(false,false,70,225,300-gap/2,225));s.add(shape(false,false,300+gap/2,225,530,225));break;
   case "parallel":for(int j=-1;j<=1;j+=2){double offset=gap/2*j;List<Geometry.Point> p=new ArrayList<>();for(int i=-1;i<=1;i+=2)p.add(new Geometry.Point(300+175*i*Math.cos(a)-offset*Math.sin(a),225+175*i*Math.sin(a)+offset*Math.cos(a)));s.add(new Geometry.Shape(p,false,false));}break;
   case "converge":s.add(shape(false,false,130,70,270,380));s.add(shape(false,false,470,70,330,380));break;
   case "cross":s.add(shape(false,false,90,225,510,225));s.add(shape(false,false,300,60,300,390));break;
   case "zigzag":s.add(shape(false,false,70,340,150,110,220,250,280,180,400,340,530,100));break;
   case "mixed":s.add(shape(false,false,70,130,250,130));s.add(arc(420,260,100,100,0,Math.PI*1.5,false,0,0,3));break;
   case "circle":s.add(arc(300,225,145,145,0,Math.PI*2,true,0,0,3));break;
   case "thick":s.add(arc(300,225,145,145,0,Math.PI*2,true,0,0,24));break;
   case "arc":s.add(arc(300,225,145,145,.4,Math.PI*2-.4,false,0,0,3));break;
   case "half":s.add(arc(300,225,145,145,0,Math.PI,false,variant,0,3));break;
   case "crescent":{List<Geometry.Point> p=new ArrayList<>(arc(300,225,140,140,-Math.PI/2,Math.PI/2,false,0,0,3).points);List<Geometry.Point> inner=arc(300,225,65,140,Math.PI/2,-Math.PI/2,false,0,0,3).points;p.addAll(inner.subList(1,inner.size()-1));double t=variant*Math.PI/8;List<Geometry.Point> rotated=new ArrayList<>();for(Geometry.Point q:p)rotated.add(new Geometry.Point(300+(q.x-300)*Math.cos(t)-(q.y-225)*Math.sin(t),225+(q.x-300)*Math.sin(t)+(q.y-225)*Math.cos(t)));s.add(new Geometry.Shape(rotated,true,true));break;}
   case "ellipse":s.add(arc(300,225,180,75,0,Math.PI*2,true,variant,0,3));break;
   case "triangle":s.add(shape(true,false,300,70,510,370,130,370));break;
   case "square":s.add(shape(true,false,160,85,440,85,440,365,160,365));break;
   case "rectangle":s.add(shape(true,false,80,140,520,140,520,310,80,310));break;
   case "rhombus":s.add(shape(true,false,300,65,520,225,300,385,80,225));break;
   case "trapezoid":s.add(shape(true,false,200,110,400,110,520,340,80,340));break;
   case "parallelogram":s.add(shape(true,false,180,100,510,100,420,350,90,350));break;
   case "nested":s.add(shape(true,false,110,55,490,55,490,395,110,395));s.add(shape(true,false,300,130,390,310,210,310));break;
   case "organic":s.add(arc(300,225,140,125,0,Math.PI*2,true,0,.08+.035*Math.floorMod(variant,6),3));break;
   case "depth":s.add(new Geometry.Shape(Arrays.asList(new Geometry.Point(90,225,1),new Geometry.Point(510,225,0)),false,false));break;
  }
  return new Geometry(4000L+clamp(lesson)*100L+Math.floorMod(variant,16),s);
 }
 /** Component order only; retains real adjacency within every path. */
 public static List<Geometry.Shape> ordered(Geometry g,int filter,int anchor,Geometry.Point local){List<Geometry.Shape> out=new ArrayList<>();for(int i=0;i<g.shapes.size();i++){Geometry.Shape s=g.shapes.get(i);if(filter==0||filter==1&&s.points.size()==1||filter==2&&!s.curve&&s.points.size()>1||filter==3&&s.curve||filter>=10&&i==filter-10)out.add(s);}final Geometry.Point origin=anchor==1?new Geometry.Point(0,0):anchor==2?new Geometry.Point(300,450):anchor==3&&local!=null?local:new Geometry.Point(300,225);if(anchor!=4)out.sort(Comparator.comparingDouble(s->s.points.get(0).distance(origin)));return out;}
}
