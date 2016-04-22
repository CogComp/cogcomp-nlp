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
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.nlp.utilities.SentenceUtils;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Read the WSJ chunk data reader, generated by the file chunlink.pl in the directory treebank-2 in
 * the standard Penn Treebank distribution. This file reads the data generated by running the
 * following command on each file in {@literal combined/wsj/$dir/$file}:
 * <p>
 * <p>
 * 
 * <pre>
 * perl ./chunlink.pl  -s -ns ./combined/wsj/$dir/$file &gt; ./chunkData/wsj/$dir/$file
 * </pre>
 * <p>
 * We assume that this creates a directory structure in treebank-2 as follows
 * <p>
 * 
 * <pre>
 *  \treebank-2
 *  - .. other standard stuff
 *  - \chunkData
 *    - \wsj
 *      - \00
 *        - wsj_0001.mrg
 *        - wsj_0002.mrg
 *        - ...
 *      - \01
 *        - wsj_0101.mrg
 *        - ...
 *      - and the other sections
 * </pre>
 * <p>
 * <p>
 * 
 * <pre>
 * #arguments: IOB tag: Begin, word numbering: sent
 * #columns: word_id iob_inner pos word function heads head_ids iob_chain trace-function trace-type trace-head_ids
 * # Sentence 0001/01
 * 0 B-NP    NNP   Pierre          NOFUNC          Vinken            1 B-S/B-NP/B-NP
 * 1 I-NP    NNP   Vinken          NP-SBJ          join              8 I-S/I-NP/I-NP
 * 2 O       COMMA COMMA           NOFUNC          Vinken            1 I-S/I-NP
 * 3 B-NP    CD    61              NOFUNC          years             4 I-S/I-NP/B-ADJP/B-NP
 * .
 * .
 * .
 * </pre>
 * <p>
 * <p>
 * This class takes the standard penn treebank annotation and adds this annotation to it
 * <p>
 * Each file in this has the following information in it:
 *
 * @author Vivek Srikumar
 */
public class TreebankChunkReader extends PennTreebankReader {

    protected String chunkHome;

    protected List<String> chunkLines;
    private int currentChunkLineId;

    public TreebankChunkReader(String treebankHome) throws Exception {
        super(treebankHome);
        setChunkHome(treebankHome);
    }

    public TreebankChunkReader(String treebankHome, String[] sections) throws Exception {
        super(treebankHome, sections);

        setChunkHome(treebankHome);
    }

    private void setChunkHome(String treebankHome) {
        chunkHome = treebankHome + "/treebank-2/chunkData/wsj";
        currentChunkLineId = 0;

    }

    @Override
    public TextAnnotation next() {

        TextAnnotation textAnnotation = super.next();

        // int currentTree = this.treeInFile - 1;
        int currentSection = this.currentSectionId - 1;
        int currentFile = this.currentFileId - 1;

        if (chunkLines == null || currentChunkLineId == chunkLines.size()) {
            try {
                chunkLines =
                        LineIO.read(chunkHome + "/" + sections[currentSection] + "/"
                                + currentSectionFiles[currentFile]);
                currentChunkLineId = 0;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return addChunkAnnotation(textAnnotation, currentChunkLineId);

    }

    private TextAnnotation addChunkAnnotation(TextAnnotation textAnnotation, int chunkLineId) {
        SpanLabelView chunkView =
                new SpanLabelView(ViewNames.SHALLOW_PARSE, "Gold", textAnnotation, 1.0);

        String currentChunkLabel = "";
        int start = -1;

        while (currentChunkLineId < chunkLines.size()) {
            String line = chunkLines.get(currentChunkLineId++);

            if (line.trim().length() == 0)
                break;

            if (line.startsWith("#")) {
                if (!line.startsWith("# Sentence"))
                    continue;
                line = line.replaceAll("# Sentence ", "");

                String[] parts = line.split("/");
                String fId = parts[0];
                int tId = Integer.parseInt(parts[1]);

                if (!currentSectionFiles[currentFileId - 1].equals("wsj_" + fId + ".mrg"))
                    throw new IllegalStateException(currentSectionFiles[currentFileId - 1]
                            + " does not match " + "wsj_" + fId + ".mrg");

                if (tId != this.treeInFile)
                    throw new IllegalStateException("Expected tree id: " + tId + ", found: "
                            + (this.treeInFile));
                continue;

            }

            String[] parts = line.split(" +");
            int id = Integer.parseInt(parts[1]);
            String chunkLabel = parts[2];
            String word = parts[4];
            word = SentenceUtils.makeSentencePresentable(word);

            String expectedWord = textAnnotation.getToken(id);
            if (!word.equals(expectedWord))
                throw new IllegalStateException("Expected word: " + expectedWord + ", found "
                        + word);

            if (currentChunkLabel.equals("")) {
                if (chunkLabel.startsWith("B")) {
                    start = id;
                    currentChunkLabel = chunkLabel;
                } else if (!chunkLabel.startsWith("O")) {
                    throw new IllegalStateException("Expected B, found " + chunkLabel);
                }
            } else if (currentChunkLabel.startsWith("B")) {
                if (chunkLabel.startsWith("B")) {
                    if (start >= 0)
                        chunkView.addSpanLabel(start, id, currentChunkLabel.replaceAll("B-", ""),
                                1d);
                    else
                        throw new IllegalStateException("Start <0");

                    currentChunkLabel = chunkLabel;
                    start = id;
                } else if (chunkLabel.startsWith("I-")) {

                } else if (chunkLabel.startsWith("O")) {
                    if (start >= 0)
                        chunkView.addSpanLabel(start, id, currentChunkLabel.replaceAll("B-", ""),
                                1d);
                    else
                        throw new IllegalStateException("Start <0");

                    start = -1;
                    currentChunkLabel = chunkLabel;
                }
            } else if (currentChunkLabel.startsWith("I-")) {
                if (chunkLabel.startsWith("B")) {
                    if (start >= 0)
                        chunkView.addSpanLabel(start, id, currentChunkLabel.replaceAll("B-", ""),
                                1d);
                    else
                        throw new IllegalStateException("Start <0");

                    currentChunkLabel = chunkLabel;
                    start = id;
                } else if (chunkLabel.startsWith("I-")) {

                } else if (chunkLabel.startsWith("O")) {
                    if (start >= 0)
                        chunkView.addSpanLabel(start, id, currentChunkLabel.replaceAll("B-", ""),
                                1d);
                    else
                        throw new IllegalStateException("Start <0");

                    currentChunkLabel = chunkLabel;
                    start = -1;
                }
            } else if (currentChunkLabel.startsWith("O")) {
                if (chunkLabel.startsWith("B")) {
                    currentChunkLabel = chunkLabel;
                    start = id;
                } else if (chunkLabel.startsWith("I-")) {
                    throw new IllegalStateException("Expected B, found " + chunkLabel);
                } else if (chunkLabel.startsWith("O")) {
                    currentChunkLabel = chunkLabel;
                }
            }

        }// end of while

        textAnnotation.addView(ViewNames.SHALLOW_PARSE, chunkView);

        return textAnnotation;

    }

}
