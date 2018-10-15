/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.tagger;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This annotation job reads data from an input file and produces output in a text output file
 * (bracket format).
 * 
 * @author redman
 */
public class FileIOAnnotationJob implements AnnotationJob {

    /** the data once read. */
    protected String data = null;

    /** the inpuyt file to read the data from. */
    protected String inputfile = null;

    /** the file to write the data to. */
    protected String outputfile = null;

    /** once processed the text annotation object. */
    protected TextAnnotation textAnnotation = null;

    /** the view labels the text annotation above. */
    protected View view = null;

    /** the text annotation builder. */
    private TextAnnotationBuilder tab;

    /** the annotator. */
    private NERAnnotator nerAnnotator = null;

    /**
     * We must be provided an input and output file name.
     * 
     * @param inputfile input file name.
     * @param outputfile output file name.
     */
    protected FileIOAnnotationJob(String inputfile, String outputfile, TextAnnotationBuilder tab,
            NERAnnotator ann) {
        this.inputfile = inputfile;
        this.outputfile = outputfile;
        this.tab = tab;
        this.nerAnnotator = ann;
    }

    /**
     * date is read from a plain text file.
     */
    @Override
    public String getData() {
        data = InFile.readFileText(inputfile);
        return data;
    }

    @Override
    public void labelData() {
        textAnnotation = tab.createTextAnnotation(data);
        nerAnnotator.addView(textAnnotation);
        view = textAnnotation.getView(nerAnnotator.getViewName());
    }

    @Override
    public void publishResults() {
        String pub = getNERString();

        OutFile of = new OutFile(outputfile);
        try {
            of.print(pub);
        } finally {
            of.close();
        }
    }

    protected String getNERString() {
        List<Constituent> constituents = new ArrayList<>(view.getConstituents());
        Collections.sort(constituents, TextAnnotationUtilities.constituentStartComparator);
        StringBuilder sb = new StringBuilder();
        String text = textAnnotation.getText();
        int where = 0;
        for (Constituent c : constituents) {
            int start = c.getStartCharOffset();
            String startstring = text.substring(where, start);
            sb.append(startstring).append("[").append(c.getLabel()).append(" ")
                    .append(c.getTokenizedSurfaceForm()).append(" ] ");
            where = c.getEndCharOffset();
        }
        return sb.toString();
    }
}
