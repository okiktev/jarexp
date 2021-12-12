package com.delfin.jarexp.utils;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;

import com.delfin.jarexp.settings.Version;


public class TableHeaderCustomizer {

	public static void customize(JTable table) {
		if (Version.JAVA_MAJOR_VER < 8) {
			return;
		}

		table.getTableHeader().setDefaultRenderer(new sun.swing.table.DefaultTableCellHeaderRenderer() {

			private static final long serialVersionUID = -5210083732051473047L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				Font font = component.getFont();
				component.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 1));
				return component;
			};
		});

	}

}
