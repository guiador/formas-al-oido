import org.formasaloido.*;
import java.util.*;

public final class SceneMapChecks {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static ImageScene.Region region(double l,double t,double r,double b,String label,double confidence){ImageScene.Region result=new ImageScene.Region(l,t,r,b,label,"","Detector");result.confidence=confidence;return result;}
 public static void main(String[] args){
  ImageTrace.Result layout=ImageTrace.layout(640,480);
  ImageScene source=new ImageScene();
  source.regions.add(region(.1,.1,.8,.8,"mesa",.92));
  source.regions.add(region(.11,.11,.79,.79,"mesa",.85));
  source.regions.add(region(.2,.2,.3,.3,"botón",.81));
  source.regions.add(region(.8,.8,.9,.9,"ruido",.59));
  source.regions.add(region(.1,.1,.9,.9,"Sin identificar",.99));
  SceneMap map=new SceneMap(source,layout);
  check(map.scene.regions.size()==2,"unknown, low confidence and duplicate boxes removed");
  check(map.scene.regions.get(0).label.equals("mesa"),"main detection first");
  check(source.regions.size()==5,"source detections untouched");
  check(map.geometry().shapes.size()==2,"one node per object");
  for(Geometry.Shape shape:map.geometry().shapes)check(shape.points.size()==1&&!shape.closed,"no box or pixel fragment becomes an audible contour");
  Geometry.Point button=map.center(1);
  check(map.hit(button)==1,"smaller object remains reachable inside larger detection");
  check(map.hit(new Geometry.Point(599,449))==-1,"outside image is empty");
  check(map.hit(new Geometry.Point(Double.NaN,225))==-1,"nonfinite pointer rejected");
  SceneMap focus=map.focus(1);
  check(focus.hit(new Geometry.Point(300,225))==1,"focus keeps original object identity");
  check(focus.geometry().shapes.size()==1,"focus has only selected node");
  check(map.hit(button)==1,"focus leaves overview coordinates intact");
  ImageScene many=new ImageScene();for(int i=0;i<20;i++)many.regions.add(region(.1,.1,.8,.8,"objeto "+i,.61+i*.01));
  SceneMap limited=new SceneMap(many,layout);check(limited.scene.regions.size()==6,"default object budget");check(limited.scene.regions.get(0).label.equals("objeto 19"),"sort before budget even for unsorted detections");
  check(new SceneMap(many,layout,.75,3).scene.regions.size()==3,"configurable thresholds and budget");
  ImageScene empty=new ImageScene();check(new SceneMap(empty,layout).geometry().shapes.isEmpty(),"empty scene adds no fallback noise");
  String noConfidence="{\"regions\":[{\"box\":[0,0,1,1],\"label\":\"cosa\"}]}";
  check(new SceneMap(ImageScene.fromJson(noConfidence,"API"),layout).scene.regions.isEmpty(),"missing confidence never bypasses threshold");
  ImageScene roundtrip=ImageScene.fromJson(map.scene.json(),"Detector");check(roundtrip.regions.get(0).confidence==.92,"confidence survives JSON protocol");
  TouchMap contact=new TouchMap();
  check(contact.sampleNodes(map,button).component==1,"shared contact uses whole object hit zone");
  check(contact.sampleNodes(map,new Geometry.Point(0,225)).frame,"node map uses common canvas frame");
  Geometry rectangle=new Geometry(0,Arrays.asList(new Geometry.Shape(Curriculum.points(100,100,500,100,500,350,100,350),true,false)));
  contact.sample("canvas",rectangle,new Geometry.Point(300,225),1,0,18,false);
  check(contact.sample("math",rectangle,new Geometry.Point(99,225),1,0,18,false).kind.equals("silent"),"context switch clears inside hysteresis");
  contact.sample("math",rectangle,new Geometry.Point(300,225),1,0,18,false);
  contact.sample("math",rectangle,new Geometry.Point(0,225),1,0,18,false);
  check(contact.sample("math",rectangle,new Geometry.Point(99,225),1,0,18,false).kind.equals("silent"),"frame crossing clears stale inside state");
  check(contact.sample(rectangle,new Geometry.Point(Double.NaN,225),0,0,18,true).kind.equals("silent"),"invalid pointer cannot produce audio");
  System.out.println("PASS "+checks+" semantic node, confidence, budget and unified contact checks.");
 }
}
