import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
	
	/**
	 * @author Wojtek
	 * Klasa odpowiedzialna z robienie zrzutów ekranu i przesyłanie ich
	 * strumieniem.
	 */
	class ScreenShoter extends TimerTask {
		
		private Rectangle screenRect;
		
		public ScreenShoter() {
			screenRect = new Rectangle(Toolkit.getDefaultToolkit()
					.getScreenSize());
		}

		@Override
		public void run() {
			try {
				File screen = captureScreenShot();
				File outputFile = new File("crscreen.jpg");
				
				
				int fileLength = (int)outputFile.length();
				byte [] mybytearray  = new byte [fileLength];
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(screen));
				bis.read(mybytearray,0,mybytearray.length);
				
				DataOutputStream dos = new DataOutputStream(Serwer.this.os);
				dos.writeInt(fileLength);
				Serwer.this.os.write(mybytearray,0,mybytearray.length);
				
			    os.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private File captureScreenShot() {
			BufferedImage capture;
			File f = new File("screen.jpg");; 
			File compressed = new File("compressed.jpg");
			try {
				capture = robot.createScreenCapture(screenRect);
				ImageIO.write(capture, "jpg", f);
				
				
				ImageReader imgRdr = ImageIO.getImageReadersByFormatName("jpg").next();
				ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
				ImageInputStream imgInStrm = ImageIO.createImageInputStream(f);
				ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(compressed);
				imgRdr.setInput(imgInStrm);
				imgWrtr.setOutput(imgOutStrm);
				IIOImage iioImg = new IIOImage(imgRdr.read(0), null, null); 

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
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return compressed;
		}
		
	}
	
	private ServerSocket ss;
	private ObjectInputStream is;
	private OutputStream os;
	private Robot robot;
	
	public Serwer() throws Exception {
		ss = new ServerSocket(1235);
		Socket socket = ss.accept();
		is = new ObjectInputStream(socket.getInputStream());
		os = socket.getOutputStream();
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		Timer t = new Timer();
		t.schedule(new ScreenShoter(), 1000, 1000);
		
		Point p = null;
		while ((p = (Point) is.readObject()) != null) {
			robot.mouseMove(p.x, p.y);
			robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
		}
	}
	
	public static void main(String... args) throws Exception {
		new Serwer();
	}

}
