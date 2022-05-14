package com.delfin.jarexp.frame;

import static com.delfin.jarexp.settings.Settings.DLG_TEXT_FONT;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.NORTHEAST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.tree.TreePath;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.ActionHistory;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.Md5Checksum;
import com.delfin.jarexp.utils.Sha1Checksum;

public class HashSumsDlg extends JDialog {

	private static final long serialVersionUID = -3729263464501392910L;
	private JTextArea taResult = new JTextArea();
	private JPanel panel = new JPanel();
	private JScrollPane spResult = new JScrollPane(panel);
	private final TreePath[] paths;
	private JButton btnResultToFile = new JButton();
	private JButton btnResultToClipboard = new JButton();

	HashSumsDlg(TreePath[] paths) {
		super((JDialog) null);

		this.paths = paths;

		try {
			initComponents();
		} catch (IOException e) {
			Msg.showException("An error occurred while calculating hash sums.", e);
		}
		alignComponents();

		setTitle("Hash sums | " + getComaSeparatedFullPaths(paths));
		setIconImage(Resources.getInstance().getSumsImage());
		Dimension DLG_DIM = new Dimension(410, 150);
		setPreferredSize(DLG_DIM);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		Msg.centerDlg(this, DLG_DIM.width, DLG_DIM.height);

		setVisible(true);
		pack();
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(taResult.getText()).append('\n');
		return out.toString();
	}

	static String getComaSeparatedFullPaths(TreePath[] paths) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < paths.length; ++i) {
			String fullPath = ((JarNode) paths[i].getLastPathComponent()).getFullPath();
			result.append(fullPath);
			if (i != paths.length - 1) {
				result.append(',');
			}
		}
		return result.toString();
	}

	private void alignComponents() {
		Insets insets = new Insets(0, 0, 0, 0);

		panel.setLayout(new GridBagLayout());
		panel.add(taResult,             new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));
		panel.add(btnResultToFile,      new GridBagConstraints(0, 0, 1, 1, 0, 0, NORTHEAST, NONE, new Insets(5, 5, 5, 25), 0, 0));
		panel.add(btnResultToClipboard, new GridBagConstraints(0, 0, 1, 1, 0, 0, NORTHEAST, NONE, new Insets(5, 0, 5, 5), 0, 0));

		setLayout(new GridBagLayout());
		add(spResult, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, insets, 0, 0));

		panel.setComponentZOrder(btnResultToClipboard, 0);
		panel.setComponentZOrder(btnResultToFile, 0);
	}

	protected void initComponents() throws IOException {
		Font font = new Font(DLG_TEXT_FONT.getName(), DLG_TEXT_FONT.getStyle(), 12);
		taResult.setFont(font);

		taResult.setEditable(false);

		if (paths.length == 1) {
			JarNode node = (JarNode) paths[0].getLastPathComponent();
			if (node.path.isEmpty()) {
				taResult.append("SHA-1: " + Sha1Checksum.get(node.origArch) + '\n');
				taResult.append("MD5: " + Md5Checksum.get(node.origArch) + '\n');
			} else {
				taResult.append("SHA-1: " + Sha1Checksum.get(node.origArch, node.path) + '\n');
				taResult.append("MD5: " + Md5Checksum.get(node.origArch, node.path) + '\n');
			}
		}

		btnResultToClipboard.setFont(DLG_TEXT_FONT);
		btnResultToClipboard.setBorder(Settings.EMPTY_BORDER);
		btnResultToClipboard.setIcon(Resources.getInstance().getCopyIcon());
		btnResultToClipboard.setToolTipText("Copy hash sums to clipboard");
		btnResultToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(new StringSelection(HashSumsDlg.this.toString()), null);			
			}
		});

		btnResultToFile.setFont(DLG_TEXT_FONT);
		btnResultToFile.setBorder(Settings.EMPTY_BORDER);
		btnResultToFile.setIcon(Resources.getInstance().getFloppyIcon());
		btnResultToFile.setToolTipText("Save file info on disk");
		btnResultToFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select file to save hash sums");
				List<File> dirs = ActionHistory.getLastDirSelected();
				if (!dirs.isEmpty()) {
					chooser.setCurrentDirectory(dirs.get(0));
				}
				if (chooser.showOpenDialog(HashSumsDlg.this) == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					if (f.exists()) {
						showMessageDialog(HashSumsDlg.this, "Specified file exists.", "Wrong input", ERROR_MESSAGE);
						return;
					}
					if (f.isDirectory()) {
						showMessageDialog(HashSumsDlg.this, "Specified file is a directory.", "Wrong input", ERROR_MESSAGE);
					} else {
						ActionHistory.addLastDirSelected(f);
						try {
							FileUtils.toFile(f, HashSumsDlg.this.toString());
						} catch (IOException ex) {
							Msg.showException("Could not dump hash sums into the file " + f, ex);
						}
					}
				}
			}
		});
	}

	public static void main(String[] args) {
		String sysLookFeel = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLookFeel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		new HashSumsDlg(new TreePath[] {});
	}

}
