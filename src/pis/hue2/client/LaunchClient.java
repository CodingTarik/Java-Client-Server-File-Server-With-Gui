package pis.hue2.client;

import java.awt.EventQueue;

/**
 * Startet den Client
 *
 */
public class LaunchClient {

	/**
	 * Eisntiegspunkt für den Client
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final ClientGUI frame = new ClientGUI();
					frame.setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

}
