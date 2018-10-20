/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by mssammon on 6/10/17.
 */
public class XmlTextAnnotationMakerOntonotesTest {


    static final private Set<String> tagsWithText = new HashSet<>();
    static final private Set<String> dropTags = new HashSet<>();
    static final private Set<String> REF_ENTITIES = new HashSet<>();


    // define the attributes we want to keep for the tags we have.
    static final private Map<String, Set<String>> tagsWithAtts = new HashMap<>();
    static {
        Set<String> docAttrs = new HashSet<>();
        docAttrs.add("docno");
        tagsWithAtts.put("doc", docAttrs);
        Set<String> nameAttrs = new HashSet<>();
        nameAttrs.add("type");
        tagsWithAtts.put("enamex", nameAttrs);

        REF_ENTITIES.add("Paula");
        REF_ENTITIES.add("Paula Zahn");
    }

    /**
     * the edit offsets get messed up when there are nested tags.
     */
    @Test
    public void testNestedNames() {

        String text = "He spoke with Paul <ENAMEX TYPE=\"PERSON\"><ENAMEX TYPE=\"PERSON\" E_OFF=\"1\">Paula</ENAMEX> Zahn</ENAMEX> .";
        // we keep everything.
        XmlDocumentProcessor xmlProcessor = new XmlDocumentProcessor(tagsWithText, tagsWithAtts, dropTags, true);
        StatefulTokenizer st = new StatefulTokenizer();
        TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(st);
        XmlTextAnnotationMaker xtam = new XmlTextAnnotationMaker(taBuilder, xmlProcessor);

        // read the file and create the annotation.
        XmlTextAnnotation xta = xtam.createTextAnnotation(text, "OntoNotes 5.0", "test");
        TextAnnotation ta = xta.getTextAnnotation();
        List<XmlDocumentProcessor.SpanInfo> fudge = xta.getXmlMarkup();

        StringTransformation xst = xta.getXmlSt();

        for (XmlDocumentProcessor.SpanInfo si : fudge) {
            int newTextStart = xst.computeModifiedOffsetFromOriginal(si.spanOffsets.getFirst());
            int newTextEnd = xst.computeModifiedOffsetFromOriginal(si.spanOffsets.getSecond());
            String neStr = ta.getText().substring(newTextStart, newTextEnd);
            assertTrue(REF_ENTITIES.contains(neStr));
        }

    }
}
