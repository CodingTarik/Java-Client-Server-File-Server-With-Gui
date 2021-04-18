package pis.hue2.common;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * Diese Klasse beinhaltet Methoden zum Senden und Empfangen von DatenPaketen
 * modelliert durch die Klasse PackageDatagram über eine aktive
 * Socket-Verbindung
 *
 */
public class SocketManager {
	/**
	 * To abort waiting for a connection
	 */
	public boolean abortFlag = false;
	/**
	 * Das Socket-Objekt, das die Verbindung zum Kommunikationspartner verwaltet
	 */
	private final Socket socket;

	/**
	 * Printer-Writer zum Senden von Strings zum Kommunikationspartner
	 */
	private final PrintWriter printOutputStream;

	/**
	 * BufferedReader zum Empfangen von String vom Kommunikationspartner
	 */
	private final BufferedInputStream buffInputStreamReader;

	/***
	 * DataOutputStream zum Senden von Daten (byte[]) zum Kommunikationspartner
	 */
	private final DataOutputStream dataOutputStream;

	/**
	 * Initalisiert den SocketManager anhand des übergebenen Sockets
	 *
	 * @param die Socket-Verbindung zum Kommunikationspartner
	 * @throws IOException
	 */
	public SocketManager(final Socket socket) throws IOException {
		// Wenn das Socket-Objekt null war, soll ein Fehler geworfen werden
		if (socket == null) {
			throw new IllegalArgumentException("The socket object was null");
		}
		this.socket = socket;
		this.printOutputStream = new PrintWriter(this.socket.getOutputStream(), true);
		this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
		this.buffInputStreamReader = new BufferedInputStream(this.socket.getInputStream());
	}

	/**
	 * Schließt die Verbindung
	 *
	 * @throws IOException
	 */
	public void CloseConnection() throws IOException {
		this.socket.close();
	}

	/**
	 * Wartet auf ein eingehendes Datenpaket
	 *
	 * @return das empfangene Datenpaket
	 * @throws IOException
	 */
	public InstructionDatagram ReceiveDatagram() throws IOException {
		int charRead;
		final StringBuilder instructionString = new StringBuilder();
		System.out.println("Waiting for message");
		// Zuerst wird die eigentliche Instruktion gelesen (diese besteht immer aus 3
		// Zeichen)
		for (int i = 0; i < 3; i++) {
			this.WaitForCharacter();
			instructionString.append((char) this.buffInputStreamReader.read());
		}
		System.out.println(instructionString.toString() + " received ");
		// Die Instruktion wurde gelesen, jetzt können wir hier ein Datenpaket anlegen
		final Instruction instruction = Instruction.valueOf(instructionString.toString());
		final InstructionDatagram data = new InstructionDatagram();
		data.instruction = instruction;
		data.SentBy = this.socket.getInetAddress().getHostAddress();

		/*
		 * Sollte nun nicht \n als Zeichen kommen, sondern ein Leerzeichen bedeutet
		 * dies, dass wir mindestens einen Parameter haben (bei allen Instruktionen vom
		 * Typ String)
		 */
		this.WaitForCharacter();
		if ((charRead = (char) this.buffInputStreamReader.read()) == '\n' || charRead == '\r') {
			// Wir haben keinen neuen Parameter
			if (charRead == '\r') {
				this.buffInputStreamReader.read();
			}
			return data;
		} else if (charRead == ' ') {
			// Es gibt noch mindestens einen Parameter, lese diesen aus
			final StringBuilder firstParam = new StringBuilder();
			this.WaitForCharacter();
			boolean ignoreSpace = false;
			/*
			 * Zwar soll hier auf Leerzeichen geachtet werden, jedoch kann es sein diese
			 * auch explizit z.B. als Dateiname mit übertragen werden, deshalb sollte man
			 * hier auf Gänsefüßchen achten
			 */
			while ((charRead = (char) this.buffInputStreamReader.read()) != '\n' && charRead != '\r'
					&& (charRead != ' ' || ignoreSpace)) {
				/*
				 * Sollte das Gänsefüßchen am Anfang kommen schalten wir die Suche nach einem
				 * Leerzeichen aus sollte danach wieder eins kommen, so müssen wir diese wieder
				 * anschalten
				 */
				if (charRead == '"') {
					ignoreSpace = !ignoreSpace;
				} else {
					firstParam.append((char) charRead);
				}
			}
			System.out.println("Param received: " + firstParam.toString() + " with a end through char " + charRead);
			data.param1 = firstParam.toString();
			if (charRead == '\r') {
				System.out.println("char was \\r or ' ' " + charRead);
				// ein \n muss folgen, deshalb hier dieses auslesen
				while ((charRead = this.buffInputStreamReader.read()) == '\r') {
					System.out.println("Read \\r --> " + charRead);
				}
			}
			if (charRead == '\n') {
				return data;
			} else {
				if (charRead != ' ') {
					this.buffInputStreamReader.read();
				}
				System.out.println("downloading third parameter");
				final byte[] param2 = new byte[Integer.parseInt(data.param1)];
				System.out.println("Reading " + param2.length + " bytes now");
				this.buffInputStreamReader.read(param2);
				data.param2 = param2;
				this.buffInputStreamReader.skip(this.buffInputStreamReader.available());
				System.out.println("Param 2 read");
				return data;
			}
		} else {
			// FormatException
			throw new IllegalArgumentException(
					"\\n or ' ' expected as next character of network stream, char read: " + (char) charRead + ".");
		}
	}

