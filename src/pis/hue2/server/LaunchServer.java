package pis.hue2.server;

import java.awt.EventQueue;

/**
 * Einstiegsklasse für den Server
 *
 */
public class LaunchServer {

	/**
	 * Einstiegspunkt für den Server Initalisiert und startet die GUI für den Server
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
