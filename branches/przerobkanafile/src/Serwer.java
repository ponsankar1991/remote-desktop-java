import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;


public class Serwer extends JFrame {
	
	class ScreenShoter extends TimerTask {

		@Override
		public void run() {
			try {
				Serwer.this.os.writeObject(new Screen(captureScreenShot()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private BufferedImage captureScreenShot() {
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
	
	private ServerSocket ss;
	private ObjectInputStream is;
	private ObjectOutputStream os;
	
	public Serwer() throws Exception {
		ss = new ServerSocket(1235);
		Socket socket = ss.accept();
		//is = new ObjectInputStream(socket.getInputStream());
		os = new ObjectOutputStream(socket.getOutputStream());
		
		Timer t = new Timer();
		t.schedule(new ScreenShoter(), 1000, 1000);
	}
	
	public static void main(String... args) throws Exception {
		new Serwer();
	}

}
