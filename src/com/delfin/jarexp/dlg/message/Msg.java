package com.delfin.jarexp.dlg.message;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.delfin.jarexp.exception.JarexpDecompilerException;

public class Msg {

	static Font defaultFont = UIManager.getDefaults().getFont("Label.font");

	public static void centerDlg(Window win, Dimension dim) {
		centerDlg(win, dim, "Jar Explorer");
	}

	public static void centerDlg(Window win, Dimension dim, String windowTitle) {
		for (Window window : JDialog.getWindows()) {
			if (window instanceof JFrame) {
				JFrame frame = (JFrame) window;
				if (frame.isActive() && frame.getTitle().startsWith(windowTitle)) {
					Rectangle rec = frame.getBounds();
					int x = (frame.getWidth() - dim.width) / 2;
					int y = (frame.getHeight() - dim.height) / 2;
					win.setBounds(rec.x + x, rec.y + y, dim.width, dim.height);
					break;
				}
			}
		}
	}

	public static void showException(String errMsg, Throwable e) {
		new Exception(errMsg, e);
	}

	public static int showList(String title, String msg, java.util.List<File> files) {
		return new List(title, msg, files).getCode();
	}

	public static void showException(Throwable e) {
		if (e instanceof JarexpDecompilerException) {
			new DecompilerException("An error occurred while decompiling class. Press the button \"View Error\" to see details.", e);
		} else {
			new Exception("An unexpected error occurred. Press the button to see details.", e);
		}		
	}

}
