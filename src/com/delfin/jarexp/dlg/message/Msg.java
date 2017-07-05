package com.delfin.jarexp.dlg.message;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class Msg {

	static Font defaultFont = UIManager.getDefaults().getFont("Label.font");

	static void centerDlg(Window win, int width, int height) {
		JFrame parent = null;
		for (Window window : JDialog.getWindows()) {
			if (window instanceof JFrame && ((JFrame) window).getTitle().startsWith("Jar Explorer")) {
				parent = (JFrame) window;
				break;
			}
		}
		if (parent != null) {
			Rectangle rec = parent.getBounds();
			int x = (parent.getWidth() - width) / 2;
			int y = (parent.getHeight() - height) / 2;
			win.setBounds(rec.x + x, rec.y + y, width, height);
		}
	}

	public static void showException(String errMsg, Throwable e) {
		new Exception(errMsg, e);
	}

	public static int showList(String title, String msg, java.util.List<File> files) {
		return new List(title, msg, files).getCode();
	}

}
