package com.delfin.jarexp.frame.resources;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.delfin.jarexp.Settings;

import static com.delfin.jarexp.Settings.*;

/**
 * For fixing nasty Swing bugs <a href=
 * "https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8149453">8149453</a>
 * and <a href=
 * "https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8151385">8151385</a><br/>
 * <a href=
 * "https://stackoverflow.com/questions/33926645/joptionpane-icon-gets-cropped-in-windows-10">See
 * more.</a>
 * 
 * @author Oleh_Kiktiev
 *
 */
public class CropIconsBugResolver {

	private static final Logger log = Logger.getLogger(CropIconsBugResolver.class.getCanonicalName());

	private static final String SUN_AWT_SHELL_WIN32_SHELL_FOLDER2 = "sun.awt.shell.Win32ShellFolder2";

	private static final CropIconsBugResolver instance = new CropIconsBugResolver();

	private static final boolean isApplied = IS_WINDOWS && JAVA_MAJOR_VER < 9
			&& BigDecimal.valueOf(Double.parseDouble(Settings.OS_VER)).compareTo(BigDecimal.valueOf(6.1)) > 0;

	private static final String[][] optionIcons = { { "OptionPane.errorIcon", "65585" },
			{ "OptionPane.warningIcon", "65581" }, { "OptionPane.questionIcon", "65583" },
			{ "OptionPane.informationIcon", "65587" } };

	private Method getIconBits;

	private Method getIcon;

	private CropIconsBugResolver() {

	}

	public static CropIconsBugResolver getInstance() {
		return instance;
	}

	public void adaptTree(DefaultTreeCellRenderer treeCellRenderer, JTree tree) {
		if (!isApplied) {
			return;
		}
		treeCellRenderer.setOpenIcon(scale(UIManager.getIcon("Tree.openIcon"), tree));
		treeCellRenderer.setClosedIcon(scale(UIManager.getIcon("Tree.closedIcon"), tree));
		treeCellRenderer.setLeafIcon(scale(UIManager.getIcon("Tree.leafIcon"), tree));

		Collection<Integer> iconSizes = Arrays.asList(treeCellRenderer.getOpenIcon().getIconHeight(),
				treeCellRenderer.getClosedIcon().getIconHeight(), treeCellRenderer.getLeafIcon().getIconHeight());

		Font currentFont = tree.getFont();
		Point2D p = new Point2D.Float(0, currentFont.getSize2D());
		FontRenderContext context = treeCellRenderer.getFontMetrics(currentFont).getFontRenderContext();
		context.getTransform().transform(p, p);

		tree.setRowHeight(Math.max((int) Math.ceil(p.getY()), Collections.max(iconSizes) + 2));
	}

	public void fixOptionIcons() {
		if (!isApplied) {
			return;
		}
		try {
			int iconSize = calculateIconSize(32);
			for (String[] pair : optionIcons) {
				String iconName = pair[0];
				if (UIManager.get(iconName) instanceof ImageIcon) {
					int[] iconBits = getIconBits(Long.parseLong(pair[1]), iconSize);
					if (iconBits != null) {
						UIManager.put(iconName, makeIcon(iconBits, iconSize));
					}
				}
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not fix option icons", e);
		}
	}

	Icon getIcon(File file) {
		if (!isApplied) {
			return null;
		}
		try {
			int iconSize = calculateIconSize(16);
			long hIcon = getIcon(file.getAbsolutePath(), false);
			int[] iconBits = getIconBits(hIcon, iconSize);
			if (iconBits != null) {
				return makeIcon(iconBits, iconSize);
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not fix icon for file " + file.getName(), e);
		}
		return null;
	}

	private static Icon makeIcon(int[] bits, int size) {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		img.setRGB(0, 0, size, size, bits, 0, size);
		return new ImageIcon(img);
	}

	private static int calculateIconSize(int dim) {
		double dpiScalingFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
		switch (dim) {
		case 32:
			return (dpiScalingFactor == 1) ? (dim)
					: ((dpiScalingFactor == 1.25) ? (40)
							: ((dpiScalingFactor == 1.5) ? (45) : ((int) (dim * dpiScalingFactor))));
		case 16:
			return (dpiScalingFactor == 1) ? (dim)
					: ((dpiScalingFactor == 1.25) ? (22)
							: ((dpiScalingFactor == 1.5) ? (25) : ((int) (dim * dpiScalingFactor))));
		}
		return (int) (dim * dpiScalingFactor);
	}

	private int[] getIconBits(long hIcon, int iconSize) throws SecurityException, NoSuchMethodException,
			ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (getIconBits == null) {
			getIconBits = Class.forName(SUN_AWT_SHELL_WIN32_SHELL_FOLDER2).getDeclaredMethod("getIconBits",
					new Class[] { long.class, int.class });
			getIconBits.setAccessible(true);
		}
		return (int[]) getIconBits.invoke(null, hIcon, iconSize);
	}

	private long getIcon(String absolutePath, boolean getLargeIcon)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
			NoSuchMethodException, ClassNotFoundException {
		if (getIcon == null) {
			getIcon = Class.forName(SUN_AWT_SHELL_WIN32_SHELL_FOLDER2).getDeclaredMethod("getIcon",
					new Class[] { String.class, boolean.class });
			getIcon.setAccessible(true);
		}
		return (Long) getIcon.invoke(null, absolutePath, getLargeIcon);
	}

	private static Icon scale(Icon icon, JTree tree) {
		double scaleFactor = 1.2;

		int width = icon.getIconWidth();
		int height = icon.getIconHeight();

		width = (int) Math.ceil(width * scaleFactor);
		height = (int) Math.ceil(height * scaleFactor);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.scale(scaleFactor, scaleFactor);
		icon.paintIcon(tree, g, 0, 0);
		g.dispose();

		return new ImageIcon(image);
	}

}
