import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Klient extends JFrame implements MouseListener, KeyListener {

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
	 * Klasa odbierająca zrzuty ekranu od serwera.
	 */
	class Receiver extends Thread {
		@Override
		public void run() {
			while (!socket.isClosed()) {
				getScreenshot();
			}
		}
		
		private void getScreenshot() {
			int bytesRead = 0;
			int current = 0;
			try {
				FileOutputStream fos = new FileOutputStream("rscreen.jpg");
			    BufferedOutputStream bos = new BufferedOutputStream(fos);
			    DataInputStream dis = new DataInputStream(Klient.this.is);
			    int filesize = dis.readInt();
			    byte [] mybytearray  = new byte [filesize];
			    bytesRead = Klient.this.is.read(mybytearray,0,mybytearray.length);
			    current = bytesRead;

			    do {
			       bytesRead =
			          is.read(mybytearray, current, (mybytearray.length-current));
			       if(bytesRead >= 0) current += bytesRead;
			    } while(bytesRead > -1 && current != filesize);

			    bos.write(mybytearray, 0 , current);
			    bos.flush();
			    Klient.this.image = ImageIO.read(new File("rscreen.jpg"));
			    
			    
			} catch (Exception e) {
				e.printStackTrace();
			}
		    
		    Klient.this.repaint();
				
			System.out.println("Odebrano screen "+DateFormat.getTimeInstance().format(new Date()));
		}
	}
	
	
	public Klient(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
		
		createGUI();
		
		contentPane.addKeyListener(this);
		contentPane.addMouseListener(this);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				System.exit(0);
			}
		});
	}

	private void createGUI() {
		JButton connect = new JButton("CONNECT");
		connect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new Socket(hostName, port);
					try {
						is = socket.getInputStream();
						os = new ObjectOutputStream(socket.getOutputStream());
						os.writeObject(config);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					new Receiver().start();
					
				} catch (Exception e1) {
					e1.printStackTrace();
					//błąd połączenia
				}
				
				
			}
		});
		
		this.setContentPane(contentPane);
		contentPane.setDoubleBuffered(true);
		contentPane.setLayout(null);
		contentPane.add(connect);
		connect.setBounds(0, 0, 200, 100);
		
		this.setUndecorated(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(config.imgWidth, config.imgHeight);
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(image, 0, 0, null);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		try {
			os.writeObject(e);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		try {
			os.writeObject(e);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		try {
			os.writeObject(e);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
	public static void main(String[] args) {
		new Klient("127.0.0.1", 1235).setVisible(true);
	}

}
