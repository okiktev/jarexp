package com.delfin.jarexp.frame.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class OnFindBtnClickListener implements ActionListener {
	
	private SearchDlg dlg;

	OnFindBtnClickListener(SearchDlg dlg) {
		this.dlg = dlg;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		dlg.taResult.setText("");
		if (dlg.isFindClass) {
			new ClassFileSearcher(dlg.jarFile).search(new ClassFileSearchCriteria(dlg));
		}
	}

}
