package com.delfin.jarexp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {

	private static final int BUFFER_SIZE = 4096;

	@Deprecated
	public static void unzip(String zipFilePath, File dst) throws IOException {
		if (!dst.exists()) {
			dst.mkdirs();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			File file = new File(dst, entry.getName());
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, file);
			} else {
				// if the entry is a directory, make the directory
				file.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	
	 public static void unzipFrom(String path, File archive, File dst) {
		 makeParentDirs(dst);
		 
	     try {

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
	         
	         out.flush();
	         st.close();
	         out.close();
	         in.close();
	         jar.close();
	        }
	        catch (Exception e) {
	         e.printStackTrace();
	       }
	 }
	
	private static void extractFile(ZipInputStream zipIn, File file) throws IOException {
		makeParentDirs(file);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		byte[] bytesIn = new byte[BUFFER_SIZE];
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


	public static void copy(File src, File dst) throws IOException {
		if (dst.isDirectory()) {
			dst = new File(dst, src.getName());
		}
		
		FileInputStream srcStream = new FileInputStream(src);
		FileOutputStream dstStream = new FileOutputStream(dst);
		
		 FileChannel srcChannel = srcStream.getChannel();
		  FileChannel dstChannel = dstStream.getChannel();
		  dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
		  
		  srcStream.close();
		  dstStream.close();
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
	            	
//	                File file = files.get(i);
//	                if (!file.isDirectory()) {
//		                FileInputStream fis = new FileInputStream(file);
//		                try {
//		                    byte[] buffer = new byte[1024];
//		                    int bytesRead = 0;
//		                    String entryName = path + file.getName();
//		                    JarEntry entry = new JarEntry(entryName);
//		                    tempJarOutputStream.putNextEntry(entry);
//		                    while((bytesRead = fis.read(buffer)) != -1) {
//		                        tempJarOutputStream.write(buffer, 0, bytesRead);
//		                        
//		                    }
//		                    adds.add(entryName);
//		                    System.out.println(entry.getName() + " was added into " + dst);
//		                }
//		                finally {
//		                    fis.close();
//		                }
//	                } else {
//	                	addDirIntoJar(tempJarOutputStream, file, adds);
//	                	
//	                	
//	                    String entryName = path + file.getName() + "/";
//	                    JarEntry entry = new JarEntry(entryName);
//	                    tempJarOutputStream.putNextEntry(entry);
//	                    add
//	                    
//	                    adds.add(entryName);
//	                    System.out.println(entry.getName() + " was added into " + dst);
//	                }
	                
	            }
	 
	            //Copy original jar file to the temporary one.
	            Enumeration jarEntries = jarFile.entries();
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
		
		
		
		
////		File[] fils = new File[files.size()];
////		for (int i = 0; i < files.size(); ++i) {
////			new File(files.get(i));
////			// fils[i] = new File(files.get(i));
////		}
//	       // get a temp file
//	    File tempFile = File.createTempFile(dst.getName(), "jarexp");
//	        // delete it, otherwise you cannot rename your existing zip to it.
//	    tempFile.delete();
//
//	    copy(dst, tempFile);
//	    
////	    boolean renameOk=dst.renameTo(tempFile);
////	    if (!renameOk)
////	    {
////	        throw new RuntimeException("could not rename the file "+dst.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
////	    }
//	    byte[] buf = new byte[1024];
//
//	    ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
//	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dst));
//
//	    ZipEntry entry = zin.getNextEntry();
//	    while (entry != null) {
//	        String name = entry.getName();
//	        boolean notInFiles = true;
//	        for (File f : files) {
//	            if (f.getName().equals(name)) {
//	                notInFiles = false;
//	                break;
//	            }
//	        }
//	        if (notInFiles) {
//	            // Add ZIP entry to output stream.
//	            out.putNextEntry(new ZipEntry(name));
//	            // Transfer bytes from the ZIP file to the output file
//	            int len;
//	            while ((len = zin.read(buf)) > 0) {
//	                out.write(buf, 0, len);
//	            }
//	        }
//	        entry = zin.getNextEntry();
//	    }
//	    // Close the streams        
//	    zin.close();
//	    // Compress the files
//	    for (int i = 0; i < files.size(); i++) {
//	        InputStream in = new FileInputStream(files.get(i));
//	        // Add ZIP entry to output stream.
//	        out.putNextEntry(new ZipEntry(files.get(i).getName()));
//	        // Transfer bytes from the file to the ZIP file
//	        int len;
//	        while ((len = in.read(buf)) > 0) {
//	            out.write(buf, 0, len);
//	        }
//	        // Complete the entry
//	        out.closeEntry();
//	        in.close();
//	    }
//	    // Complete the ZIP file
//	    out.close();
//	    tempFile.delete();
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
		List<File> files = new ArrayList<>();
		files.add(new File("bookstore-web.war"));
		//files.add(new File("img.jpg"));
		//add("META-INF/", new File("hibernate.jar"), files);
		add("", new File("bookstore.ear"), files);
	 }

}
