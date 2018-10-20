/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReaderTests;

import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Process an ner annotation file in the OntoNotes 5.0 distribution, using an XmlTextAnnotationMaker
 * Exemplifies use of StringTransformation to retrieve markup associated with cleaned text to augment
 *    TextAnnotation view
 *
 * @author mssammon
 */
public class OntonotesNerReaderExample {
    static public void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

        String inFile = "/shared/corpora/corporaWeb/multi-mode/multi/ontonotes-release-5.0/data/files/data/english/annotations/nw/wsj/00/wsj_0061.name";
        // make sure the output directory exists.

        // "en"
        int counter = 0;
        long start = System.currentTimeMillis();

        // define all tags with text.
        Set<String> tagsWithText = new HashSet<>();

        // define the attributes we want to keep for the tags we have.
        Map<String, Set<String>> tagsWithAtts = new HashMap<>();
        {
            Set<String> docAttrs = new HashSet<>();
            docAttrs.add("docno");
            tagsWithAtts.put("doc", docAttrs);
        }
        {
            Set<String> nameAttrs = new HashSet<>();
            nameAttrs.add("type");
            tagsWithAtts.put("enamex", nameAttrs);
        }

        boolean throwExceptionOnXmlParseFail = true;
        // we keep everything.
        Set<String> dropTags = new HashSet<>();
        XmlDocumentProcessor xmlProcessor = new XmlDocumentProcessor(tagsWithText, tagsWithAtts, dropTags, true);
        StatefulTokenizer st = new StatefulTokenizer();
        TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(st);
        XmlTextAnnotationMaker xtam = new XmlTextAnnotationMaker(taBuilder, xmlProcessor);

        String document = LineIO.slurp(inFile);
        XmlTextAnnotation xta = xtam.createTextAnnotation(document, "OntoNotes 5.0", "test");
        TextAnnotation ta = xta.getTextAnnotation();
        List<XmlDocumentProcessor.SpanInfo> fudge = xta.getXmlMarkup();
        System.out.println(ta + "\n");

        View nerView = new SpanLabelView(ViewNames.NER_ONTONOTES, ta);
        String cleanText = ta.getText();
        for (XmlDocumentProcessor.SpanInfo si : fudge) {
            if ("enamex".equalsIgnoreCase(si.label)) {

                IntPair charOffsets = si.spanOffsets;
                String neLabel = si.attributes.get("type").getFirst();
                int cleanTextCharStart = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getFirst());
                int cleanTextCharEnd = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getSecond());
                System.err.println("ne string: '" + cleanText.substring(cleanTextCharStart, cleanTextCharEnd) + "'");
                int cleanTextNeTokStart = ta.getTokenIdFromCharacterOffset(cleanTextCharStart);
                int cleanTextNeTokEnd = ta.getTokenIdFromCharacterOffset(cleanTextCharEnd-1); // StringTransformation returns one-past-the-end index; TextAnnotation maps at-the-end index
                Constituent neCon = new Constituent(neLabel, nerView.getViewName(), ta, cleanTextNeTokStart, cleanTextNeTokEnd + 1); //constituent token indexing uses one-past-the-end
                nerView.addConstituent(neCon);
            }
            counter++;
            System.out.println("Read " + counter + " documents in " + (System.currentTimeMillis() - start));
            System.out.println(nerView.toString());
        }
    }
}