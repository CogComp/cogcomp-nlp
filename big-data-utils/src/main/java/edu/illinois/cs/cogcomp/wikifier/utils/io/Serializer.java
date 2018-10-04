/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.io;

/**
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;


/**
 * Default Java serializer for quick storage
 * @author Xiao Cheng
 *
 */
public class Serializer {

    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    private static ObjectInputStream getInput(String inputFile) throws IOException {
        // use buffering
        InputStream file = new FileInputStream(inputFile);
        InputStream buffer = new BufferedInputStream(file);
        return new ObjectInputStream(buffer);
    }

    private static ObjectOutputStream getOutput(String output) throws IOException {
        // use buffering
        OutputStream file = new FileOutputStream(output);
        OutputStream buffer = new BufferedOutputStream(file);
        return new ObjectOutputStream(buffer);
    }

    public static Object read(String filename) {
        return read(new File(filename));
    }

    public static Object read(File inputFile) {
        Object doc = null;
        try {
            ObjectInput input = getInput(inputFile.getPath());
            try {
                doc = input.readObject();
            } finally {
                input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return doc;
    }

    public static void read(Externalizable obj, File file) throws IOException {
        read(file.getPath());
    }

    public static void read(Externalizable obj, String file) throws IOException {
        try {
            obj.readExternal(getInput(file));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void write(Externalizable obj, String file) throws IOException {
        obj.writeExternal(getOutput(file));
    }

    // public static <T> T readKryo(String inputFile,Class<? extends T> c){
    // try {
    // return kryo.readObject(new Input(new FileInputStream(inputFile)), c);
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // }
    // return null;
    // }

    public static void write(Serializable ob, String outputFile) {
        write(ob, new File(outputFile));
    }

    // public static void writeKryo(Object ob, String outputFile){
    // try {
    // kryo.writeObject(new Output(new FileOutputStream(outputFile)), ob);
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // }
    // }

    public static void write(Serializable ob, File outputCheck) {
        try {
            Serializable o = ob;

            if (outputCheck.getParentFile() != null)
                outputCheck.getParentFile().mkdirs();
            outputCheck.createNewFile();
            ObjectOutput output = getOutput(outputCheck.getPath());
            try {
                output.writeObject(o);
            } finally {
                output.close();
            }
        } catch (IOException ex) {
            System.err.println("Cannot write to output." + ex);
        }
    }

}