	/***
	 * Diese Methode sendet Protokoll-Anweisungen zum Gegenüber
	 *
	 * @param das Datenpaket, welches gesendet werden soll
	 * @throws IOException
	 */
	public void SendInstruction(final InstructionDatagram data) throws IOException {
		switch (data.instruction) {
		case CON:
			System.out.println("Writing CON THROUGH print Stream");
			this.printOutputStream.println("CON");
			break;
		case DSC:
			this.printOutputStream.println("DSC");
			break;
		case ACK:
			this.printOutputStream.println("ACK");
			break;
		case DND:
			this.printOutputStream.println("DND");
			break;
		case LST:
			this.printOutputStream.println("LST");
			break;
		case PUT:
			if (data.param1 != null) {
				this.printOutputStream.println("PUT " + data.param1);
				break;
			} else {
				throw new IllegalArgumentException("Trying to use PUT with invalid parameters");
			}
		case GET:
			if (data.param1 != null) {
				this.printOutputStream.println("GET " + data.param1);
				break;
			} else {
				throw new IllegalArgumentException("Trying to use GET with invalid parameters");
			}
		case DEL:
			if (data.param1 != null) {
				this.printOutputStream.println("DEL " + data.param1);
				break;
			} else {
				throw new IllegalArgumentException("Trying to use DEL with invalid parameters");
			}
		case DAT:
			System.out.println("Sending DAT with " + data.param2.length + " bytes");
			if (data.param1 != null && data.param2 != null) {
				this.printOutputStream.print("DAT " + data.param1 + " ");
				this.printOutputStream.flush();
				this.dataOutputStream.write(data.param2);
				this.dataOutputStream.flush();
				this.printOutputStream.print("\r\n");
				this.printOutputStream.flush();
				break;
			} else {
				throw new IllegalArgumentException("Trying to use GET with invalid parameters");
			}
		}
	}

	/**
	 * Wird unter anderem für das Empfangen von Nachrichten benötigt, um auf
	 * einzelen Zeichen zu warten
	 *
	 * @throws IOException
	 */
	private void WaitForCharacter() throws IOException {
		// Solange noch nichts im Buffer ist
		while (!(this.buffInputStreamReader.available() > 0)) {
			// Warte
			if (this.abortFlag) {
				break;
			}
		}
	}
}
