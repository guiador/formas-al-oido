package org.formasaloido;

import java.util.*;

/** Contact hysteresis and region modes, independent of output and Android. */
public final class TouchMap {
 public static final class Feedback {
  public final Geometry.Point position;public final String kind;public final int component;public final boolean inside,frame;
  Feedback(Geometry.Point p,String k,int c,boolean in,boolean frame){position=p;kind=k;component=c;inside=in;this.frame=frame;}
 }
 private int previous=-1;private boolean wasInside=false;private Object context;
 public void reset(){previous=-1;wasInside=false;}
 /** One contact state; changing workspace drops all hysteresis from the previous one. */
 public Feedback sample(Object owner,Geometry g,Geometry.Point p,int surface,int filter,double tolerance,boolean air){use(owner);return sample(g,p,surface,filter,tolerance,air);}
 private void use(Object owner){if(!Objects.equals(context,owner)){reset();context=owner;}}
 public Feedback sampleNodes(SceneMap nodes,Geometry.Point p){use(nodes);if(!Double.isFinite(p.x)||!Double.isFinite(p.y)||p.x<0||p.x>600||p.y<0||p.y>450){reset();return new Feedback(p,"silent",-1,false,false);}double margin=Math.min(Math.min(p.x,600-p.x),Math.min(p.y,450-p.y));if(margin<12){reset();return new Feedback(p,"frame",-1,false,true);}int index=nodes.hit(p);return new Feedback(index<0?p:nodes.center(index),index<0?"silent":"vertex",index,index>=0,false);}
 public Feedback sample(Geometry g,Geometry.Point p,int surface,int filter,double tolerance,boolean air){
  if(!Double.isFinite(p.x)||!Double.isFinite(p.y)){reset();return new Feedback(p,"silent",-1,false,false);}double margin=Math.min(Math.min(p.x,600-p.x),Math.min(p.y,450-p.y));if(margin<0){reset();return new Feedback(p,"silent",-1,false,false);}if(margin<12){reset();return new Feedback(p,"frame",-1,false,true);}
  List<Geometry.Shape> shapes=Curriculum.ordered(g,filter,4,null);double best=Double.POSITIVE_INFINITY,nearestDepth=0;int nearest=-1,winding=0;boolean inside=false;Geometry.Point pos=p;
  for(Geometry.Shape s:shapes){int si=g.shapes.indexOf(s);double d=Double.POSITIVE_INFINITY,depth=0;if(s.points.size()==1){d=p.distance(s.points.get(0));depth=s.points.get(0).z;}else{int segments=s.points.size()-(s.closed?0:1);for(int i=0;i<segments;i++){Geometry.Point a=s.points.get(i),b=s.points.get((i+1)%s.points.size());double dx=b.x-a.x,dy=b.y-a.y,length=Math.hypot(dx,dy),t=length==0?0:((p.x-a.x)*dx+(p.y-a.y)*dy)/(length*length);double cap=s.strokeWidth/2/Math.max(1,length);
    // A large contact halo must not extend a line across its missing segment.
    if(!s.closed&&(i==0&&t< -cap||i==segments-1&&t>1+cap))continue;
    double candidate=Geometry.segmentDistance(p,a,b);if(candidate<d){d=candidate;double q=Math.max(0,Math.min(1,t));depth=a.z+(b.z-a.z)*q;}
   }}d=Math.max(0,d-s.strokeWidth/2);if(d<best){best=d;nearest=si;nearestDepth=depth;}
   if(s.closed&&Geometry.inside(p,s))winding+=s.hole?-1:1;
  }
  inside=winding>0;
  // Near the boundary, retain the last region state instead of flickering.
  if(best<6)inside=wasInside;wasInside=inside;
  if(surface!=0){boolean sound=surface==1?inside:!inside;previous=-1;return new Feedback(p,sound?"region":air?"air":"silent",nearest,inside,false);}
  double limit=nearest==previous?tolerance+6:tolerance;String kind;
  if(best<=limit&&nearest>=0){Geometry.Shape s=g.shapes.get(nearest);kind=s.points.size()==1?"vertex":"line";pos=kind.equals("vertex")?s.points.get(0):new Geometry.Point(p.x,p.y,nearestDepth);previous=nearest;}
  else {previous=-1;kind=air?(best<tolerance+35?"near":"air"):"silent";}
  return new Feedback(pos,kind,nearest,inside,false);
 }
}
