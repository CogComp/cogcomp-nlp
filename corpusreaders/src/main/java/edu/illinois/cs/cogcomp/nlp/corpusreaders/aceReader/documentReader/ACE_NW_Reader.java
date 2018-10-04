/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader;

import edu.illinois.cs.cogcomp.core.constants.DocumentMetadata;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ACE_NW_Reader {
    private static Logger logger = LoggerFactory.getLogger(ACE_NW_Reader.class);

    /**
     * TODO: make the compiled patterns static fields.
     */
    static boolean isDebug = false;

    public static Pair<List<Pair<String, Paragraph>>, Map<String, String>> parse(String content,
                                                                                 String contentRemovingTags) {
        List<Pair<String, Paragraph>> paragraphs = new ArrayList<>();
        Map<String, String> metadata = new HashMap<>();

        Pattern pattern = null;
        Matcher matcher = null;

        String docID = "";
        String dateTime = "";
        String headLine = "";
        String text = "";

        pattern = Pattern.compile("<DOCID>(.*?)</DOCID>");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            docID = (matcher.group(1)).trim();
        }
        metadata.put(DocumentMetadata.DocumentID, docID);

        pattern = Pattern.compile("<DATETIME>(.*?)</DATETIME>");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            dateTime = (matcher.group(1)).trim();
        }
        metadata.put(DocumentMetadata.DocumentCreationTime, dateTime);

        pattern = Pattern.compile("<HEADLINE>(.*?)</HEADLINE>");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            headLine = (matcher.group(1)).trim();
        }
        metadata.put(DocumentMetadata.HeadLine, headLine);

        pattern = Pattern.compile("<TEXT>(.*?)</TEXT>");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            text = (matcher.group(1)).trim();
            int index4 = content.indexOf(text);
            Paragraph para4 = new Paragraph(index4, text);
            Pair<String, Paragraph> pair4 = new Pair<String, Paragraph>("text", para4);
            paragraphs.add(pair4);
        }

        int index = 0;
        for (int i = 0; i < paragraphs.size(); ++i) {
            int offsetWithFiltering =
                    contentRemovingTags.indexOf(paragraphs.get(i).getSecond().content, index);
            paragraphs.get(i).getSecond().offsetFilterTags = offsetWithFiltering;

            index += paragraphs.get(i).getSecond().content.length();
        }

        if (isDebug) {
            for (int i = 0; i < paragraphs.size(); ++i) {
                logger.info(paragraphs.get(i).getFirst() + "--> "
                        + paragraphs.get(i).getSecond().content);
                logger.info(content.substring(paragraphs.get(i).getSecond().offset,
                        paragraphs.get(i).getSecond().offset
                                + paragraphs.get(i).getSecond().content.length()));
                logger.info(contentRemovingTags.substring(
                        paragraphs.get(i).getSecond().offsetFilterTags, paragraphs.get(i)
                                .getSecond().offsetFilterTags
                                + paragraphs.get(i).getSecond().content.length()));
                logger.info("\n");
            }
        }

        return new Pair<>(paragraphs, metadata);
    }
}
