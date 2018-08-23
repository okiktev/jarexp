package com.delfin.jarexp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.delfin.jarexp.JarexpException;
import com.delfin.jarexp.Settings;
import com.delfin.jarexp.utils.Cmd.Result;

public class FileUtils {
	
	private static abstract class PostAminExecution {

		void doWait() {
			long start = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - start > Settings.ADMIN_WAIT_TIMEOUT) {
					throw new JarexpException("Timeout of waiting after run with admin privileges was exceeded.");
				}
				if (accept()) {
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new JarexpException(e);
				}
			}
		}

		protected abstract boolean accept();
	}

	private static final Logger log = Logger.getLogger(FileUtils.class.getCanonicalName());

	private static final String EOL = Settings.EOL;

	public static String toString(File file) {
		try {
			return read(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new JarexpException("An error occurred while reading file " + file, e);
		}
	}

	public static String toString(URL url) throws IOException {
		return read(url.openStream());
	}

	public static void toFile(File file, String content) throws IOException {
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(file));
			out.print(content);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private static String read(InputStream stream) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF8"));
			StringBuilder out = new StringBuilder();
			for (String line; (line = reader.readLine()) != null;) {
				out.append(line).append('\n');
			}
			return out.toString();
		} catch (Exception e) {
			throw new JarexpException("An error occurred while reading file", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Unable to close reader of file", e);
				}
			}
		}
	}

    public static void copy(File src, File dst) {
        if (dst.isDirectory()) {
            dst = new File(dst, src.getName());
        }
        if (src.isDirectory()) {
            dst.mkdirs();
            File[] files = src.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    copy(files[i], new File(dst, files[i].getName()));
                }
            }
        } else {
            try {
                Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                if (Settings.IS_WINDOWS) {
                    if (e instanceof java.nio.file.AccessDeniedException && dst.getAbsolutePath().equals(e.getMessage())) {
                        try {
                            copyWithAdminPrivileges(src, dst);
                            return;
                        } catch (Exception ex) {
                            throw new JarexpException("An error occurred while copying file " + src + " into " + dst, ex);
                        }
                    }
                }
                throw new JarexpException("An error occurred while copying file " + src + " into " + dst, e);
            }
        }



