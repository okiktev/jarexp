package com.delfin.jarexp.utils;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

public class FolderTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -3854438137640730745L;

	private static final Color HIGHLIGHTED = new Color(242, 241, 227);
	private static final Border BORDER = BorderFactory.createLineBorder(Color.BLACK);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (isSelected) {
			if (hasFocus) {
				FolderTableCellRenderer renderer = (FolderTableCellRenderer) component;
				renderer.setBorder(BORDER);
				return renderer;
			}
			return component;
		}
		component.setBackground(row % 2 == 1 ? HIGHLIGHTED : Color.WHITE);
		return component;
	}
}