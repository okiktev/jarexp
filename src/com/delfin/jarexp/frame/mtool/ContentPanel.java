package com.delfin.jarexp.frame.mtool;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JViewport;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;

/**
 * This class to fix a known <a href="https://bugs.openjdk.java.net/browse/JDK-6233456">issue</a>
 * Specified workaround doesn't work properly. Wrapping class works better.
 * @author delfin
 */
class ContentPanel extends JPanel {

	private static final long serialVersionUID = -7328299051353850707L;

	JTabbedContentPane tabbedPane;

	ContentPanel(Component content, RepoTree repoTree) {
		super(new BorderLayout());
		setBorder(Settings.EMPTY_BORDER);
		add(content);
		tabbedPane = new JTabbedContentPane(repoTree, this);
	}

	void addContent(JComponent content, RepoNode node, StatusBar statusBar, boolean isDirectory) {
		addContent(new TabComponent(content, node, statusBar, isDirectory));
	}

	void addContent(JComponent content, RepoNode node, StatusBar statusBar) {
		addContent(new TabComponent(content, node, statusBar, false));
	}

	private void addContent(TabComponent content) {
		tabbedPane.add(content);
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

	static abstract class PanelContainer {
		PanelContainer(JFrame frame) {
			for (Component comp : ((MtoolPanel) frame.getContentPane()).getComponents()) {
				if (comp instanceof JSplitPane) {
					receive((ContentPanel)((JSplitPane) comp).getRightComponent());
					break;
				}
			}
		}
		protected abstract void receive(ContentPanel contentPanel);
	}

	class TabComponent {

		boolean isDirectory;
		String fullPath;
		String name;
		String filesCount;

		boolean isEdited;
		RepoNode node;
		Component content;
		boolean isFiltered;

		private TabComponent() {
			
		}

		private TabComponent(Component content, RepoNode node, StatusBar statusBar, boolean isDirectory) {
			this.content = content;
			this.fullPath = node.file.getAbsolutePath();
			this.filesCount = statusBar.getChildren();
			this.name = node.file.getName();
			this.node = node;
			this.isDirectory = isDirectory;
		}

		void overwrite(TabComponent by) {
			isDirectory = by.isDirectory;
			content = by.content;
			fullPath = by.fullPath;
			name = by.name;
			filesCount = by.filesCount;
			node = by.node;
			isFiltered = by.isFiltered;
			isEdited = by.isEdited;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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