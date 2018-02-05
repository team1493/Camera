package application;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.media.MediaView;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.videoio.*;
import javafx.scene.control.Toggle;

import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;


public class TextAreaController 
{
	
    @FXML
    // The reference of inputText will be injected by the FXML loader
    
    private TextField textfield1;
    // The reference of outputText will be injected by the FXML loader
    @FXML
    private TextArea textarea1,taMessages;
    @FXML
    private TextField tfColorA,tfColorB,tfThreshA,tfThreshB,targetWidth,targetHeight,contourNum;
    @FXML
    private Button button1,button2,bVideo;
    @FXML
    private Slider filter1a,filter1b,thresh1a,thresh1b;
    // location and resources will be automatically injected by the FXML loader
    @FXML
    private URL location;
    @FXML
    private ImageView imageview1,imageview2,imageview3,imageview4;
    @FXML
    private Label distTarget,aspectRatio;
    @FXML
    private ResourceBundle resources;
    @FXML
    private MediaView mediaview;
  
    public Label labelContour = new Label("Contour Used"); 
    public Label labelContourID = new Label("0"); 
    public Media media1 ;
    public MediaPlayer mediaplayer;

    Image[]  imtemp = new Image[4];
    public Mat m;
    public Mat filter, filter2, filter3, thresh,grey, hierarchy, drawing;
    public Image im_original,im_filter1,im_filter2,im_filter3,im_thresh, im_drawing;
    public VideoCapture capture ;
	public FileChooser fileChooser = new FileChooser();
	public boolean havecontours = false,videoWindow=false, videoWindowPrev=false,videoCapture=false;
    public Window currentWindow;
    public FileHandler filehandler=new FileHandler() ;
	public int numcontours=0,ia=0;
	public double[] contourAspectRatio = new double[5000];
	public double[] contourArea = new double[5000];
	public double[] contourGravityx = new double[5000];
	public double[] contourGravityy = new double[5000];
	public int[] contourCenterx = new int[5000];
	public int[] contourCentery = new int[5000];
	public int[] contourHeight = new int[5000];
	public int[] contourWidth = new int[5000];
	public int camarasource=1;
	static Parameters par= new Parameters();
	private SocketComm socom;
//    Converters conv=new Converters();
//	ShowVideo showvid;
    public  TextAreaController()
    {
    }
    
