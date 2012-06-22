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
 * KLASA: Config - odpowiedzialna za obsluge konfiguracji ustawien
 */
package pl.screenshooter.server;
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
