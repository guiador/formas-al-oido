package org.formasaloido;

/** Describes stored geometry only; never guesses what a photograph represents. */
public final class Orientation {
 private Orientation(){}
 public static String overview(Geometry g){
  int points=0,open=0,closed=0,holes=0;double left=600,right=0,top=450,bottom=0;
  for(Geometry.Shape s:g.shapes){if(s.points.isEmpty())continue;if(s.points.size()==1)points++;else if(s.closed){if(s.hole)holes++;else closed++;}else open++;
   for(Geometry.Point p:s.points){left=Math.min(left,p.x);right=Math.max(right,p.x);top=Math.min(top,p.y);bottom=Math.max(bottom,p.y);}}
  if(points+open+closed+holes==0)return "La capa actual no contiene geometría para explorar.";
  return "Vista general de la geometría disponible: "+points+" puntos aislados, "+open+" trazos abiertos, "+closed+" contornos cerrados y "+holes+" huecos marcados. Ocupa desde "+Geometry.position(new Geometry.Point(left,top))+" hasta "+Geometry.position(new Geometry.Point(right,bottom))+". El lienzo mide 600 de ancho y 450 de alto; el origen está arriba a la izquierda. Puedes escuchar un elemento a la vez. Esta descripción no identifica objetos ni garantiza que el análisis de una foto sea correcto.";
 }
 public static String at(Geometry g,Geometry.Point p){
  String start="Posición del puntero: "+Math.round(p.x)+" horizontal y "+Math.round(p.y)+" vertical, "+Geometry.position(p)+". ";
  if(p.x<0||p.x>600||p.y<0||p.y>450)return start+"Fuera del lienzo.";
  double best=Double.POSITIVE_INFINITY;int index=-1;
  for(int n=0;n<g.shapes.size();n++){Geometry.Shape s=g.shapes.get(n);double distance=Double.POSITIVE_INFINITY;for(int j=0;j<s.points.size();j++){distance=Math.min(distance,p.distance(s.points.get(j)));if(j>0)distance=Math.min(distance,Geometry.segmentDistance(p,s.points.get(j-1),s.points.get(j)));}if(s.closed&&s.points.size()>1)distance=Math.min(distance,Geometry.segmentDistance(p,s.points.get(s.points.size()-1),s.points.get(0)));if(distance<best){best=distance;index=n;}}
  if(index<0)return start+"No hay componentes en esta capa.";
  Geometry.Shape s=g.shapes.get(index);String type=s.points.size()==1?"punto":s.hole?"borde de hueco":s.closed?"contorno cerrado":"trazo abierto";
  return start+"Componente más cercano: "+(index+1)+", "+type+", a "+Math.round(best)+" unidades. La distancia se refiere al trazo, no al relleno.";
 }
}