    @FXML
    private void initialize() 
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME);
        capture = new VideoCapture(0);
        m = new Mat();
        capture.open(par.captureID);
        capture.set(Videoio.CAP_PROP_BRIGHTNESS, 10); 
        currentWindow=Main.getWindow();
    }
     
    @FXML
    private void button1Action()
    {
        readCamera();
        processImage();
        showImage();
    }	
    
    
    @FXML
    private void menuOpenImage()
    {
    	try {
			m=filehandler.openImage(true,taMessages);
		} catch (IOException e) {
			System.out.print("Failed to Open file");
		}
    	if(par.haveImage){
    	processImage();
        showImage(); }   	
    }
    
    @FXML
    private void menuSaveImage()
    { 
    	filehandler.saveImage(m, taMessages);
}
    
    @FXML
    private void menuLoadConfig()
    {
    String[] data = new String[40];
    int i; double f; 
    data = filehandler.openconfig();
	i=Integer.valueOf(data[1]);
	tfColorA.setText(data[1]); filter1a.setValue(i);par.maskcolor1=i;
	i=Integer.valueOf(data[2]);
	tfColorB.setText(data[2]); filter1b.setValue(i);par.maskcolor2=i;
	tfThreshB.setText(data[4]); thresh1b.setValue(i);par.threshhigh=i;

	
	i=Integer.valueOf(data[5]);
	par.bestContour=i; contourNum.setText(String.valueOf(i));
	i=Integer.valueOf(data[6]);
	if (data[6] != null) { par.FocalLength=i;}
	i=Integer.valueOf(data[7]);
	if (data[7] != null)  {par.captureID=i;}
	i=Integer.valueOf(data[8]);
	if (data[8] != null) { par.exposure=i;}
	
	f=Double.valueOf(data[9]);
	par.actualHeight=f; targetHeight.setText(String.valueOf(f));
	f=Double.valueOf(data[10]);
	par.actualWidth=f; targetWidth.setText(String.valueOf(f));
	calculateAR();
	
	if(data[11]!=null) {
		par.imagefile=new File(data[11]); 
    	try {
			m=filehandler.openImage(false,taMessages);
		} catch (IOException e) {
			System.out.println("Failed to open file");
			e.printStackTrace();
		}
    	if(!(m==null)){
    		par.haveImage=true;
        	processImage();
            showImage(); }   	
	}
    } 
    
    @FXML
    private void menuSaveConfig() 
    {
    	filehandler.saveconfig();
    } 
    
    @FXML
    private void contoursAction()
    {	
    	double[][] contourdata = new double[6][5000];
    	if (camarasource==1 && par.haveImage) {
	    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
 		Mat heirarchy = Mat.zeros(m.size(), CvType.CV_8UC3);
		drawing = Mat.zeros(thresh.size(), CvType.CV_8UC3);  //needs to be CV_8UC3!!!
	    Rect rectContour = new Rect(0,0,0,0);
	    Imgproc.findContours(thresh.clone(),contours,heirarchy, Imgproc.RETR_LIST ,Imgproc.CHAIN_APPROX_NONE);
	    numcontours=contours.size();
	    Moments mom;
		int i;
			
//draw contours
//for each contour calculate area, moment, center of gravity, aspect ratio
//should also calculate orientation !!!
    
		  for( i = 0; i< numcontours; i++ )
		     {				  
			  contourArea[i]=Imgproc.contourArea(contours.get(i));
			   mom = Imgproc.moments(contours.get(i), true);
			   contourGravityx[i]= mom.get_m10() / mom.get_m00();
			   contourGravityy[i]= mom.get_m10() / mom.get_m00();
			   rectContour=Imgproc.boundingRect(contours.get(i));
			   contourCenterx[i]=rectContour.x;
			   contourCentery[i]=rectContour.y;
			   contourHeight[i]=rectContour.height;
			   contourWidth[i]=rectContour.width;
			   contourAspectRatio[i]=1.0*rectContour.width/(1.0*rectContour.height);

		      if (i==3){Imgproc.drawContours(drawing, contours, i, new Scalar(100,255,255));}  
		      else if (i==4){Imgproc.drawContours(drawing, contours, i, new Scalar(170,170,255));}  
		      else if (i==7){Imgproc.drawContours(drawing, contours, i, new Scalar(100,255,255));}  
		      else if (i==8){Imgproc.drawContours(drawing, contours, i, new Scalar(170,170,255));}  
		      else {Imgproc.drawContours(drawing, contours, i, new Scalar(255,255,255));}  
		     }
	        im_drawing=(mat2Image(drawing));
//print results
		  i=0;
		  while (i< numcontours){				  
Imgproc.putText(drawing, "c"+i, new Point (contourCenterx[i],contourCentery[i]), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar (255,255,255));

		  System.out.print(i+" "+contourArea[i]+"   "+contourGravityx[i]+"   "+contourGravityy[i]+"   "+contourAspectRatio[i]+"\n");
		  i++;
		  }		
    	}
    else if (camarasource==2 && par.haveImage){
		imtemp = socom.getImagePi(7,1);
		im_drawing= imtemp[0];
		contourdata=socom.getContourData();
		numcontours=(int)contourdata[0][0];
		int i = 0;
		while (i<numcontours) {
			contourAspectRatio[i]=contourdata[0][i+1];
			contourArea[i]=contourdata[1][i+1];
			contourWidth[i]=(int)contourdata[2][i+1];
			contourHeight[i]=(int)contourdata[3][i+1];
			contourCenterx[i]=(int)contourdata[4][i+1];
			contourCentery[i]=(int)contourdata[5][i+1];
			i++;
		}
    }

    		imageview4.setImage(im_drawing);
	        havecontours = true;
			printContourResults();

    }
    
    @FXML    
    protected void pickContour() {
    	int i=0;    	
    	par.bestContour=0;
    	double bestDifference,targetAspectRatio,diff,maxsize=0;
    	targetAspectRatio=Double.parseDouble(aspectRatio.getText());
    	bestDifference=Math.abs(targetAspectRatio-contourAspectRatio[0]);
    	while(i<numcontours){
// Calculate best contour on closest match to aspect ratio    		
    		diff=Math.abs(targetAspectRatio-contourAspectRatio[i]);
/*    		if(diff<bestDifference){
    			bestDifference=diff;
    			par.bestContour=i;
    		}
*/
// Calculate best contour on largest area    		
    		if(maxsize<contourArea[i]) {
    			maxsize=contourArea[i];
    			par.bestContour=i;
    		}
    		i++;
    	}
    	contourNum.setText(String.valueOf(par.bestContour));
    	labelContourID.setText(String.valueOf(par.bestContour));
 		contourNum.setText(String.valueOf(par.bestContour));
    }
    
    @FXML    
    protected void ManualContourSet() {
        contourNum.setOnAction(e -> {
	        try {par.bestContour= Integer.parseInt(contourNum.getText());
	        	labelContourID.setText(String.valueOf(par.bestContour));}
	     	catch(NumberFormatException f) {
	     		par.bestContour=0;
	     		contourNum.setText(String.valueOf(par.bestContour));}
            });
    }
       
    public void readCamera() {
    	videoCapture=false;
    	Mat m1=new Mat();
    	if (camarasource==1 ) {
    		if (capture.isOpened()) {
    			capture.read(m1);
    			m=m1;
    		}
    	}
    	else {
    		imtemp = socom.getImagePi(4,1);
    		im_original=imtemp[0];
    		}
    	par.haveImage=true;
    		}

    
    protected void processImage() {
    	if (m != null & camarasource==1) {
    		try {
    	filter= Mat.zeros(m.size(), CvType.CV_8UC3);
	    filter2= Mat.zeros(m.size(), CvType.CV_8UC3);
	    filter3= Mat.zeros(m.size(), CvType.CV_8UC3);
	    grey= Mat.zeros(m.size(), CvType.CV_8UC3);
	    hierarchy = Mat.zeros(m.size(), CvType.CV_8UC3);
 		thresh= Mat.zeros(m.size(), CvType.CV_8UC3);
// Convert color BGR to HSV format		    
	    Imgproc.cvtColor( m,filter, Imgproc.COLOR_BGR2HSV);
//Create mask by selecting only green color  //30 to 100 for green
	    Core.inRange(filter, new Scalar(par.maskcolor1,0,0),new Scalar(par.maskcolor2,255,255), filter2);
//inRange changes form to 1 channel, convert back to 3 channel
	    Imgproc.cvtColor(filter2,filter2,Imgproc.COLOR_GRAY2BGR,3);
	    Core.bitwise_and(filter, filter2,filter2);
	    Imgproc.cvtColor( filter2,filter3, Imgproc.COLOR_HSV2BGR);
//convert color to greyscale so we can do contours
	    Imgproc.cvtColor( filter3,grey, Imgproc.COLOR_RGB2GRAY);
		 Imgproc.threshold( grey, thresh, par.threshlow, par.threshhigh, Imgproc.THRESH_BINARY);
    	}
    	
    	catch(Exception e) {
    		System.out.println("Error Processing Image");
    	}   		
    	}
    if(camarasource==2) {
    	int[] settings = new int[6];
    	settings[0]=par.maskcolor1;settings[1]=par.maskcolor2;
    	settings[2]=par.threshlow;settings[3]=par.threshhigh;
    	settings[4]=par.threshlow;settings[5]=par.threshhigh;
    	socom.sendSettings(settings);
		imtemp = socom.getImagePi(6,2);
		im_filter3=imtemp[0];
		im_thresh=imtemp[1];
    }
    }
    
    public  void showImage() {
    	if(par.haveImage) {
    		if (camarasource==1) {
    			im_original = mat2Image(m);
    			im_filter3 = mat2Image(filter3);
    			im_thresh = mat2Image(thresh);
    			if(havecontours) im_drawing = mat2Image(drawing);
    			}
    		try {	
    				imageview1.setImage(im_original);
    				imageview2.setImage(im_filter3);    				
    				imageview3.setImage(im_thresh);
    				if(havecontours) imageview4.setImage(im_drawing);
    			}
    			
    		
    		catch(Exception e) {
    			System.out.println("No Image Available");
    		}
    		
    }
    }
    
    public static Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
    
    @FXML    
    protected void changeFilter() {
    	par.maskcolor1= (int)filter1a.getValue();
        tfColorA.setText(Integer.toString(par.maskcolor1));	
    	par.maskcolor2=(int) filter1b.getValue();
    	tfColorB.setText(Integer.toString(par.maskcolor2));	
    	par.threshlow=(int) thresh1a.getValue();
    	tfThreshA.setText(Integer.toString(par.threshlow));	
    	par.threshhigh=(int) thresh1b.getValue();
    	tfThreshB.setText(Integer.toString(par.threshhigh));	        
        processImage();
        showImage();
      }
      
    @FXML    
 // Update sliders if textfield input changes, then apply filters   
    protected void setSliders1() {
    	int i;
        try {i = Integer.parseInt(tfColorA.getText());}
     	catch(NumberFormatException e) {i=0;}
         filter1a.setValue(i);
        
        try {i = Integer.parseInt(tfColorB.getText());}
    	catch(NumberFormatException e) {i=255;}
        filter1b.setValue(i);

        try {i = Integer.parseInt(tfThreshA.getText());}
    	catch(NumberFormatException e) {i=0;}
        thresh1a.setValue(i);
        
        try {i = Integer.parseInt(tfThreshB.getText());}
    	catch(NumberFormatException e) {i=255;}
        thresh1b.setValue(i);

        changeFilter();     
      }
    
