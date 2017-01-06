package com.delfin.jarexp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class FileUtils {

	public static String toString(File file) {
		
		StringBuilder out = new StringBuilder();
		try {

			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
	                      new FileInputStream(file), "UTF8"));

			String line;

			while ((line = in.readLine()) != null) {
			    out.append(line).append('\n');
			}

	                in.close();
	                
		    }
		    catch (UnsupportedEncodingException e)
		    {
				System.out.println(e.getMessage());
		    }
		    catch (IOException e)
		    {
				System.out.println(e.getMessage());
		    }
		    catch (Exception e)
		    {
				System.out.println(e.getMessage());
		    }
		
		
		return out.toString();
		
		
//		String content = null;
//		FileReader reader = null;
//		try {
//			reader = new FileReader(file);
//			char[] chars = new char[(int) file.length()];
//			reader.read(chars);
//			content = new String(chars);
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		return content;
	}

}
