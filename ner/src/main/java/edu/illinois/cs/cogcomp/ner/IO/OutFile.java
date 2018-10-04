/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.IO;

import java.io.FileOutputStream;
import java.io.PrintStream;


public class OutFile {
    public PrintStream out = null;

    public OutFile(String filename) {
        try {
            out = new PrintStream(new FileOutputStream(filename));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void println(String s) {
        out.println(s);
    }

    public void print(String s) {
        out.print(s);
    }

    public void close() {
        out.flush();
        out.close();
    }
}