// update textfields if slider input changes, then apply filters    
    @FXML    
    protected void setTextFields1() {
    tfColorA.setText(Double.toString(filter1a.getValue()));	
    tfColorB.setText(Double.toString(filter1b.getValue()));
    tfThreshA.setText(Double.toString(thresh1a.getValue()));	
    tfThreshB.setText(Double.toString(thresh1b.getValue()));	
      changeFilter();     
    }
      
    public void zoomContour() {
    	String title = "Contour";
    	Image image=imageview4.getImage();
    	zoom(title,image);		
      }

    public void zoomOriginal() {
    	String title = "Original";
    	Image image=imageview1.getImage();
    	zoom(title,image);		
      }

    public void zoom(String title, Image image) {
    	ImageView imvZoom = new ImageView();
    	try{	
                  final Stage Zoom = new Stage();
                  Zoom.initModality(Modality.NONE);
                  Zoom.initOwner(null);
                  StackPane pane = new StackPane();
                  Zoom.setTitle(title);
                  imvZoom.setImage(image);
                  imvZoom.setPreserveRatio(true);
                  imvZoom.setFitHeight(500);
                  pane.getChildren().add(imvZoom);
                  Scene dialogScene = new Scene(pane, 800, 600);
                  Zoom.setScene(dialogScene);
                  Zoom.show();
              }
		
	 catch(Exception e) {
		e.printStackTrace();
	} 
      }
    
