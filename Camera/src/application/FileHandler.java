package application;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;


public class FileHandler {
	 FileChooser fileChooser = new FileChooser();
	 Mat m;

	 
	public FileHandler(){

	}
	
	public void chooseImagefile(String title, Window currentWindow,boolean openfile) {

    	File filename=null,filenametemp=null;
    	
    	if (openfile) {
        	fileChooser.setTitle(title);    	
    		filename = fileChooser.showOpenDialog(currentWindow);

        	}
    	else {    		
    		filenametemp = fileChooser.showSaveDialog(currentWindow);
    		String s = filenametemp.toString();
    		int len = s.length();
    		int loc = s.indexOf(".");
    		if (loc==-1) s=s+".png";
    		filename= new File(s);
    		
    		}
    	TextAreaController.par.imagefile=filename;
	}
	
	public void chooseConfigfile(String title, Window currentWindow) {
    	fileChooser.setTitle(title);    	
    	File filename = fileChooser.showOpenDialog(currentWindow);
		TextAreaController.par.configfile=filename;

	}
	
	public Mat openImage(boolean getinput, TextArea ta) throws IOException{
		String s = new String("Unable to open image");
		m=Imgcodecs.imread("C:\\Users\\Teram 1493\\image_rumble1.jpeg");
		TextAreaController.par.haveImage=false;
		if (getinput) chooseImagefile("Load Image",TextAreaController.par.currentWindow,true);
		if(TextAreaController.par.imagefile!= null) {
			if (TextAreaController.par.imagefile.exists())	{

				String mimetype = Files.probeContentType(TextAreaController.par.imagefile.toPath());
				if (mimetype != null && mimetype.split("/")[0].equals("image")) {
				    s="image type:  "+ mimetype;
					TextAreaController.par.haveImage=true;
					m=Imgcodecs.imread(TextAreaController.par.imagefile.toString());
				}
			}
		}
    	ta.appendText(s+"\n"); 
		return(m);
	}
	
	public void saveImage(Mat m, TextArea ta){
		String s = new String();
		s = "file saved";
		try {		chooseImagefile("Save Image",TextAreaController.par.currentWindow,false);}
		catch(Exception e) {s = "file not valid";}
    	if (!(TextAreaController.par.imagefile==null) )
    		try {
    		Imgcodecs.imwrite(TextAreaController.par.imagefile.toString(), m);}
    	catch(Exception e) {s="File not saved";}
    	ta.appendText(s+"\n"); 

	}
	
	
	
	public String[] openconfig(){
    	chooseConfigfile("Load Configuration", TextAreaController.par.currentWindow);
    	File fileName = TextAreaController.par.configfile;
    	String[] data = new String[40];
    	String readline = " ";
    	int i = 0;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(fileName));
				while(readline != null) {
					readline=reader.readLine();
					data[i]=readline;
					System.out.println(readline);
					i++;
				}
				reader.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
			return (data);

	}
    	
	
	
	public void saveconfig(){

    	String[] dataS = new String[50];
    	Integer[] dataI = new Integer[50];
    	Double[] dataD = new Double[50];
    	dataI[0]=TextAreaController.par.maskcolor1;  dataI[1]=TextAreaController.par.maskcolor2;
    	dataI[2]=TextAreaController.par.threshlow;dataI[3]=TextAreaController.par.threshhigh;
    	dataI[4]=TextAreaController.par.bestContour;dataI[5]=TextAreaController.par.FocalLength;
    	dataI[6]=TextAreaController.par.captureID;dataI[7]=TextAreaController.par.exposure;
    	dataD[0]=TextAreaController.par.actualHeight; dataD[1]=TextAreaController.par.actualWidth;
    	dataS[0]=String.valueOf(TextAreaController.par.imagefile);
    	chooseConfigfile("Save Configuration", TextAreaController.par.currentWindow);
    	File fileName = TextAreaController.par.configfile;
		
		
		BufferedWriter writer;
    	if (!(fileName==null) ){
			try {
			int i = 0;
			writer = new BufferedWriter(new FileWriter(fileName));
		    writer.write("Configuration File");
			while(dataI[i]!=null){
    		    writer.newLine();
    		    writer.append(String.valueOf(dataI[i]));
    		    i++;
			}
			i=0;
			while(dataD[i]!=null){
    		    writer.newLine();
    		    writer.append(String.valueOf(dataD[i]));
    		    i++;
			}
			i=0;
			while(dataS[i]!=null){
    		    writer.newLine();
    		    writer.append(dataS[i]);
    		    i++;
			}
			
		    writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
	}

	public void saveCalibrationData(String[] entry, Window currentWindow) {
    	fileChooser.setTitle("Save Calibration Data");    	
    	File filename = fileChooser.showOpenDialog(currentWindow);
		try {
			int i=0;
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			while(entry[i]!=null) {
				writer.append(entry[i]);
				writer.newLine();
				i++;
			}
			writer.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public double[][] OpenCalibrationData(Window currentWindow) {
		double[][] data = new double[6][];
			int i = 0;
	    	fileChooser.setTitle("Save Calibration Data");    	
	    	File filename = fileChooser.showOpenDialog(currentWindow);
			try {
				double[] d = new double[100];
				double[] h = new double[100];
				double[] w = new double[100];
				double[] a = new double[100];
				double[] hrec = new double[100];
				double[] ilen= new double[100];
				String[] entry = new String[100];
				BufferedReader reader = new BufferedReader(new FileReader(filename));
				while((entry[i]=reader.readLine()) !=null) {
					String datavalue[] = entry[i].split("\t");
 			        d[i] = Double.parseDouble(datavalue[1]);
 			        h[i] = Double.parseDouble(datavalue[2]);
 			        w[i] = Double.parseDouble(datavalue[3]);
 			        a[i] = Double.parseDouble(datavalue[4]);
 			        hrec[i] = 1/h[i];
					reader.readLine();
					i++;
				}
				ilen[0]=i;
				reader.close();
				data[0]=d;
				data[1]=h;
				data[2]=w;
				data[3]=a;
				data[4]=hrec;
				data[5]=ilen;
			} catch (IOException e) {

				e.printStackTrace();
			}
			return data;
	}
		
		
	
	
}  