package com.delfin.jarexp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Version;


public class Zip {
	
	public static interface TempFileCreator {
		File create(String prefix, String suffix) throws IOException;
	}
	
	private static final Logger log = Logger.getLogger(Zip.class.getCanonicalName());
	
	private static final Map<String, File> unpacked = new ConcurrentHashMap<String, File>();

	private static TempFileCreator tempFileCreator = new TempFileCreator() {
		@Override
		public File create(String prefix, String suffix) throws IOException {
			return File.createTempFile(prefix, suffix, new File(System.getProperty("java.io.tmpdir")));
		}
	};
	
	public static void setTempFileCreator(TempFileCreator tempFileCreator) {
		Zip.tempFileCreator = tempFileCreator;
	}
	
	
	// private static final int BUFFER_SIZE = 4096;

//	@Deprecated
//	public static void unzip(String zipFilePath, File dst) throws IOException {
//		if (!dst.exists()) {
//			dst.mkdirs();
//		}
//		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
//		ZipEntry entry = zipIn.getNextEntry();
//		// iterates over entries in the zip file
//		while (entry != null) {
//			File file = new File(dst, entry.getName());
//			if (!entry.isDirectory()) {
//				// if the entry is a file, extracts it
//				extractFile(zipIn, file);
//			} else {
//				// if the entry is a directory, make the directory
//				file.mkdir();
//			}
//			zipIn.closeEntry();
//			entry = zipIn.getNextEntry();
//		}
//		zipIn.close();
//	}

	public static File unzip(String fullPath, String path, File archive, File dst) {
		return unzip(fullPath, path, archive, dst, true);
	}
	
