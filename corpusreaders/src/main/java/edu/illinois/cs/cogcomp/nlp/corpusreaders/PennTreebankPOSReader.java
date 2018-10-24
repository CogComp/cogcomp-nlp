/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple reader for the bracketed version of the Penn Treebank corpus
 *
 * @author Christos Christodoulopoulos
 */
public class PennTreebankPOSReader {
    private static Logger logger = LoggerFactory.getLogger(PennTreebankPOSReader.class);
    private final static Pattern splitWordsPattern = Pattern.compile("\\)\\s+\\(");
    private final static Pattern whitespacePattern = Pattern.compile("\\s+");

    private List<TextAnnotation> textAnnotations;
    protected final String corpusName;

    public PennTreebankPOSReader(String corpusName) {
        this.corpusName = corpusName;
        textAnnotations = new ArrayList<>();
    }

    public void readFile(String fileName) {
        try {
            List<String> lines = LineIO.read(fileName);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String sentId = IOUtils.getFileName(fileName) + ":" + i;
                textAnnotations.add(createTextAnnotation(line, sentId));
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not read {}; unable to continue.", fileName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new {@link TextAnnotation} from a single line of bracketed text
     *
     * @param line The bracketed string to be processed
     * @param lineId The ID of the {@link TextAnnotation}
     * @return A {@link TextAnnotation} with a populated {@link ViewNames#POS} view
     */
    public TextAnnotation createTextAnnotation(String line, String lineId) {
        String[] wordPOSPairs = splitWordsPattern.split(line.substring(1, line.length() - 1));
        List<String> words = new ArrayList<>(wordPOSPairs.length);
        List<String> pos = new ArrayList<>(wordPOSPairs.length);
        for (String wordPOSPair : wordPOSPairs) {
            String[] split = whitespacePattern.split(wordPOSPair);
            words.add(split[1]);
            pos.add(split[0]);
        }
        List<String[]> tokenizedSentences =
                Collections.singletonList(words.toArray(new String[words.size()]));
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(corpusName, lineId,
                        tokenizedSentences);
        TokenLabelView posView = new TokenLabelView(ViewNames.POS, ta);
        for (int i = 0; i < pos.size(); i++)
            posView.addTokenLabel(i, pos.get(i), 1.0);
        ta.addView(ViewNames.POS, posView);
        return ta;
    }

    public List<TextAnnotation> getTextAnnotations() {
        return textAnnotations;
    }
}
