package com.delfin.jarexp.frame;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

class ImgPanel extends JPanel {

	private static final long serialVersionUID = -8324612145574417005L;

	private Image image;

	ImgPanel(Image image) {
		this.image = image;
		setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = (this.getWidth() - image.getWidth(null)) / 2;
		int y = (this.getHeight() - image.getHeight(null)) / 2;
		g.drawImage(image, x, y, null);
	}

}
