import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;


public class Klient extends JFrame {


	private Socket socket;
	ObjectOutputStream o = null;

	public static void main(String[] args) {
		new Klient().setVisible(true);

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
					socket = new Socket("127.0.0.1", 1235);
					try {
						o = new ObjectOutputStream(socket.getOutputStream());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				
			}
		});
		
		JButton shot = new JButton("SHOT!");
		shot.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Screen s = new Screen(captureScreenShot());
				
				
				
				try {
					o.writeObject(s);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		});
		
		this.getContentPane().setLayout(new FlowLayout());
		this.getContentPane().add(connect);
		this.getContentPane().add(shot);
		this.pack();
	}
	
	BufferedImage captureScreenShot() {
		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit()
				.getScreenSize());
		BufferedImage capture;
		try {
			capture = new Robot().createScreenCapture(screenRect);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return capture;
	}

}
