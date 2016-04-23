/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReaderTests;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACEDocumentAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACEEntity;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACERelation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Bhargav Mangipudi on 4/7/16.
 */
public class ACEReaderParseTest {

    @Ignore("ACE Dataset files will not be commited to repo.")
    @Test
    public void test2004Dataset() throws Exception {
        String corpusHomeDir = "src/test/resources/ACE/ace2004/data/English";
        ACEReader reader = new ACEReader(corpusHomeDir, true);
        testReaderParse(reader, corpusHomeDir, 2);
    }

    @Ignore("ACE Dataset files will not be commited to repo.")
    @Test
    public void test2005Dataset() throws Exception {
        String corpusHomeDir = "src/test/resources/ACE/ace2005/data/English";
        ACEReader reader = new ACEReader(corpusHomeDir, false);
        testReaderParse(reader, corpusHomeDir, 6);
    }

    private void testReaderParse(ACEReader reader, String corpusHomeDir, int numberOfDocs) throws XMLException {
        int numDocs = 0;
        ReadACEAnnotation.is2004mode = reader.Is2004Mode();
        String corpusIdGold = reader.Is2004Mode() ? "ACE2004" : "ACE2005";

        assertTrue(reader.hasNext());
        while (reader.hasNext()) {
            TextAnnotation doc = reader.next();
            ACEDocumentAnnotation annotation = ReadACEAnnotation.readDocument(corpusHomeDir + File.separatorChar + doc.getId());

            assertNotNull(doc);
            assertNotNull(annotation);
            assertEquals(doc.getCorpusId(), corpusIdGold);

            Set<String> documentViews = doc.getAvailableViews();
            assertTrue(documentViews.contains(ViewNames.TOKENS));
            assertTrue(documentViews.contains(ACEReader.ENTITYVIEW));
            assertTrue(documentViews.contains(ACEReader.ENTITYVIEW_COARSE));
            assertTrue(documentViews.contains(ACEReader.ENTITYVIEW_FINE));
            assertTrue(documentViews.contains(ACEReader.RELATIONVIEW));
            assertTrue(documentViews.contains(ACEReader.RELATIONVIEW_COARSE));
            assertTrue(documentViews.contains(ACEReader.RELATIONVIEW_FINE));
            assertTrue(documentViews.contains(ViewNames.COREF));

            int entityMentions = 0;
            for (ACEEntity entity : annotation.entityList) entityMentions += entity.entityMentionList.size();

            SpanLabelView entityView = (SpanLabelView) doc.getView(ACEReader.ENTITYVIEW_COARSE);
            assertEquals(entityView.getNumberOfConstituents(), entityMentions);

            SpanLabelView entityFineView = (SpanLabelView) doc.getView(ACEReader.ENTITYVIEW_FINE);
            assertEquals(entityFineView.getNumberOfConstituents(), entityMentions);

            CoreferenceView coreferenceView = (CoreferenceView) doc.getView(ViewNames.COREF);
            assertEquals(coreferenceView.getNumberOfConstituents(), entityMentions);

            int relationMentions = 0;
            for (ACERelation relation : annotation.relationList) relationMentions += relation.relationMentionList.size();

            PredicateArgumentView relationView = (PredicateArgumentView) doc.getView(ACEReader.RELATIONVIEW_COARSE);
            assertEquals(relationView.getPredicates().size(), relationMentions);

            PredicateArgumentView relationFineView = (PredicateArgumentView) doc.getView(ACEReader.RELATIONVIEW_FINE);
            assertEquals(relationFineView.getPredicates().size(), relationMentions);

            numDocs++;
        }

        assertEquals(numDocs, numberOfDocs);
    }
}
