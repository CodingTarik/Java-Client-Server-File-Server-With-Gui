package pis.hue2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Der ServerConnectionManager kümmert sich darum, verschiedene Verbindungen
 * (maximal 3) entgegen zu nehmen
 *
 */
public class ServerConnectionManager extends Thread {
	/**
	 * Offene Verbindungen die aktiv verwaltet werden
	 */
	private final ServerSocketManager[] openConnections = new ServerSocketManager[3];
	/***
	 * Port auf dem der Server laufen soll
	 */
	private final int port = 7777;

	/**
	 * Start-Methode zum Starten des Listen-Loops zum Abfangen von Verbindungen
	 */
	@Override
	public void run() {
		try {
			ServerGUI.activeGUI.UpdateServerStatusLabel("Online");
			final ServerSocket serverSocket = new ServerSocket(this.port);
			serverSocket.setSoTimeout(3000);
			while (true) {
				final Socket socket;
				try {
					socket = serverSocket.accept();
				} catch (final SocketTimeoutException ex) {
					if (this.isInterrupted()) {
						break;
					}
					continue;
				}
				try {
					for (int i = 0; i < this.openConnections.length; i++) {
						if (this.openConnections[i] == null || this.openConnections[i].getState() == State.TERMINATED) {
							this.openConnections[i] = new ServerSocketManager(socket, true);
							this.openConnections[i].start();
							break;
						}
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			for (final ServerSocketManager openConnection : this.openConnections) {
				if (openConnection != null && openConnection.getState() != State.TERMINATED) {
					openConnection.interrupt();
					openConnection.socketManager.abortFlag = true;
				}
			}
		} catch (final IOException e) {
			ServerGUI.activeGUI.UpdateServerStatusLabel("Offline");
			e.printStackTrace();
		}
		ServerGUI.activeGUI.UpdateServerStatusLabel("Offline");
	}
}
