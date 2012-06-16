import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;


public class Serwer extends JFrame {
	
	class ScreenShoter extends TimerTask {
		
		private Robot robot;
		private Rectangle screenRect;
		
		public ScreenShoter() {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
			screenRect = new Rectangle(Toolkit.getDefaultToolkit()
					.getScreenSize());
		}

		@Override
		public void run() {
			try {
				File screen = captureScreenShot();
				File outputFile = new File("crscreen.jpg");
				
				
				ImageReader imgRdr = ImageIO.getImageReadersByFormatName("jpg").next();
				ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
				ImageInputStream imgInStrm = ImageIO.createImageInputStream(screen);
				ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(outputFile);
				imgRdr.setInput(imgInStrm);
				imgWrtr.setOutput(imgOutStrm);
				IIOImage iioImg = new IIOImage(imgRdr.read(0), null, imgRdr.getImageMetadata(0)); 

				// set compression level 
				ImageWriteParam jpgWrtPrm = imgWrtr.getDefaultWriteParam();
				jpgWrtPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
				jpgWrtPrm.setCompressionQuality(0.1f);

				// write out JPEG
				imgWrtr.write(null, iioImg, jpgWrtPrm);

				// clean up 
				imgInStrm.close();
				imgOutStrm.close();

				imgWrtr.dispose();
				imgRdr.dispose();
				
				
				
				
				
				int fileLength = (int)screen.length();
				byte [] mybytearray  = new byte [fileLength];
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(screen));
				DataOutputStream dos = new DataOutputStream(Serwer.this.os); 
				bis.read(mybytearray,0,mybytearray.length);
				dos.writeInt(fileLength);
				Serwer.this.os.write(mybytearray,0,mybytearray.length);
			    os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private File captureScreenShot() {
			BufferedImage capture;
			File f = null; 
			try {
				capture = robot.createScreenCapture(screenRect);
				f = new File("screen.jpg");
				ImageIO.write(capture, "jpg", f);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return f;
		}
		
	}
	
	private ServerSocket ss;
	//private ObjectInputStream is;
	private OutputStream os;
	
	public Serwer() throws Exception {
		ss = new ServerSocket(1235);
		Socket socket = ss.accept();
		//is = new ObjectInputStream(socket.getInputStream());
		os = socket.getOutputStream();
		
		Timer t = new Timer();
		t.schedule(new ScreenShoter(), 1000, 1000);
	}
	
	public static void main(String... args) throws Exception {
		new Serwer();
	}

}
