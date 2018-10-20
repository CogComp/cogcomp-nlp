/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.io;

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Some utility functions involving files and directory. Some of these commands are modeled after
 * Unix commands.
 *
 * @author Vivek Srikumar
 */
public abstract class IOUtils {

    /**
     * Check if a file exists.
     */
    public static boolean exists(String file) {
        return (new File(file)).exists();
    }

    /**
     * Check if this the argument is a file.
     */
    public static boolean isFile(String file) {
        return (new File(file)).isFile();
    }

    /**
     * Check if the argument is a directory.
     */
    public static boolean isDirectory(String dir) {
        return (new File(dir)).isDirectory();
    }

    /**
     * Create a directory, if it does not exist.
     */
    public static boolean mkdir(String dir) {
        if (!exists(dir)) {
            return (new File(dir)).mkdirs();
        } else {
            return isDirectory(dir);
        }
    }

    /**
     * Get the extension of a file. This function does not check if the file exists.
     */
    public static String getFileExtension(String name) {
        int pos = name.lastIndexOf('.');
        if (pos > 0 & pos < name.length() - 1)
            return name.substring(pos + 1);

        return "";
    }

    /**
     * Remove the extension from the file name. This function does not check if the file exists.
     */
    public static String stripFileExtension(String name) {
        int pos = name.lastIndexOf('.');
        if (pos > 0 & pos < name.length() - 1)
            return name.substring(0, pos);
        return name;

    }

    /**
     * Returns the file name from a full path.
     */
    public static String getFileName(String path) {
        int slashIndex = path.lastIndexOf(File.separator);
        path = path.substring(slashIndex + 1);

        return path;
    }


    public static String getFileStem(String path) {
        return stripFileExtension(getFileName(path));
    }

    /**
     * List the contents of a directory. NOTE: Order of list is not guaranteed to be consistent across runs/machines.
     */
    public static String[] ls(String directory) throws IOException {
        if (!isDirectory(directory)) {
            throw new IOException("Invalid directory! " + directory);
        }

        return (new File(directory)).list();
    }

