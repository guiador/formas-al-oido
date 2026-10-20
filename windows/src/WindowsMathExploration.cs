using System;using System.Globalization;using System.Windows.Forms;

sealed partial class FormasForm {
 readonly ComboBox mathScanMode=new ComboBox(),mathScanOrigin=new ComboBox(),mathTouchFilter=new ComboBox();readonly NumericUpDown mathScanSpacing=new NumericUpDown();readonly CheckBox mathScanInvert=new CheckBox();int mathScanSection=-1;string mathScanResult="";
 void BuildMathExploration(FlowLayoutPanel p){
  Paragraph(p,"Exploración matemática con el mismo marco, aire del vacío y contacto de figuras e imágenes. La zona entre el plano graduado y el borde también se puede explorar.");
  MathButton(p,"Explorar lienzo matemático",()=>mathCanvas.Focus());MathButton(p,"Me perdí: centro y cuatro esquinas",()=>{Stop();Send("MATH_REFERENCE");});MathButton(p,"Escuchar marco completo",()=>{Stop();Send("MATH_FRAME");});
  MathButton(p,"Ajustar vacío, contacto y vibración",()=>{foreach(TabPage page in tabs.TabPages)if(page.Text=="Sonido y mando"){tabs.SelectedTab=page;break;}});
  Select(p,"Componentes explorados",mathTouchFilter,new string[]{"Todo: ejes y figura","Sólo figura","Sólo ejes"},0,()=>{Stop();mathScanSection=-1;});
  Select(p,"Barrido sonoro del plano",mathScanMode,new string[]{"Filas","Columnas","Cuadrantes","Relleno de silueta","Contorno desde referencia"},0,()=>mathScanSection=-1);
  Select(p,"Inicio del barrido",mathScanOrigin,new string[]{"Superior izquierda","Superior derecha","Inferior derecha","Inferior izquierda","Último punto explorado"},0,()=>mathScanSection=-1);
  MathNumber(p,"Separación del barrido, unidades",mathScanSpacing,16,80,40,4,()=>mathScanSection=-1);Check(p,mathScanInvert,"Invertir contraste sonoro del barrido",false,()=>mathScanSection=-1);
  MathButton(p,"Leer barrido completo",()=>MathScan(-1));MathButton(p,"Leer siguiente fila o sección",()=>MathScan(mathScanSection+1));MathButton(p,"Volver a primera sección del barrido",()=>MathScan(0));
  string[] names={"Pausar recorrido matemático","Continuar recorrido matemático","Retroceder cinco segundos y pausar","Guardar marcador del recorrido","Volver al marcador del recorrido","Repetir últimos cinco segundos"},commands={"pause","resume","back","mark","return","fragment"};for(int i=0;i<names.Length;i++){string command=commands[i];MathButton(p,names[i],()=>{StopMathVoice();Send("MATH_CONTROL|"+command);});}
 }
 void MathScan(int section){Stop();int origin=mathScanOrigin.SelectedIndex;double x=origin==4?mathCanvas.Position.X:origin==1||origin==2?600:0,y=origin==4?mathCanvas.Position.Y:origin>=2?450:0;mathScanSection=section;Send("MATH_SCAN|"+mathPlane.SelectedIndex+"|"+mathScanMode.SelectedIndex+"|"+x.ToString(CultureInfo.InvariantCulture)+"|"+y.ToString(CultureInfo.InvariantCulture)+"|"+mathScanSpacing.Value+"|"+section+"|"+mathScanInvert.Checked.ToString().ToLowerInvariant()+"|"+mathTouchFilter.SelectedIndex);}
 int mathExploreSmoke;
 bool SmokeMathExploration(){if(mathExploreSmoke==0){mathCanvas.MoveLogical(0,225);if(mathCanvas.Position.X!=0)throw new Exception("Math frame remains unreachable");mathScanResult="";mathScanMode.SelectedIndex=2;MathScan(-1);mathExploreSmoke=1;return false;}if(mathScanResult.Length==0)return false;if(!mathScanResult.Contains("4 secciones"))throw new Exception("Math quadrant scan failed");return true;}
}
