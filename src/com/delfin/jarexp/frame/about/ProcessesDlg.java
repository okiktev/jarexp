package com.delfin.jarexp.frame.about;

import static com.delfin.jarexp.settings.Settings.DLG_DIM;
import static com.delfin.jarexp.settings.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.Cmd;
import com.delfin.jarexp.utils.Cmd.Result;
import com.delfin.jarexp.utils.FileUtils;

public class ProcessesDlg extends JFrame {

	private static final long serialVersionUID = -1830466068946227746L;
	private static final String EOL;
	static {
		if (Version.JAVA_MAJOR_VER > 6) {
			EOL = System.lineSeparator();
		} else {
			EOL = System.getProperty("line.separator");
		}
	}
	
	private JTable tProcesses = new JTable();
	private JPanel panel = new JPanel();
	private JScrollPane spResult = new JScrollPane(panel);
	private JButton btnResultToFile = new JButton();
	private JButton btnResultToClipboard = new JButton();

	public ProcessesDlg(Component parent) {
		super();
		setTitle("Process command lines");
		setIconImage(Resources.getInstance().getProcessesImage());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setSize(DLG_DIM);
		setPreferredSize(DLG_DIM);

		initComponents();
		alignComponents();

		Msg.centerDlg(this, DLG_DIM);

		setVisible(true);
		pack();
	}

	private void alignComponents() {
		Insets insets = new Insets(0, 0, 0, 0);

		panel.setLayout(new GridBagLayout());

		panel.add(btnResultToFile, new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(5, 5, 5, 25), 0, 0));
		panel.add(btnResultToClipboard, new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(5, 0, 5, 5), 0, 0));

		panel.add(tProcesses.getTableHeader(), new GridBagConstraints(0, 1, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
		panel.add(tProcesses, new GridBagConstraints(0, 2, 1, 1, 0, 0, NORTH, BOTH, insets, 0, 0));

		setLayout(new GridBagLayout());
		add(spResult, new GridBagConstraints(0, 0, 0, 0, 1, 1, NORTH, BOTH, insets, 0, 0));
	}

	private void initComponents() {
		Font font = new Font(DLG_TEXT_FONT.getName(), DLG_TEXT_FONT.getStyle(), 12);
		tProcesses.setFont(font);

		btnResultToClipboard.setFont(DLG_TEXT_FONT);
		btnResultToClipboard.setBorder(Settings.EMPTY_BORDER);
		btnResultToClipboard.setIcon(Resources.getInstance().getCopyIcon());
		btnResultToClipboard.setToolTipText("Copy command lines to clipboard");
		btnResultToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(ProcessesDlg.this.toString()), null);
			}
		});

		btnResultToFile.setFont(DLG_TEXT_FONT);
		btnResultToFile.setBorder(Settings.EMPTY_BORDER);
		btnResultToFile.setIcon(Resources.getInstance().getFloppyIcon());
		btnResultToFile.setToolTipText("Save command lines to file");
		btnResultToFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select file to save command lines");
				List<File> dirs = ActionHistory.getLastDirSelected();
				if (!dirs.isEmpty()) {
					chooser.setCurrentDirectory(dirs.get(0));
				}
				if (chooser.showOpenDialog(ProcessesDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (f.exists()) {
						showMessageDialog(ProcessesDlg.this, "Specified file exists.", "Wrong input", ERROR_MESSAGE);
						return;
					}
					if (f.isDirectory()) {
						showMessageDialog(ProcessesDlg.this, "Specified file is a directory.", "Wrong input",
								ERROR_MESSAGE);
					} else {
						ActionHistory.addLastDirSelected(f);
						try {
							FileUtils.toFile(f, ProcessesDlg.this.toString());
						} catch (IOException ex) {
							Msg.showException("Could not dump comand lines into the file " + f, ex);
						}
					}
				}
			}
		});

		initComandLines();
	}

	private void initComandLines() {
		StringBuilder scriptContent = new StringBuilder();
		scriptContent.append("set svc=getobject(\"winmgmts:root\\cimv2\")").append(EOL);
		scriptContent.append("set cproc=svc.execquery(\"select * from win32_process\")").append(EOL);
		scriptContent.append("For Each ln in cproc").append(EOL);
		scriptContent.append("WScript.echo(ln.ProcessId & \";\" & ln.Name & \";\" & ln.CommandLine)").append(EOL);
		scriptContent.append("Next").append(EOL);
		scriptContent.append("set cproc=nothing : set svc=nothing").append(EOL);

		try {
			File vbs = File.createTempFile("jarexp-", ".vbs");
			vbs.deleteOnExit();
			FileUtils.toFile(vbs, scriptContent.toString());
			Result res = Cmd.run(new String[] { "cscript", vbs.getAbsolutePath() }, null);
			FileUtils.delete(vbs);
			Collection<Process> processes = new ArrayList<Process>();
			Scanner scanner = new Scanner(res.out);
			while (scanner.hasNextLine()) {
				Process p = Process.valueOf(scanner.nextLine());
				if (p != null) {
					processes.add(p);
				}
			}
			scanner.close();
			final ProcessesTableModel model = new ProcessesTableModel(processes);
			tProcesses.setModel(model);
			tProcesses.setAutoCreateRowSorter(true);
			tProcesses.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (!SwingUtilities.isRightMouseButton(e)) {
						return;
					}
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();
					final int row = table.rowAtPoint(p);
					int col = table.columnAtPoint(p);

					// The autoscroller can generate drag events outside the Table's range.
					if (col == -1 || row == -1) {
						return;
					}
					table.setRowSelectionInterval(row, row);

					JPopupMenu popup = new JPopupMenu();
					JMenuItem item = new JMenuItem("Copy");
					item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(new StringSelection(model.getRowAt(row)), null);

						}
					});
					item.setIcon(Resources.getInstance().getCopyIcon());
					popup.add(item);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});

		} catch (Exception e) {
			Msg.showException("Unable to get processes", e);
		}
	}

	@Override
	public String toString() {
		return tProcesses.getModel().toString();
	}

	public static void main(String[] args) {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}

		new ProcessesDlg(null);
	}

	private static class Process {

		Integer pid;
		String name;
		String cmd;

		private Process(Integer pid, String name, String cmd) {
			this.pid = pid;
			this.name = name;
			this.cmd = cmd;
		}

		static Process valueOf(String row) {
			String[] splitted = row.split(";");
			if (splitted == null || splitted.length < 2) {
				return null;
			}
			if (splitted.length == 2) {
				return new Process(Integer.valueOf(splitted[0]), splitted[1], null);
			} else {
				return new Process(Integer.valueOf(splitted[0]), splitted[1], splitted[2]);
			}
		}

	}

	private static class ProcessesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 6872008441176314027L;

		private static final String[] columnNames = { "Pid", "Name", "Command line" };

		private final Object[][] data;

		ProcessesTableModel(Collection<Process> processes) {
			data = new Object[processes.size()][columnNames.length];
			int i = 0;
			for (Process proc : processes) {
				data[i][0] = proc.pid;
				data[i][1] = proc.name;
				data[i][2] = proc.cmd;
				++i;
			}
		}

		public String getRowAt(int row) {
			return data[row][0] + ";" + data[row][1] + ";" + data[row][2];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == 0 ? Integer.class : String.class;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}

		@Override
		public String toString() {
			StringBuilder out = new StringBuilder();
			for (int i = 0; i < data.length; ++i) {
				out.append(data[i][0] + ";" + data[i][1] + ";" + data[i][2]).append(EOL);
			}
			return out.toString();
		}

	}

}
