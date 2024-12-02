package com.delfin.jarexp.frame.search;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.settings.Version;
import com.delfin.jarexp.utils.FileUtils;
import com.delfin.jarexp.utils.FileUtils.BypassCfg;
import com.delfin.jarexp.utils.Utils;
import com.delfin.jarexp.utils.Zip;
import com.delfin.jarexp.utils.Zip.BypassAction;
import com.delfin.jarexp.utils.Zip.BypassRecursivelyAction;

import static java.util.concurrent.TimeUnit.SECONDS;


class Progress {

    private static final boolean isJava6 = Version.JAVA_MAJOR_VER == 6;

    File dir;
    private SearchDlg dlg;
    private boolean isInAll;
    private boolean isDone;
    private Thread counter;
    private Thread archiveBypasser;
    private volatile LinkedBlockingQueue<File> files;
    private AtomicLong overallCount = new AtomicLong(0);
    private volatile int processedCount;
    private ExecutorService exec = Executors.newFixedThreadPool(Settings.SEARCH_PROGRESS_THREADS_NUMBER);

    Progress(SearchDlg dlg) {
        this.dlg = dlg;
        isInAll = dlg.cbInAllSubArchives.isSelected();
    }

    void run() {
        if (!dlg.cbProgressBar.isSelected()) {
            return;
        }
        ProgressRunner progressRunner = new ProgressRunner();
        if (isJava6 || dlg.cbToDisk.isSelected()) {
            new Thread(progressRunner).start();
        } else {
            counter = new Thread(new Runnable() {
                @Override
                public void run() {
                    String dots = ".";
                    long lastCheck = updateUI(dots);
                    while (!isDone || files != null) {
                        if (currentTimeMillis() - lastCheck < 1000) {
                            Utils.sleep(200);
                            continue;
                        }
                        if (dots.length() == 3) {
                            dots = "";
                        }
                        dots += '.';
                        lastCheck = updateUI(dots);
                    }
                }
                private long updateUI(String dots) {
                    dlg.lbResult.setText("Found entries to search: " + overallCount.get() + dots);
                    return currentTimeMillis();
                }
            });
            counter.start();
            progressRunner.run();
        }
    }

    void increaseProcessed() {
        dlg.refreshProgress(++processedCount, overallCount.intValue());
    }

    private class ProgressRunner implements Runnable {
        @Override
        public void run() {
            if (isDone && !isJava6) {
                return;
            }
            dlg.progressCalculate(false);
            if (!isJava6 && isInAll) {
                Runnable archivesCounter = new Runnable() {
                    @Override
                    public void run() {
                        while (!files.isEmpty() || !isDone) {
                            try {
                                File arch = files.poll(1, SECONDS);
                                if (arch == null) {
                                    continue;
                                }
                                exec.submit(createArchiveBypasser(arch));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        exec.shutdown();
                        while (!exec.isTerminated()) {
                            Utils.sleep(100);
                        }
                        files = null;
                    }
                };
                files = new LinkedBlockingQueue<File>();
                archiveBypasser = new Thread(archivesCounter);
                archiveBypasser.start();
            }
            if (Zip.isArchive(dir)) {
                countInsideArchives(dir, overallCount);
                isDone = true;
            } else {
                FileUtils.bypass(new BypassCfg(dir) {
                    @Override
                    protected void visitFile(File file) throws IOException {
                        if (isInAll && Zip.isArchive(file)) {
                            try {
                                files.offer(file, 1, SECONDS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        overallCount.addAndGet(1);
                    }
                });
                isDone = true;
                while (files != null) {
                    Utils.sleep(100);
                }
            }
            dlg.progressCalculate(true);
        }
    }

    public void interrupt() {
        if (counter != null) {
            counter.interrupt();
        }
        if (archiveBypasser != null) {
            archiveBypasser.interrupt();
        }
    }

    private Callable<Void> createArchiveBypasser(final File archFile) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                AtomicLong count = new AtomicLong(0);
                countInsideArchives(archFile, count);
                overallCount.addAndGet(count.get());
                return null;
            }
        };
    }

    private void countInsideArchives(File archFile, final AtomicLong count) {
        Zip.bypass(archFile, new BypassAction() {
            @Override
            public void process(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
                if (Zip.isArchive(zipEntry.getName()) && isInAll) {
                    InputStream stream = zipFile.getInputStream(zipEntry);
                    Zip.bypass(new ZipInputStream(stream), null, new BypassRecursivelyAction () {
                        @Override
                        public void process(ZipEntry zipEntry, String fullPath) throws IOException {
                            count.addAndGet(1);
                        }
                        @Override
                        public void error(ZipEntry zipEntry, Exception error) {
                            error.printStackTrace();
                        }
                    });
                }
                count.addAndGet(1);
            }
        });
    }

}
