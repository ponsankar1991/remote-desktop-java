import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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


public class Klient extends JFrame implements MouseListener {


	private Socket socket;
	private InputStream is = null;
	private ObjectOutputStream os = null;
	private BufferedImage image = null;
	
	
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		    
		    try {
				Klient.this.image = ImageIO.read(new File("rscreen.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		    Klient.this.repaint();
				
			System.out.println("Odebrano screen"+DateFormat.getTimeInstance().format(new Date()));
		}
	}
	
	
	public Klient() {
		
		createGUI();
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
					socket = new Socket("192.168.1.4", 1235);
					try {
						is = socket.getInputStream();
						os = new ObjectOutputStream(socket.getOutputStream());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					new Receiver().start();
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				
			}
		});
		
		
		this.getContentPane().setLayout(null);
		this.getContentPane().add(connect);
		connect.setBounds(0, 0, 200, 100);
		//this.getContentPane().add(shot);
		//this.pack();
		
		this.setUndecorated(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 1280, 800);
		this.setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(image, 0, 0, null);
	}
	
	public static void main(String[] args) {
		new Klient().setVisible(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		try {
			os.writeObject(new Point(e.getX(), e.getY()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	

}
