/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class StandardInputOutput {
	// ====================
	public static String[] listDirectory(String dirPath) {
		try {
			File dir = new File(dirPath);
			String[] children = dir.list();
			return children;
		} catch (Exception e) {
			return null;
		}
	}

	// ====================
	public static boolean deleteDirectory(String dirPath) {
		File dir = new File(dirPath);
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i].getAbsolutePath());
				} else {
					files[i].delete();
				}
			}
		}
		return (dir.delete());
	}

	// ====================
	public static BufferedReader openResourceReader(Class c, String fname) {
		BufferedReader reader;
		try {
			InputStream istream = c.getResourceAsStream(fname);
			if (istream != null)
				reader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));
			else
				throw new IllegalArgumentException("ERROR: couldn't find resource " + fname);
			return reader;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedReader openReader(String fname) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fname), "UTF-8"));
			return reader;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ====================
	public static boolean closeReader(BufferedReader reader) {
		try {
			reader.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// ====================
	public static BufferedWriter openWriter(String fname) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname), "UTF-8"));
			return writer;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ====================
	public static boolean closeWriter(BufferedWriter writer) {
		try {
			writer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// ====================
	public static BufferedWriter openAppender(String fname) {
		BufferedWriter appender;
		try {
			appender = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname, true), "UTF-8"));
			return appender;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ====================
	public static boolean closeAppender(BufferedWriter appender) {
		try {
			appender.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// ====================
	public static boolean moveFile(String fileName, String directoryName) {
		File file = new File(fileName);
		File dir = new File(directoryName);
		File newFile = new File(dir, file.getName());
		if (isFileExist(newFile.getPath()))
			deleteFile(newFile.getPath());
		boolean success = file.renameTo(new File(dir, file.getName()));
		return success;
	}

	// ====================
	public static String readContent(String contentFileName) {
		BufferedReader reader = openReader(contentFileName);
		String line;
		String content = "";
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				content += line + " ";
			}
			content = content.trim();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// =====================
	public static void sleepingChild(int numSeconds) {
		try {
			Thread.sleep(numSeconds * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// =====================
	public static boolean isFileExist(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	// =====================
	public static boolean deleteFile(String filePath) {
		boolean success = true;
		if (isFileExist(filePath)) {
			File file = new File(filePath);
			success = file.delete();
		}
		return success;
	}

	// =====================
	public static ArrayList<String> readLines(String fileName) {
		BufferedReader reader = openReader(fileName);
		String line;
		ArrayList<String> content = new ArrayList<String>();
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				content.add(line);
			}

			reader.close();

			return content;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to read from file " + fileName);
			System.exit(1);
			return null;
		}
	}
}
