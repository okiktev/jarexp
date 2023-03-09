package com.delfin.jarexp.frame.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.table.TableModel;

class OnFindBtnClickListener implements ActionListener {

	private static final TableModel EMPTY_TABLE_DATA = new FileSearchResultTableModel();

	private SearchDlg dlg;

	OnFindBtnClickListener(SearchDlg dlg) {
		this.dlg = dlg;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dlg.tResult.setModel(EMPTY_TABLE_DATA);

		if (dlg.isFindClass == null) {
			new IconSearcher().search(dlg);
		} else if (dlg.isFindClass) {
			new FileSearcher().search(dlg);
		} else {
			new FileContentSearcher().search(dlg);
		}
	}

}
