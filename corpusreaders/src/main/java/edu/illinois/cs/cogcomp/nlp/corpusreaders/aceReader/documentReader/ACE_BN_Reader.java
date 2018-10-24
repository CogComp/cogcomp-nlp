/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader;

import edu.illinois.cs.cogcomp.core.constants.DocumentMetadata;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ACE_BN_Reader {
    private static Logger logger = LoggerFactory.getLogger(ACE_BN_Reader.class);
    static boolean isDebug = false;

    public static Pair<List<Pair<String, Paragraph>>, Map<String, String>> parse(String content,
                                                                                 String contentRemovingTags,
                                                                                 boolean is2004) {
        List<Pair<String, Paragraph>> paragraphs = new ArrayList<>();
        Map<String, String> metadata = new HashMap<>();

        Pattern pattern = null;
        Matcher matcher = null;

        String docID = "";
        String dateTime = "";
        String headLine = "";
        String text = "";

        pattern =
                is2004 ? Pattern.compile("<DOCNO>(.*?)</DOCNO>") :
                        Pattern.compile("<DOCID>(.*?)</DOCID>");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            docID = (matcher.group(1)).trim();
        }
        metadata.put(DocumentMetadata.DocumentID, docID);

        pattern =
                is2004 ? Pattern.compile("<DATE_TIME>(.*?)</DATE_TIME>") :
                        Pattern.compile("<DATETIME>(.*?)</DATETIME>");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            dateTime = (matcher.group(1)).trim();
        }
        metadata.put(DocumentMetadata.DocumentCreationTime, dateTime);

        if (is2004) {
            pattern =
                    Pattern.compile("<TEXT>(.*?)<TURN>|<TURN>(.*?)<TURN>|<TURN>(.*?)</TEXT>|<TEXT>(.*?)</TEXT>");
        } else {
            pattern = Pattern.compile("<TURN>(.*?)</TURN>");
        }

        matcher = pattern.matcher(content);
        int regionStart = 0;
        while (matcher.find(regionStart)) {
            // Pick the first non-empty group.
            for (int i = 1; i <= matcher.groupCount(); ++i) {
                if (matcher.group(i) != null) {
                    text = (matcher.group(i)).trim();
                    break;
                }
            }

            int index4 = content.indexOf(text);
            Paragraph para4 = new Paragraph(index4, text);
            Pair<String, Paragraph> pair4 = new Pair<String, Paragraph>("text", para4);
            paragraphs.add(pair4);

            if (is2004) {
                regionStart = matcher.end() - 6; // Hack to move back to the overlapping <TURN> tag
            } else {
                regionStart = matcher.end();
            }
        }

        int index = 0;
        for (int i = 0; i < paragraphs.size(); ++i) {
            String paraContent = paragraphs.get(i).getSecond().content;
            int offsetWithFiltering = contentRemovingTags.indexOf(paraContent, index);
            paragraphs.get(i).getSecond().offsetFilterTags = offsetWithFiltering;

            index += paraContent.length();
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
