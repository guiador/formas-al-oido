package org.formasaloido;

import java.util.*;

/** Detection first, bounded spatial nodes second. Boxes are hit zones, never silhouettes. */
public final class SceneMap {
 public static final double MIN_CONFIDENCE=.60;
 public static final int MAX_OBJECTS=6;
 public final ImageScene scene;
 private final double left,top,width,height;
 private final int focused;
 public SceneMap(ImageScene input,ImageTrace.Result layout){this(input,layout,MIN_CONFIDENCE,MAX_OBJECTS);}
 public SceneMap(ImageScene input,ImageTrace.Result layout,double confidence,int maximum){
  if(!Double.isFinite(confidence)||confidence<0||confidence>1||maximum<1||maximum>24)throw new IllegalArgumentException("Límites de detección no válidos");
  scene=new ImageScene();scene.source=input.source;
  List<ImageScene.Region> candidates=new ArrayList<>();
  for(ImageScene.Region r:input.regions)if(Double.isFinite(r.confidence)&&r.confidence>=confidence&&r.confidence<=1&&!unknown(r.label)&&!r.source.equals("Geometría extraída"))candidates.add(r);
  candidates.sort((a,b)->{int c=Double.compare(b.confidence,a.confidence);return c!=0?c:Double.compare(area(b),area(a));});
  for(ImageScene.Region r:candidates){boolean duplicate=false;for(ImageScene.Region kept:scene.regions)if(r.label.equalsIgnoreCase(kept.label)&&overlap(r,kept)>.65){duplicate=true;break;}if(!duplicate)scene.regions.add(r);if(scene.regions.size()==maximum)break;}
  scene.overview=scene.regions.isEmpty()?"No hay objetos reconocidos con la confianza requerida.":scene.regions.size()+" objetos priorizados. Las etiquetas son hipótesis.";
  left=layout.left;top=layout.top;width=layout.width*layout.scale;height=layout.height*layout.scale;focused=-1;
 }
 private SceneMap(SceneMap source,int index){scene=source.scene;ImageScene.Region r=scene.regions.get(index);double w=(r.right-r.left)*source.width,h=(r.bottom-r.top)*source.height,s=Math.min(568/w,418/h);width=w*s;height=h*s;left=(600-width)/2;top=(450-height)/2;focused=index;}
 public SceneMap focus(int index){return new SceneMap(this,index);}
 private static boolean unknown(String value){String s=value.toLowerCase(Locale.ROOT).trim();return s.isEmpty()||s.contains("sin identificar")||s.contains("no reconocido")||s.equals("unknown")||s.equals("unidentified");}
 private static double area(ImageScene.Region r){return (r.right-r.left)*(r.bottom-r.top);}
 private static double overlap(ImageScene.Region a,ImageScene.Region b){double intersection=Math.max(0,Math.min(a.right,b.right)-Math.max(a.left,b.left))*Math.max(0,Math.min(a.bottom,b.bottom)-Math.max(a.top,b.top));return intersection/(area(a)+area(b)-intersection);}
 public int hit(Geometry.Point p){
  if(!Double.isFinite(p.x)||!Double.isFinite(p.y))return -1;
  double x=(p.x-left)/width,y=(p.y-top)/height;
  if(focused>=0)return x>=0&&x<=1&&y>=0&&y<=1?focused:-1;
  int best=-1;double smallest=Double.POSITIVE_INFINITY;
  for(int i=0;i<scene.regions.size();i++){ImageScene.Region r=scene.regions.get(i);if(x>=r.left&&x<=r.right&&y>=r.top&&y<=r.bottom&&area(r)<smallest){best=i;smallest=area(r);}}
  return best;
 }
 public Geometry.Point center(int index){if(focused>=0)return new Geometry.Point(300,225);ImageScene.Region r=scene.regions.get(index);return new Geometry.Point(left+(r.left+r.right)*width/2,top+(r.top+r.bottom)*height/2);}
 public Geometry geometry(){List<Geometry.Shape> nodes=new ArrayList<>();for(int i=0;i<scene.regions.size();i++)if(focused<0||focused==i)nodes.add(new Geometry.Shape(Arrays.asList(center(i)),false,false));return new Geometry(0,nodes);}
}
