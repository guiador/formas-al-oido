package org.formasaloido;

import android.content.ContentResolver;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.*;

public final class ImageImport {
 public static Bitmap decode(ContentResolver resolver,Uri uri)throws IOException{
  BitmapFactory.Options info=new BitmapFactory.Options();info.inJustDecodeBounds=true;try(InputStream in=resolver.openInputStream(uri)){if(in==null)throw new IOException("No se pudo abrir la imagen");BitmapFactory.decodeStream(in,null,info);}if(info.outWidth<2||info.outHeight<2)throw new IOException("El archivo no es una imagen compatible");if((long)info.outWidth*info.outHeight>120000000L)throw new IOException("Imagen demasiado grande; elige una copia reducida");
  BitmapFactory.Options opts=new BitmapFactory.Options();opts.inPreferredConfig=Bitmap.Config.ARGB_8888;opts.inSampleSize=1;while(Math.max(info.outWidth,info.outHeight)/opts.inSampleSize>768)opts.inSampleSize*=2;Bitmap raw;try(InputStream in=resolver.openInputStream(uri)){raw=BitmapFactory.decodeStream(in,null,opts);}if(raw==null)throw new IOException("No se pudo decodificar la imagen");int orientation=ExifInterface.ORIENTATION_NORMAL;try(InputStream in=resolver.openInputStream(uri)){if(in!=null)orientation=new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);}catch(Exception ignored){}
  Matrix m=new Matrix();switch(orientation){case 2:m.setScale(-1,1);break;case 3:m.setRotate(180);break;case 4:m.setScale(1,-1);break;case 5:m.setRotate(90);m.postScale(-1,1);break;case 6:m.setRotate(90);break;case 7:m.setRotate(270);m.postScale(-1,1);break;case 8:m.setRotate(270);break;}
  Bitmap rotated=Bitmap.createBitmap(raw,0,0,raw.getWidth(),raw.getHeight(),m,true);if(rotated!=raw)raw.recycle();Bitmap opaque=Bitmap.createBitmap(rotated.getWidth(),rotated.getHeight(),Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(opaque);canvas.drawColor(Color.WHITE);canvas.drawBitmap(rotated,0,0,null);rotated.recycle();return opaque;
 }
}
