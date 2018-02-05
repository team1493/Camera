package application;

import java.io.File;
import org.opencv.core.Mat;

import javafx.stage.Window;

public class Parameters {
public int     maskcolor1=0,maskcolor2=255,threshlow=0,threshhigh=255;
public int FocalLength=1,bestContour=0, captureID=0,exposure=100;
public double actualHeight=1.0, actualWidth=1.0;
public File imagefile,configfile;
public Window currentWindow;
public boolean haveImage=false,videoOn=false;
//public Mat m2  = new Mat();


	public Parameters() {
		currentWindow=Main.getWindow();
	}
}


//    	dataI[0]=maskcolor1;dataI[1]=maskcolor2;dataI[2]=threshlow;dataI[3]=threshhigh;
//dataI[4]=bestContour;dataI[5]=FocalLength;dataI[6]=captureID;dataI[7]=exposure;
//dataD[0]=actualHeight; dataD[1]=actualWidth;
//dataS[0]=String.valueOf(imagefile);
