/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor.SpanInfo;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.CoNLL2002Writer;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator.Language;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * convert the Ontonotes 5.0 format to CoNLL 2003 column format. Input arguments are
 * required, the data directory, language, file kind and so on.
 * @author redman
 */
public class ConvertOntonotesToColumn {
    /** the tags. */
    static final private Set<String> tagsWithText = new HashSet<>();
    /** tags to ignore. */
    static final private Set<String> dropTags = new HashSet<>();

    /** define the attributes we want to keep for the tags we have. */
    static final private Map<String, Set<String>> tagsWithAtts = new HashMap<>();
    static {
        Set<String> docAttrs = new HashSet<>();
        docAttrs.add("docno");
        tagsWithAtts.put("doc", docAttrs);
        Set<String> nameAttrs = new HashSet<>();
        nameAttrs.add("type");
        tagsWithAtts.put("enamex", nameAttrs);
    }

    /**
     * read the file indicated by the argument which is the file name, and path.
     * @param file the file to read.
     * @param document the data read from the file.
     * @return the XmlTextAnnotation containing the text annotation, and xml markup offset data.
     * @throws IOException 
     */
    static private XmlTextAnnotation getNameTextAnnotation(File file) throws IOException {
        String document = LineIO.slurp(file.getCanonicalPath());
        // we keep everything.
        XmlDocumentProcessor xmlProcessor = new XmlDocumentProcessor(tagsWithText, tagsWithAtts, dropTags, true);
        StatefulTokenizer st = new StatefulTokenizer();
        TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(st);
        XmlTextAnnotationMaker xtam = new XmlTextAnnotationMaker(taBuilder, xmlProcessor);

        // read the file and create the annotation.
        XmlTextAnnotation xta = xtam.createTextAnnotation(document, "OntoNotes 5.0", "test");
        TextAnnotation ta = xta.getTextAnnotation();
        List<SpanInfo> fudge = xta.getXmlMarkup();

        // create the named entity vi
        View nerView = new SpanLabelView(ViewNames.NER_ONTONOTES, ta);
        for (SpanInfo si : fudge) {
            if ("enamex".equalsIgnoreCase(si.label)) {

                IntPair charOffsets = si.spanOffsets;
                String neLabel = si.attributes.get("type").getFirst();
                int cleanTextCharStart = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getFirst());
                int cleanTextCharEnd = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getSecond());
                int cleanTextNeTokStart = ta.getTokenIdFromCharacterOffset(cleanTextCharStart);
                int cleanTextNeTokEnd = ta.getTokenIdFromCharacterOffset(cleanTextCharEnd - 1); // StringTransformation returns one-past-the-end index; TextAnnotation maps at-the-end index
                Constituent neCon = new Constituent(neLabel, nerView.getViewName(), ta, cleanTextNeTokStart, cleanTextNeTokEnd + 1); //constituent token indexing uses one-past-the-end
                nerView.addConstituent(neCon);
            }
        }
        ta.addView(ViewNames.NER_ONTONOTES, nerView);
        return xta;
    }

    /**
     * This is used primarily for quick testing. The bulk of this should be used as a
     * starting point for a unit test.
     * @param args
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException 
     */
    static public void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("This executable requires four arguments:\n" 
        + " ConvertOntonotesToColumn <OntoNotes Directory> <language>" + " <file_kind> <output_directory>");
            System.exit(-1);
        }
        String topdir = args[0];
        Language language = null;
        try {
            language = DocumentIterator.Language.valueOf(args[1]);
        } catch (IllegalArgumentException iae) {
            System.out.print("language must be one of : ");
            for (DocumentIterator.Language l : DocumentIterator.Language.values())
                System.out.print(l.toString() + " ");
            System.out.println();
            System.exit(-1);
        }

        DocumentIterator.FileKind kind = null;
        try {
            kind = DocumentIterator.FileKind.valueOf(args[2]);
        } catch (IllegalArgumentException iae) {
            System.out.print("file_kind must be one of : ");
            for (DocumentIterator.FileKind l : DocumentIterator.FileKind.values())
                System.out.print(l.toString() + " ");
            System.out.println();
            System.exit(-1);
        }
        String outputdir = args[3];

        // make sure the output directory exists.
        File f = new File(outputdir);
        if (f.exists()) {
            if (!f.isDirectory()) {
                System.err.println("The output directory must specify a directory:\n" + " ConvertOntonotesToColumn <OntoNotes Directory> <language>" + " <file_kind> <output_directory>");
            }
        } else {
            f.mkdirs();
        }

        // make sure there is a file separator.
        if (!outputdir.endsWith(File.separator))
            outputdir += File.separator;

        // "en"
        DocumentIterator di = new DocumentIterator(topdir, language, kind);
        int counter = 0;
        long start = System.currentTimeMillis();
        while (di.hasNext()) {
            File document = di.next();
            TextAnnotation ta = getNameTextAnnotation(document).getTextAnnotation();
            String path = document.getAbsolutePath();
            path = outputdir + path.substring(topdir.length());
            path += ".conll";
            System.out.println(counter + ":" + path);
            CoNLL2002Writer.writeViewInCoNLL2003Format(ta.getView(ViewNames.NER_ONTONOTES), ta, path);
            counter++;
        }
        System.out.println("Read " + counter + " documents in " + (System.currentTimeMillis() - start));
    }
}