	/**
	 * 
	 * @param fullPath - string representation of the full path to resource.
	 * @param path - path of resource in the archive.
	 * @param archive - existing archive file where <code>path</code> specified.
	 * @param dst - file where will be extracted resource.
	 */
	 public static File unzip(String fullPath, String path, File archive, File dst, boolean keepCaching) {
		// System.out.println("unpacking file path " + path + " fullPath " + fullPath + " to " + dst);
		if (keepCaching) {
			File cached = unpacked.get(fullPath);
			if (cached != null && cached.exists()) {
				String cachedKey = Md5Checksum.get(cached);
				try {
					String newKey = Md5Checksum.get(archive, path);
					if (cachedKey.equals(newKey)) {
						// System.out.println("returned from cache " + path + " file " + cached);
						return cached;
					}
				} catch (Exception e) {
					
					log.log(Level.SEVERE, "Unable to get MD5 checksum for " + archive, e);
					throw new JarexpException("Unable to get MD5 checksum for " + fullPath, e);
				}
			}
		}
		
		//try {throw new RuntimeException();} catch (RuntimeException e) {e.printStackTrace(System.out);}
		 
		// System.out.println("unzipping " + path + " from " + archive + " into " + dst);
		 
		 JarFile jar = null;
	     try {
			 if (dst.exists()) {
				dst = tempFileCreator.create(dst.getName(), "jarexp");
				log.log(Level.WARNING, "File to unpack already exists '" + dst + "' path '" + path + "' full path '"
						+ fullPath + "'. Unpacking into " + dst);

				// System.out.println("new temp file for path " + path + " full path " + fullPath);
			 }

			 makeParentDirs(dst);
	    	 
	         jar = new JarFile(archive);
	         ZipEntry entry = jar.getEntry(path);
	         //File efile = new File(entry.getName());
	         FileOutputStream st = null;
	         
	         
	         InputStream in =
	            new BufferedInputStream(jar.getInputStream(entry));
	         OutputStream out =
	            new BufferedOutputStream(st = new FileOutputStream(dst));
	         byte[] buffer = new byte[2048];
	         for (;;)  {
	           int nBytes = in.read(buffer);
	           if (nBytes <= 0) break;
	           out.write(buffer, 0, nBytes);
	         }

	         if (keepCaching) {
	        	 unpacked.put(fullPath, dst);
	         }

	         out.flush();
	         st.close();
	         out.close();
	         in.close();
	        }
	        catch (Exception e) {
	        	log.log(Level.SEVERE, "Unable to unzip " + path + " from " + archive + " to " + dst, e);
	         throw new JarexpException("Unable to unzip " + path + " from " + archive + " to " + dst, e);
	       } finally {
	    	   if (jar != null) {
	    		   try {
					jar.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	   }
	       }
	     return dst;
	 }
	
	private static void extractFile(ZipInputStream zipIn, File file) throws IOException {
		makeParentDirs(file);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		byte[] bytesIn = new byte[4096];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
	
	private static String readFile(ZipInputStream zipIn) throws IOException {
//		// BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//		StringBuilder out = new StringBuilder();
//		byte[] bytesIn = new byte[4096];
//		while (zipIn.read(bytesIn) != -1) {
//			out.append(new String(bytesIn));
//			// bos.write(bytesIn, 0, read);
//		}
//		//bos.close();
//		return out.toString();
		
		StringBuilder out = new StringBuilder();
		Scanner scanner = new Scanner(zipIn);
		while (scanner.hasNextLine()) {
			out.append(scanner.nextLine()).append('\n');
		}
		return out.toString();
	}
	

	 private static void makeParentDirs(File file) {
		 File parent = file.getParentFile();
		 if (parent == null) {
			 return;
		 }
		 if (!parent.exists()) {
			 parent.mkdirs();
		 }
	}

	public static void delete(String path, File dst) throws IOException {
		
		System.out.println("deleted " + path + " from " + dst);
		
	    File tmpJarFile = File.createTempFile("tempJar", ".tmp");
	    JarFile jarFile = new JarFile(dst);
	    boolean jarUpdated = false;
	 
	    try {
	        JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tmpJarFile));
	 
	        try {
	            //Copy original jar file to the temporary one.
	            Enumeration<?> jarEntries = jarFile.entries();
	            while(jarEntries.hasMoreElements()) {
	                JarEntry entry = (JarEntry) jarEntries.nextElement();
	                entry.setCompressedSize(-1);
	                if (entry.getName().startsWith(path)) { 
	                	continue;
	                }
	                InputStream entryInputStream = jarFile.getInputStream(entry);
	                tempJarOutputStream.putNextEntry(entry);
	                byte[] buffer = new byte[1024];
	                int bytesRead = 0;
	                while ((bytesRead = entryInputStream.read(buffer)) != -1) {
	                    tempJarOutputStream.write(buffer, 0, bytesRead);
	                }
	            }
	 
	            jarUpdated = true;
	        }
	        catch(Exception ex) {
	            reset();
	            throw new RuntimeException(ex);
	        }
	        finally {
	            tempJarOutputStream.close();
	        }
	 
	    }
	    finally {
	        jarFile.close();
	        //System.out.println(srcJarFile.getAbsolutePath() + " closed.");
	 
	        if (!jarUpdated) {
	            tmpJarFile.delete();
	        }
	    }
	 
	    if (jarUpdated) {
	    	dst.delete();
	        tmpJarFile.renameTo(dst);
	        //System.out.println(srcJarFile.getAbsolutePath() + " updated.");
	    }

		reset();
	}
	
	
	public static void add(String path, File dst, List<File> files) throws IOException {
		
	    File tmpJarFile = File.createTempFile("tempJar", ".tmp");
	    JarFile jarFile = new JarFile(dst);
	    boolean jarUpdated = false;
	 
	    try {
	        JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tmpJarFile));
	 
