/*
 * Nazwa robocza: ScreenShooter
 * Wersja: 0.1
 * Z dnia: 2012-06-21
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
 * KLASA: SSLogger - odpowiedzialna za przechowywanie i udostępnianie
 * obiektu Logger'a.
 */
package pl.screenshooter.logger;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class SSLogger {
    private static volatile Logger instance = null;
    
    static {
    	 DOMConfigurator.configure("config/log.xml");
    }
 
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (SSLogger.class) {
                if (instance == null) {
                    instance = Logger.getLogger(SSLogger.class);
                }
            }
        }
        return instance;
    }
 
    private SSLogger() {}
}
