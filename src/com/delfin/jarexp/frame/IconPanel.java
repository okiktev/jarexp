package com.delfin.jarexp.frame;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

class IconPanel extends JPanel {

	private static final long serialVersionUID = -2489712229277827816L;

	private Image image;

	private static Map<String, Dimension> dimensions = new ConcurrentHashMap<String, Dimension>();

	IconPanel(Image image) {
		this.image = image;
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
