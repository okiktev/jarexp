package com.delfin.jarexp.frame;

import java.util.logging.Level;
import java.util.logging.Logger;

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
		String msg = "Unhandled error occurred";
		log.log(Level.SEVERE, msg, e);
		throw new JarexpException(msg, e);
	}

	void execute() {
		worker.execute();
	}
}
