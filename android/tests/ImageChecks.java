import org.formasaloido.*;
import java.util.*;

public final class ImageChecks {
 static int count=0;
 static void check(boolean value,String message){count++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  ImageTrace.Options options=new ImageTrace.Options();options.maxDimension=192;options.minArea=3;
  int w=160,h=120;int[] white=new int[w*h];Arrays.fill(white,0xffffffff);
  check(ImageTrace.analyze(white,w,h,options).geometry.shapes.isEmpty(),"white drawing is empty");
  int[] transparent=new int[w*h];check(ImageTrace.analyze(transparent,w,h,options).geometry.shapes.isEmpty(),"transparent background composites white");
  int[] rect=white.clone();for(int y=25;y<95;y++)for(int x=30;x<130;x++)rect[y*w+x]=0xff000000;
  ImageTrace.Result rectangle=ImageTrace.analyze(rect,w,h,options);check(rectangle.geometry.shapes.size()==1,"one rectangle boundary");check(!rectangle.geometry.shapes.get(0).hole,"outer boundary is not a hole");check(rectangle.geometry.shapes.get(0).points.size()==4,"rectangle simplified to four corners");
  int[] inverse=rect.clone();for(int i=0;i<inverse.length;i++)inverse[i]=0xff000000|(~inverse[i]&0xffffff);options.mode=1;ImageTrace.Result reversed=ImageTrace.analyze(inverse,w,h,options);check(reversed.geometry.shapes.size()==rectangle.geometry.shapes.size(),"light-on-dark inversion preserves boundary count");options.mode=0;
  ImageTrace.Result ring=ImageTrace.analyze(ImageTrace.demo(1,w,h),w,h,options);check(ring.geometry.shapes.size()==2,"ring has outer and inner boundaries");check(ring.geometry.shapes.get(1).hole,"inner boundary is a hole");check(ring.geometry.elements(2).size()==1,"hole is not a material region");TouchMap touch=new TouchMap();check(touch.sample(ring.geometry,new Geometry.Point(300,225),1,0,8,false).kind.equals("silent"),"ring center stays empty in fill mode");check(touch.sample(ring.geometry,new Geometry.Point(300,225),2,0,8,false).kind.equals("region"),"ring center sounds in negative mode");
  int[] separated=white.clone();for(int y=45;y<75;y++)for(int x=20;x<140;x++)if(x<70||x>90)separated[y*w+x]=0xff000000;ImageTrace.Result gap=ImageTrace.analyze(separated,w,h,options);check(gap.geometry.shapes.size()==2,"separated strokes remain distinct");
  options.mode=2;check(ImageTrace.analyze(white,w,h,options).geometry.shapes.isEmpty(),"photo edge mode has no edges on uniform image");check(!ImageTrace.analyze(rect,w,h,options).geometry.shapes.isEmpty(),"photo edge mode finds contrast");options.mode=0;
  int[] diagonal=white.clone();diagonal[50*w+50]=0xff000000;diagonal[51*w+51]=0xff000000;options.minArea=1;check(ImageTrace.analyze(diagonal,w,h,options).geometry.shapes.size()==2,"diagonal pixels not falsely merged at a vertex");
  options.maxContours=4;options.maxDimension=256;options.minArea=1;options.detail=.5;Random random=new Random(123);int[] noise=new int[256*256];for(int i=0;i<noise.length;i++)noise[i]=random.nextBoolean()?0xff000000:0xffffffff;long start=System.nanoTime();ImageTrace.Result busy=ImageTrace.analyze(noise,256,256,options);check(busy.geometry.shapes.size()<=4,"contour budget enforced");check(busy.dropped>0,"omitted candidates reported");
  for(Geometry.Shape shape:busy.geometry.shapes){check(shape.points.size()<=160,"point budget enforced");check(shape.closed,"boundary walk closes");for(Geometry.Point p:shape.points)check(p.x>=0&&p.x<=600&&p.y>=0&&p.y<=450,"image mapping within canvas");}
  for(int id=0;id<3;id++)for(int mode=0;mode<3;mode++)for(int resolution:new int[]{128,192,256}){ImageTrace.Options o=new ImageTrace.Options();o.mode=mode;o.maxDimension=resolution;ImageTrace.Result r=ImageTrace.analyze(ImageTrace.demo(id,480,360),480,360,o);check(Math.max(r.width,r.height)<=resolution,"bounded analysis dimensions");check(r.geometry.shapes.size()<=o.maxContours,"bounded components");check(r.left>=0&&r.top>=0,"aspect ratio letterboxing");for(Geometry.Shape shape:r.geometry.shapes)check(shape.points.size()>=3,"valid polygon after simplification");}
  System.out.println("PASS "+count+" image checks: thresholding, inversion, alpha, holes, discontinuity, photo edges, topology, limits and mapping. Noisy image "+((System.nanoTime()-start)/1000000)+" ms including fixture matrix.");
 }
}