//		FileInputStream srcStream = new FileInputStream(src);
//		FileOutputStream dstStream = new FileOutputStream(dst);
//
//		FileChannel srcChannel = srcStream.getChannel();
//		FileChannel dstChannel = dstStream.getChannel();
//		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
//
//		srcStream.close();
//		dstStream.close();
    }

    private static void copyWithAdminPrivileges(File src, File dst) throws IOException {
        File bat = File.createTempFile("jarexp-", ".bat");
        bat.deleteOnExit();
        final File copyRes = new File(bat.getParentFile(), "jarexp-copyres");
        copyRes.deleteOnExit();
        try {
            StringBuilder outBat = new StringBuilder();
            outBat.append("@echo off").append(EOL);
            outBat.append("set LOGFILE=%TEMP%\\jarexp-console-adm.txt").append(EOL);
            outBat.append("echo %date% %time% ADMIN COPY. Starting... >> %LOGFILE%").append(EOL);
            outBat.append("set COPYRES=%TEMP%\\jarexp-copyres").append(EOL);
            outBat.append("call copy /B/Y \"").append(src.getAbsolutePath()).append("\" \"").append(dst.getAbsolutePath()).append("\" > %COPYRES%").append(EOL);
            outBat.append("set /p RES=<%COPYRES%").append(EOL);
            outBat.append("echo %date% %time% ADMIN COPY. %RES% >> %LOGFILE%").append(EOL);
            outBat.append("echo %date% %time% ADMIN COPY. Done. >> %LOGFILE%").append(EOL);
            outBat.append("set LOGFILE=").append(EOL);
            outBat.append("set RES=").append(EOL);
            outBat.append("set COPYRES=").append(EOL);
            toFile(bat, outBat.toString());

            if (copyRes.exists()) {
                copyRes.delete();
            }
            runWithAdminPrivileges(bat);
            new PostAminExecution() {
                @Override
                protected boolean accept() {
                    return copyRes.exists();
                }
            }.doWait();
            if (!toString(copyRes).contains("1 file(s) copied.")) {
                throw new JarexpException("Unable copy file " + src + " with admin privileges. See the logs for details. Output:\n" + toString(copyRes));
            }
        } finally {
            bat.delete();
            copyRes.delete();
        }
    }

    public static boolean isUnlocked(File file) {
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file, "rw");
            FileChannel channel = accessFile.getChannel();
            FileLock lock = channel.lock();
            try {
                lock = channel.tryLock();
                return false;
            } catch (OverlappingFileLockException e) {
                return true;
            } finally {
                lock.release();
            }
        } catch (Exception e) {
            if (Settings.IS_WINDOWS && e instanceof FileNotFoundException && e.getMessage().equals(file.getAbsolutePath() + " (Access is denied)")) {
                try {
                    return isUnlockedWithAdminPriviliges(file);
                } catch (Exception ex) {
                    throw new JarexpException("An error occurred while checking is file being used " + file, ex);
                }
            }
            throw new JarexpException("An error occurred while checking is file " + file + " locked", e);
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Unable to close random access for file " + file, e);
                }
            }
        }
    }

    private static boolean isUnlockedWithAdminPriviliges(File file) throws IOException {
        File bat = File.createTempFile("jarexp-", ".bat");
        bat.deleteOnExit();
        final File isfilelockedRes = new File(bat.getParentFile(), "jarexp-isfilelocked");
        try {
            StringBuilder outBat = new StringBuilder();
            outBat.append("@echo off").append(EOL);
          	outBat.append("set LOGFILE=%TEMP%\\jarexp-console-adm.txt").append(EOL);
            outBat.append("echo %date% %time% ADMIN ISFILELOCKED. Starting... >> %LOGFILE%").append(EOL);
            outBat.append("set ISFILELOCKEDRES=%TEMP%\\jarexp-isfilelocked").append(EOL);
            outBat.append("call ren \"").append(file.getAbsolutePath()).append("\" \"").append(file.getName()).append("\" > %ISFILELOCKEDRES% 2>&1").append(EOL);
            outBat.append("echo %date% %time% ADMIN ISFILELOCKED. Done. >> %LOGFILE%").append(EOL);
            outBat.append("set LOGFILE=").append(EOL);
            outBat.append("set ISFILELOCKEDRES=").append(EOL);
            toFile(bat, outBat.toString());

            if (isfilelockedRes.exists()) {
                isfilelockedRes.delete();
            }
            runWithAdminPrivileges(bat);
            new PostAminExecution() {
                @Override
                protected boolean accept() {
                    return isfilelockedRes.exists();
                }
            }.doWait();
            return !toString(isfilelockedRes).contains("The process cannot access the file because it is being used by another process.");
        } finally {
            bat.delete();
            isfilelockedRes.delete();
        }
    }

    private static void runWithAdminPrivileges(File bat) {
        File vbs = null;
        try {
            vbs = File.createTempFile("jarexp-", ".vbs");
            vbs.deleteOnExit();
            StringBuilder outVbs = new StringBuilder();
            outVbs.append("Set UAC = CreateObject(\"Shell.Application\")").append(EOL);
            outVbs.append("UAC.ShellExecute \"" + bat.getAbsolutePath() + "\", \"\", \"\", \"runas\", 0").append(EOL);
            toFile(vbs, outVbs.toString());

            Result code = Cmd.run(new String[] { "cscript", vbs.getAbsolutePath(), "//B" }, null);
            if (!code.out.isEmpty() || !code.err.isEmpty()) {
                throw new JarexpException("Unable to run file " + bat + " with admin privileges. See the logs for details. Result code:\n" + code);
            }
        } catch (Exception e) {
            throw new JarexpException("An error occurred while running ", e);
        } finally {
            if (vbs != null) {
                vbs.delete();
            }
        }
    }

}