// Method to print results of contour calculations 
    public void printContourResults(){
    	TextArea textarea = new TextArea();
    	int i; String string;
    	try{	
   				textarea.appendText("N       Area            Aspet Ratio    Width     Height          X         Y" +"\n");
   				System.out.println("Printing results");
   				for ( i=0;i<numcontours;i++){
    				string=String.format("%d     %8.2f     %8.3f            %d            %d            %d            %d %n",
    						   i,contourArea[i], contourAspectRatio[i],contourWidth[i],contourHeight[i],contourCenterx[i],contourCentery[i]);
    				textarea.appendText(string);
    			}
     			
                  final Stage textStage = new Stage();
                  textStage.initModality(Modality.NONE);
                  textStage.initOwner(null);
                  StackPane stackpane = new StackPane();
                  textStage.setTitle("Contour Data");

                  stackpane.getChildren().add(textarea);
                  Scene scene = new Scene(stackpane, 400, 400);
                  textStage.setScene(scene);
                  textStage.show();
              }
		
	 catch(Exception e) {
		e.printStackTrace();
	} 
    	
      }
    
    
// Method to set camera ID number, Focal Length and Exposure
    @FXML    
    protected void cameraSettings() {
    	final Stage stage1 = new Stage();
    	boolean sourcebuttonInit=true;
        stage1.initModality(Modality.NONE);
        stage1.initOwner(null);
        if (camarasource==2)sourcebuttonInit=false;
        ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton("Computer");
        rb1.setToggleGroup(group);
        rb1.setSelected(sourcebuttonInit);
        RadioButton rb2 = new RadioButton("RaspberryPi");
        rb2.setToggleGroup(group);
        rb2.setSelected(!sourcebuttonInit);
        rb1.setUserData(1);
        rb2.setUserData(2);
        Label labelSource = new Label("Source");
        Label labelWait = new Label("status");
        Label labelID = new Label("Camera ID");
        Label labelExposure = new Label("Camera Exposure");
        Label labelFocal = new Label("Focal Length");
        Label labelFailed = new Label();
        TextField tfSource= new TextField();
        
        TextField tfID = new TextField();
        TextField tfExposure = new TextField();
        TextField tfFocal= new TextField();
        tfID.setText(String.valueOf(par.captureID));
        tfExposure.setText(String.valueOf(par.exposure));
        tfFocal.setText(String.valueOf(par.FocalLength));
        
        Button button = new Button("Close");
//        labelFailed.setText("Camera ID set to "+par.captureID);
        VBox pane = new VBox();
        stage1.setTitle("Camera Settings");
        pane.getChildren().addAll(labelID,tfID,labelExposure,tfExposure,labelFocal,tfFocal,labelFailed);
        pane.getChildren().addAll(labelSource,rb1,rb2,labelWait,button);

        Scene scene1 = new Scene(pane, 500, 300);
        stage1.setScene(scene1);
        stage1.show();
        
// use the radio buttons to set the camara source        
        group.selectedToggleProperty().addListener(
        		(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
        	        if (group.getSelectedToggle() != null) {
        	            labelWait.setText("Wait while connecting");
        	            System.out.println(group.getSelectedToggle().getUserData().toString());
        	            camarasource=Integer.parseInt(group.getSelectedToggle().getUserData().toString());
        	           if (camarasource==2) { 
           	            	labelWait.setText("Wait while connecting");
        	           		if(socom==null) socom=new SocketComm();
        	           		labelWait.setText("Source is RaspberryPi");}
        	           	else labelWait.setText("Source is computer");

        	        }});
        
        tfID.setOnAction(e -> {
        	int id;
	        try {id= Integer.parseInt(tfID.getText());}
	     	catch(NumberFormatException f) {id=0;}
        	par.captureID=id;
        	capture.release();
        	capture.open(par.captureID);
        	if (capture.isOpened())
        		{ labelFailed.setText("Camera ID set to "+par.captureID);}
        	else
        		{labelFailed.setText("Camera failed to open");}	
            });
        
        tfExposure.setOnAction(e -> {
    		{ 
    	        try {par.exposure = Integer.parseInt(tfExposure.getText());}
    	     	catch(NumberFormatException f) {par.exposure=0;}
    	        try {
    	        	System.out.println(par.exposure);
    	        	socom.setExposure(par.exposure);
	    	        labelFailed.setText("Camera Exposure set to "+par.exposure);
    	        }
    	        catch (Exception e1) {
					labelFailed.setText("Set Exposure Failed");
					e1.printStackTrace();
				}
    		}});
        
        tfFocal.setOnAction(e -> {
    		if (capture.isOpened()) { 
    	        try {
    	        	par.FocalLength = Integer.parseInt(tfFocal.getText());
    	        	labelFailed.setText("Focal Length Set to "+par.FocalLength);}
    	     	catch(NumberFormatException f) {par.FocalLength=1;}
    			}
		else
			{labelFailed.setText("Camera feed is not open");}
            });        
        
        button.setOnAction(value ->  {
        	stage1.close();
         });
    }
    

    
    @FXML    
    protected void calculateDistance() {
    double d1,d2,apparantWidth,apparantHeight;
    par.actualWidth=Double.parseDouble(targetWidth.getText());
    par.actualHeight=Double.parseDouble(targetHeight.getText());
    apparantWidth=contourWidth[par.bestContour];
    apparantHeight=contourHeight[par.bestContour];
	d1=par.actualWidth*par.FocalLength/apparantWidth;
    d2=par.actualHeight*par.FocalLength/apparantHeight;
    distTarget.setText(String.valueOf(d1));
    }
    
    
    
    @FXML    
    protected void calculateAR() {
        double ar;
		try {par.actualWidth= Double.parseDouble(targetWidth.getText());
		}
     	catch(NumberFormatException e) {par.actualWidth=1;}

		try {par.actualHeight= Double.parseDouble(targetHeight.getText());
		}
     	catch(NumberFormatException e) {par.actualHeight=1;}
		
		if(par.actualHeight !=0) ar=par.actualHeight/par.actualWidth;
		else ar=1;
		aspectRatio.setText(String.valueOf(ar) );
    }
    
    
    
