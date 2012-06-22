/*
 * Nazwa robocza: ScreenShooter
 * Wersja: 0.1
 * Z dnia: 2012-06-20
 *
 * Opis programu:
 *          Aplikacja typu klient-serwer oparta na protokole TCP/IP,
 *          umozliwiajaca podglad ekranu komputera-serwera oraz
 *          proste operacje przeprowadzane za pomoca myszki oraz klawiatury
 *
 * Autorzy:
 *          Wojciech Gołuchowski
 *          Daniel Czyczyn-Egird
 *
 * KLASA: Server - odpowiedzialna za obsluge serwera
 */
package pl.screenshooter.server;
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
import javax.swing.JOptionPane;

import pl.screenshooter.logger.SSLogger;


public class Server {
	
	//rozmiar ekranu na serwerze
	private int screenWidth, screenHeight;
	//port
	private int port;
	//konfiguracja
	private Config config;
	//wątek odbierający komunikaty
	private Thread receiver;
	
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
		
		DataOutputStream dos = new DataOutputStream(Server.this.os);

		@Override
		public void run() {
			BufferedInputStream bis = null;
			try {
				File screen = captureScreenShot();
				
				//wysłanie wielkości pliku
				int filesize = (int)screen.length();
				byte [] mybytearray  = new byte [filesize];
				bis = new BufferedInputStream(new FileInputStream(screen));
				bis.read(mybytearray,0,mybytearray.length);
				
				dos.writeInt(filesize);
				Server.this.os.write(mybytearray,0,mybytearray.length);
			    os.flush();
			    
			    SSLogger.getInstance().debug("Wysłano zrzut ekranu o wielkości " + filesize + "b");
			    
			} catch (IOException e) {
				SSLogger.getInstance().error("Błąd przy wysyłaniu zrzutu." + e.getMessage());
			} finally {
				try {
					if (bis != null)
						bis.close();
				} catch (IOException e) {
					SSLogger.getInstance().fatal("Błąd przy zamykaniu strumienia");
				}
			}
		}
		
		/**
		 * Metoda zwracająca plik z zapisanym zrzutem ekranu
		 * @return
		 */
		private File captureScreenShot() {
			BufferedImage capture;
			File f = new File("screen.jpg"); 
			File compressed = new File("compressed.jpg");
			try {
				capture = robot.createScreenCapture(screenRect);
				
				//zmiana rozmiaru obrazka
				BufferedImage resizedImage = new BufferedImage(config.imgWidth, config.imgHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = resizedImage.createGraphics();
				g.drawImage(capture, 0, 0, config.imgWidth, config.imgHeight, null);
				g.dispose();				
				ImageIO.write(resizedImage, "jpg", f);
				
				//zmiana jakości jpg
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
				SSLogger.getInstance().fatal("Wystąpił błąd przy robieniu zrzutu.");
				return null;
			}
			
			SSLogger.getInstance().debug("Zrzut wykonany pomyślnie.");
			return compressed;
		}
		
	}
	
	private ServerSocket ss;
	private ObjectInputStream is;
	private OutputStream os;
	private Robot robot;

	public Server(int port) {
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
		
		
		receiver = new Thread() {
			public void run() {
				//utworzenie gniazda serwera tcp
				try {
					ss = new ServerSocket(port);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Nie można utworzyć serwera, prawdopodobnie wybrany port jest zajęty.", "Błąd", JOptionPane.ERROR_MESSAGE);
					SSLogger.getInstance().error("Nie można utworzyć gniazda serwera.");
					return;
				}
				
				//utworzenie gniazda do połączenia z klientem
				Socket socket = null;
				try {
					socket = ss.accept();
					is = new ObjectInputStream(socket.getInputStream());
					os = socket.getOutputStream();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Nie można połączyć z klientem.", "Błąd", JOptionPane.ERROR_MESSAGE);
					SSLogger.getInstance().fatal("Wystąpił błąd przy połączeniu z klientem.");
					
					try {
						if (os != null)
							os.close();
						if (is != null)
							is.close();
						if (socket != null)
							socket.close();
					} catch (IOException e) {
						SSLogger.getInstance().error("Wystąpił błąd przy zamykaniu gniazda.");
					}
					
					return;
				}
				
				//odczytanie konfiguracji od klienta
				try {
					config = (Config) is.readObject();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Nie można połączyć z klientem.", "Błąd", JOptionPane.ERROR_MESSAGE);
					SSLogger.getInstance().fatal("Wystąpił błąd przy odbieraniu konfiguracji.");
					return;
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
					JOptionPane.showMessageDialog(null, "Połączenie przerwane.", "Błąd", JOptionPane.ERROR_MESSAGE);
					SSLogger.getInstance().fatal("Połączenie przerwane.");
				}
			}
		};
		receiver.start();
	}
	
	/**
	 * Metoda przyjmująca zdarzenia klawiatury i myszy, przekazująca je
	 * do odpowiednich metod
	 * @param e zdarzenie klawiatury lub myszy
	 */
	private void processIncomingEvent(Object e) {
		if (e instanceof MouseEvent) {
			processMouseEvent((MouseEvent) e);
		} else if (e instanceof KeyEvent) {
			processKeyboardEvent((KeyEvent) e);
		}
	}
	
	/**
	 * Metoda symulująca naciśniecie klawisza
	 * @param e zdarzenie klawiatury
	 */
	private void processKeyboardEvent(KeyEvent e) {
		int keyCode = e.getKeyCode();
		robot.keyPress(keyCode);
		robot.keyRelease(keyCode);
	}
	
	/**
	 * Metoda symulująca klikanie myszki.
	 * @param e zdarzenie myszy
	 */
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
	
	/**
	 * Metoda przeliczająca współrzędne kliknięcia na postawie rozdzielczości
	 * @param p punkt
	 * @return zmodyfikowany punkt
	 */
	private Point convertPoint(Point p) {
		p.x = screenWidth*p.x/config.imgWidth;
		p.y = screenHeight*p.y/config.imgHeight;
		return p;
	}

	/**
	 * Metoda testowa, uruchamiająca przykładowy serwer.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) {
		new Server(1235).start();
	}

}
