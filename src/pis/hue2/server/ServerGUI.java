package pis.hue2.server;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.State;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Diese Klasse beinhaltet die Java-Swing Implementierung für die GUI des
 * Datei-Servers
 */
public class ServerGUI extends JFrame {
	/**
	 * Ein Verweis auf die aktive Server GUI
	 */
	public static ServerGUI activeGUI;

	/**
	 * JPanel zum Hosten der Elemente
	 */
	private final JPanel contentPane;

	/**
	 * CardLayout zum Steuern der einzelnen Views durch das Menü
	 */
	final CardLayout cl;

	/**
	 * Das InfoPanel mit dem CardLayout
	 */
	final JPanel InfoPanel;

	/**
	 * Tabelle, wo die übertragenen Dateien angezeigt werden
	 */
	private final JTable FileTransferTable;
	/**
	 * Tabellenmodell für die übertragenen und empfangenen Instruktionen
	 */
	private final DefaultTableModel DataTableModelProtocolLog;
	/**
	 * Tabellenmodell für übertragene Dateien
	 */
	private final DefaultTableModel DataTableModelFileTransferLog;

	/**
	 * Statuslabel für den Server-Status (offline / online)
	 */
	private final JLabel ServerStatusLabel;

	/**
	 * Der ServerConnectionManager, der sich um den Empfang und die Initalisierung
	 * von Verbindungen kümmert
	 */
	private ServerConnectionManager serverConnectionManager = new ServerConnectionManager();

	/**
	 * Erstellt ein Java Swing Frame für
	 */
	public ServerGUI() {
		ServerGUI.activeGUI = this;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 671, 375);
		this.contentPane = new JPanel();
		this.contentPane.setBackground(Color.DARK_GRAY);
		this.contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setContentPane(this.contentPane);
		this.contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		this.setResizable(false);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(200);
		splitPane.setDividerSize(0);
		this.contentPane.add(splitPane);
		final JPanel MenuPanel = new JPanel();
		MenuPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
		MenuPanel.setBackground(Color.DARK_GRAY);
		splitPane.setLeftComponent(MenuPanel);
		MenuPanel.setLayout(new GridLayout(3, 1, 0, 0));

