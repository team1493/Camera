package application;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SocketComm {

	public static Image im;	
	public static 	Socket s=null;
	public static 	InputStream in=null;
	public static 	OutputStream os=null;
	public static 	BufferedReader bin=null;
	public static 	ByteArrayOutputStream baos=null;
	public static 	DataOutputStream dout=null;
	public static 	DataInputStream din=null;
	public static 	PrintWriter pout = null;
	public static 	ByteArrayInputStream bais=null;
	public static 	byte[] data = new byte[4096];
	public static 	String rpiaddress; 
	public static 	String mjpgadd = new String();
	public static 	URL url;
	SocketComm(){
		try {
			System.out.println("Flag A");
			InetAddress rpi=InetAddress.getByName("raspberrypi.local");
			rpiaddress= rpi.toString().split("/")[1];
			System.out.println(rpiaddress);
		} catch (UnknownHostException e) {
			System.out.println("Could not find raspberrypi.local");
		}
		mjpgadd="http://"+rpiaddress +":8080/?action=snapshot";
		
	   	 
			try {

				url = new URL(mjpgadd);
				System.out.println(url);

			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
	}	

	public void opensocket() {
		try {
			s = new Socket(rpiaddress,8000);
//			s.setReceiveBufferSize(921600);
			in = s.getInputStream();
			os = s.getOutputStream();
			bin = new BufferedReader(new InputStreamReader(in));
			baos = new ByteArrayOutputStream();
			din = new DataInputStream(in);
			dout = new DataOutputStream(os);
			pout = new PrintWriter(os);
		}
		catch (IOException e) {
			System.out.println("Error Opening Socket");
		}
//	    System.out.println("Socket Opened");	
	}
	
	public void closesocket() {
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void closeConnections() {
		byte[] instruction = new byte[1];
		try {
			opensocket();
			instruction[0]=10;
			os.write(instruction, 0, 1);
		} 
		catch (IOException e1) {
		    System.out.println("failed to send command");	
		}
		closesocket();

	}
	
	
	
	public void getTarget() {
		byte[] instruction = new byte[1];
		try {
			instruction[0]=3;
			os.write(instruction, 0, 1);
			String dists=bin.readLine();
			String angles=bin.readLine();
			String timestamp=bin.readLine();
		    System.out.println("dist = "+dists+"  angle = "+angles);
		    System.out.println("timestamp = "+timestamp);
		} catch (IOException e1) {
		    System.out.println("failed to send command");	
		}

	}
	
	public void setExposure(int exposure) {
		byte[] instruction = new byte[1];
		try {
			opensocket();
			instruction[0]=8;
			os.write(instruction, 0, 1);
			sendint(exposure);
		} catch (IOException e1) {
			System.out.println(e1);
		    System.out.println("failed to send command");	
		}		closesocket();
	}
	
	
	public void sendSettings(int[] settings) {
		opensocket();
		byte[] instruction = new byte[1];
		byte[] params = new byte[12];
		try {
			instruction[0]=5;
			os.write(instruction);
			int i = 0;
			while(i<6) {
				sendint(settings[i]);
				i++;
			}
		}	
		catch (IOException e1) {
		System.out.println("failed to send command");	
		}
		closesocket();
	}
	
	
	public Image[] getImagePi(int k,int numimages) {
		byte[] instruction = new byte[1];
		byte[] params = new byte[12];
		int numbytes=0,i=0,z=0;
		Image[] imarray = new Image[4];
		String msgrec = new String();
// Send a message to the rPi. rPi returns the image and processed images in an Image array
		byte kb=(byte)k;
		opensocket();
		try {instruction[0]=kb;
			os.write(instruction);
		}	
		catch (IOException e1) {
	    System.out.println("failed to send command");	
		}
		
		z=0;
		while (z<numimages) {
		try {
			msgrec = bin.readLine();
			System.out.println("Lenth of msgrec = "+msgrec.length());
//			System.out.println(msgrec);
//			System.out.println(msgrec.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String tmp;
		int itmp;
		i=0;
		while (i<msgrec.length()/4) {
			tmp=msgrec.substring(i*4+2, 4*i+4);
//			System.out.println("tmp = "+tmp);
			itmp=Integer.parseInt(tmp,16);
			byte btmp = (byte) itmp;
			baos.write(btmp);
			i++;}

		bais = new ByteArrayInputStream( baos.toByteArray() );
		System.out.println("z = " + z);
		imarray[z] = new Image(bais);
    	try {instruction[0]=4;
		os.write(instruction);}
    	catch (IOException e) {
			e.printStackTrace();
		}
		baos.reset();
		bais.reset();

		z++;
		}		
		
		try {
			s.shutdownInput();		
		} catch (IOException e) {
			System.out.println("Failed to close socket");
		}
    
//    	 TextAreaController.par.m2=Imgcodecs.imdecode(new MatOfByte(baos.toByteArray()),
//          		 Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);    	
		try {
			s.close();
//			System.out.println("Socket Closed");
		} catch (IOException e) {
			System.out.println("Failed to Close Socket");
		}			
		return (imarray);
	}


	public double[][] getContourData() {
		double[][] contourdata = new double[6][5000];
		byte[] instruction = new byte[1];
		instruction[0]=8;
		int i = 0;
		opensocket();
		try {
			String numcontours, aspectratio,area,width,height,centerx,centery;
			os.write(instruction, 0, 1);
			numcontours=bin.readLine();		
			System.out.println("numcontours = "+numcontours);
			int ic = Integer.valueOf(numcontours);
			contourdata[0][0]=ic;
			while (i<ic) {
				aspectratio=bin.readLine();
				area=bin.readLine();
				width=bin.readLine();
				height=bin.readLine();
				centerx=bin.readLine();
				centery=bin.readLine();
				contourdata[0][i+1]=Double.parseDouble(aspectratio);
				contourdata[1][i+1]=Double.parseDouble(area);
				contourdata[2][i+1]=Double.parseDouble(width);
				contourdata[3][i+1]=Double.parseDouble(height);
				contourdata[4][i+1]=Double.parseDouble(centerx);
				contourdata[5][i+1]=Double.parseDouble(centery);
				System.out.println("i = "+i+"   AR = "+contourdata[0][i+1]+
						"   area = "+contourdata[1][i+1]);
				i++;

			}
		} 
		catch (IOException e1) {
			System.out.println("failed to send command");	
		}
		closesocket();
		return (contourdata);		
	}
	
	
	
	private void sendint(int i) throws IOException {
		int length;
		String msg=new String((String.valueOf(i)).getBytes("UTF-8"));
		length=msg.length();
		String msglength=new String((String.valueOf(length)).getBytes("UTF-8"));
		dout.writeBytes(msglength);		
		dout.writeBytes(msg);
		System.out.println("length = "+length);
		System.out.println("Message = "+msg);
		return;
	}

	
	

}
