package com.delfin.jarexp.utils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import com.delfin.jarexp.settings.Settings;

public class ImgPanel extends JPanel {

	private static final long serialVersionUID = -6388812194245596215L;

	private Image image;

	public ImgPanel(Image image) {
		setImage(image);
		setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
		setBorder(Settings.EMPTY_BORDER);
	}

	public void setImage(Image image) {
		this.image = image;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = (getWidth() - image.getWidth(null)) / 2;
		int y = (getHeight() - image.getHeight(null)) / 2;
		g.drawImage(image, x, y, null);
	}

}
