/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.io;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * This project was started by Nicholas Rizzolo (rizzolo@uiuc.edu) . Most of design, development,
 * modeling and coding was done by Lev Ratinov (ratinov2@uiuc.edu). For modeling details and
 * citations, please refer to the paper: External Knowledge and Non-local Features in Named Entity
 * Recognition by Lev Ratinov and Dan Roth submitted/to appear/published at NAACL 09.
 * 
 **/

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

    public OutFile(String filename, boolean append) {
        try {
            out = new PrintStream(new FileOutputStream(filename, append));
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
        out.close();
    }
}
