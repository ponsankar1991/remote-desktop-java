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
 * KLASA: Client - odpowiedzialna za obsluge klienta
 */
package pl.screenshooter.client;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pl.screenshooter.logger.SSLogger;
import pl.screenshooter.server.Config;


public class Klient extends JFrame {

	private Socket socket;
	private InputStream is = null;
	private ObjectOutputStream os = null;
	private BufferedImage image = null;
	
	private String hostName;
	private int port;
	private Config config = new Config();
	
	private JPanel contentPane = new JPanel();
	
	/**
	 * 
	 * @author Wojtek
	 * Klasa wewnętrzna odbierająca zrzuty ekranu od serwera w osobnym wątku.
	 */
	private class Receiver extends Thread {
		@Override
		public void run() {
			while (!socket.isClosed()) {
				getScreenshot();
			}
		}
		
		DataInputStream dis = new DataInputStream(Klient.this.is);
		
		/**
		 * Metoda odbierająca zrzut ekranu
		 */
		private void getScreenshot() {
			int bytesRead = 0;
			int current = 0;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				fos = new FileOutputStream("rscreen.jpg");
			    bos = new BufferedOutputStream(fos);
			    //odebranie wielkości pliku
			    int filesize = dis.readInt();
			    byte [] mybytearray  = new byte [filesize];
			    bytesRead = Klient.this.is.read(mybytearray,0,mybytearray.length);
			    current = bytesRead;

			    //odebranie zrzutu ekranu
			    do {
			       bytesRead =
			          is.read(mybytearray, current, (mybytearray.length-current));
			       if(bytesRead >= 0) current += bytesRead;
			    } while(bytesRead > -1 && current != filesize);

			    bos.write(mybytearray, 0 , current);
			    bos.flush();
			    //zapisanie zrzutu w polu nadrzędnej klasy
			    Klient.this.image = ImageIO.read(new File("rscreen.jpg"));
			    
			    SSLogger.getInstance().debug("Odebrano zrzut ekranu o wielkości " + filesize + "b");
			    
			} catch (Exception e) {
				SSLogger.getInstance().error("Błąd w trakcie odbierania zrzutu");
				return;
			} finally {
				try {
					if (bos != null)
						bos.close();
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					SSLogger.getInstance().fatal("Błąd przy zamykaniu strumieni");
				}
			}
		    
		    Klient.this.repaint();
		}
	}
	
	/**
	 * Konstruktor przyjmujący w parametrach adres i port serwera.
	 * @param hostName adres serwera
	 * @param port port połączenia
	 */
	public Klient(String hostName, int port, Config config) {
		this.hostName = hostName;
		this.port = port;
		this.config = config;
		
		createGUI();
		addListeners();
	}
	
	/**
	 * Metoda nawiązująca połączenie z serwerem.
	 * @return czy nawiązano połączenie
	 */
	public boolean establishConnection() {
		try {
			socket = new Socket(hostName, port);
			//przesłanie konfiguracji do serwera
			try {
				is = socket.getInputStream();
				os = new ObjectOutputStream(socket.getOutputStream());
				os.writeObject(config);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "Błąd przy nawiązywaniu połączenia", "Błąd", JOptionPane.ERROR_MESSAGE);
				SSLogger.getInstance().fatal("Błąd w trakcie nawiązywania połączenia");
				
				if (os != null)
					os.close();
				
				return false;
			}
			
			//uruchomienie wątku odbierającego dane od serwera
			new Receiver().start();
			
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(this, "Wprowadzono adres jest nieprawidłowy.", "Błąd", JOptionPane.ERROR_MESSAGE);
			SSLogger.getInstance().error("Wprowadzono adres jest nieprawidłowy.");
			return false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Błąd przy nawiązywaniu połączenia", "Błąd", JOptionPane.ERROR_MESSAGE);
			SSLogger.getInstance().fatal("Błąd w trakcie nawiązywania połączenia");
			return false;
		} 
		
		return true;
	}

	
	/**
	 * Metoda rysująca otrzymany zrzut ekranu.
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(image, 0, 0, null);
	}
	
	/**
	 * Metoda tworząca interfejs graficzny.
	 */
	private void createGUI() {
		
		this.setContentPane(contentPane);
		contentPane.setDoubleBuffered(true);
		
		this.setUndecorated(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(config.imgWidth, config.imgHeight);
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		
		SSLogger.getInstance().debug("Utworzono GUI");
	}
	
	/**
	 * Metoda dodająca listenery do głównego okna
	 * (akcje klawiatury i myszy oraz zamknięcie okna)
	 */
	private void addListeners() {
		//listener odpowiedzialny za przesłanie zdarzenia naciśnięcia klawisza
		contentPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					os.writeObject(e);
				} catch (IOException e1) {
					SSLogger.getInstance().fatal("Błąd w trakcie wysyłania KeyEvent");
				}
			}
		});
		
		//listener odpowiedzialny za przesłanie zdarzeń myszy (naciśnięcia 
		//lub zwolnienia przycisku)
		contentPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				try {
					os.writeObject(e);
				} catch (IOException e1) {
					SSLogger.getInstance().fatal("Błąd w trakcie wysyłania MouseEvent");
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					os.writeObject(e);
				} catch (IOException e1) {
					SSLogger.getInstance().fatal("Błąd w trakcie wysyłania MouseEvent");
				}
			}
		});
		
		//listener odpowiedzialny za zamknięcie połączenia w momencie zamykania
		//okna
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						SSLogger.getInstance().fatal("Błąd w trakcie zamykania strumienia");
					}
				}
				System.exit(0);
			}
		});
	}
	
	
	/**
	 * Testowa metoda main nawiązująca połączenie localhostem
	 */
	public static void main(String[] args) {
		new Klient("127.0.0.1", 1235, new Config()).setVisible(true);
	}

}
