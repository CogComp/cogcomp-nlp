/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.IO;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ResourceUtilities {
    private static Logger logger = LoggerFactory.getLogger(ResourceUtilities.class);
    // TODO Ideally this should be read from the config file
    private static final String localResourceDir = "";

    /**
     * Loads a resource either from the local resource directory or from the classpath
     * 
     * @param resourceFile The name of the resource file
     * @return A FileInputStream or a GZIPInputStream depending on the file type
     */
    public static InputStream loadResource(String resourceFile) {
        InputStream stream = null;
        try {
            String file = localResourceDir + resourceFile;
            // If the file doesn't exist locally it must be in the classpath (in common-resources
            // jar)
            if (!new File(file).exists()) {
                List<URL> list = IOUtils.lsResources(ResourceUtilities.class, resourceFile);
                if (list.isEmpty()) {
                    return null;
                }
                URL fileURL = list.get(0);
                URLConnection connection = fileURL.openConnection();
                stream = connection.getInputStream();
            } else {
                logger.debug("Loading {} from local directory", resourceFile);
                stream = new FileInputStream(file);
            }
            // Open stream as GZipped if needed
            stream = checkGZipped(stream);
        } catch (Exception e) {
            logger.error("Could not load {}. Exception {}", resourceFile, e.toString());
            System.exit(-1);
        }
        return stream;
    }

    /**
     * Overloaded version of {@code loadResource(String)} to work with {@code lsDirectory}. Loads a
     * resource either from the local resource directory or from the classpath
     */
    public static InputStream loadResource(URL resourceURL) {
        InputStream stream = null;
        try {
            URLConnection connection = resourceURL.openConnection();
            stream = connection.getInputStream();
            // Open stream as GZipped if needed
            stream = checkGZipped(stream);
        } catch (Exception e) {
            logger.error("Could not load {}. Exception {}", resourceURL, e.toString());
            System.exit(-1);
        }
        return stream;
    }

    /**
     * Lists the contents of a directory that exists either in the local path or in the classpath
     * 
     * @param resourceDir The name of the directory
     * @param dirListingFile In case the directory is in a jar we need to have an index given to us
     * @return An array of URL objects to be read
     * @throws IOException
     */
    public static String[] lsDirectory(String resourceDir, String dirListingFile)
            throws IOException {
        String dir = localResourceDir + resourceDir;
        if (!new File(dir).exists()) {
            if (dirListingFile == null) {
                logger.error("Could not list dir {} from classpath without a listing file.",
                        resourceDir);
                System.exit(-1);
            }
            logger.debug("Loading dir listing {} from classpath.", dirListingFile);
            InputStream stream = loadResource(resourceDir + "/" + dirListingFile);
            InFile in = new InFile(stream);
            String line;
            List<String> files = new ArrayList<>();
            while ((line = in.readLine()) != null) {
                files.add(line);
            }
            in.close();
            return files.toArray(new String[files.size()]);
        } else {
            logger.debug("Found dir {} in local path.", resourceDir);
            String[] files = new File(dir).list();
            for (int i = 0; i < files.length; i++) {
                files[i] = resourceDir + "/" + files[i];
            }
            return files;
        }
    }

    /**
     * Checks if a file is gzipped and returns the appropriate stream.
     * 
     * @throws IOException
     */
    private static InputStream checkGZipped(InputStream stream) throws IOException {
        // we need a pushbackstream to look ahead
        PushbackInputStream pb = new PushbackInputStream(stream, 2);
        byte[] signature = new byte[2];
        // read the signature
        pb.read(signature);
        // push back the signature to the stream
        pb.unread(signature);
        // check if matches standard gzip magic number
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b)
            return new GZIPInputStream(pb);
        return pb;
    }
}
