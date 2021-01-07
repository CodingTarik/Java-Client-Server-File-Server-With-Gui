package pis.hue2.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Bitet Methoden zum Verwalten von Dateien, welche durch den Client oder Server
 * ben�tigt, ver�ndert oder verarbeitet werden m�ssen
 *
 */
public class FileManager {

	/**
	 * Stellt ein Lock-Objekt dar, damit Datei-Operationen threadsicher sind
	 */
	private static volatile Object FileLock = new Object();

	/**
	 * L�scht eine Datei
	 *
	 * @param filename der Dateiname mit Dateiendung
	 * @return true, wenn die Datei erfolgreich gel�scht werden konnte, ansonsten
	 *         false
	 */
	public static boolean DeleteFile(final String filePath) {
		synchronized (FileManager.FileLock) {
			final File file = new File(filePath);
			if (file.exists()) {
				return file.delete();
			} else {
				return false;
			}
		}
	}

	/**
	 * Sucht nach der angegebenen Datei und gibt diese als byte-Array zur�ck
	 *
	 * @param filename Die Datei, welche als byte[] gelesen werden soll mit
	 *                 Dateiendung
	 * @return Wenn die Datei gefunden wurde, dann die Datei als byte[], ansonsten
	 *         null
	 */
	public static byte[] GetBytesFromFile(final String filePath) {
		synchronized (FileManager.FileLock) {
			final File file = new File(filePath);
			// �berpr�fe, ob die Datei �berhaupt existiert
			if (file.exists()) {
				try {
					// Gebe die Datei in Form eines Byte-Arrays zur�ck, wenn vorhanden
					return Files.readAllBytes(file.toPath());
				} catch (final IOException e) {
					// Sollte es einen Fehler geben, gebe diesen aus
					System.out.println("Es gab einen Fehler beim laden der Datei");
					e.printStackTrace();
					return null;
				}
			} else {
				System.out.println("Die Datei im Verzeichnis " + filePath + " wurde nicht gefunden");
				// Wenn die Datei nicht gefunden wurde, gebe null zur�ck
				return null;
			}
		}
	}

	/**
	 * Gibt eine Auflistung an Dateien in einem bestimmten Verzeichnis zur�ck. Die
	 * Dateinamen werden mit einem Semikolon (;) voneinander getrennt
	 *
	 * @param directoryPath der Ordner-Pfad
	 * @return
	 */
	public static String GetFileList(final String directoryPath) {
		synchronized (FileManager.FileLock) {
			final File folder = new File(directoryPath);
			final File[] listOfFiles = folder.listFiles();

			final StringBuilder fileList = new StringBuilder();
			for (final File f : listOfFiles) {
				if (f.isFile()) {
					fileList.append(f.getName() + ";");
				}
			}
			return fileList.toString();
		}
	}

	/**
	 * Erstellt eine neue Datei aus einem byte[] und schreibt diese in das
	 * angegebene Verzeichnis, wenn die Datei bereits existiert, wird diese
	 * �berschrieben
	 *
	 * @param fileData Die �bertragenen Daten
	 * @param filePath Das Verzeichnis, wohin die Daten gespeichert werden sollen
	 * @return true, wenn das Ablegen der Datei erfolgreich war, ansonsten false
	 */
	public static boolean PutFile(final byte[] fileData, final String filePath) {
		synchronized (FileManager.FileLock) {
			final File file = new File(filePath);
			// Pr�fe ob die Datei bereits existiert
			if (file.exists()) {
				// Wenn ja l�sche diese
				if (!FileManager.DeleteFile(filePath)) {
					// Wenn die L�schung nicht erfolgreich war, gebe false zur�ck
					return false;
				}
			}
			// Erstelle die Datei mit fileData
			try {
				final FileOutputStream fos = new FileOutputStream(filePath);
				fos.write(fileData);
				fos.close();
			} catch (final IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
}
