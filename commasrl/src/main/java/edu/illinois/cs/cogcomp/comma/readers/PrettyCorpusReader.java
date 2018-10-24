/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.readers;

import edu.illinois.cs.cogcomp.comma.annotators.PreProcessor;
import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.NombankReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PropbankReader;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class PrettyCorpusReader implements IResetableIterator<CommaSRLSentence>, Serializable {

    private static final long serialVersionUID = 1L;

    private List<Comma> commas;
    private List<CommaSRLSentence> sentences;
    int sentenceIdx;
    private static String treebankHome, propbankHome, nombankHome;

    public PrettyCorpusReader(String annotationFileName) {
        CommaProperties properties = CommaProperties.getInstance();
        treebankHome = properties.getPTBHDir();
        propbankHome = properties.getPropbankDir();
        nombankHome = properties.getNombankDir();

        try {
            readData(annotationFileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        reset();
    }

    private void readData(String annotationFileName) throws Exception {
        PreProcessor preProcessor = new PreProcessor();
        sentences = new ArrayList<>();

        Map<String, TextAnnotation> taMap = getTAMap();
        Scanner scanner;
        scanner = new Scanner(new File(annotationFileName));

        int count = 0;
        int failures = 0, skipped = 0;
        while (scanner.hasNext()) {
            count++;

            // wsj pentreebank sentence id
            String textId = scanner.nextLine().trim();
            assert textId.startsWith("wsj") : textId;

            String[] tokens = scanner.nextLine().trim().split("\\s+");
            String[] cleanedTokens = cleanTokens(tokens);
            String blankLine = scanner.nextLine();
            assert blankLine.trim().length() == 0 : String
                    .format("line is not blank:%s", blankLine);

            boolean skip = false;// should we skip this sentence due to some error?
            TextAnnotation goldTa = null, ta = null;
            if (taMap.containsKey(textId)) {
                goldTa = taMap.get(textId);
                try {
                    ta = preProcessor.preProcess(Collections.singletonList(cleanedTokens));
                } catch (Exception e) {
                    e.printStackTrace();
                    skip = true;
                    failures++;
                }
            } else {
                System.out.println("No gold standard annotation available for:" + textId);
                skip = true;
                skipped++;
            }

            if (!skip) {
                List<List<String>> commaLabels = new ArrayList<>();
                for (String token : tokens) {
                    if (token.matches(",\\[.*\\]")) {
                        String labels = token.substring(2, token.length() - 1);
                        List<String> commaLabelsForIdx = Arrays.asList(labels.split(","));
                        commaLabels.add(commaLabelsForIdx);
                    } else if (token.equals(",")) {
                        // add null for commas which have not been annotated.
                        // The sentence constructor will optionally discard
                        // these or convert them to Other labels based on
                        // comma.properties.INCLUDE_NULL_LABEL_COMMAS
                        commaLabels.add(null);
                    }
                }

                try {
                    CommaSRLSentence sentence = new CommaSRLSentence(ta, goldTa, commaLabels);
                    sentences.add(sentence);
                } catch (Exception e) {
                    e.printStackTrace();
                    failures++;
                }
            }

            System.out.print(count);
            if (skipped > 0)
                System.out.print(" SKIPPED(" + skipped + ")");
            if (failures > 0)
                System.out.print(" ANNOTATION FAILED(" + failures + ")");

        }
        scanner.close();
    }

    /**
     * the tokens read by this reader may contain extra data such as the comma labels. This function
     * will remove this and return a cleaned list of tokens
     * 
     * @param tokens list of tokens to be cleaned
     * @return cleaned list of tokens
     */
    private String[] cleanTokens(String[] tokens) {
        String[] cleanedTokens = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith(",") && tokens[i].length() > 1)
                cleanedTokens[i] = ",";
            else
                cleanedTokens[i] = tokens[i];
        }
        return cleanedTokens;
    }

    /**
     * Returns the map of gold-standard annotations (SRLs, parses) for the comma data (found in
     * section 00 of PTB).
     *
     * @return A map of 'gold' {@link TextAnnotation} indexed by their IDs
     */
    private Map<String, TextAnnotation> getTAMap() {
        Map<String, TextAnnotation> taMap = new HashMap<>();
        Iterator<TextAnnotation> ptbReader, propbankReader, nombankReader;

        String[] sections = {"00"};
        String goldVerbView = ViewNames.SRL_VERB + "_GOLD";
        String goldNomView = ViewNames.SRL_NOM + "_GOLD";

        try {
            ptbReader = new PennTreebankReader(treebankHome, sections);
            propbankReader =
                    new PropbankReader(treebankHome, propbankHome, sections, goldVerbView, true);
            nombankReader =
                    new NombankReader(treebankHome, nombankHome, sections, goldNomView, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Add the gold parses for each sentence in the corpus
        while (ptbReader.hasNext()) {
            TextAnnotation ta = ptbReader.next();
            taMap.put(ta.getId(), ta);
        }
        // Add the new SRL_VERB view (if it exists)
        while (propbankReader.hasNext()) {
            TextAnnotation verbTA = propbankReader.next();
            if (!verbTA.hasView(goldVerbView))
                continue;

            TextAnnotation ta = taMap.get(verbTA.getId());
            ta.addView(ViewNames.SRL_VERB, verbTA.getView(goldVerbView));
            taMap.put(ta.getId(), ta);
        }
        // Add the new SRL_NOM view (if it exists)
        while (nombankReader.hasNext()) {
            TextAnnotation nomTA = nombankReader.next();
            if (!nomTA.hasView(goldNomView))
                continue;

            TextAnnotation ta = taMap.get(nomTA.getId());
            ta.addView(ViewNames.SRL_NOM, nomTA.getView(goldNomView));
            taMap.put(ta.getId(), ta);
        }
        return taMap;
    }

    public List<CommaSRLSentence> getSentences() {
        return sentences;
    }

    public List<Comma> getCommas() {
        if (commas == null) {
            commas = new ArrayList<>();
            for (CommaSRLSentence s : sentences)
                commas.addAll(s.getCommas());
        }
        return commas;
    }

    @Override
    public boolean hasNext() {
        return sentenceIdx >= sentences.size();
    }

    @Override
    public CommaSRLSentence next() {
        return sentences.get(sentenceIdx);
    }

    @Override
    public void remove() {
        sentences.remove(sentenceIdx - 1);
        sentenceIdx--;
    }

    @Override
    public void reset() {
        sentenceIdx = 0;
    }

}
