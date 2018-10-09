package com.delfin.jarexp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
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

import com.delfin.jarexp.JarexpException;

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

	/**
	 * 
	 * @param fullPath - string representation of the full path to resource.
	 * @param path - path of resource in the archive.
	 * @param archive - existing archive file where <code>path</code> specified.
	 * @param dst - file where will be extracted resource.
	 */
	 public static File unzip(String fullPath, String path, File archive, File dst) {
		// System.out.println("unpacking file path " + path + " fullPath " + fullPath + " to " + dst);
		File cached = unpacked.get(fullPath);
		if (cached != null && cached.exists()) {
			String cachedKey = Md5Checksum.get(cached);
			String newKey;
			try {
				newKey = Md5Checksum.get(archive, path);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to get MD5 checksum for " + fullPath, e);
				newKey = null;
			}
			if (cachedKey.equals(newKey)) {
				// try {throw new RuntimeException();} catch (RuntimeException e) {e.printStackTrace(System.out);}
				// System.out.println("returned from cache " + path);
				System.out.println("returned from cache " + path + " file " + cached);
				return cached;
			}

		}
		 
		 //System.out.println("unzipping " + path + " from " + archive + " into " + dst);
		 
	     try {
			 if (dst.exists()) {
				dst = tempFileCreator.create(dst.getName(), "jarexp");
				log.log(Level.WARNING, "File to unpack already exists '" + dst + "' path '" + path + "' full path '"
						+ fullPath + "'. Unpacking into " + dst);

				// System.out.println("new temp file for path " + path + " full path " + fullPath);
			 }

			 makeParentDirs(dst);
	    	 

	         JarFile jar = new JarFile(archive);
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
	         
	         unpacked.put(fullPath, dst);
	         
	         out.flush();
	         st.close();
	         out.close();
	         in.close();
	         jar.close();
	        }
	        catch (Exception e) {
	         e.printStackTrace();
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
				|| path.endsWith(".apk");
	}

	public static boolean isArchive(String path) {
		return isArchive(path, false);
	}

	public static File getUnpacked(String fullName) {
		return unpacked.get(fullName);
	}

	public static void reset() {
		unpacked.clear();
	}
}
