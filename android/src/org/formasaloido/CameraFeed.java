package org.formasaloido;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.*;
import android.os.*;
import android.util.Size;
import android.view.Surface;
import java.nio.ByteBuffer;
import java.util.*;

/** Low-rate camera2 feed. Only luminance is decoded; all Images are closed promptly. */
public final class CameraFeed implements AutoCloseable {
 public interface Listener {void frame(Bitmap bitmap);void error(String message);void ready(String camera);}
 private final Activity activity;private final Listener listener;private final Handler main=new Handler(Looper.getMainLooper());private HandlerThread worker;private Handler handler;private CameraDevice device;private CameraCaptureSession session;private ImageReader reader;private volatile boolean closed=false;private int rotation;private boolean front;private long lastFrame=0;
 public CameraFeed(Activity activity,Listener listener){this.activity=activity;this.listener=listener;}
 public void open(){
  if(activity.checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){fail("No hay permiso de cámara. Puedes importar una imagen.");return;}
  try{CameraManager manager=(CameraManager)activity.getSystemService(Activity.CAMERA_SERVICE);String selected=null;for(String id:manager.getCameraIdList()){CameraCharacteristics c=manager.getCameraCharacteristics(id);if(selected==null)selected=id;if(Integer.valueOf(CameraCharacteristics.LENS_FACING_BACK).equals(c.get(CameraCharacteristics.LENS_FACING))){selected=id;break;}}
   if(selected==null)throw new IllegalStateException("No se encontró cámara");CameraCharacteristics c=manager.getCameraCharacteristics(selected);front=Integer.valueOf(CameraCharacteristics.LENS_FACING_FRONT).equals(c.get(CameraCharacteristics.LENS_FACING));Integer sensor=c.get(CameraCharacteristics.SENSOR_ORIENTATION);int display=activity.getWindowManager().getDefaultDisplay().getRotation(),degrees=display==Surface.ROTATION_90?90:display==Surface.ROTATION_180?180:display==Surface.ROTATION_270?270:0;rotation=((sensor==null?0:sensor)+(front?degrees:-degrees)+360)%360;
   StreamConfigurationMap map=c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);Size[] sizes=map==null?null:map.getOutputSizes(ImageFormat.YUV_420_888);if(sizes==null||sizes.length==0)throw new IllegalStateException("Cámara sin formato de análisis compatible");Size chosen=sizes[0];long best=Long.MAX_VALUE;for(Size size:sizes){long area=(long)size.getWidth()*size.getHeight(),penalty=area>640*480?area+10000000:Math.abs(area-640*480);if(penalty<best){best=penalty;chosen=size;}}
   worker=new HandlerThread("FormasCamera");worker.start();handler=new Handler(worker.getLooper());reader=ImageReader.newInstance(chosen.getWidth(),chosen.getHeight(),ImageFormat.YUV_420_888,2);reader.setOnImageAvailableListener(this::imageAvailable,handler);
   manager.openCamera(selected,new CameraDevice.StateCallback(){public void onOpened(CameraDevice camera){synchronized(CameraFeed.this){if(closed){camera.close();return;}device=camera;}configure(camera);}public void onDisconnected(CameraDevice camera){camera.close();fail("Cámara desconectada. La última imagen permanece disponible.");}public void onError(CameraDevice camera,int code){camera.close();fail("No se pudo abrir la cámara ("+code+"). Puedes importar una imagen.");}},handler);
  }catch(Exception e){fail("Cámara: "+e.getMessage());}
 }
 private void configure(CameraDevice camera){try{camera.createCaptureSession(Collections.singletonList(reader.getSurface()),new CameraCaptureSession.StateCallback(){public void onConfigured(CameraCaptureSession capture){synchronized(CameraFeed.this){if(closed){capture.close();return;}session=capture;try{CaptureRequest.Builder request=camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);request.addTarget(reader.getSurface());request.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);capture.setRepeatingRequest(request.build(),null,handler);main.post(()->{if(!closed)listener.ready(front?"Cámara frontal, imagen reflejada":"Cámara trasera");});}catch(Exception e){fail("No se pudo iniciar la captura: "+e.getMessage());}}}public void onConfigureFailed(CameraCaptureSession capture){capture.close();fail("La cámara no admite esta configuración de análisis.");}},handler);}catch(Exception e){fail("Captura: "+e.getMessage());}}
 private void imageAvailable(ImageReader source){Image image=null;try{image=source.acquireLatestImage();if(image==null)return;long now=SystemClock.elapsedRealtime();if(closed||now-lastFrame<900)return;lastFrame=now;Image.Plane plane=image.getPlanes()[0];ByteBuffer y=plane.getBuffer();Rect crop=image.getCropRect();int step=Math.max(1,(int)Math.ceil(Math.max(crop.width(),crop.height())/480.0)),w=Math.max(1,crop.width()/step),h=Math.max(1,crop.height()/step);int[] pixels=new int[w*h];int row=plane.getRowStride(),stride=plane.getPixelStride();for(int j=0;j<h;j++)for(int i=0;i<w;i++){int at=(crop.top+j*step)*row+(crop.left+i*step)*stride;int value=at<y.limit()?(y.get(at)&255):0;pixels[j*w+i]=0xff000000|(value<<16)|(value<<8)|value;}
   Bitmap raw=Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);Matrix matrix=new Matrix();matrix.postRotate(rotation);if(front)matrix.postScale(-1,1);Bitmap oriented=Bitmap.createBitmap(raw,0,0,w,h,matrix,true);if(oriented!=raw)raw.recycle();main.post(()->{if(closed)oriented.recycle();else listener.frame(oriented);});
  }catch(Exception e){if(!closed)fail("No se pudo leer el fotograma: "+e.getMessage());}finally{if(image!=null)image.close();}}
 private void fail(String message){close();main.post(()->listener.error(message));}
 @Override public synchronized void close(){if(closed)return;closed=true;if(session!=null){session.close();session=null;}if(device!=null){device.close();device=null;}if(reader!=null){reader.close();reader=null;}if(worker!=null){worker.quitSafely();worker=null;}}
}
