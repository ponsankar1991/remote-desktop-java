package pl.screenshooter.gui;
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
 * KLASA: AppForm - główne okno aplikacji
 */

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import pl.screenshooter.client.Klient;
import pl.screenshooter.server.Config;
import pl.screenshooter.server.Server;

import java.awt.Font;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class AppForm extends javax.swing.JFrame {
	
	private OpcjeSerwera opcjeSerwera = new OpcjeSerwera();
	private OpcjeKlienta opcjeKlienta = new OpcjeKlienta();
	
	private boolean clientRunning = false, serverRunning = false;

    /** Stworzenie nowej formy AppForm */
    public AppForm() {

        initComponents();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ScreenShooter ver. 0.1");
        setResizable(false);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(0, 102, 102), null));

        jLabel3.setText("ScreenShooter ver. 0.1");

        javax.swing.GroupLayout gl_jPanel3 = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(gl_jPanel3);
        gl_jPanel3.setHorizontalGroup(
            gl_jPanel3.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_jPanel3.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                .addContainerGap())
        );
        gl_jPanel3.setVerticalGroup(
            gl_jPanel3.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_jPanel3.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/network.png"))); // NOI18N
        jLabel4.setText("obrazek");

        jMenu1.setText("Ustawienia");

        jMenuItem1.setText("Opcje Serwera");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Opcje Klienta");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Pomoc");

        jMenuItem3.setText("Pokaż plik pomocy");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);
        
        btnSerwer = new JButton("SERWER");
        btnSerwer.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		int port = opcjeSerwera.getPort();
        		new Server(port).start();
        	}
        });
        btnSerwer.addMouseMotionListener(new MouseMotionAdapter() {
        	@Override
        	public void mouseMoved(MouseEvent e) {
        		jLabel3.setText("SERWER - uruchamia serwer zdalnej administracji");
        	}
        });
        btnSerwer.setFont(new Font("Dialog", Font.BOLD, 15));
        
        JButton btnKlient = new JButton("KLIENT");
        btnKlient.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		Config config = opcjeKlienta.getConfig();
        		int port = opcjeKlienta.getPort();
        		String host = opcjeKlienta.getHost();
        		
        		Klient klient = new Klient(host, port, config);
        		klient.establishConnection();
        		
        	}
        });
        btnKlient.addMouseMotionListener(new MouseMotionAdapter() {
        	@Override
        	public void mouseMoved(MouseEvent e) {
        		jLabel3.setText("KLIENT - umożliwia administrowanie zdalnym pulpitem na serwerze");
        	}
        });
        btnKlient.setFont(new Font("Dialog", Font.BOLD, 15));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addGap(44)
        					.addComponent(btnSerwer, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
        					.addGap(18)
        					.addComponent(btnKlient, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
        					.addGap(20)
        					.addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE))
        				.addGroup(layout.createSequentialGroup()
        					.addContainerGap()
        					.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)))
        			.addContainerGap())
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap(18, Short.MAX_VALUE)
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
        					.addGap(21))
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(btnSerwer, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
        						.addComponent(btnKlient, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
        					.addGap(31)))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        getContentPane().setLayout(layout);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-467)/2, (screenSize.height-288)/2, 467, 288);
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        opcjeKlienta.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
       try {
                Desktop desktop = null;
            if (Desktop.isDesktopSupported())
            {
                desktop = Desktop.getDesktop();
                File file = new File("Pomoc/pomoc.html");
                desktop.open(file);
            }
        } catch (IOException e)
        {
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        jLabel3.setText("ScreenShooter ver. 0.1");
    }//GEN-LAST:event_formMouseMoved

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        opcjeSerwera.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
    	
    	try {
    	    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
    	        if ("Nimbus".equals(info.getName())) {
    	            UIManager.setLookAndFeel(info.getClassName());
    	            break;
    	        }
    	    }
    	} catch (Exception e) {
    	    // If Nimbus is not available, fall back to cross-platform
    	    try {
    	        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    	    } catch (Exception ex) {
    	        // not worth my time
    	    }
    	}
    	
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AppForm().setVisible(true);
            }
        });
    }
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel3;
    private JButton btnSerwer;
}


