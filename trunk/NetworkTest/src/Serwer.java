import java.awt.image.BufferedImage;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;


public class Serwer {
	
	private static ServerSocket ss;

	public static void main(String[] args) throws Exception {
		ss = new ServerSocket(1235);
		Socket socket = ss.accept();
		ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
		
		Screen b = null;
		int i = 0;
		while ((b = (Screen) is.readObject()) != null) {
			ImageIO.write(b.getImage(), "jpeg", new File("image"+i+".jpg"));
			++i;
		}
	}

}
