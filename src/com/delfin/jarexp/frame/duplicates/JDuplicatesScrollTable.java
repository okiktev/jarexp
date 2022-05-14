package com.delfin.jarexp.frame.duplicates;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneLayout;
import javax.swing.table.DefaultTableCellRenderer;

import com.delfin.jarexp.frame.search.SearchResult;
import com.delfin.jarexp.utils.TableHeaderCustomizer;

public class JDuplicatesScrollTable extends JScrollPane {

	private static final long serialVersionUID = -7929350286330872397L;

	private static final DuplicatesTableCellRenderer TABLE_CELL_RENDERER = new DuplicatesTableCellRenderer();

	private JTable table;

	private MouseListener addMouseListener;

	void render(Map<String, List<SearchResult>> searchResult) {
		DuplicatesFileSearchResultTableModel model = new DuplicatesFileSearchResultTableModel(searchResult);

		table = new JTable(model);
		if (model.isFileSizeRendered()) {
			table.setFillsViewportHeight(true);
			table.setAutoCreateRowSorter(true);
			TableHeaderCustomizer.customize(table);
		} else {
			table.setTableHeader(null);
		}
		table.addMouseListener(addMouseListener);
		table.setDefaultRenderer(Number.class, TABLE_CELL_RENDERER);
		table.setDefaultRenderer(Object.class, TABLE_CELL_RENDERER);

		setLayout(new ScrollPaneLayout.UIResource());
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setViewport(createViewport());
		setVerticalScrollBar(createVerticalScrollBar());
		setHorizontalScrollBar(createHorizontalScrollBar());
		setViewportView(table);
		updateUI();

		if (model.isFileSizeRendered() && !this.getComponentOrientation().isLeftToRight()) {
			viewport.setViewPosition(new Point(Integer.MAX_VALUE, 0));
		}
	}

	@Override
	public synchronized void addMouseListener(MouseListener addMouseListener) {
		this.addMouseListener = addMouseListener;
	}

	public SearchResult getSelectedSearchResult() {
		if (table == null) {
			return null;
		}
		int row = table.getSelectedRow();
		if (row == -1) {
			return null;
		}
		return (SearchResult) table.getModel().getValueAt(row, 0);
	}

	private static class DuplicatesTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 914300240645750561L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			SearchResult searchResult = (SearchResult) value;
			if (!isSelected) {
				int pos = searchResult.position;
				if (pos == 1) {
					component.setBackground(Color.WHITE);
				} else if (pos == 2) {
					component.setBackground(SearchResult.COLOR_CONTENT);
				}
			}
			if (column == 1 && value instanceof DuplicatesSearchResult) {
				setValue(((DuplicatesSearchResult) searchResult).size);
			} else {
				setValue(searchResult.line);
			}
			return component;
		}
	}

}
