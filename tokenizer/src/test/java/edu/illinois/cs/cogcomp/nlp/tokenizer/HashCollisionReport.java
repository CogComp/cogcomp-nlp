/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * This class will identify hash collisions within a set of documents. It is provided a directory
 * full of text documents. It will simply tokenize the the documents, and check the resulting
 * constituents for hash collisions. Collisions are reported.
 * @author redman
 */
public class HashCollisionReport {

    /**
     * report the error and exit.
     * @param message the error message.
     */
    private static void error(String message) {
        System.err.println(message);
        System.exit(-1);
    }
    
    /**
     * Read each test file in the directory, tokenize and create the token view. Then check for
     * collisions.
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            error("Must pass in the name of a directory with files to test against.");
        File dir = new File(args[0]);
        if (!dir.exists()) {
            error("The directory did not exist : "+dir);
        }
        if (!dir.isDirectory()) {
            error("The path was not a directory : "+dir);
        }
        File [] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String normal = FileUtils.readFileToString(file);
                TextAnnotationBuilder tabldr = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
                TextAnnotation taNormal = tabldr.createTextAnnotation("test", "normal", normal);
                List<Constituent> normalToks = taNormal.getView(ViewNames.TOKENS).getConstituents();
                HashMap<Integer, Constituent> hashmap = new HashMap<>();

                // add each constituent to the map keyed by it's hashcode. Check first to see if the hashcode
                // is already used, if it is report it.
                for (Constituent c : normalToks) {
                    int code = c.hashCode();
                    if (hashmap.containsKey(code)) {
                        Constituent dup = hashmap.get(code);
                        System.err.println(c+" == "+dup);
                    } else {
                        hashmap.put(code, c);
                    }
                }
            }
        }

    }
}
