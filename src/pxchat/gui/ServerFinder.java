/**
 * 
 */
package pxchat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import pxchat.net.Client;
import pxchat.util.Config;

/**
 * @author Markus Döllinger
 * 
 */
public class ServerFinder extends JDialog {

	private static final long serialVersionUID = 1166794484525146317L;

	private JTable table;

	private JButton connectBtn;
	private JButton refreshBtn;

	private JTextField userName;
	private JPasswordField password;

	/**
	 * @throws IOException
	 * 
	 */
	public ServerFinder(JFrame parent) {
		super(parent, I18n.getInstance().getString("sfTitle"));
		setLayout(new BorderLayout());

		table = new JTable();
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setModel(new ServerTableModel(new Vector<ServerEntry>()));
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getFirstIndex() > -1)
					connectBtn.setEnabled(true);
				else
					connectBtn.setEnabled(false);
			}
		});

		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(320, 240));

		getContentPane().add(scrollPane, BorderLayout.NORTH);

		JPanel loginPane = new JPanel();
		loginPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
		loginPane.setLayout(new GridLayout(2, 2));
		loginPane.add(new JLabel("Username"));
		userName = new JTextField();
		loginPane.add(userName);
		loginPane.add(new JLabel("Password"));
		password = new JPasswordField();
		loginPane.add(password);
		getContentPane().add(loginPane, BorderLayout.CENTER);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
		refreshBtn = new JButton(I18n.getInstance().getString("refresh"));
		refreshBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				connectBtn.setEnabled(false);
				refreshBtn.setEnabled(false);
				table.clearSelection();
				updateEntries();
				refreshBtn.setEnabled(true);
			}
		});
		buttonPane.add(refreshBtn);
		buttonPane.add(Box.createHorizontalGlue());
		connectBtn = new JButton(I18n.getInstance().getString("connect"));
		connectBtn.setEnabled(false);
		connectBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = table.getSelectedRow();
				if (index > -1) {
					ServerEntry entry = ((ServerTableModel) table.getModel()).getEntry(index);
					System.out.println(entry);

					String host = entry.getRemoteAddress();
					String sport = entry.getPort();
					String user = userName.getText();
					String pass = String.valueOf(password.getPassword());
					int port = 0;
					try {
						port = Integer.valueOf(sport);
					} catch (NumberFormatException ne) {
						port = 12345;
					}

					if (user.equals("") || pass.equals("")) {
						JOptionPane.showMessageDialog(ServerFinder.this, I18n.getInstance()
								.getString("inputFail"));
						return;
					}

					try {
						Client.getInstance().connect(host, port, user, pass);
					} catch (Exception ee) {
						JOptionPane.showMessageDialog(ServerFinder.this, I18n.getInstance()
								.getString("connectFail"));
						return;
					}
					dispose();
				}
			}
		});
		buttonPane.add(connectBtn);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		JButton cancelBtn = new JButton(I18n.getInstance().getString("cancel"));
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ServerFinder.this.dispose();
			}
		});
		buttonPane.add(cancelBtn);

		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		this.pack();

		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.setLocationRelativeTo(this.getParent());

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateEntries();
			}
		});

		this.setVisible(true);
	}

	/**
	 * This method connects to a master server where the addresses of available
	 * pxchat servers are stored. If it can connect it shows the servers in a
	 * table, else it shows an error message and disposes the ServerFinder.
	 */
	private void updateEntries() {
		
		List<ServerEntry> entries = Collections.synchronizedList(new Vector<ServerEntry>());
		
		UDPClient client = new UDPClient(entries);
		client.start();

		try {
			URL u = new URL(Config.get("masterServer"));
			BufferedReader r;
			URLConnection c = u.openConnection();
			c.setConnectTimeout(1000);
			r = new BufferedReader(new InputStreamReader(c.getInputStream()));

			String line = null;
			while ((line = r.readLine()) != null) {

				Pattern p = Pattern.compile("^([^ ]*) ([^ ]*) (.*)$");
				Matcher m = p.matcher(line);
				if (m.matches()) {
					String name = m.group(3);
					String remoteAddress = m.group(1);
					String port = m.group(2);
					entries.add(new ServerEntry(name, remoteAddress, port));
				}
			}

			table.setModel(new ServerTableModel(entries));
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(this, I18n.getInstance().getString("masterServerUnavailable"));
			this.dispose();
		}

	}
	
	class UDPClient extends Thread {
		
		private List<ServerEntry> entries;
		
		public UDPClient(List<ServerEntry> entries) {
			this.entries = entries;
		}

		public void run() {
			DatagramSocket socket = null;

			try {
				socket = new DatagramSocket(1338);
				socket.setSoTimeout(100);

				DatagramPacket request = new DatagramPacket("Request".getBytes(),
						"Request".length(), new InetSocketAddress("255.255.255.255", 1337));
				
				socket.setBroadcast(true);
				socket.send(request);

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
			long start = System.currentTimeMillis();

			while (!isInterrupted()) {
				DatagramPacket packet = new DatagramPacket(new byte[256], 256);
				try {
					socket.receive(packet);
					byte[] data = new byte[packet.getLength()];
					System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
					System.out.println(new String(data));
					String[] response = new String(data).split(" ");
					entries.add(new ServerEntry(response[1], packet.getAddress().getHostAddress(), response[0]));
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							table.setModel(new ServerTableModel(entries));
						}
					});
					
				} catch (Exception e) {
					return;
				}
				if (System.currentTimeMillis() - start > 3000)
					interrupt();
			}
			socket.close();
		}
	}
}

/**
 * @author Markus Döllinger
 * 
 * This class realizes the displaying of the server table.
 */
class ServerTableModel implements TableModel {

	List<ServerEntry> data;

	String[] columns = new String[] { "Name", "Address", "Port" };

	public ServerTableModel(List<ServerEntry> entries) {
		this.data = entries;
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns[columnIndex];
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return data.get(rowIndex).getName();
			case 1:
				return data.get(rowIndex).getRemoteAddress();
			case 2:
				return data.get(rowIndex).getPort();
		}
		return "";
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	public ServerEntry getEntry(int row) {
		return data.get(row);
	}
}

class ServerEntry {
	private String name;
	private String remoteAddress;
	private String port;

	public ServerEntry(String name, String remoteAddress, String port) {
		this.name = name;
		this.remoteAddress = remoteAddress;
		this.port = port;
	}

	public String toString() {
		return "[" + name + "@" + remoteAddress + ":" + port + "]";
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @return the remoteAddress
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ServerEntry))
			return false;

		ServerEntry that = (ServerEntry) other;
		return that.name.equals(this.name) && that.port.equals(this.port) && that.remoteAddress
				.equals(this.remoteAddress);
	}
}
