package com.delfin.jarexp.frame.resources;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;

class ClassFileIcon {

	private static final Logger log = Logger.getLogger(ClassFileIcon.class.getCanonicalName());

	private static Icon defaultFileIcon;
	static {
		File file = new File(Settings.getJarexpTmpDir(), "jarexp" + System.currentTimeMillis());
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new JarexpException("Couldn't create new file " + file, e);
		}
		file.deleteOnExit();
		defaultFileIcon = CropIconsBugResolver.getInstance().getIcon(file);
		if (defaultFileIcon == null) {
			defaultFileIcon = FileSystemView.getFileSystemView().getSystemIcon(file);
		}
		file.delete();
	}

	static Icon defineIcon(Icon icon) {
		BufferedImage dimg;
		BufferedImage iimg;
		if (Version.JAVA_MAJOR_VER > 8) {
			dimg = convertToBufferedImage(((ImageIcon) defaultFileIcon).getImage());
			iimg = convertToBufferedImage(((ImageIcon) icon).getImage());
		} else {
			dimg = (BufferedImage) ((ImageIcon) defaultFileIcon).getImage();
			iimg = (BufferedImage) ((ImageIcon) icon).getImage();
		}
		try {
			byte[] dbyte = imageToByteArray(dimg);
			byte[] ibyte = imageToByteArray(iimg);
			if (dbyte.length == ibyte.length) {
				for (int i = 0; i < ibyte.length; ++i) {
					if (dbyte[i] != ibyte[i]) {
						break;
					}
					if (i == ibyte.length - 1) {
						return Resources.getIcon("r_clssf.png",
								"Unable to load java item class file icon from class path");
					}
				}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Something happened while defining class file icon.", e);
		}
		return icon;
	}

	public static BufferedImage convertToBufferedImage(Image img) {
		BufferedImage res = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = res.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return res;
	}

	private static byte[] imageToByteArray(RenderedImage img) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);
		return baos.toByteArray();
	}

}
