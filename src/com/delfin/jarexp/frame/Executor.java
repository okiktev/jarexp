package com.delfin.jarexp.frame;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.delfin.jarexp.JarexpException;

abstract class Executor {

	private static final Logger log = Logger.getLogger(Executor.class.getCanonicalName());

	private SwingWorker<Void, Void> worker;

	Executor() {
		worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				perform();
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
				} catch (Exception e) {
					doCatch(e);
				} finally {
					doFinally();
				}
			}
		};
	}

	protected abstract void perform();

	protected void doFinally() {
	}

	protected void doCatch(Exception e) {
		Throwable exception = e.getCause();
		Throwable cause = exception.getCause();
		if (cause instanceof ZipException && "error in opening zip file".equals(cause.getMessage())) {
			JOptionPane.showConfirmDialog(null, "Unknown archive format", "Error", JOptionPane.DEFAULT_OPTION,
			        JOptionPane.ERROR_MESSAGE);
		} else {
			ErrorDlg.showException("Error", exception);
		}
		String msg = "Unhandled error occurred";
		log.log(Level.SEVERE, msg, e);
		throw new JarexpException(msg, e);
	}

	void execute() {
		worker.execute();
	}

}
