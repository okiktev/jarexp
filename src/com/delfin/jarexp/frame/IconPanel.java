package com.delfin.jarexp.frame;

import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.ActionHistory;

import static javax.imageio.ImageIO.write;
import static java.lang.System.currentTimeMillis;


class IconPanel extends JPanel {

	private static final long serialVersionUID = -2489712229277827816L;

	private Image image;

	private static Map<String, Dimension> dimensions = new ConcurrentHashMap<String, Dimension>();

	IconPanel(final Image image) {
		this.image = image;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) {
					return;
				}
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem dump = new JMenuItem("Dump image");
				dump.setIcon(Resources.getInstance().getFloppyIcon());
				dump.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
				        JFileChooser chooser = new JFileChooser();
				        chooser.setDialogTitle("Select directory for dumping");
				        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						List<File> dirs = ActionHistory.getLastDirSelected();
						if (!dirs.isEmpty()) {
							chooser.setCurrentDirectory(dirs.get(0));
						}
				        if (chooser.showOpenDialog(IconPanel.this) != JFileChooser.APPROVE_OPTION) {
				        	return;
				        }
			            File dir = chooser.getSelectedFile();
			            if (!dir.exists()) {
			                errorDlg("File " + dir + " is not exist");
			                return;
			            }
			            if (!dir.isDirectory()) {
			                errorDlg("File " + dir + " is not a folder");
			                return;
			            }
			            ActionHistory.addLastDirSelected(dir);
			            try {
			                write((RenderedImage) image, "png", new File(dir, "ico_" + currentTimeMillis() + ".png"));
			            } catch (IOException ex) {
			            	errorDlg("Unable to dump image. Cause: " + ex.getMessage());
			            }
					}
				});
				popupMenu.add(dump);
				popupMenu.show(IconPanel.this, e.getX(), e.getY());
			}
			private void errorDlg(String msg) {
				showConfirmDialog(IconPanel.this, msg, "Error", DEFAULT_OPTION, ERROR_MESSAGE);
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);

		Dimension dim = resize(g, w, h);
		Image img = createImage(dim.width, dim.height);
		Graphics g2 = img.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.drawString(w + "x" + h, 0, h + 16);
		g.drawImage(img, 0, 0, this);
		g.dispose();
	}

	private Dimension resize(Graphics g, int w, int h) {
		String key = w + "x" + h;
		Dimension d = dimensions.get(key);
		if (d == null) {
			Graphics2D g2 = (Graphics2D) g;
			GlyphVector gv = g2.getFont().createGlyphVector(g2.getFontRenderContext(), key);
			Rectangle r = gv.getPixelBounds(null, 0, h + 16);
			d = new Dimension(r.width > w ? r.width + 5 : w, h + 16);
			dimensions.put(key, d);
		}
		setPreferredSize(d);
		setMinimumSize(d);
		setSize(d);
		return d;
	}

}
