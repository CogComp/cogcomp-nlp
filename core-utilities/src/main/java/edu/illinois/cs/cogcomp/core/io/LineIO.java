/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.io;

import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.transformers.StringTransformer;
import edu.illinois.cs.cogcomp.core.utilities.AvoidUsing;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A container for utility functions that perform line-by-line reading and writing of files.
 *
 * @author Vivek Srikumar
 * @author upadhya3
 */
@SuppressWarnings("serial")
public class LineIO {

    private static ITransformer<String, String> identityTransformer =
            new ITransformer<String, String>() {

                public String transform(String line) {
                    return line;
                }
            };

    /**
     * Read the contents of a file and return as a single string
     */
    public static String slurp(String fileName, String charsetName) throws FileNotFoundException {
        /* use FileInputStream to handle characters outside some "standard" range -- or scanner
         * may read nothing
         * http://stackoverflow.com/questions/13881861/weird-behavior-with-java-scanner-reading-files
         */
        Scanner scanner = new Scanner(new FileInputStream(new File(fileName)), charsetName);

        StringBuilder sb = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sb.append(line).append("\n");
        }

        scanner.close();

        return sb.toString();
    }

    /**
     * Read the contents of a file using the default charset and return as a single string
     */
    public static String slurp(String fileName) throws FileNotFoundException {
        return slurp(fileName, Charset.defaultCharset().name());
    }

    /**
     * This function reads a file line-by-line and converts each line into an object using a
     * transformer that is passed as a parameter. This passes a {@code FileInputStream} created with
     * {@code fileName} into
     * {@link edu.illinois.cs.cogcomp.core.io.LineIO#read(java.io.InputStream, String, ITransformer)}
     * .
     *
     * @param <T> Each line of the file represents an object of type T
     * @param fileName The name of the file to be read
     * @param transformer A transformer from String to type T
     * @return An {@code ArrayList} containing objects of type T, with one object corresponding to
     *         each line.
     */
    public static <T> ArrayList<T> read(String fileName, String charsetName,
            ITransformer<String, T> transformer) throws FileNotFoundException {
        return read(new FileInputStream(fileName), charsetName, transformer);
    }

    /**
     * This function reads an InputStream line-by-line and converts each line into an object using a
     * transformer that is passed as a parameter.
     *
     * @return An {@code ArrayList} containing objects of type T, with one object corresponding to
     *         each line.
     */
    public static <T> ArrayList<T> read(InputStream fileStream, String charsetName,
            ITransformer<String, T> transformer) throws FileNotFoundException {
        Scanner scanner = new Scanner(fileStream, charsetName);

        ArrayList<T> list = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            list.add(transformer.transform(line));
        }

        scanner.close();

        return list;
    }

    /**
     * This function reads a gzipped file line-by-line and converts each line into an object using a
     * transformer that is passed as a parameter.
     *
     * @param <T> Each line of the file represents an object of type T
     * @param fileName The name of the file to be read
     * @param transformer A transformer from String to type T
     * @return An {@code ArrayList} containing objects of type T, with one object corresponding to
     *         each line.
     */
    public static <T> ArrayList<T> readGZip(String fileName, ITransformer<String, T> transformer)
            throws IOException {
        ArrayList<T> list = new ArrayList<>();

        GZIPInputStream zipin;
        if (IOUtils.exists(fileName))
            zipin = new GZIPInputStream(new FileInputStream(fileName));
        // We assume that someone has already checked if the file exists, therefore it's in a jar
        // somewhere
        else
            zipin =
                    new GZIPInputStream(LineIO.class.getClassLoader().getResourceAsStream(
                            fileName.substring(fileName.lastIndexOf('/') + 1)));

        BufferedReader reader = new BufferedReader(new InputStreamReader(zipin));

        String line;
        while ((line = reader.readLine()) != null) {
            list.add(transformer.transform(line));
        }

        zipin.close();

        return list;
    }

    /**
     * This function reads a file line-by-line and converts each line into an object using a
     * transformer that is passed as a parameter.
     *
     * @param <T> Each line of the file represents an object of type T
     * @param fileName The name of the file to be read
     * @param transformer A transformer from String to type T
     * @return An {@code ArrayList} containing objects of type T, with one object corresponding to
     *         each line.
     */
    public static <T> ArrayList<T> read(String fileName, ITransformer<String, T> transformer)
            throws FileNotFoundException {
        return read(fileName, Charset.defaultCharset().name(), transformer);
    }

    /**
     * An implementation of {@code read(String, Transformer<String, T>)}. This function reads the
     * lines in a file and returns them.
     *
     * @param fileName The name of the file to be read
     * @return An arraylist of the lines of the file.
     */
    public static ArrayList<String> read(String fileName, String charsetName)
            throws FileNotFoundException {
        return read(fileName, charsetName, identityTransformer);
    }

    /**
     * An implementation of {@code readGZip(String, Transformer<String, T>)}. This function reads
     * the lines in a gzipped file and returns them.
     *
     * @param fileName The name of the file to be read
     * @return An arraylist of the lines of the file.
     */
    public static ArrayList<String> readGZip(String fileName) throws IOException {
        return readGZip(fileName, identityTransformer);
    }

    /**
     * An implementation of {@code read(String, Transformer<String, T>)}. This function reads the
     * lines in a file and returns them.
     *
     * @param fileName The name of the file to be read
     * @return An arraylist of the lines of the file.
     */
    @AvoidUsing(reason = "Cannot be used for accessing files inside jars in the classpath",
            alternative = "readFromClasspath(String fileName)")
    public static ArrayList<String> read(String fileName) throws FileNotFoundException {
        return read(fileName, Charset.defaultCharset().name(), identityTransformer);
    }


    /**
     * This searches for the file on the classpath before reading it. If it doesn't find the file,
     * it throws a {@code FileNotFoundException}.
     *
     * @param fileName The name of the file to be read
     * @return An arraylist of the lines of the file.
     */
    public static ArrayList<String> readFromClasspath(String fileName) throws FileNotFoundException {
        return read(getInputStream(fileName), Charset.defaultCharset().name(), identityTransformer);
    }

    /**
     * This looks around to find the file, then returns an open inputstream. This checks the
     * classpath first, then adds a forward slash to the file name and checks the classpath again,
     * then it looks in the current directory. Note that this uses
     * {@link ClassLoader#getResourceAsStream(String)}, which works better in maven.
     *
     * @param fileName The name of the file to be read
     * @return an open input stream.
     */
    public static InputStream getInputStream(String fileName) throws FileNotFoundException {
        InputStream is = LineIO.class.getClassLoader().getResourceAsStream(fileName);

        if (is == null) {
            // try with a leading slash
            is = LineIO.class.getClassLoader().getResourceAsStream("/" + fileName);

            if (is == null) {
                is = new FileInputStream(fileName);
            }
        }
        return is;
    }

    /**
     * This function writes a list of objects into a file, one per line. Each object is transformed
     * into a string using a transformer.
     */
    public static <T> void write(String fileName, Iterable<T> list,
            ITransformer<T, String> transformer) throws IOException {
        BufferedOutputStream stream =
                new BufferedOutputStream(new FileOutputStream(fileName));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
        for (T object : list) {
            writer.write(transformer.transform(object));
            writer.newLine();
        }

        writer.close();
    }

    /**
     * This function writes a list of objects into a Gzipped file, one per line. Each object is
     * transformed into a string using a transformer.
     *
     * @param <T> Each line of the file represents an object of type T
     * @param fileName The name of the file to be read
     * @param transformer A transformer from String to type T
     */
    public static <T> void writeGZip(String fileName, Iterable<T> list,
            ITransformer<T, String> transformer) throws IOException {
        BufferedOutputStream stream =
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

        for (T object : list) {
            writer.write(transformer.transform(object));
            writer.newLine();
        }

        writer.close();
    }

    /**
     * Writes a list of strings to a file, one per line.
     */
    public static void write(String fileName, Iterable<String> list) throws IOException {
        write(fileName, list, identityTransformer);
    }

    /**
     * Writes a list of strings to a file gzipped, one per line.
     */
    public static void writeGzip(String fileName, Iterable<String> list) throws IOException {
        writeGZip(fileName, list, identityTransformer);
    }

    /**
     * Append a list of items to a file, one per line
     */
    public static <T> void append(String fileName, Iterable<T> list,
            ITransformer<T, String> transformer) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

        for (T object : list) {
            writer.write(transformer.transform(object));
            writer.newLine();
        }

        writer.close();
    }

    /**
     * Append a list of strings to a file, one per line
     */
    public static void append(String fileName, Iterable<String> list) throws IOException {
        append(fileName, list, new StringTransformer<String>());
    }

    /**
     * Append an item to a file
     */
    public static <T> void append(String fileName, T line, ITransformer<T, String> transformer)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

        writer.write(transformer.transform(line));
        writer.newLine();

        writer.close();
    }

    /**
     * Append a string to a file
     */
    public static void append(String fileName, String line) throws IOException {
        append(fileName, line, new StringTransformer<String>());
    }
}
