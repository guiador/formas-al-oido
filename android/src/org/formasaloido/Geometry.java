package org.formasaloido;

import java.util.*;

/** Geometry is independent of Android so it can be verified on the host JVM. */
public final class Geometry {
 public static final String[] FAMILIES={"Mezcla","Triángulos","Cuadriláteros","Trazos abiertos","Curvas","Figuras anidadas"};
 public static final class Point {
  public final double x,y,z; public Point(double x,double y){this(x,y,0);}public Point(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
  public double distance(Point p){return Math.hypot(x-p.x,y-p.y);}
 }
 public static final class Shape {
  public final List<Point> points; public final boolean closed,curve,hole;public final double strokeWidth;
  public Shape(List<Point> p,boolean closed,boolean curve){this(p,closed,curve,3);}
  public Shape(List<Point> p,boolean closed,boolean curve,double width){this(p,closed,curve,width,false);}
  public Shape(List<Point> p,boolean closed,boolean curve,double width,boolean hole){points=Collections.unmodifiableList(new ArrayList<>(p));this.closed=closed;this.curve=curve;strokeWidth=width;this.hole=hole;}
 }
 public static final class Element {
  public final List<Point> points; public final int shape,number;public final boolean curve;
  Element(List<Point> p,int s,int n,boolean c){points=Collections.unmodifiableList(p);shape=s;number=n;curve=c;}
  public Point center(){double x=0,y=0;for(Point p:points){x+=p.x;y+=p.y;}return new Point(x/points.size(),y/points.size());}
 }
 public final long seed;public final int family;public final List<Shape> shapes=new ArrayList<>();
 public Geometry(long seed,List<Shape> prepared){this.seed=seed;family=0;shapes.addAll(prepared);}
 public Geometry(long seed,int requested){this.seed=seed;Random r=new Random(seed);family=requested==0?1+r.nextInt(5):requested;List<double[][]> raw=new ArrayList<>();
  if(family==1)raw.add(new double[][]{{0,-1},{.95,.8},{-.85,.8}});
  if(family==2)raw.add(new double[][]{{-1,-.65},{.8,-.65},{1,.65},{-1,.65}});
  if(family==3)raw.add(new double[][]{{-1,.7},{-.5,-.7},{.2,.4},{1,-.4}});
  if(family==4){double[][] p=new double[96][2];for(int i=0;i<p.length;i++){double t=i*2*Math.PI/p.length;p[i]=new double[]{Math.cos(t),.7*Math.sin(t)};}raw.add(p);}
  if(family==5){raw.add(new double[][]{{-1,-1},{1,-1},{1,1},{-1,1}});raw.add(new double[][]{{-.4,-.3},{.4,-.3},{0,.45}});}
  double a=r.nextDouble()*Math.PI*2,scale=85+r.nextDouble()*55,cx=270+r.nextDouble()*60,cy=210+r.nextDouble()*30;
  for(double[][] polygon:raw){List<Point> p=new ArrayList<>();for(double[] q:polygon)p.add(new Point(cx+scale*(q[0]*Math.cos(a)-q[1]*Math.sin(a)),cy+scale*(q[0]*Math.sin(a)+q[1]*Math.cos(a))));shapes.add(new Shape(p,family!=3,family==4));}
 }
 public boolean closed(){for(Shape s:shapes)if(!s.closed)return false;return true;}
 public List<Element> elements(int level){List<Element> out=new ArrayList<>();for(int si=0;si<shapes.size();si++){Shape s=shapes.get(si);if(level==0){if(s.points.size()==1)out.add(new Element(s.points,si,0,false));else if(s.curve){List<Point> p=new ArrayList<>(s.points);if(s.closed)p.add(p.get(0));out.add(new Element(p,si,0,true));}else for(int i=0;i<s.points.size()-(s.closed?0:1);i++)out.add(new Element(Arrays.asList(s.points.get(i),s.points.get((i+1)%s.points.size())),si,i,false));}
   else if(level==1&&!s.curve){for(int i=0;i<s.points.size();i++)out.add(new Element(Collections.singletonList(s.points.get(i)),si,i,false));}
   else if(level==2&&s.closed&&!s.hole){double x=0,y=0;for(Point p:s.points){x+=p.x;y+=p.y;}out.add(new Element(Collections.singletonList(new Point(x/s.points.size(),y/s.points.size())),si,0,false));}}
  return out;
 }
 public static double segmentDistance(Point p,Point a,Point b){double dx=b.x-a.x,dy=b.y-a.y,den=dx*dx+dy*dy,t=den==0?0:Math.max(0,Math.min(1,((p.x-a.x)*dx+(p.y-a.y)*dy)/den));return Math.hypot(p.x-a.x-t*dx,p.y-a.y-t*dy);}
 public static boolean inside(Point p,Shape s){boolean in=false;for(int i=0,j=s.points.size()-1;i<s.points.size();j=i++){Point a=s.points.get(i),b=s.points.get(j);if((a.y>p.y)!=(b.y>p.y)&&p.x<(b.x-a.x)*(p.y-a.y)/(b.y-a.y)+a.x)in=!in;}return in;}
 public static double distance(Point p,Element e){double d=Double.POSITIVE_INFINITY;if(e.points.size()==1)return p.distance(e.points.get(0));for(int i=1;i<e.points.size();i++)d=Math.min(d,segmentDistance(p,e.points.get(i-1),e.points.get(i)));return d;}
 public static String position(Point p){return (p.y<170?"arriba":p.y>280?"abajo":"a media altura")+(p.x<240?", a la izquierda":p.x>360?", a la derecha":", en el centro");}
 public static String description(Element e,int level){if(level==2)return "Centro de la región "+(e.shape+1)+", "+position(e.center())+".";if(level==1||e.points.size()==1)return "Punto "+(e.number+1)+", "+position(e.center())+".";if(e.curve)return "Curva. Las campanas marcan el giro. Escucha la señal final para saber si se cierra.";Point a=e.points.get(0),b=e.points.get(e.points.size()-1);String horizontal=b.x-a.x>20?"hacia la derecha":a.x-b.x>20?"hacia la izquierda":"casi vertical";String vertical=b.y-a.y>20?"bajando":a.y-b.y>20?"subiendo":"casi horizontal";return "Borde "+(e.number+1)+", desde "+position(a)+", "+horizontal+", "+vertical+". Termina "+position(b)+".";}
}
