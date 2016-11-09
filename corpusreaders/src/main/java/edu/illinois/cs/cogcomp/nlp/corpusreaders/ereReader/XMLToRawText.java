/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;

/**
 * @author redman
 *
 */
public class XMLToRawText {
    /** these tags contain attributes we want to keep. */
    static private ArrayList<String> retainTags = new ArrayList<String>();
    /** the attributes to keep for the above tags. */
    static private ArrayList<String> retainAttributes = new ArrayList<String>();
    
    static {
        retainTags.add("quote");
        retainTags.add("post");
    }

    static {
        retainAttributes.add("orig_author");
        retainAttributes.add("author");
    }

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        final String source = 
                        "/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/source/";
        final String stripped = 
                        "/Volumes/xdata/CCGStuff/LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3/data/stripped/";

        File[] sourcefiles = new File(source).listFiles();
        for (File fsourcefile : sourcefiles) {
            String sourcefile = fsourcefile.getAbsolutePath();
            String resultfile = stripped+fsourcefile.getName()+".txt";
            String origText = LineIO.slurp(sourcefile);
            String strippedText = TextCleaner.removeXmlLeaveAttributes(origText, retainTags, retainAttributes);
            
            try (PrintWriter out = new PrintWriter(resultfile)) {
                out.println (strippedText);
            }
        }
    }
}
