/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.nlp.utilities.SentenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on {@code edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocCoNLLLoader} from the Coref
 * package. This reader just processes {@link TextAnnotation}s; Coreference mentions and gold labels
 * are ignored.
 */
public class OntonotesReader extends AnnotationReader<TextAnnotation> {
    private static Logger logger = LoggerFactory.getLogger(OntonotesReader.class);
    private List<TextAnnotation> textAnnotations;
    private int taCounter;

    /**
     * TODO: handle file extensions in OntoNotes. This is complicated because it has many different types of annotation file
     * and is not well organized.
     * @param ontonotesDirectory
     */
    public OntonotesReader(String ontonotesDirectory) {
        super(CorpusReaderConfigurator.buildResourceManager("Ontonotes", ontonotesDirectory, ontonotesDirectory, "", ""));
        this.taCounter = 0;

    }

    @Override
    public boolean hasNext() {
        return textAnnotations.size() > taCounter;
    }


    @Override
    protected void initializeReader() {
        this.textAnnotations = new ArrayList<>();

        String ontonotesDirectory =
                this.resourceManager.getString(CorpusReaderConfigurator.SOURCE_DIRECTORY.key);

        String[] files = new String[0];
        // In case the input argument is a single file
        if (!IOUtils.isDirectory(ontonotesDirectory)) {
            files = new String[] {ontonotesDirectory};
        } else {
            try {
                files = IOUtils.ls(ontonotesDirectory);
                for (int i = 0; i < files.length; i++) {
                    files[i] = Paths.get(ontonotesDirectory, files[i]).toString();
                }
            } catch (IOException e) {
                logger.error("Error listing directory.");
                logger.error(e.getMessage());
            }
        }
        try {
            for (String file : files) {
                System.out.println("loading: " + file);
                // Load all parts of the document (part # = -1)
                // TODO Add this as a global parameter?
                textAnnotations.add(loadCoNLLfile(file, -1));
            }
        } catch (IOException e) {
            logger.error("Error reading file.");
            logger.error(e.getMessage());
        }
    }


    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     *
     * @return an annotation object.
     */
    @Override
    public TextAnnotation next() {
        if (!hasNext())
            return null;
        return textAnnotations.get(taCounter++);
    }


