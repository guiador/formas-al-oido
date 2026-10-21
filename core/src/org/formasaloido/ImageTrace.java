package org.formasaloido;

import java.util.*;

/** Bounded local raster-to-boundary extraction. No object recognition or invented depth. */
public final class ImageTrace {
 public static final class Options {
  public int mode=0,offset=0,edgeThreshold=70,maxDimension=192,minArea=8,maxContours=12;public double detail=1.5;
  public Options copy(){Options o=new Options();o.mode=mode;o.offset=offset;o.edgeThreshold=edgeThreshold;o.maxDimension=maxDimension;o.minArea=minArea;o.maxContours=maxContours;o.detail=detail;return o;}
 }
 public static final class Result {
  public final Geometry geometry;public final int width,height,threshold,detected,dropped,foreground;public final double scale,left,top;
  Result(Geometry g,int w,int h,int t,int count,int omitted,int fg){geometry=g;width=w;height=h;threshold=t;detected=count;dropped=omitted;foreground=fg;scale=Math.min(568.0/w,418.0/h);left=(600-w*scale)/2;top=(450-h*scale)/2;}
 }
 static final class Loop {List<Geometry.Point> points;double area;Loop(List<Geometry.Point> p,double a){points=p;area=a;}}
 /** Image placement without extracting or classifying any pixel fragments. */
 public static Result layout(int width,int height){if(width<2||height<2||width>4096||height>4096)throw new IllegalArgumentException("Dimensiones de imagen no válidas");return new Result(new Geometry(0,new ArrayList<Geometry.Shape>()),width,height,0,0,0,0);}
 public static Result analyze(int[] pixels,int width,int height,Options input){
  if(width<2||height<2||width>4096||height>4096||pixels.length!=width*height)throw new IllegalArgumentException("Dimensiones de imagen no válidas");
  Options o=input.copy();int limit=Math.max(64,Math.min(256,o.maxDimension));double resize=Math.min(1,limit/(double)Math.max(width,height));int w=Math.max(2,(int)Math.round(width*resize)),h=Math.max(2,(int)Math.round(height*resize));int[] gray=new int[w*h],hist=new int[256];
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){int color=pixels[Math.min(height-1,y*height/h)*width+Math.min(width-1,x*width/w)],alpha=color>>>24;int value=((77*((color>>16)&255)+150*((color>>8)&255)+29*(color&255))>>8);value=(value*alpha+255*(255-alpha))/255;gray[y*w+x]=value;hist[value]++;}
  // A small blur suppresses isolated photographic noise; drawing mode retains gaps.
  if(o.mode==2){int[] blurred=gray.clone();for(int y=1;y<h-1;y++)for(int x=1;x<w-1;x++){int sum=0;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)sum+=gray[(y+dy)*w+x+dx];blurred[y*w+x]=sum/9;}gray=blurred;}
  int threshold=Math.max(0,Math.min(255,otsu(hist,w*h)+o.offset));boolean[] mask=new boolean[w*h];int fg=0;
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){boolean on;if(o.mode==2){if(x==0||y==0||x==w-1||y==h-1)on=false;else{int a=gray[(y-1)*w+x-1],b=gray[(y-1)*w+x],c=gray[(y-1)*w+x+1],d=gray[y*w+x-1],e=gray[y*w+x+1],f=gray[(y+1)*w+x-1],g=gray[(y+1)*w+x],i=gray[(y+1)*w+x+1];int gx=-a-2*d-f+c+2*e+i,gy=-a-2*b-c+f+2*g+i;on=Math.hypot(gx,gy)/4>=Math.max(10,o.edgeThreshold);}}else on=o.mode==1?gray[y*w+x]>threshold:gray[y*w+x]<=threshold;mask[y*w+x]=on;if(on)fg++;}
  int maxEdges=w*h*4,vertices=(w+1)*(h+1);int[] heads=new int[vertices],from=new int[maxEdges],to=new int[maxEdges],next=new int[maxEdges],direction=new int[maxEdges];Arrays.fill(heads,-1);int count=0;
  for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(mask[y*w+x]){int a=y*(w+1)+x,b=a+1,c=a+w+2,d=a+w+1;
   if(y==0||!mask[(y-1)*w+x])count=edge(a,b,0,count,heads,from,to,next,direction);
   if(x==w-1||!mask[y*w+x+1])count=edge(b,c,1,count,heads,from,to,next,direction);
   if(y==h-1||!mask[(y+1)*w+x])count=edge(c,d,2,count,heads,from,to,next,direction);
   if(x==0||!mask[y*w+x-1])count=edge(d,a,3,count,heads,from,to,next,direction);
  }
  boolean[] visited=new boolean[count];List<Loop> loops=new ArrayList<>();int rejected=0;
  for(int initial=0;initial<count;initial++){if(visited[initial])continue;int current=initial,start=from[initial],steps=0;boolean closed=false;List<Geometry.Point> points=new ArrayList<>();
   while(current>=0&&!visited[current]&&steps++<=count){visited[current]=true;int vertex=from[current];points.add(new Geometry.Point(vertex%(w+1),vertex/(w+1)));int end=to[current];if(end==start){closed=true;break;}int best=-1,score=99;for(int candidate=heads[end];candidate!=-1;candidate=next[candidate])if(!visited[candidate]){int turn=(direction[candidate]-direction[current]+4)%4,priority=turn==1?0:turn==0?1:turn==3?2:3;if(priority<score){score=priority;best=candidate;}}current=best;}
   double area=signedArea(points);if(closed&&points.size()>=3&&Math.abs(area)>=Math.max(1,o.minArea)){loops.add(new Loop(points,area));}else rejected++;
  }
  loops.sort((a,b)->Double.compare(Math.abs(b.area),Math.abs(a.area)));int retained=Math.min(Math.max(1,Math.min(64,o.maxContours)),loops.size());double scale=Math.min(568.0/w,418.0/h),left=(600-w*scale)/2,top=(450-h*scale)/2;List<Geometry.Shape> shapes=new ArrayList<>();
  for(int i=0;i<retained;i++){Loop loop=loops.get(i);List<Geometry.Point> closed=new ArrayList<>(loop.points);closed.add(closed.get(0));List<Geometry.Point> simple=simplify(closed,Math.max(.4,Math.min(5,o.detail)));if(simple.size()>1&&simple.get(0).distance(simple.get(simple.size()-1))<.001)simple.remove(simple.size()-1);if(simple.size()<3)simple=loop.points;simple=cap(simple,160);List<Geometry.Point> mapped=new ArrayList<>();for(Geometry.Point p:simple)mapped.add(new Geometry.Point(left+p.x*scale,top+p.y*scale));boolean smooth=simple.size()>6&&maxTurn(simple)<.72;shapes.add(new Geometry.Shape(mapped,true,smooth,3,loop.area<0));}
  long seed=Arrays.hashCode(gray);return new Result(new Geometry(seed,shapes),w,h,o.mode==2?o.edgeThreshold:threshold,loops.size()+rejected,Math.max(0,loops.size()-retained)+rejected,fg);
 }
 static int edge(int a,int b,int d,int count,int[] head,int[] from,int[] to,int[] next,int[] direction){from[count]=a;to[count]=b;direction[count]=d;next[count]=head[a];head[a]=count;return count+1;}
 static int otsu(int[] hist,int total){double sum=0;for(int i=0;i<256;i++)sum+=(double)i*hist[i];int weight=0,threshold=0;double partial=0,best=-1;for(int i=0;i<256;i++){weight+=hist[i];if(weight==0)continue;int other=total-weight;if(other==0)break;partial+=(double)i*hist[i];double delta=partial/weight-(sum-partial)/other,score=(double)weight*other*delta*delta;if(score>best){best=score;threshold=i;}}return threshold;}
 static double signedArea(List<Geometry.Point> p){double area=0;for(int i=0;i<p.size();i++){Geometry.Point a=p.get(i),b=p.get((i+1)%p.size());area+=a.x*b.y-b.x*a.y;}return area/2;}
 static double maxTurn(List<Geometry.Point> points){double max=0;for(int i=0;i<points.size();i++){Geometry.Point a=points.get((i+points.size()-1)%points.size()),b=points.get(i),c=points.get((i+1)%points.size());double ux=b.x-a.x,uy=b.y-a.y,vx=c.x-b.x,vy=c.y-b.y;max=Math.max(max,Math.abs(Math.atan2(ux*vy-uy*vx,ux*vx+uy*vy)));}return max;}
 static List<Geometry.Point> cap(List<Geometry.Point> input,int maximum){if(input.size()<=maximum)return input;List<Geometry.Point> out=new ArrayList<>();for(int i=0;i<maximum;i++)out.add(input.get(i*input.size()/maximum));return out;}
 // Iterative RDP avoids call-stack growth on noisy images.
 static List<Geometry.Point> simplify(List<Geometry.Point> p,double epsilon){boolean[] keep=new boolean[p.size()];keep[0]=keep[p.size()-1]=true;ArrayDeque<int[]> stack=new ArrayDeque<>();stack.push(new int[]{0,p.size()-1});while(!stack.isEmpty()){int[] range=stack.pop();double max=epsilon;int at=-1;for(int i=range[0]+1;i<range[1];i++){double d=Geometry.segmentDistance(p.get(i),p.get(range[0]),p.get(range[1]));if(d>max){max=d;at=i;}}if(at>=0){keep[at]=true;stack.push(new int[]{range[0],at});stack.push(new int[]{at,range[1]});}}List<Geometry.Point> out=new ArrayList<>();for(int i=0;i<p.size();i++)if(keep[i])out.add(p.get(i));return out;}
 public static int[] demo(int id,int w,int h){int[] p=new int[w*h];Arrays.fill(p,0xffffffff);for(int y=0;y<h;y++)for(int x=0;x<w;x++){double nx=x/(double)w,ny=y/(double)h;boolean on=id==0?Math.hypot((nx-.5)*w,(ny-.5)*h)<h*.3:id==1?(nx>.2&&nx<.8&&ny>.15&&ny<.85)&&!(nx>.4&&nx<.6&&ny>.35&&ny<.65):(ny>.4&&ny<.85&&nx>.22&&nx<.78)||(ny>.15&&ny<=.4&&Math.abs(nx-.5)<(ny-.15)*1.2);if(on)p[y*w+x]=0xff000000;}return p;}
}
