package com.delfin.jarexp.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.jar.Manifest;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.FileUtils;

/**
 * This class to fix a known <a href="https://bugs.openjdk.java.net/browse/JDK-6233456">issue</a>
 * Specified workaround doesn't work properly. Wrapping class works better.
 * @author delfin
 */
class ContentPanel extends JPanel {

	private static final long serialVersionUID = -7328299051353850707L;

	private JTabbedContentPane tabbedPane;

	private JarTree jarTree;

	ContentPanel(Component content, JarTree jarTree) {
		super(new BorderLayout());
		setBorder(Settings.EMPTY_BORDER);
		add(content);
		tabbedPane = new JTabbedContentPane(this.jarTree = jarTree, this);
	}

	void addContent(JComponent content, JarNode node, StatusBar statusBar, boolean isDirectory) {
		addContent(new TabComponent(content, node, statusBar, isDirectory));
	}

	void addContent(JComponent content, JarNode node, StatusBar statusBar) {
		addContent(new TabComponent(content, node, statusBar, false));
	}

	private void addContent(TabComponent content) {
		tabbedPane.add(content);
	}

	void showFilterPanel(FilterPanel filter) {
		tabbedPane.addFilterToSelectedTab(filter);
	}

	void removeFilter() {
		tabbedPane.removeFilterFromSelectedTab();
	}

	RSyntaxTextArea getSelectedComponent() {
		Component comp = tabbedPane.getSelectedComponent();
		if (comp instanceof RTextScrollPane) {
			for (Component c1 : ((RTextScrollPane)comp).getComponents()) {
				if (c1 instanceof JViewport) {
					for (Component c2 : ((JViewport)c1).getComponents()) {
						if (c2 instanceof RSyntaxTextArea) {
							return (RSyntaxTextArea) c2;
						}
					}
				}
			}
		} else {
			for (Component c : ((JPanel)comp).getComponents()) {
				if (c instanceof RTextScrollPane) {
					for (Component c1 : ((RTextScrollPane)c).getComponents()) {
						if (c1 instanceof JViewport) {
							for (Component c2 : ((JViewport)c1).getComponents()) {
								if (c2 instanceof RSyntaxTextArea) {
									return (RSyntaxTextArea) c2;
								}
							}
						}
					}
				}
			}
		}
		throw new JarexpException("Unable to find RSyntaxTextArea");
	}

	void setSelected(String fullPath) {
		tabbedPane.setSelected(fullPath, true);
	}

	TabComponent getSelectedTabComponent() {
		int indx = tabbedPane.getSelectedIndex();
		if (indx < 0) {
			return new TabComponent();
		}
		return tabbedPane.getSelectedTabComponent(tabbedPane.getToolTipTextAt(indx));
	}

	void saveChanges() {
		getSelectedTabComponent().saveChanges();
	}

	class TabComponent {

		boolean isDirectory;
		String fullPath;
		String name;
		String filesCount;
		String compiledVersion;
		DecompilerType decompilerType;

		boolean isEdited;
		JarNode node;
		Component content;
		boolean isFiltered;

		private TabComponent() {
			
		}

		private TabComponent(Component content, JarNode node, StatusBar statusBar, boolean isDirectory) {
			this.content = content;
			this.fullPath = statusBar.getPath();
			this.filesCount = statusBar.getChildren();
			this.compiledVersion = statusBar.getCompiledVersion();
			this.decompilerType = Settings.getDecompilerType();
			this.name = node.name;
			this.node = node;
			this.isDirectory = isDirectory;
		}

		void saveChanges() {
			if (!isEdited) {
				return;
			}
			try {
				jarTree.statusBar.enableProgress("Saving...");
				int reply = JOptionPane.showConfirmDialog(jarTree.frame,
						"File " + node.path + " was changed. Do you want to keep changes?",
						"Change confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (reply == JOptionPane.YES_OPTION) {
					List<JarNode> path = node.getPathList();
					JarNode root = path.get(path.size() - 1);
					File f = new File(root.name);
					if (!FileUtils.isUnlocked(f)) {
						JOptionPane.showConfirmDialog(jarTree.frame,
								"Cannot save the file " + f + " because it is being used by another process.",
								"Error saving...", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
						return;
					}
					File tmp = File.createTempFile("edit", node.name, Settings.getJarexpTmpDir());

					String content = ((RTextScrollPane) this.content).getTextArea().getText();
					if (name.toLowerCase().endsWith(".mf")) {
						try {
							InputStream is = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
							new Manifest(is).write(new FileOutputStream(tmp));
						} catch (IOException e) {
							JOptionPane.showConfirmDialog(jarTree.frame, "Failed to save manifest.\nCause: " + e.getMessage(),
									"Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
							return;
						}
					} else {
						FileUtils.toFile(tmp, content);
					}
					Jar.delete(node, false);
					Jar.pack(node, tmp);
				}
			} catch (Throwable t) {
				Msg.showException("An error occurred while saving changes in " + fullPath, t);
			} finally {
				isEdited = false;
				jarTree.statusBar.disableProgress();
			}
		}

		void overwrite(TabComponent by) {
			isDirectory = by.isDirectory;
			content = by.content;
			fullPath = by.fullPath;
			name = by.name;
			filesCount = by.filesCount;
			compiledVersion = by.compiledVersion;
			decompilerType = by.decompilerType;
			node = by.node;
			isFiltered = by.isFiltered;
			isEdited = by.isEdited;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((compiledVersion == null) ? 0 : compiledVersion.hashCode());
			result = prime * result + ((decompilerType == null) ? 0 : decompilerType.hashCode());
			result = prime * result + ((filesCount == null) ? 0 : filesCount.hashCode());
			result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
			result = prime * result + (isDirectory ? 1231 : 1237);
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TabComponent other = (TabComponent) obj;
			if (compiledVersion == null) {
				if (other.compiledVersion != null)
					return false;
			} else if (!compiledVersion.equals(other.compiledVersion))
				return false;
			if (decompilerType != other.decompilerType)
				return false;
			if (filesCount == null) {
				if (other.filesCount != null)
					return false;
			} else if (!filesCount.equals(other.filesCount))
				return false;
			if (fullPath == null) {
				if (other.fullPath != null)
					return false;
			} else if (!fullPath.equals(other.fullPath))
				return false;
			if (isDirectory != other.isDirectory)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

	}

}