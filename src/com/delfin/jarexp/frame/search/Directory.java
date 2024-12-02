package com.delfin.jarexp.frame.search;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.Jar;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.FileUtils.BypassCfg;

public abstract class Directory extends Jar {

	public Directory(File file) {
		super(file);
	}

	@Override
	protected void process(JarEntry entry) throws IOException {

	}

	@Override
	public void bypass() {
		this.bypass((JarBypassErrorAction) null);
	}

	@Override
	public void bypass(final JarBypassErrorAction errorAction) {
        try {
            FileUtils.bypass(new BypassCfg(file) {
                @Override
                protected void visitFile(File file) throws IOException {
                    process(file);
                }
                @Override
                protected void handleError(Exception error) {
                    if (errorAction != null) {
                        errorAction.apply(error);
                    }
                }
            });
        } catch (Exception e) {
            if (errorAction == null) {
                throw new JarexpException("An error occurred while bypassing directory " + file, e);
            }
            RuntimeException toThrow = errorAction.apply(e);
            if (toThrow != null) {
                throw new RuntimeException(toThrow);
            }
        }
	}

	protected abstract void process(File file) throws IOException;

}
