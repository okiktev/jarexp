package com.delfin.jarexp.utils;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import static javax.swing.JOptionPane.*;
import javax.swing.SwingWorker;

import com.delfin.jarexp.dlg.message.Msg;
import com.delfin.jarexp.exception.JarexpException;

public abstract class Executor {

	private static final Logger log = Logger.getLogger(Executor.class.getCanonicalName());

	private SwingWorker<Void, Void> worker;

	public Executor() {
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
					worker.cancel(false);
				} catch (ExecutionException t) {
					doCatch(t);
				} catch (InterruptedException e) {
					log.log(Level.SEVERE, "Thread was interrupted.", e);
					Thread.currentThread().interrupt();
				} finally {
					doFinally();
				}
			}
		};
	}

	protected abstract void perform();

	protected void doFinally() {
	}

	protected void doCatch(Throwable e) {
		Throwable exception = e.getCause();
		Throwable cause = exception.getCause();
		if (cause instanceof ZipException && "error in opening zip file".equals(cause.getMessage())) {
			showConfirmDialog(null, "Unknown archive format", "Error", DEFAULT_OPTION, ERROR_MESSAGE);
		} else {
			Msg.showException(exception);
		}
		String msg = "Unhandled error occurred";
		log.log(Level.SEVERE, msg, e);
		throw new JarexpException(msg, e);
	}

	public void execute() {
		worker.execute();
	}

}
