package pis.hue2.server;

import java.awt.EventQueue;

/**
 * Einstiegsklasse f�r den Server
 *
 */
public class LaunchServer {

	/**
	 * Einstiegspunkt f�r den Server Initalisiert und startet die GUI f�r den Server
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final ServerGUI frame = new ServerGUI();
					frame.setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
