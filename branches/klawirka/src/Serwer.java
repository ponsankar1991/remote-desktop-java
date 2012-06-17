import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Point;
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


public class Serwer {
	
	//rozmiar ekranu na serwerze
	private int screenWidth, screenHeight;
	//port
	private int port;
	//konfiguracja
	private Config config;
	
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
				
				//resize 
				BufferedImage resizedImage = new BufferedImage(config.imgWidth, config.imgHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = resizedImage.createGraphics();
				g.drawImage(capture, 0, 0, config.imgWidth, config.imgHeight, null);
				g.dispose();				
				ImageIO.write(resizedImage, "jpg", f);
				
				ImageReader imgRdr = ImageIO.getImageReadersByFormatName("jpg").next();
				ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
				ImageInputStream imgInStrm = ImageIO.createImageInputStream(f);
				ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(compressed);
				imgRdr.setInput(imgInStrm);
				imgWrtr.setOutput(imgOutStrm);
				IIOImage iioImg = new IIOImage(imgRdr.read(0), null, null); 

				//poziom kompresji
				ImageWriteParam jpgWrtPrm = imgWrtr.getDefaultWriteParam();
				jpgWrtPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
				jpgWrtPrm.setCompressionQuality(config.quality);

				imgWrtr.write(null, iioImg, jpgWrtPrm);
 
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
	
	public void setQuality(float quality) {
		this.config.quality = quality;
	}

	public void setPeriod(int period) {
		this.config.period = period;
	}

	public void setImgWidth(int imgWidth) {
		this.config.imgWidth = imgWidth;
	}

	public void setImgHeight(int imgHeight) {
		this.config.imgHeight = imgHeight;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Serwer(int port) {
		//inicjalizacja zmiennych
		this.port = port;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		Rectangle r = new Rectangle(Toolkit.getDefaultToolkit()
				.getScreenSize());
		screenWidth = r.width;
		screenHeight = r.height;
		
	}

	public void start() {
		//utworzenie gniazda serwera tcp
		try {
			ss = new ServerSocket(port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//utworzenie gniazda do połączenia z klientem
		Socket socket;
		try {
			socket = ss.accept();
			is = new ObjectInputStream(socket.getInputStream());
			os = socket.getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//odczytanie konfiguracji od klienta
		try {
			config = (Config) is.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//uruchomienie timera wysyłającego zrzuty
		Timer t = new Timer();
		t.schedule(new ScreenShoter(), 100, config.period);
		
		//odbieranie komunikatów od klienta
		Object o = null;
		try {
			while ((o = is.readObject()) != null) {
				processIncomingEvent(o);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		Point p = convertPoint(new Point(e.getX(), e.getY()));
		robot.mouseMove(p.x, p.y);
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			robot.mousePress(button);
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			robot.mouseRelease(button);
		}
	}
	
	private Point convertPoint(Point p) {
		p.x = screenWidth*p.x/config.imgWidth;
		p.y = screenHeight*p.y/config.imgHeight;
		return p;
	}


	public static void main(String... args) throws Exception {
		new Serwer(1235).start();
	}

}
