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
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

/**
 * This utility will produce data in CoNLL 2002 or 2003 format. The provided TextAnnotation must
 * contain a <code>View.NER</code> view, since 2002 has only two columns, the token and the NER
 * label.
 * 
 * @author redman
 */
public class CoNLL2002Writer {

    /**
     * Pass in the view, text annotation and the filename, it will produce the labels in the view to
     * a file named filename in CoNLL2002 format.
     * 
     * @param view the view with the labels to produce.
     * @param ta the text annotation.
     * @param filename the filename.
     * @throws IOException
     */
    static public void writeViewInCoNLL2002Format(View view, TextAnnotation ta, String filename)
            throws IOException {
        String text = produceCoNLL2002Annotations(view, ta);
        FileUtils.writeStringToFile(new File(filename), text);
    }

    /**
     * Pass in the view, text annotation and the filename, it will produce the labels in the view to
     * a file named filename in CoNLL2003 format.
     * 
     * @param view the view with the labels to produce.
     * @param ta the text annotation.
     * @param filename the filename output file name.
     * @throws IOException
     */
    static public void writeViewInCoNLL2003Format(View view, TextAnnotation ta, String filename)
            throws IOException {
        String text = produceCoNLL2003Annotations(view, ta);
        FileUtils.writeStringToFile(new File(filename), text);
    }

    /**
     * Render a string representing the original data with embedded labels in the text.
     * 
     * @param view the NER label view.
     * @param ta the text annotation.
     * @return the original text marked up with the annotations.
     */
    static private String produceCoNLL2002Annotations(View view, TextAnnotation ta) {
        StringBuilder sb = new StringBuilder();

        // get the tokens.
        List<Constituent> tokens = new ArrayList<>(ta.getView(ViewNames.TOKENS).getConstituents());
        Collections.sort(tokens, TextAnnotationUtilities.constituentStartEndComparator);

        // get the sentences.
        List<Constituent> sentences =
                new ArrayList<>(ta.getView(ViewNames.SENTENCE).getConstituents());
        Collections.sort(sentences, TextAnnotationUtilities.constituentStartEndComparator);

        // get the entities
        List<Constituent> entities = new ArrayList<>(view.getConstituents());
        Collections.sort(entities, TextAnnotationUtilities.constituentStartEndComparator);
        int entityindx = 0;
        int sentenceindex = 0;
        int sentenceEndIndex = sentences.get(sentenceindex).getEndCharOffset();
        for (Constituent token : tokens) {

            // make sure we have the next entity.
            for (; entityindx < entities.size(); entityindx++) {
                Constituent entity = entities.get(entityindx);
                if (token.getStartCharOffset() <= entity.getStartCharOffset())
                    break;
                else if (token.getEndCharOffset() <= entity.getEndCharOffset())
                    break; // we are inside of the entity.
            }
            sb.append(token.getSurfaceForm());
            sb.append(' ');
            if (entityindx < entities.size()) {
                Constituent entity = entities.get(entityindx);
                if (token.getStartCharOffset() == entity.getStartCharOffset()) {
                    if (token.getEndCharOffset() == entity.getEndCharOffset()) {
                        sb.append("U-" + entity.getLabel());
                    } else if (token.getEndCharOffset() > entity.getEndCharOffset()) {
                        sb.append("U-" + entity.getLabel());
                        System.err
                                .println("Odd. There is an entity enclosed within a single token!");
                    } else {
                        sb.append("B-" + entity.getLabel());
                    }
                } else if (token.getStartCharOffset() > entity.getStartCharOffset()) {
                    if (token.getEndCharOffset() <= entity.getEndCharOffset()) {
                        sb.append("I-" + entity.getLabel());
                    } else {
                        sb.append('O');
                    }
                } else {
                    sb.append('O');
                }
            } else {
                sb.append('O');
            }
            sb.append('\n');
            if (token.getEndCharOffset() >= sentenceEndIndex) {
                sb.append('\n');
                if (sentenceindex < (sentences.size() - 1))
                    sentenceindex++;
                sentenceEndIndex = sentences.get(sentenceindex).getEndCharOffset();
            }
        }
        return sb.toString();
    }

    /**
     * Render a string representing the original data with embedded labels in the text.
     * 
     * @param view the NER label view.
     * @param ta the text annotation.
     * @return the original text marked up with the annotations.
     */
    static private String produceCoNLL2003Annotations(View view, TextAnnotation ta) {
        StringBuilder sb = new StringBuilder();
        sb.append("O\t0\t0\tO\t-X-\t-DOCSTART-\tx\tx\t0\n\n");

        // get the tokens.;
        List<Constituent> tokens = new ArrayList<>(ta.getView(ViewNames.TOKENS).getConstituents());
        Collections.sort(tokens, TextAnnotationUtilities.constituentStartEndComparator);

        // get the sentences.
        List<Constituent> sentences =
                new ArrayList<>(ta.getView(ViewNames.SENTENCE).getConstituents());
        Collections.sort(sentences, TextAnnotationUtilities.constituentStartEndComparator);

        // get the entities
        List<Constituent> entities = new ArrayList<>(view.getConstituents());
        Collections.sort(entities, TextAnnotationUtilities.constituentStartEndComparator);
        int entityindx = 0;
        int sentenceindex = 0;
        int sentenceEndIndex = sentences.get(sentenceindex).getEndCharOffset();
        int entityWithinSentence = 0;
        for (Constituent token : tokens) {

            // make sure we have the next entity.
            for (; entityindx < entities.size(); entityindx++) {
                Constituent entity = entities.get(entityindx);
                if (token.getStartCharOffset() <= entity.getStartCharOffset())
                    break;
                else if (token.getEndCharOffset() <= entity.getEndCharOffset())
                    break; // we are inside of the entity.
            }
            // write entity name
            if (entityindx < entities.size()) {
                Constituent entity = entities.get(entityindx);
                if (token.getStartCharOffset() == entity.getStartCharOffset()) {
                    if (token.getEndCharOffset() == entity.getEndCharOffset()) {
                        sb.append("U-" + entity.getLabel());
                    } else if (token.getEndCharOffset() > entity.getEndCharOffset()) {
                        sb.append("U-" + entity.getLabel());
                        System.err
                                .println("Odd. There is an entity enclosed within a single token!");
                    } else {
                        sb.append("B-" + entity.getLabel());
                    }
                } else if (token.getStartCharOffset() > entity.getStartCharOffset()) {
                    if (token.getEndCharOffset() <= entity.getEndCharOffset()) {
                        sb.append("I-" + entity.getLabel());
                    } else {
                        sb.append('O');
                    }
                } else {
                    sb.append('O');
                }
            } else {
                sb.append('O');
            }

            // sentence index I guess
            sb.append("\t"+sentenceindex+"\t");

            // token offset
            sb.append(entityWithinSentence);
            entityWithinSentence++;

            // part of speech stuff
            sb.append("\tO\tO\t");

            // the token value
            sb.append(token.getSurfaceForm());

            // x, x and a zero.
            sb.append("\tx\tx\t0");
            sb.append('\n');
            if (token.getEndCharOffset() >= sentenceEndIndex) {
                sb.append('\n');
                entityWithinSentence = 0;
                if (sentenceindex < (sentences.size() - 1))
                    sentenceindex++;
                sentenceEndIndex = sentences.get(sentenceindex).getEndCharOffset();
            }
        }
        return sb.toString();
    }
}