		final JButton btnServer = new JButton("Start / Stop");
		btnServer.setFont(new Font("Arial", Font.BOLD, 13));
		btnServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ServerGUI.this.cl.show(ServerGUI.this.InfoPanel, "OfflineOnlinePanel");
			}
		});
		MenuPanel.add(btnServer);

		final JButton btnNewButton = new JButton("Protokoll-Verlauf");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ServerGUI.this.cl.show(ServerGUI.this.InfoPanel, "ProtocolLogPanel");
			}
		});
		btnNewButton.setFont(new Font("Arial", Font.BOLD, 13));
		MenuPanel.add(btnNewButton);

		final JButton btnNewButton_1 = new JButton("\u00DCbertragungen");
		btnNewButton_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ServerGUI.this.cl.show(ServerGUI.this.InfoPanel, "FileTransferPanel");
			}
		});
		btnNewButton_1.setFont(new Font("Arial", Font.BOLD, 13));
		MenuPanel.add(btnNewButton_1);

		this.InfoPanel = new JPanel();
		this.InfoPanel.setBackground(Color.ORANGE);
		splitPane.setRightComponent(this.InfoPanel);
		this.cl = new CardLayout(0, 0);
		this.InfoPanel.setLayout(this.cl);

		final JPanel OfflineOnlinePanel = new JPanel();
		OfflineOnlinePanel.setBackground(Color.ORANGE);
		this.InfoPanel.add(OfflineOnlinePanel, "OfflineOnlinePanel");
		OfflineOnlinePanel.setLayout(null);

		final JButton btnNewButton_2 = new JButton("Start / Stop Server");
		btnNewButton_2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if (ServerGUI.this.serverConnectionManager.getState() == State.TERMINATED
						|| ServerGUI.this.serverConnectionManager.getState() == State.NEW) {
					ServerGUI.this.serverConnectionManager = new ServerConnectionManager();
					ServerGUI.this.serverConnectionManager.start();
				} else {
					ServerGUI.this.serverConnectionManager.interrupt();
				}
			}
		});
		btnNewButton_2.setBounds(118, 163, 239, 37);
		OfflineOnlinePanel.add(btnNewButton_2);
		btnNewButton_2.setFont(new Font("Arial", Font.BOLD, 16));

		this.ServerStatusLabel = new JLabel("Server Status: Offline");
		this.ServerStatusLabel.setBounds(118, 132, 239, 20);
		OfflineOnlinePanel.add(this.ServerStatusLabel);
		this.ServerStatusLabel.setFont(new Font("Arial", Font.BOLD, 23));

		final JPanel ProtocolLogPanel = new JPanel();
		ProtocolLogPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		ProtocolLogPanel.setBackground(Color.ORANGE);
		this.InfoPanel.add(ProtocolLogPanel, "ProtocolLogPanel");
		ProtocolLogPanel.setLayout(new GridLayout(0, 1, 0, 0));

		String[] columnNames = { "Instruktion", "Sender", "Datum & Uhrzeit", "Parameter" };
		this.DataTableModelProtocolLog = new DefaultTableModel(0, 0);
		this.DataTableModelProtocolLog.setColumnIdentifiers(columnNames);
		final JTable table = new JTable();
		table.setModel(this.DataTableModelProtocolLog);
		ProtocolLogPanel.add(new JScrollPane(table));

		final JPanel FileTransferPanel = new JPanel();
		FileTransferPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		FileTransferPanel.setBackground(Color.ORANGE);
		this.InfoPanel.add(FileTransferPanel, "FileTransferPanel");
		FileTransferPanel.setLayout(new GridLayout(0, 1, 0, 0));

		columnNames = new String[] { "Datei", "Empfänger", "Datum & Uhrzeit" };
		this.DataTableModelFileTransferLog = new DefaultTableModel(0, 0);
		this.DataTableModelFileTransferLog.setColumnIdentifiers(columnNames);
		this.FileTransferTable = new JTable();
		this.FileTransferTable.setModel(this.DataTableModelFileTransferLog);
		FileTransferPanel.add(new JScrollPane(this.FileTransferTable));
	}

	/**
	 * Logt einen Dateitransfer mit und fügt diesen der Tabelle für Dateitransfers
	 * hinzu
	 *
	 * @param file        Der Dateiname
	 * @param receiver    Der Empfänger
	 * @param dateAndTime Datum und Uhrzeit nach erfolgreicher Sendung
	 */
	public void LogFileTransfer(final String file, final String receiver, final String dateAndTime) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ServerGUI.this.DataTableModelFileTransferLog.addRow(new Object[] { file, receiver, dateAndTime });
			}

		});
	}

	/**
	 * Logt Protokollnachrichten mit und fügt diese der Protokolltabelel hinzu
	 *
	 * @param instruction Instruktion
	 * @param sender      Host oder Client (IP)
	 * @param dateAndTime Zeit und Datum
	 * @param parameter   Ein möglicher Parameter der bei der Instruktion mit
	 *                    übertragen wurde (nur der erste)
	 */
	public void LogProtocol(final String instruction, final String sender, final String dateAndTime,
			final String parameter) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ServerGUI.this.DataTableModelProtocolLog
						.addRow(new Object[] { instruction, sender, dateAndTime, parameter });
			}

		});
	}

	/**
	 * Setzt das Status Label für den Server Status (Online / Offline)
	 *
	 * @param status
	 */
	public void UpdateServerStatusLabel(final String status) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ServerGUI.this.ServerStatusLabel.setText("Server Status " + status);
			}

		});
	}
}