    /**
     * List the files contained in a directory.
     */
    public static String[] lsFiles(String directory) throws IOException {
        return lsFiles(directory, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
    }

    /**
     * Filters the files contained in a directory.
     */
    public static String[] lsFiles(String directory, FilenameFilter filter) throws IOException {
        File dir = new File(directory);
        ArrayList<String> files = new ArrayList<>();
        for (File filepath : dir.listFiles(filter)) {
            if (isFile(filepath.getAbsolutePath()))
                files.add(filepath.getAbsolutePath());
        }
        return files.toArray(new String[files.size()]);
    }


    /**
     * Filters the files contained in a directory or in its subdirectory structure. Returns all
     * files (not directories) that pass the filter.
     */
    public static String[] lsFilesRecursive(String directory, FilenameFilter filter)
            throws IOException {
        File dir = new File(directory);
        ArrayList<String> files = new ArrayList<>();
        for (File filepath : dir.listFiles(filter)) {
            if (isFile(filepath.getAbsolutePath()))
                files.add(filepath.getAbsolutePath());
            else if (isDirectory(filepath.getAbsolutePath()))
                files.addAll(Arrays.asList(lsFilesRecursive(filepath.getAbsolutePath(), filter)));
        }
        return files.toArray(new String[files.size()]);
    }

    /**
     * Filters the files contained in a directory or in its subdirectory structure. Returns all
     * files (not directories) that pass the filter.
     */
    public static String[] lsFilesRecursive(String directory, FileFilter filter)
            throws IOException {
        File dir = new File(directory);
        ArrayList<String> files = new ArrayList<>();
        for (File filepath : dir.listFiles(filter)) {
            if (filepath.isFile())
                files.add(filepath.getAbsolutePath());
            else if (filepath.isDirectory())
                files.addAll(Arrays.asList(lsFilesRecursive(filepath.getAbsolutePath(), filter)));
        }
        return files.toArray(new String[files.size()]);
    }


    /**
     * List the directories contained within a directory.
     */
    public static String[] lsDirectories(String directory) throws Exception {
        String[] tmp = ls(directory);

        ArrayList<String> files = new ArrayList<>();

        for (String s : tmp) {
            if (isDirectory(directory + File.separator + s))
                files.add(s);
        }

        return files.toArray(new String[files.size()]);
    }

    /**
     * Get the current working directory.
     */
    public static String pwd() throws IOException {
        return (new File(".")).getCanonicalPath();
    }

    /**
     * Creates a temporary directory and returns its name. If this already exists, then it is
     * deleted.
     *
     * @param prefix The prefix of the temporary directory name. This can be used to specify the
     *        directory where the temporary directory resides.
     */
    public static String mkTmpDir(String prefix) throws IOException {
        File tmpDir = File.createTempFile(prefix, "");

        if (!tmpDir.delete())
            throw new IOException();

        if (!tmpDir.mkdir())
            throw new IOException();

        return tmpDir.getCanonicalPath();
    }

    /**
     * Create a new empty file.
     */
    public static boolean touch(String file) throws IOException {
        return !exists(file) && (new File(file)).createNewFile();
    }

    /**
     * Delete a file
     *
     * @return true only if the delete was successful
     */
    public static boolean rm(String file) throws IOException {
        if (!exists(file))
            return false;

        if (!isFile(file))
            throw new IOException(file + " is not a file!");

        return (new File(file)).delete();
    }

    /**
     * Empty a directory without deleting it
     *
     * @param directory The directory to be cleaned
     * @return true only if cleaning was successful
     */
    public static boolean cleanDir(String directory) throws IOException {
        String[] files = ls(directory);
        boolean clean = true;
        // Some JVMs return null for empty dirs
        if (files != null) {
            for (String f : files) {
                String filename = directory + File.separator + f;
                File file = new File(filename);
                if (file.isDirectory())
                    clean = cleanDir(filename) && file.delete();
                else
                    clean = file.delete();
            }
        }
        return clean;
    }

    public static boolean rmDir(String directory) throws IOException {
        if (!exists(directory))
            return false;

        if (!isDirectory(directory))
            throw new IOException(directory + " is not a directory!");

        return (new File(directory)).delete();
    }

    /**
     * Copy a file. This uses Java's NIO to defer the actual heavy lifting to the OS.
     */
    public static void cp(String sourceFileName, String destFileName) throws IOException {
        File sourceFile = new File(sourceFileName);
        File destFile = new File(destFileName);

        if (!destFile.exists()) {
            boolean createNewFile = destFile.createNewFile();

            if (!createNewFile) {
                throw new IOException("Unable to create " + destFileName);
            }
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    /**
     * Lists resources that are contained within a path. This works for any resource on the
     * classpath, either in the file system or in a jar file. The function returns a list of URLs,
     * connections to which can be opened for reading.
     * <p>
     * <b>NB</b>: This method works only for full file names. If you need to list the files of a
     * directory contained in the classpath use {@link #lsResourcesDir(Class, String)}
     *
     * @param clazz The class whose path is scanned
     * @param path The name of the resource(s) to be returned
     * @return A list of URLs
     */
    public static List<URL> lsResources(Class clazz, String path) throws URISyntaxException,
            IOException {
        URL dirURL = clazz.getResource(path);

        if (dirURL == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            dirURL = loader.getResource(path);
        }

        if (dirURL == null) {
            return new ArrayList<>();
        }

        String dirPath = dirURL.getPath();
        if (dirURL.getProtocol().equals("file")) {
            String[] list = new File(dirURL.toURI()).list();
            List<URL> urls = new ArrayList<>();

            if (list == null) {
                // if the list is null, but the dirURL is not, then dirURL is
                // actually a file!
                urls.add(dirURL);
            } else {
                for (String l : list) {
                    URL url = (new File(dirPath + File.separator + l)).toURI().toURL();
                    urls.add(url);
                }
            }
            return urls;
        }

        if (dirURL.getProtocol().equals("jar")) {
            int exclamation = dirPath.indexOf("!");
            String jarPath = dirPath.substring(5, exclamation);
            String jarRoot = dirPath.substring(0, exclamation + 1);

            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries();

            List<URL> urls = new ArrayList<>();
            while (entries.hasMoreElements()) {
                JarEntry element = entries.nextElement();

                String name = element.getName();

                // Because the path string comes from JarEntry, We SHOULD use
                // '/'' instead of File.SEPERATOR.
                // And it seems that the only way to figure out if a JarEntry
                //  path is a folder or file is to check the last character.
                if (name.startsWith(path) && !name.equals(path + "/")) {
                    URL url = new URL("jar:" + jarRoot + "/" + name);
                    urls.add(url);
                }
            }
            return urls;
        }
        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    /**
     * Lists resources that are contained within a path. This works for any resource on the
     * classpath, either in the file system or in a jar file. The function returns a list of URLs,
     * connections to which can be opened for reading.
     * <p>
     * <b>NB</b>: This method can be used to list the files of a directory contained in the
     * classpath. However, since it explicitly lists the contents of each classpath resource it is
     * very slow. If you need a list of specific files (or for directories that are not inside
     * jars), use {@link #lsResources(Class, String)}
     *
     * @param clazz The class whose path is scanned
     * @param path The name of the resource(s) to be returned
     * @return A list of URLs
     */
    public static List<URL> lsResourcesDir(Class clazz, String path) throws IOException {
        final Pattern pattern = Pattern.compile(".*" + path + ".*");
        final ArrayList<URL> urls = new ArrayList<>();
        URL[] resourceUrls = ((URLClassLoader) clazz.getClassLoader()).getURLs();
        for (final URL url : resourceUrls) {
            final File resource = new File(url.getFile());
            // The resource is a directory
            if (resource.isDirectory()) {
                urls.addAll(getResourcesFromDirectory(resource, pattern));
            }

            // The resource is a jar
            else {
                String jarPath = "file:" + resource.getPath() + "!";
                ZipFile zf;
                try {
                    zf = new ZipFile(resource);
                } catch (IOException e) {
                    continue;
                }
                final Enumeration e = zf.entries();
                while (e.hasMoreElements()) {
                    final ZipEntry ze = (ZipEntry) e.nextElement();
                    final String fileName = ze.getName();
                    if (pattern.matcher(fileName).matches()) {
                        urls.add(new URL("jar:" + jarPath + "/" + fileName));
                    }
                }
                zf.close();
            }
        }
        return urls;
    }

    private static List<URL> getResourcesFromDirectory(File resource, Pattern pattern)
            throws IOException {
        ArrayList<URL> urls = new ArrayList<>();
        final File[] fileList = resource.listFiles();
        if (fileList == null)
            return urls;
        for (final File file : fileList) {
            if (file.isDirectory()) {
                urls.addAll(getResourcesFromDirectory(file, pattern));
            } else {
                final String fileName = file.getCanonicalPath();
                if (pattern.matcher(fileName).matches()) {
                    urls.add(file.getCanonicalFile().toURI().toURL());
                }
            }
        }
        return urls;
    }

    public static List<URL> getListOfFilesInDir(String path){
        File[] listOfFiles = new File(path).listFiles();
        ArrayList<URL> urls = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                try {
                    urls.add(listOfFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else if (listOfFile.isDirectory()) {
                // do nothing
            }
        }
        return urls;
    }

    public static <T> void writeObject(T object, String fileName) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
        outputStream.writeObject(object);
        outputStream.close();
    }

    public static <T> T readObject(String fileName) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(fileName)));

        @SuppressWarnings("unchecked")
        T obj = (T) in.readObject();

        in.close();
        return obj;
    }

    public static <T> T readObjectAsResource(Class clazz, String fileName) throws Exception {
        List<URL> lsResources = IOUtils.lsResources(clazz, fileName);

        assert lsResources.size() > 0;

        URL resource = lsResources.get(0);

        InputStream stream = resource.openStream();

        ObjectInputStream in = new ObjectInputStream(stream);

        @SuppressWarnings("unchecked")
        T obj = (T) in.readObject();

        in.close();
        return obj;
    }

    /**
     * Changes "/a/b/c/d/e/f/g.h" into "/a/b/.../e/f/g.h",
     * or "C:\d\e\f\g\h\i\j.k" into "C:\d\e\...\h\i\j.k"
     * @param path The long path
     * @return The shortened path
     */
    public static String shortenPath(String path) {
        return path.replaceAll("^(\\w+:|)([\\\\|/][^\\\\|/]+[\\\\|/][^\\\\|/]+[\\\\|/]).*([\\\\|/][^\\\\|/]+[\\\\|/][^\\\\|/]+)$", "$1$2...$3");
    }
}
