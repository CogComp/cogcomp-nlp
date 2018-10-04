/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReaderTests;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACEDocumentAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACEEntity;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACEEntityMention;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.ACERelation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;


public class ACEReaderParseTest {

    public static final String ACE2005CORPUS = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/ace2005";
    public static final String ACE2004CORPUS = "src/test/resources/edu/illinois/cs/cogcomp/nlp/corpusreaders/ace2004";

    @Test
    public void test2004Dataset() throws Exception {
        String corpusHomeDir = ACE2004CORPUS;
        ACEReader reader = new ACEReader(corpusHomeDir, true);
        testReaderParse(reader, corpusHomeDir, 1);
    }

    @Test
    public void test2005Dataset() throws Exception {
        String corpusHomeDir = ACE2005CORPUS;
        ACEReader reader = new ACEReader(corpusHomeDir, false);
        testReaderParse(reader, corpusHomeDir, 1);
    }

    @Test
    public void testReaderReset() throws Exception {
        String corpusHomeDir = ACE2004CORPUS;
        ACEReader reader = new ACEReader(corpusHomeDir, true);
        testReaderParse(reader, corpusHomeDir, 1);

        reader.reset();
        testReaderParse(reader, corpusHomeDir, 1);
    }

    private void testReaderParse(ACEReader reader, String corpusHomeDir, int numberOfDocs)
            throws XMLException {
        int numDocs = 0;
        ReadACEAnnotation.is2004mode = reader.Is2004Mode();
        String corpusIdGold = reader.Is2004Mode() ? "ACE2004" : "ACE2005";

        assertTrue(reader.hasNext());
        while (reader.hasNext()) {
            TextAnnotation doc = reader.next();
            ACEDocumentAnnotation annotation =
                    ReadACEAnnotation
                            .readDocument(corpusHomeDir + File.separatorChar + doc.getId());

            assertNotNull(doc);
            assertNotNull(annotation);
            assertEquals(doc.getCorpusId(), corpusIdGold);

            Set<String> documentViews = doc.getAvailableViews();
            assertTrue(documentViews.contains(ViewNames.TOKENS));
            assertTrue(documentViews.contains(ViewNames.MENTION_ACE));
            assertTrue(documentViews.contains(ViewNames.COREF_HEAD));
            assertTrue(documentViews.contains(ViewNames.COREF_EXTENT));

            List<ACEEntityMention> entityMentionList = new ArrayList<>();
            for (ACEEntity entity : annotation.entityList) {
                entityMentionList.addAll(entity.entityMentionList);
            }

            SpanLabelView entityView = (SpanLabelView) doc.getView(ViewNames.MENTION_ACE);
            assertEquals(entityView.getNumberOfConstituents(), entityMentionList.size());

            CoreferenceView coreferenceView = (CoreferenceView) doc.getView(ViewNames.COREF_HEAD);
            assertEquals(coreferenceView.getNumberOfConstituents(), entityMentionList.size());

            CoreferenceView coreferenceExtentView = (CoreferenceView) doc.getView(ViewNames.COREF_EXTENT);
            assertEquals(coreferenceExtentView.getNumberOfConstituents(), entityMentionList.size());

            int relationMentions = 0;
            for (ACERelation relation : annotation.relationList) {
                relationMentions += relation.relationMentionList.size();
            }

            assertEquals(entityView.getRelations().size(), relationMentions);

            // Sort entityMention annotation based on their extent starts
            Collections.sort(entityMentionList, new Comparator<ACEEntityMention>() {
                @Override
                public int compare(ACEEntityMention o1, ACEEntityMention o2) {
                    return Integer.compare(o1.extentStart, o2.extentStart);
                }
            });

            int index = 0;
            for (Constituent mention : entityView.getConstituents()) {
                ACEEntityMention mentionAnnotation = entityMentionList.get(index++);

                int startTokenId =
                        doc.getTokenIdFromCharacterOffset(
                                Integer.parseInt(
                                        mention.getAttribute(ACEReader.EntityHeadStartCharOffset)));
                int endTokenId =
                        doc.getTokenIdFromCharacterOffset(
                                Integer.parseInt(
                                        mention.getAttribute(ACEReader.EntityHeadEndCharOffset)) - 1) + 1;

                // Get the HEAD Mention String
                String headMentionString = TextAnnotationUtilities.getTokenSequence(doc, startTokenId, endTokenId);

                // Assert that HEAD Mention String is same as in the mention annotation.
                assertTrue(
                        String.format("Expected :\"%s\", Found:\"%s\"", headMentionString, mentionAnnotation.head),
                        headMentionString.equalsIgnoreCase(mentionAnnotation.head));
            }

            numDocs++;
        }

        assertEquals(numberOfDocs, numDocs);
    }



    @Test
    public void testAce2005()
    {
        boolean is2004 = false;
        ACEReader reader = null;
        try {
            reader = new ACEReader( ACE2005CORPUS, is2004 );
        } catch (Exception e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assertTrue( reader.hasNext() );
    }
}
