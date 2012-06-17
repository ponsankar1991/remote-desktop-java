import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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

import pl.sieci.remote.RemoteClick;


public class Serwer {
	
	//jakość pliku jpg (od 0 do 1)
	private float quality = 0.1f;
	//odstęp czasowy pomiędzy wysyłanymi zrzutami ekranu (w ms)
	private int period = 1000;
	
	/**
	 * @author Wojtek
	 * Klasa odpowiedzialna z robienie zrzutów ekranu i przesyłanie ich
	 * strumieniem.
	 */
	private class ScreenShoter extends TimerTask {
		
		private Rectangle screenRect;
		
		public ScreenShoter() {
			screenRect = new Rectangle(Toolkit.getDefaultToolkit()
					.getScreenSize());
		}

		@Override
		public void run() {
			try {
				File screen = captureScreenShot();
				
				
				int fileLength = (int)screen.length();
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
		
		Object o = null;
		while ((o = is.readObject()) != null) {
			processIncomingEvent(o);
			
            System.out.println("klik!");
		}
	}
	
	private void processIncomingEvent(Object e) {
		if (e instanceof MouseEvent) {
			processMouseEvent((MouseEvent) e);
		} else if (e instanceof KeyEvent) {
			processKeyboardEvent((KeyEvent) e);
		}
	}
	
	private void processKeyboardEvent(KeyEvent e) {
		int keyCode = e.getKeyCode();
		robot.keyPress(keyCode);
		robot.keyRelease(keyCode);
	}
	
	private void processMouseEvent(MouseEvent e) {
		int button;
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			button = InputEvent.BUTTON1_MASK;
			break;
		case MouseEvent.BUTTON2:
			button = InputEvent.BUTTON2_MASK;
			break;
		case MouseEvent.BUTTON3:
			button = InputEvent.BUTTON3_MASK;
			break;
		default:
			return;
		}
		
		robot.mouseMove(e.getX(), e.getY());
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			robot.mousePress(button);
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			robot.mouseRelease(button);
		}
		
	}


	public static void main(String... args) throws Exception {
		new Serwer();
	}

}
