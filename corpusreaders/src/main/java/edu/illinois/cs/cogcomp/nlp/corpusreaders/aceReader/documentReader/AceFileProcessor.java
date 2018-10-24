/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.EventConstants;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.EventConstants.*;

/**
 * reads a set of related ACE files corresponding to a single document, builds an ACEDocument object
 *
 * Created by mssammon on 8/27/15.
 */
public class AceFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ACEReader.class);

    public AceFileProcessor() { }

    /**
     * given a subfolder entry corresponding to a single ACE document, build an ACEDocument object
     * with a simple TextAnnotation for the text (with tokenization and sentence splits), and fields
     * representing the different types of ACE annotation (entities, relations, events, etc.)
     *
     * @param subFolderEntry
     * @param annotationFile full path to main annotation file (.apf.xml)
     * @return null if file cannot be parsed, ACEDocument otherwise
     */
    public ACEDocument processAceEntry(File subFolderEntry,
                                       String annotationFile)
            throws FileNotFoundException {

        try {
            ACEDocumentAnnotation annotationACE = ReadACEAnnotation.readDocument(annotationFile);
            return processAceEntry(subFolderEntry, annotationACE, annotationFile);
        } catch (XMLException e) {
            logger.error("Error while processing ACE Document " + annotationFile, e);
        }

        return null;
    }

    /**
     * processes the main annotation file-- accessing other files with same prefix in same directory
     * -- to create an ACEDocument structure. The ACEDocument has a list of TextAnnotations
     * corresponding to paragraphs, and which also have a view recording character offsets
     * corresponding to ACE gold annotations, but no views corresponding to the gold annotations
     * themselves (relations, entities etc.).
     *
     * The gold annotations are kept in lists of ACE-specific data structures.
     *
     * @param subFolderEntry
     * @param annotationACE
     * @param annotationFile
     * @return
     */
    private ACEDocument processAceEntry(File subFolderEntry,
                                        ACEDocumentAnnotation annotationACE,
                                        String annotationFile) throws FileNotFoundException {
        boolean is2004mode = ReadACEAnnotation.is2004mode;
        ACEDocument aceDoc = new ACEDocument();

        String docFile = annotationFile.replace(".apf.xml", ".sgm");
        List<String> lines = LineIO.read(docFile);
        String content = "";
        for (String line : lines) {
            content += line + "\n";
        }

        // Some files have stray "<ANNOTATION>" tags. Removing them before processing.
        content = content.replaceAll("<ANNOTATION>", "").replaceAll("</ANNOTATION>", "");
        String contentWithoutEnter = content.replaceAll("\n", " ");

        int textContentStartPosition = contentWithoutEnter.indexOf("<TEXT>");
        int textContentEndPosition = contentWithoutEnter.lastIndexOf("</TEXT>");

        // Handling special cases
        if (subFolderEntry.getAbsolutePath().endsWith("nw")) {
            int headlineEndOffset = content.indexOf("</HEADLINE>");
            if (headlineEndOffset != -1 && headlineEndOffset < textContentStartPosition) {
                textContentStartPosition = headlineEndOffset;
            }
        } else if (subFolderEntry.getAbsolutePath().endsWith("un")) {
            // Entity Annotation are present in headlines as well.
            int headlineStartOffset = content.indexOf("<HEADLINE>");
            if (headlineStartOffset != -1 && headlineStartOffset < textContentStartPosition) {
                textContentStartPosition = headlineStartOffset;
            }
        }
        StringBuilder textContentWithoutTagsBuilder = new StringBuilder();

        // Replace context before the TEXT start with appropriate number of spaces.
        String preTextMarketString = removeXMLTags(contentWithoutEnter.substring(0, textContentStartPosition));
        for (int i = 0; i < preTextMarketString.length(); i++) textContentWithoutTagsBuilder.append(" ");

        // Extract the text content for the document.
        String textContent = removeXMLTags(
                contentWithoutEnter.substring(textContentStartPosition, textContentEndPosition));
        textContentWithoutTagsBuilder.append(textContent);

        String contentRemovingTags = textContentWithoutTagsBuilder.toString();

        Pair<List<Pair<String, Paragraph>>, Map<String, String>> parsedResult = null;
        if (subFolderEntry.getAbsolutePath().endsWith("bc")) {
            parsedResult = ACE_BC_Reader.parse(contentWithoutEnter, contentRemovingTags);
        } else if (subFolderEntry.getAbsolutePath().endsWith("bn")) {
            parsedResult = ACE_BN_Reader.parse(contentWithoutEnter, contentRemovingTags, is2004mode);
        } else if (subFolderEntry.getAbsolutePath().endsWith("cts")) {
            parsedResult = ACE_CTS_Reader.parse(contentWithoutEnter, contentRemovingTags);
        } else if (subFolderEntry.getAbsolutePath().endsWith("nw")) {
            parsedResult = ACE_NW_Reader.parse(contentWithoutEnter, contentRemovingTags);
        } else if (subFolderEntry.getAbsolutePath().endsWith("un")) {
            parsedResult = ACE_UN_Reader.parse(contentWithoutEnter, contentRemovingTags);
        } else if (subFolderEntry.getAbsolutePath().endsWith("wl")) {
            parsedResult = ACE_WL_Reader.parse(contentWithoutEnter, contentRemovingTags);
        }

        // Normalize the text content
        String cleanedTextContent = StringCleanup.normalizeToAscii(contentRemovingTags);

        aceDoc.aceAnnotation = annotationACE;
        aceDoc.orginalContent = content;
        aceDoc.contentRemovingTags = cleanedTextContent;
        aceDoc.originalLines = lines;

        if (parsedResult != null) {
            aceDoc.paragraphs = parsedResult.getFirst();
            aceDoc.metadata = parsedResult.getSecond();
        }

        return aceDoc;
    }

    /**
     * Remove XML tags (both opening and closing) from the input text string.
     */
    private static String removeXMLTags(String inputText) {
        if (inputText == null) return null;

        while (inputText.contains("<")) {
            int p = inputText.indexOf('<');
            int q = inputText.indexOf('>');
            inputText = inputText.substring(0, p) + inputText.substring(q + 1, inputText.length());
        }

        return inputText;
    }
}
