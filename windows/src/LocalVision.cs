using System;
using System.IO;
using System.Drawing;
using System.Collections.Generic;
using System.Linq;
using System.Web.Script.Serialization;
using Microsoft.ML.OnnxRuntime;
using Microsoft.ML.OnnxRuntime.Tensors;

static class LocalVision {
 public static string Analyze(Bitmap original,string folder,bool standard){
  var labels=new Dictionary<int,string>();foreach(string line in File.ReadAllLines(Path.Combine(folder,"labels.tsv"),System.Text.Encoding.UTF8)){string[] a=line.Split('\t');if(a.Length==2)labels[int.Parse(a[0])]=a[1];}
  double scale=Math.Min(1,320.0/Math.Max(original.Width,original.Height));int w=Math.Max(2,(int)(original.Width*scale)),h=Math.Max(2,(int)(original.Height*scale));var input=new DenseTensor<byte>(new int[]{1,h,w,3});using(var image=new Bitmap(w,h)){using(Graphics g=Graphics.FromImage(image)){g.Clear(Color.White);g.DrawImage(original,0,0,w,h);}for(int y=0;y<h;y++)for(int x=0;x<w;x++){Color c=image.GetPixel(x,y);input[0,y,x,0]=c.R;input[0,y,x,1]=c.G;input[0,y,x,2]=c.B;}}
  string source="Hipótesis local SSD-MobileNet "+(standard?"estándar":"compacto");var regions=new List<object>();using(var options=new SessionOptions()){options.IntraOpNumThreads=2;options.InterOpNumThreads=1;using(var session=new InferenceSession(Path.Combine(folder,standard?"model-standard.onnx":"model.onnx"),options))using(var results=session.Run(new[]{NamedOnnxValue.CreateFromTensor(session.InputMetadata.Keys.First(),input)})){
   float[] boxes=results.First(v=>v.Name.StartsWith("detection_boxes")).AsTensor<float>().ToArray(),scores=results.First(v=>v.Name.StartsWith("detection_scores")).AsTensor<float>().ToArray(),classes=results.First(v=>v.Name.StartsWith("detection_classes")).AsTensor<float>().ToArray();
   for(int i=0;i<scores.Length;i++){if(float.IsNaN(scores[i])||float.IsInfinity(scores[i])||scores[i]<.60)continue;double l=Math.Max(0,boxes[4*i+1]),t=Math.Max(0,boxes[4*i]),r=Math.Min(1,boxes[4*i+3]),b=Math.Min(1,boxes[4*i+2]);string label;if(r-l<.002||b-t<.002||!labels.TryGetValue((int)classes[i],out label))continue;regions.Add(new Dictionary<string,object>{{"box",new double[]{l,t,r,b}},{"confidence",scores[i]},{"label","Podría ser "+label},{"analogy",""}});}
  }}
  regions=regions.OrderByDescending(item=>Convert.ToDouble(((Dictionary<string,object>)item)["confidence"])).ToList();
  string overview=regions.Count==0?"El detector no propuso objetos por encima del umbral; no demuestra que la imagen esté vacía.":"El detector propone "+regions.Count+" regiones. Son hipótesis, no confirmaciones.";for(int i=0;i<Math.Min(4,regions.Count);i++){var region=(Dictionary<string,object>)regions[i];var box=(double[])region["box"];overview+=" "+region["label"]+", "+((box[1]+box[3])/2<.33?"arriba":(box[1]+box[3])/2>.66?"abajo":"a media altura")+", "+((box[0]+box[2])/2<.33?"a la izquierda":(box[0]+box[2])/2>.66?"a la derecha":"en el centro")+".";}if(regions.Count>4)overview+=" Hay más componentes en la lista.";return new JavaScriptSerializer().Serialize(new Dictionary<string,object>{{"source",source},{"overview",overview},{"regions",regions}});
 }
}