    private TextAnnotation loadCoNLLfile(String filename, int part) throws FileNotFoundException {
        String m_docID = null;
        int wordNum = 0;
        ArrayList<String> lines;
        List<String> parses = new ArrayList<>();
        List<String[]> sentences = new ArrayList<>();
        List<String> pos = new ArrayList<>();
        List<String> neLabels = new ArrayList<>();
        List<Integer> neStart = new ArrayList<>();
        List<Integer> neEnd = new ArrayList<>();
        List<String> verbSenses = new ArrayList<>();
        List<String> baseForms = new ArrayList<>();
        List<List<String>> argumentLabels = new ArrayList<>();
        List<List<Integer>> argumentStart = new ArrayList<>();
        List<List<Integer>> argumentEnd = new ArrayList<>();
        List<Integer> predicatePositions = new ArrayList<>();

        String neLabel;
        StringBuilder parse = new StringBuilder();
        List<String> sentence = new ArrayList<>();

        // A document may contain several parts
        // We distinguish different parts by filenameCode = [filename]_part[#part]

        // Find the starting location in the document
        lines = LineIO.read(filename);
        int documentStart = 0;
        if (part != -1) {
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("#begin document")
                        && Integer.parseInt(line.split("\\s+")[4].trim()) == part) {
                    documentStart = i + 1;
                    m_docID = line.replace("#begin document ", "");
                }
            }
            if (documentStart == 0) {
                logger.error("Cannot find the document " + filename + "_part" + part);
            }
        } else {
            m_docID = Paths.get(filename).getFileName().toString();
            //m_docID = filename;
        }

        boolean sentenceStart = true;
        int predicateNumOffset = 0;
        int numPredicates = 0;
        // parsing words and tags
        for (int i = documentStart; i < lines.size(); i++) {
            String line = lines.get(i);

            // end of the document
            if (line.contains("#end document"))
                continue;

            if (line.contains("#begin document"))
                continue;

            // Initialize a sentence
            if (sentenceStart) {
                if (!lines.get(i + 1).contains("#end document")) {
                    numPredicates = lines.get(i).split("\\s+").length - 12;
                    for (int i1 = 0; i1 < numPredicates; i1++) {
                        argumentLabels.add(new ArrayList<String>());
                        argumentEnd.add(new ArrayList<Integer>());
                        argumentStart.add(new ArrayList<Integer>());
                    }
                }
                sentenceStart = false;
            }
            // end of a sentence
            if (line.length() == 0) {
                parses.add(parse.toString());
                parse.setLength(0);
                sentences.add(sentence.toArray(new String[sentence.size()]));
                sentenceStart = true;
                predicateNumOffset += numPredicates;
                sentence = new ArrayList<>();
                continue;
            }

            String[] parts = line.split("\\s+");
            for (int partId = 0; partId < parts.length; partId++)
                parts[partId] = parts[partId].trim();

            String word = replacement(parts[3]);
            sentence.add(word);

            // PARSE SRL
            if (predicateNumOffset < 0) {
                logger.error("Error when parsing SRL in " + filename);
            }
            for (int columnId = 11; columnId < parts.length - 1; columnId++) {
                String chunkLabel = null;
                if (parts[columnId].startsWith("(")) {
                    argumentStart.get(predicateNumOffset + columnId - 11).add(wordNum);
                    chunkLabel =
                            parts[columnId].replaceAll("\\(", "").replaceAll("\\)", "")
                                    .replaceAll("\\*", "");
                    argumentLabels.get(predicateNumOffset + columnId - 11).add(chunkLabel);
                }
                if (parts[columnId].endsWith(")"))
                    for (int k = 0; k < (parts[columnId].length() - parts[columnId].replaceAll(
                            "\\)", "").length()); k++)
                        argumentEnd.get(predicateNumOffset + columnId - 11).add(wordNum + 1);
                if (chunkLabel != null && chunkLabel.equals("V")
                        && !(parts[7].equals("-") && parts[6].equals("-"))) {
                    verbSenses.add(parts[7]);
                    baseForms.add(parts[6]);
                    predicatePositions.add(wordNum);
                }
            }

            // pos
            pos.add(SentenceUtils.convertBracketsToPTB(parts[4]));
            // name entity
            if (parts[10].startsWith("(")) {
                neStart.add(wordNum);
                neLabel =
                        parts[10].replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\*", "");
                if (neLabel.equals("PERSON"))
                    neLabel = "PER";
                neLabels.add(neLabel);
            }
            if (parts[10].endsWith(")")) {
                neEnd.add(wordNum);
            }

            // parse tree
            parse.append(parts[5].replaceAll("\\*", " \\(" + parts[4].replaceAll("\\$", "\\\\\\$")
                    + " \\(" + word.replaceAll("\\$", "\\\\\\$") + "\\)\\)"));
            wordNum++;
        }

        // setup TextAnnotation
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(m_docID, m_docID,
                        sentences);
        addPOSView(pos, ta);
        addParseView(parses, ta);
        addNERView(neLabels, neStart, neEnd, ta);
        addSRLView(verbSenses, baseForms, argumentLabels, argumentStart, argumentEnd,
                predicatePositions, numPredicates, ta);

        return ta;
    }

    private void addSRLView(List<String> verbSenses, List<String> baseForms,
            List<List<String>> argumentLabels, List<List<Integer>> argumentStart,
            List<List<Integer>> argumentEnd, List<Integer> predicatePositions, int numPredicates,
            TextAnnotation ta) {
        // SRL view
        PredicateArgumentView pav =
                new PredicateArgumentView(ViewNames.SRL_VERB, "GoldStandard", ta, 1.0);

        for (int predicateId = 0; predicateId < numPredicates; predicateId++) {
            List<Constituent> args = new ArrayList<>();
            List<String> relations = new ArrayList<>();

            for (int argId = 0; argId < argumentLabels.get(predicateId).size(); argId++) {
                String label = argumentLabels.get(predicateId).get(argId);
                if (!label.equals("V")) {
                    int start = argumentStart.get(predicateId).get(argId);
                    int end = argumentEnd.get(predicateId).get(argId);

                    Constituent arg =
                            new Constituent(label, 1.0, ViewNames.SRL_VERB, ta, start, end);

                    args.add(arg);
                    relations.add(label);
                }
            }

            int predicatePos = predicatePositions.get(predicateId);
            Constituent predicate =
                    new Constituent("Predicate", 1.0, ViewNames.SRL_VERB, ta, predicatePos,
                            predicatePos + 1);

            predicate.addAttribute(PredicateArgumentView.SenseIdentifer,
                    verbSenses.get(predicateId));
            predicate.addAttribute(PredicateArgumentView.LemmaIdentifier,
                    baseForms.get(predicateId));

            double[] scoresDoubleArray = new double[relations.size()];
            for (int relationId = 0; relationId < relations.size(); relationId++) {
                scoresDoubleArray[relationId] = 1.0;
            }
            pav.addPredicateArguments(predicate, args,
                    relations.toArray(new String[relations.size()]), scoresDoubleArray);
        }

        if(pav.getConstituents().size() > 0) {
            ta.addView(ViewNames.SRL_VERB, pav);
        }
    }

    private void addNERView(List<String> neLabels, List<Integer> neStart, List<Integer> neEnd,
            TextAnnotation ta) {
        // NER View
        SpanLabelView nerView = new SpanLabelView(ViewNames.NER_ONTONOTES, "GoldStandard", ta, 1.0);
        for (int j = 0; j < neEnd.size(); j++) {
            int endWord = neEnd.get(j);
            int startWord = neStart.get(j);
            String nelabel = neLabels.get(j);

            // Fix the Chinese name with Hyphen
            // TODO This needs Gazetteers, is there no better way?
            // if (endWord < wordNum - 2 && getPOS(ta, endWord + 1).equals("HYPH") &&
            // Gazetteers.getChineseLastName().contains(firstWord.toLowerCase())) {
            // endWord += 2;
            // nelabel = "PER";
            // }

            // Fix Incorrect Boundary
            while (startWord <= endWord
                    && (getPOS(ta, startWord).equals("UH") || getPOS(ta, startWord).equals(",") || getWord(
                            ta, startWord).startsWith("%")
                    // TODO This needs Gazetteers, is there no better way?
                    // || Gazetteers.getSayWords().contains(getWord(ta, startWord).toLowerCase())
                    )) {
                startWord++;
            }
            while (startWord <= endWord
                    && (getPOS(ta, endWord).equals(".") || getPOS(ta, endWord).equals(","))) {
                endWord--;
            }
            if (startWord == endWord && getWord(ta, startWord).toLowerCase().equals("one"))
                continue;
            if (startWord <= endWord
                    && nerView.getConstituentsCoveringSpan(startWord, endWord + 1).isEmpty())
                nerView.addSpanLabel(startWord, endWord + 1, nelabel, 1.0);
        }
        ta.addView(ViewNames.NER_ONTONOTES, nerView);
    }

    private void addParseView(List<String> parses, TextAnnotation ta) {
        // PARSE View
        TreeView parseView = new TreeView(ViewNames.PARSE_CHARNIAK, "GoldStandard", ta, 1.0);
        for (int j = 0; j < parses.size(); j++) {
            Tree<String> parseTree = TreeParserFactory.getStringTreeParser().parse(parses.get(j));
            parseView.setParseTree(j, parseTree);
        }
        ta.addView(ViewNames.PARSE_CHARNIAK, parseView);
    }

    private void addPOSView(List<String> pos, TextAnnotation ta) {
        // POS View
        TokenLabelView posView = new TokenLabelView(ViewNames.POS, "GoldStandard", ta, 1.0);
        for (int j = 0; j < pos.size(); j++)
            posView.addTokenLabel(j, pos.get(j), 1.0);
        ta.addView(ViewNames.POS, posView);
    }

    private String getWord(TextAnnotation ta, int token) {
        return ta.getView(ViewNames.TOKENS).getConstituentsCoveringToken(token).get(0)
                .getTokenizedSurfaceForm();
    }

    private String getPOS(TextAnnotation ta, int token) {
        return ((TokenLabelView) ta.getView(ViewNames.POS)).getLabel(token);
    }

    private String replacement(String s) {
        Map<String, String> stringReplaceMap = new HashMap<>();
        stringReplaceMap.put("-LRB-", "(");
        stringReplaceMap.put("-RRB-", ")");
        stringReplaceMap.put("-LSB-", "[");
        stringReplaceMap.put("-RSB-", "]");
        stringReplaceMap.put("-LCB-", "{");
        stringReplaceMap.put("-RCB-", "}");

        // modify /? -> ?, otherwise Curator cannot parse this sentence
        if (s.contains("/") && s.length() == 2)
            s = String.valueOf(s.charAt(1));
        if (stringReplaceMap.containsKey(s))
            return stringReplaceMap.get(s);
        else
            return s;
    }

    /**
     * TODO: generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */

    public String generateReport() {
        throw new UnsupportedOperationException("ERROR: generateReport() Not yet implemented.");
    }

}
