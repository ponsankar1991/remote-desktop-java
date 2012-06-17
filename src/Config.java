import java.io.Serializable;


public class Config implements Serializable {
	private static final long serialVersionUID = -6514239195482092832L;
	
	//jakość pliku jpg (od 0 do 1)
	public float quality = 0.1f;
	//odstęp czasowy pomiędzy wysyłanymi zrzutami ekranu (w ms)
	public int period = 1000;
	//rozmiar obrazu (zrzutu ekranu)
	public int imgWidth = 800, imgHeight = 600;
}
