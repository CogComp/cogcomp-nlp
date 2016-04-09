package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IOManager {

    // =======
    public static boolean isDirectoryExist(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory())
            return false;

        return dir.exists();

    }

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
    public static BufferedReader openReader(String fname) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fname), "UTF-8"));
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
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fname), "UTF-8"));
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
            appender = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fname, true), "UTF-8"));
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
    // CYS: changed the original "String" implementation of the var "content", to StringBuffer, for faster performance
    public static String readContent(String contentFileName) {
        BufferedReader reader = openReader(contentFileName);
        String line;
        StringBuffer content = new StringBuffer();
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                //content.append(" "+line);
                content.append("\n");
            }
            reader.close();

            return content.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String readContentNoTrim(String contentFileName) {
        BufferedReader reader = openReader(contentFileName);
        String line;
        StringBuffer content = new StringBuffer();
        try {
            while ((line = reader.readLine()) != null) {
                //if (line.length() == 0)
                //    continue;
                content.append(line);
                content.append("\n");
            }
            reader.close();

            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String readContentCharBuf(String contentFileName) {
    	StringBuffer buf = new StringBuffer();
    	BufferedReader reader = openReader(contentFileName);
    	File file = new File(contentFileName);
    	char[] cbuf = new char[(int) file.length()];
    	try {
			reader.read(cbuf);
			buf.append(cbuf);
	    	reader.close();
	    	return buf.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}  
    }

    public static String readContentIntoSingleSentence(String contentFileName) {
        BufferedReader reader = openReader(contentFileName);
        String line;
        StringBuffer content = new StringBuffer();
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                content.append(line);
                content.append(" ");
            }
            reader.close();

            String s = content.toString();
            s = s.replaceAll("\\s+", " ");    // replace multiple spaces with single space
            return s.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // ====================
    // CYS: changed the original "String" implementation of the var "content", to StringBuffer, for faster performance
    public static String readContentAddingPeriod(String contentFileName) {
        BufferedReader reader = openReader(contentFileName);
        String line;
        StringBuffer content = new StringBuffer();
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                content.append(line);
                content.append("\n");
            }
            
            String stringContent = content.toString();
            stringContent = stringContent.replaceAll("\\.+", ".");
            stringContent = stringContent.trim();
            reader.close();

            return stringContent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ====================
    public static Object[] readContent2Array(String contentFileName) {
        BufferedReader reader = openReader(contentFileName);
        String line;
        ArrayList<String> arrLines = new ArrayList<String>();
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0)
                    continue;
                arrLines.add(line);
            }
            reader.close();

            return arrLines.toArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ======================
    public static void writeContent(String content, String outputFileName) {
        BufferedWriter writer = IOManager.openWriter(outputFileName);
        try {
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to write to file " + outputFileName);
            System.exit(1);
        }
        IOManager.closeWriter(writer);
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

    // =====================
    public static ArrayList<String> readLinesWithoutTrimming(String fileName) {
        BufferedReader reader = openReader(fileName);
        String line;
        ArrayList<String> content = new ArrayList<String>();
        try {
            while ((line = reader.readLine()) != null) {
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

    public static void writeLines(List<String> outputLines,
            String outputFile) {
        BufferedWriter writer = IOManager.openWriter(outputFile);
        try {
            for (String line : outputLines) {
                writer.write(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to write to file " + outputFile);
            System.exit(1);
        }
        IOManager.closeWriter(writer);
    }

    public static void writeLines(ArrayList<String> outputLines,
            BufferedWriter writer) {
        try {
            for (String line : outputLines) {
                writer.write(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to write to bufferedwriter.");
            System.exit(1);
        }
    }

    public static void writeLinesAddingReturn(List<String> outputLines,
            String outputFile) {
        BufferedWriter writer = IOManager.openWriter(outputFile);
        try {
            for (String line : outputLines) {
                writer.write(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to write to file " + outputFile);
            System.exit(1);
        }
        IOManager.closeWriter(writer);
    }

    public static void writeString(String line, BufferedWriter writer) {
            try {
                writer.write(line);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Unable to write to file. Function 'writeString' in IOManager!");
                System.exit(1);
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
        if (!file.isFile())
            return false;
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

    // ================
    public static boolean createDirectory(String dirPath) {
        if (isDirectoryExist(dirPath)) {
            deleteDirectory(dirPath);
        }
        File dir = new File(dirPath);
        return dir.mkdir();
    }

    // ================
    public static boolean createDirectoryNotDelete(String dirPath) {
        if (isDirectoryExist(dirPath)) {
            return true;
        }
        File dir = new File(dirPath);
        return dir.mkdir();
    }
}