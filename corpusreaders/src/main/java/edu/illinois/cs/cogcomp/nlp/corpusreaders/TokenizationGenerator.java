/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import java.io.File;
import java.io.IOException;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.CoNLL2002Writer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

public class TokenizationGenerator {
    /**
     * Just for testing.
     * 
     * @param args
     * @throws IOException 
     */
    static public void main(String[] args) throws IOException {
        String outputIllinois = "/Volumes/xdata/CCGStuff/ace_2005/doc/ilTokens/";
        String outputState = "/Volumes/xdata/CCGStuff/ace_2005/doc/stateTokens/";
        File[] files = new File("/Volumes/xdata/CCGStuff/ace_2005/doc/bn/").listFiles();
        for (File file : files) {
            String ap = file.getAbsolutePath();
            if (ap.endsWith(".sgm")) {
                System.out.println("\n"+file.getName());
                String original = LineIO.slurp(ap);
                String issue = TextCleaner.replaceXmlTags(original);
                String fn = file.getName();
                final TextAnnotationBuilder tab =
                        new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
                TextAnnotation ta1 = tab.createTextAnnotation(issue);
                final TextAnnotationBuilder stab =
                        new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
                TextAnnotation ta2 = stab.createTextAnnotation(issue);
                View illinois = ta1.getView(ViewNames.TOKENS);
                View stateful = ta2.getView(ViewNames.TOKENS);
                CoNLL2002Writer.writeViewInCoNLL2003Format(illinois, 
                    ta1, outputIllinois+fn);
                CoNLL2002Writer.writeViewInCoNLL2003Format(stateful, 
                    ta2, outputState+fn);
                int statefulindex = 0;
                int illinoisindex = 0;
                while (true) {
                    if (illinoisindex >= illinois.count())
                        break;
                    Constituent ilc = illinois.getConstituents().get(illinoisindex);
                    if (statefulindex >= stateful.count())
                        break;
                    Constituent stc = stateful.getConstituents().get(statefulindex);
                    String illinoisSurfaceForm = ilc.getSurfaceForm();
                    String stateSurfaceForm = stc.getSurfaceForm();
                    if (illinoisSurfaceForm.equals(stateSurfaceForm)) {
                        //.out.println(illinoisSurfaceForm+"\t"+stateSurfaceForm);
                        statefulindex++;
                        illinoisindex++;
                    } else {
                        System.out.println(illinoisindex+"-\""+illinoisSurfaceForm+"\"\t"+
                                        statefulindex+"-\""+stateSurfaceForm+"\"");
                        if (illinoisSurfaceForm.endsWith(stateSurfaceForm)) {
                            statefulindex++;
                            illinoisindex++;
                        } else if (stateSurfaceForm.endsWith(illinoisSurfaceForm)) {
                            statefulindex++;
                            illinoisindex++;
                        } else if (illinoisSurfaceForm.contains(stateSurfaceForm)) {
                            statefulindex++;
                        } else if (stateSurfaceForm.contains(illinoisSurfaceForm)) {
                            illinoisindex++;
                        } else {
                            statefulindex++;
                            illinoisindex++;
                        }
                    }
                }
            }
        }
    }

}