// Method to calibrate focal length
    @FXML    
    protected void calibrateFocalLength() {
    	double[] d = new double[100];
    	double[] hrec = new double[100];
    	double[] h = new double[100];
    	double[] w = new double[100];
    	double[] a = new double[100];
    	String[] entry = new String[100];
    	final Stage stage2 = new Stage();
        stage2.initModality(Modality.NONE);
        stage2.initOwner(null);
        ia=0;
        Button button = new Button("Calculate");
        Button button2 = new Button("Remove Data Point");
        Button button3 = new Button("Save Data");
        Button button4 = new Button("Load Data");
        Label labelFocalLength= new Label("Focal Length = ");
        Label labelFocalLengthValue= new Label("");
        Label labelEntDist = new Label("Enter Distance");
        TextField tfDistance = new TextField();
        TextField tfRemove = new TextField();
        TextArea data = new TextArea();

        labelFocalLength.setTranslateX(20);
        labelFocalLengthValue.setTranslateX(20);
        stage2.setTitle("Calibrate Camera Focal Length");
        VBox vbox = new VBox();
        HBox hbox1 = new HBox();
        HBox hbox2 = new HBox();
        HBox hbox3 = new HBox();
        hbox1.setSpacing(10);
        hbox1.setPadding(new Insets(10, 0, 0, 10));
        hbox2.setPadding(new Insets(10, 0, 0, 10));
        hbox1.getChildren().addAll(labelEntDist,tfDistance,labelContour,labelContourID);
        hbox2.getChildren().addAll(button,labelFocalLength,labelFocalLengthValue);
        hbox3.getChildren().addAll(button2 , tfRemove,button3,button4);
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(hbox1,hbox2,hbox3,data);
        Scene scene2 = new Scene(vbox, 500, 300);
        stage2.setScene(scene2);
        stage2.show();       

        tfDistance.setOnAction(e -> {
        	try {
    	        d[ia] = Double.parseDouble(tfDistance.getText());
    	        hrec[ia]=1.0/((1.0)*contourHeight[par.bestContour]);
    	        h[ia]=contourHeight[par.bestContour];
    	        w[ia]=contourWidth[par.bestContour];
    	        a[ia]=contourArea[par.bestContour];
    	        entry[ia]=String.valueOf(ia+"\t"+d[ia])+"\t"+h[ia]+"\t"+w[ia]+"\t"+a[ia]+"\n";
    	        data.appendText(entry[ia]);    	        
    	        tfDistance.setText("");
    	        ia++;
    	        }
    	     catch(NumberFormatException f) {}
            });        
        
        button.setOnAction(value ->  {
        	LinearRegression linearRegression = new LinearRegression(ia,hrec,d);
        	System.out.println("ia = "+ia);
        	System.out.println(linearRegression.toString());
        	System.out.println(linearRegression.R2());
        	double focalLengthValue=linearRegression.slope()/Double.parseDouble(targetHeight.getText());
        	labelFocalLengthValue.setText(String.valueOf(focalLengthValue)+"  R^2 = "+linearRegression.R2());
         });
// Remove a data point
        button2.setOnAction(value ->  {
        	int iremove=999;
        	try {
        		iremove=Integer.parseInt(tfRemove.getText());
            	int k = 0,m=0;
            	data.setText("");
            	while(k<ia) {
            		if(k!=iremove) {                    	
            	        d[m] = d[k];
            	        hrec[m]=hrec[k];
            	        h[m]=h[k];
            	        w[m]=w[k];
            	        a[m]=a[k];
            	        entry[m]=String.valueOf(m+"\t"+d[k])+"\t"+h[k]+"\t"+w[k]+"\t"+a[k]+"\n";
            	        data.appendText(entry[m]);
            	        m++;
            		}
        	        k++;            		
            	}
            	tfRemove.setText("");
            	ia=m;
    	        }
    	     catch(NumberFormatException f) {}
         });
    
        button3.setOnAction(value ->  {
        	filehandler.saveCalibrationData(entry, currentWindow);	
         });
        
        button4.setOnAction(value ->  {
        	double[][] datatemp = new double[5][];
        	datatemp=filehandler.OpenCalibrationData(currentWindow);
        	ia=(int)datatemp[5][0];
        	int k=0;
        	while(k<ia) {
        	d[k]=datatemp[0][k];
           	h[k]=datatemp[1][k];
           	w[k]=datatemp[2][k];
           	a[k]=datatemp[3][k];
           	hrec[k]=datatemp[4][k];
        	System.out.println(d[k]);
        	System.out.println(h[k]);

        	k++;
        	}
        	
         });
        
        
    }   


    @FXML
    private void toggleVideo()
    {videoCapture = !videoCapture;
    if (videoCapture)
    	if(!capture.isOpened()) capture.open(par.captureID);
    	showvid();
    }
    
    private void showvid()
    {    

    	Thread t = new Thread() {
        @Override 
        
        public void run() {        	        	
	    	int j=0;
	        while(videoCapture) {
	        		try {
						Thread.sleep(20);
					} catch (InterruptedException e1) {
						System.out.println("Error on sleep");
						e1.printStackTrace();
					}
	            	if(camarasource==2 ) {
	            		if(socom == null )socom = new SocketComm();
	    			try {
	    				Image image = SwingFXUtils.toFXImage(ImageIO.read(SocketComm.url), null);
	    	            imageview1.setImage(image);} 
	    			 catch (IOException e) {
	    				System.out.println("failed to get image from mjpg-streamer");
	    				e.printStackTrace();
	    			 }	            	
	            	}
	            	else {if(capture==null)System.out.println("cap null");
	            	if(m==null)System.out.println("m null");
	            		capture.read(m);
	            		imageview1.setImage(mat2Image(m));
	            	}
	           } 
			}        
    	};
        t.start();
    	}

    
    
    @FXML    
    protected void quitprogram() {
//    	capture.release();
    	videoWindow=false;
//    	socom.closeConnections();
    	Platform.exit();
      }

	public Mat getM() {
		return m;
	}

	public void setM(Mat m2) {
		m = m2;
	}   
    

}