	        try {
	        	List<String> adds = new ArrayList<String>();
	            //Added the new files to the jar.
	            for(int i=0; i < files.size(); i++) {
	            	File file = files.get(i);
	            	addFileIntoJar(path, tempJarOutputStream, file, adds);
	            	
	                
	            }
	 
	            //Copy original jar file to the temporary one.
	            Enumeration<?> jarEntries = jarFile.entries();
	            while(jarEntries.hasMoreElements()) {
	                JarEntry entry = (JarEntry) jarEntries.nextElement();
	                entry.setCompressedSize(-1);
	                if (adds.contains(entry.getName())) {
	                	continue;
	                }
	                InputStream entryInputStream = jarFile.getInputStream(entry);
	                tempJarOutputStream.putNextEntry(entry);
	                byte[] buffer = new byte[1024];
	                int bytesRead = 0;
	                while ((bytesRead = entryInputStream.read(buffer)) != -1) {
	                    tempJarOutputStream.write(buffer, 0, bytesRead);
	                }
	            }
	 
	            jarUpdated = true;
	        }
	        catch(Exception ex) {
	            ex.printStackTrace();
	            tempJarOutputStream.putNextEntry(new JarEntry("stub"));
	        }
	        finally {
	            tempJarOutputStream.close();
	        }
	 
	    }
	    finally {
	        jarFile.close();
	        //System.out.println(srcJarFile.getAbsolutePath() + " closed.");
	 
	        if (!jarUpdated) {
	            tmpJarFile.delete();
	        }
	    }
	 
	    if (jarUpdated) {
	    	dst.delete();
	        tmpJarFile.renameTo(dst);
	        //System.out.println(srcJarFile.getAbsolutePath() + " updated.");
	    }
		
		reset();
		
	}

	private static void addFileIntoJar(String path, JarOutputStream outStream, File file, List<String> adds) throws IOException {
		
        if (file.isDirectory()) {
        	
            // String entryName = path + file.getName() + "/";
            String entryName = path;
            if (entryName.length() == 0 || entryName.charAt(entryName.length() - 1) == '/') {
            	entryName += file.getName() + "/";
            }
            JarEntry entry = new JarEntry(entryName);
            outStream.putNextEntry(entry);
            adds.add(entryName);
            System.out.println(entry.getName() + " was added into ");            
            for (File f : file.listFiles()) {
            	addFileIntoJar(entryName, outStream, f, adds);
            }
        } else {
            FileInputStream fis = new FileInputStream(file);
            try {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                // String entryName = path + file.getName();
                String entryName = path;
                if (entryName.length() == 0 || entryName.charAt(entryName.length() - 1) == '/') {
                	entryName += file.getName();
                }
                JarEntry entry = new JarEntry(entryName);
                outStream.putNextEntry(entry);
                while((bytesRead = fis.read(buffer)) != -1) {
                	outStream.write(buffer, 0, bytesRead);
                }
                adds.add(entryName);
                System.out.println(entry.getName() + " was added into ");
            }
            finally {
                fis.close();
            }
    
        }
	}


	public static void main(String[] args) throws IOException {
		 // unzip("D:\\fernflower\\arpluginsvr90_build001.jar", "D:\\fernflower\\out");
		 // unzip("D:\\fernflower\\mt\\MidTier.jar", "D:\\fernflower\\mt\\out");
		 // unzip("D:\\fernflower\\je\\JarExplorer.jar", new File("D:\\fernflower\\je\\out"));
		List<File> files = new ArrayList<File>();
		files.add(new File("bookstore-web.war"));
		//files.add(new File("img.jpg"));
		//add("META-INF/", new File("hibernate.jar"), files);
		add("", new File("bookstore.ear"), files);
	 }
	
	public static Map<String, String> unzip(InputStream stream) {
		Map<String, String> content = new HashMap<String, String>();
		ZipInputStream is = null;
		try {
			is = new ZipInputStream(stream);
			ZipEntry entry = is.getNextEntry();
			while (entry != null) {
				content.put(entry.getName(), readFile(is));
				is.closeEntry();
				entry = is.getNextEntry();
			}
			return content;
		} catch (Exception e) {
			throw new JarexpException("An error occurred while unpacking stream.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while closing zip stream to stream.", e);
				}
			}
		}
	}

	public static void unzip(InputStream stream, File dst) {
		if (!dst.exists()) {
			dst.mkdirs();
		}
		ZipInputStream is = null;
		try {
			is = new ZipInputStream(stream);
			ZipEntry entry = is.getNextEntry();
			while (entry != null) {
				File file = new File(dst, entry.getName());
				if (!entry.isDirectory()) {
					extractFile(is, file);
				} else {
					file.mkdir();
				}
				is.closeEntry();
				entry = is.getNextEntry();
			}
		} catch (Exception e) {
			throw new JarexpException("An error occurred while unpacking stream to " + dst, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while closing stream to " + dst, e);
				}
			}
		}
	}

	public static void unzip(File archive, File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		ZipInputStream is = null;
		try {
			is = new ZipInputStream(new FileInputStream(archive));
			ZipEntry entry = is.getNextEntry();
			while (entry != null) {
				File file = new File(dir, entry.getName());
				if (!entry.isDirectory()) {
					extractFile(is, file);
				} else {
					file.mkdir();
				}
				is.closeEntry();
				entry = is.getNextEntry();
			}

		} catch (Exception e) {
			throw new JarexpException("An error occurred while unpacking file " + archive, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "An error occurred while closing stream to archive " + archive, e);
				}
			}
		}
	}

	public static String unzip(File archive, String path) {
		try {
	        ZipFile zip = new ZipFile(archive);
	        Scanner scanner = new Scanner(zip.getInputStream(zip.getEntry(path)));
	        StringBuilder out = new StringBuilder();
	        while (scanner.hasNextLine()) {
	        	out.append(scanner.nextLine()).append('\n');
	        }
	        scanner.close();
	        zip.close();
			return out.toString();
		} catch (Exception e) {
			throw new JarexpException("An error occurred while unpacking path " + path + " from archive " + archive, e);
		}
	}

	public static boolean isArchive(String path, boolean doLower) {
		if (doLower) {
			path = path.toLowerCase();
		}
		return path.endsWith(".jar") || path.endsWith(".war") || path.endsWith(".ear") || path.endsWith(".zip")
				|| path.endsWith(".apk") || path.endsWith(".aar");
	}

	public static boolean isArchive(String path) {
		return isArchive(path, false);
	}

	public static boolean isArchive(File file) {
	    return isArchive(file.getName(), false);
	}

	public static File getUnpacked(String fullName) {
		return unpacked.get(fullName);
	}

	public static void reset() {
		unpacked.clear();
	}
	
	public static void stream(File archive, String path, StreamProcessor processor) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(archive);
			ZipEntry entry = zip.getEntry(path);
			processor.process(zip.getInputStream(entry));
		} catch (IOException e) {
			throw new JarexpException("Error while streaming by '" + path + "' from " + archive, e);
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Couldn't close zip file " + archive, e);
				}
			}
		}
	}

	public interface StreamProcessor {
		void process(InputStream stream) throws IOException;
	}

    public static void bypass(File archive, BypassAction bypassAction) {
        ZipFile zip = null;
        try {
            if (Version.JAVA_MAJOR_VER >= 7) {
                zip = new ZipFile(archive, Charset.forName("Cp866"));
            } else {
                zip = new ZipFile(archive);
            }
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (!e.isDirectory()) {
                    bypassAction.process(zip, e);
                }
            }
        } catch (IOException e) {
            throw new JarexpException("Error while bypassing '" + archive, e);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Couldn't close zip file " + archive, e);
                }
            }
        }
    }

    public static interface BypassAction {
        void process(ZipFile zipFile, ZipEntry zipEntry) throws IOException;
    }

    public static interface BypassRecursivelyAction {
        void process(ZipEntry zipEntry, String fullPath) throws IOException;
        void error(ZipEntry zipEntry, Exception error);
    }

    public static void bypass(ZipInputStream stream, String fullPath, BypassRecursivelyAction action) throws IOException {
        try {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                try {
                    String entryName = entry.getName();
                    if (!Zip.isArchive(entryName)) {
                        action.process(entry, fullPath);
                    } else {
                        byte[] zipData = readArchive(stream);
                        ZipInputStream nestedStream = new ZipInputStream(new ByteArrayInputStream(zipData));
                        try {
                            if (fullPath != null && fullPath.charAt(fullPath.length() - 1) != '/') {
                                fullPath += '/';
                            }
                            bypass(nestedStream, fullPath + entryName + '!', action);
                        } catch (Exception e) {
                            action.error(entry, e);
                        } finally {
                            nestedStream.close();
                        }
                    }
                    stream.closeEntry();
                } catch (Exception e) {
                    action.error(entry, e);
                }
            } 
        } catch (Exception e ) {
            action.error(null, e);
        }
    }

    private static byte[] readArchive(ZipInputStream stream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        while ((len = stream.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

}
