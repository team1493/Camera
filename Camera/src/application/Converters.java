package application;

import java.awt.image.BufferedImage;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Converters {

Converters(){
	
}

public static Mat img2Mat(BufferedImage in)
{
      Mat out;
      byte[] data;
      int r, g, b;

      if(in.getType() == BufferedImage.TYPE_INT_RGB)
      {
          out = new Mat(240, 320, CvType.CV_8UC3);
          data = new byte[320 * 240 * (int)out.elemSize()];
          int[] dataBuff = in.getRGB(0, 0, 320, 240, null, 0, 320);
          for(int i = 0; i < dataBuff.length; i++)
          {
              data[i*3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
              data[i*3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
              data[i*3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
          }
      }
      else
      {
          out = new Mat(240, 320, CvType.CV_8UC1);
          data = new byte[320 * 240 * (int)out.elemSize()];
          int[] dataBuff = in.getRGB(0, 0, 320, 240, null, 0, 320);
          for(int i = 0; i < dataBuff.length; i++)
          {
            r = (byte) ((dataBuff[i] >> 16) & 0xFF);
            g = (byte) ((dataBuff[i] >> 8) & 0xFF);
            b = (byte) ((dataBuff[i] >> 0) & 0xFF);
            data[i] = (byte)((0.21 * r) + (0.71 * g) + (0.07 * b)); //luminosity
          }
       }
       out.put(0, 0, data);
       return out;
 } 


	
}
