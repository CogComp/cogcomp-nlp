/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.nlp.utilities.SentenceUtils;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Assumes the input is formatted with one word per line as follows. <blockquote> <code>
 * form  POS  full-parse  chunk  NE  verb-sense  verb-lemma  [verb1-args
 * [verb2-args ... ]]
 * </code> </blockquote>
 *
 * @author Vivek Srikumar
 */
public class CoNLLColumnFormatReader extends AnnotationReader<TextAnnotation> {

    private static org.slf4j.Logger logger =
            LoggerFactory.getLogger(CoNLLColumnFormatReader.class);
    protected final String predicateArgumentViewName;
    protected final ArrayList<String> lines;
    protected final String section;
    private final TextAnnotationBuilder textAnnotationBuilder;
    protected int currentLine;

    /**
     * Initialize the reader.
     *
     * @param corpus The name of the corpus
     * @param section The section of WSJ that is to be read. This is largely inconsequential, and is
     *        used only to assign identifiers to the {@code TextAnnotation} objects.
     * @param columnFile The file containing the column format data
     * @param predicateArgumentViewName The name of the predicate argument view. For consistency,
     *        use {@code ViewNames#SRL_VERB} for verb SRL_VERB and {@code ViewNames#SRL_NOM} for
     *        SRL_NOM.
     */
    public CoNLLColumnFormatReader(String corpus, String section, String columnFile,
            String predicateArgumentViewName, TextAnnotationBuilder textAnnotationBuilder)
            throws Exception {
        super(CorpusReaderConfigurator.buildResourceManager(corpus));
        this.section = section;
        this.predicateArgumentViewName = predicateArgumentViewName;
        this.textAnnotationBuilder = textAnnotationBuilder;

        List<URL> list = IOUtils.lsResources(CoNLLColumnFormatReader.class, columnFile);
        if (list.size() > 0) {
            lines = new ArrayList<>();
            URL url = list.get(0);

            Scanner scanner = new Scanner(url.openStream());

            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
        } else {
            lines = LineIO.read(columnFile);
        }
    }

    public static void main(String[] args) throws Exception {
        String columnFile = "02.feats";

        CoNLLColumnFormatReader reader =
                new CoNLLColumnFormatReader("PennTreebank-WSJ", "02", columnFile,
                        ViewNames.SRL_VERB, new BasicTextAnnotationBuilder());

        Counter<String> counter = new Counter<>();

        List<String> predicates = new ArrayList<>();

        for (TextAnnotation ta : reader) {
            counter.incrementCount("Sentences");
            System.out.println(ta.getTokenizedText());

            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;

            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
            List<Constituent> predicates2 = pav.getPredicates();
            counter.incrementCount("Predicates", predicates2.size());
            for (Constituent c : predicates2) {
                predicates.add(c.getAttribute(PredicateArgumentView.LemmaIdentifier));
            }
        }

        System.out.println((int) counter.getCount("Sentences") + " sentences");
        System.out.println((int) counter.getCount("Predicates") + " predicates");
    }

    @Override
    public boolean hasNext() {
        return currentLine < lines.size();
    }


    @Override
    public void reset() {
        super.reset();
        currentLine = 0;
    }


    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     *
     * @return an annotation object.
     */
    @Override
    public TextAnnotation next() {

        // first read all the lines. Track the sentence.
        StringBuilder sentence = new StringBuilder();

        List<String> tokens = new ArrayList<>();
        List<String> pos = new ArrayList<>();

        StringBuilder parse = new StringBuilder();

        List<String> chunkLabels = new ArrayList<>();
        List<Integer> chunkStart = new ArrayList<>();
        List<Integer> chunkEnd = new ArrayList<>();

        List<String> neLabels = new ArrayList<>();
        List<Integer> neStart = new ArrayList<>();
        List<Integer> neEnd = new ArrayList<>();

        // List<Pair<String, String>> verbSenseBaseForm = new
        // ArrayList<Pair<String, String>>();

        List<String> verbSenses = new ArrayList<>();
        List<String> baseForms = new ArrayList<>();

        List<List<String>> argumentLabels = new ArrayList<>();
        List<List<Integer>> argumentStart = new ArrayList<>();
        List<List<Integer>> argumentEnd = new ArrayList<>();

        if (currentLine >= lines.size())
            return null;

        int numPredicates = lines.get(currentLine).split(" +").length - 7;
        for (int i = 0; i < numPredicates; i++) {
            argumentLabels.add(new ArrayList<String>());
            argumentEnd.add(new ArrayList<Integer>());
            argumentStart.add(new ArrayList<Integer>());
        }

        List<Integer> predicatePositions = new ArrayList<>();

        int tokenId = 0;

        while (true) {
            String line = lines.get(currentLine++).trim();

            line = line.trim();

            // logger.info(line);

            if (line.length() == 0)
                break;

            String[] parts = line.split(" +");

            for (int partId = 0; partId < parts.length; partId++) {
                parts[partId] = parts[partId].trim();
            }

            // form
            String token = SentenceUtils.convertFromPTBBrackets(parts[0]);
            tokens.add(token);
            sentence.append(token).append(" ");

            // pos
            parts[1] = SentenceUtils.convertBracketsToPTB(parts[1]);
            pos.add(parts[1]);

            // parse
            parse.append(parts[2].replaceAll("\\*", " \\(" + parts[1].replaceAll("\\$", "\\\\\\$")
                    + " \\(" + parts[0].replaceAll("\\$", "\\\\\\$") + "\\)\\)"));

            // chunk

            if (parts[3].startsWith("(")) {
                chunkStart.add(tokenId);

                String chunkLabel =
                        parts[3].replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\*", "");
                chunkLabels.add(chunkLabel);
            }
            if (parts[3].endsWith(")"))
                chunkEnd.add(tokenId + 1);

            // NE
            if (parts[4].startsWith("(")) {
                neStart.add(tokenId);

                String neLabel =
                        parts[4].replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\*", "");
                neLabels.add(neLabel);
            }
            if (parts[4].endsWith(")"))
                neEnd.add(tokenId + 1);

            // verb sense and base forms
            if (!(parts[5].equals("-") && parts[6].equals("-"))) {
                verbSenses.add(parts[5]);
                baseForms.add(parts[6]);

                predicatePositions.add(tokenId);
            }

            // now the argument data
            for (int columnId = 7; columnId < parts.length; columnId++) {
                if (parts[columnId].startsWith("(")) {
                    argumentStart.get(columnId - 7).add(tokenId);

                    String chunkLabel =
                            parts[columnId].replaceAll("\\(", "").replaceAll("\\)", "")
                                    .replaceAll("\\*", "");
                    argumentLabels.get(columnId - 7).add(chunkLabel);
                }
                if (parts[columnId].endsWith(")"))
                    argumentEnd.get(columnId - 7).add(tokenId + 1);

            }

            // end
            tokenId++;

        }

        if (!validate(chunkLabels, chunkStart, chunkEnd))
            throw new IllegalStateException("Invalid chunk data. Check line " + currentLine);

        if (!validate(neLabels, neStart, neEnd))
            throw new IllegalStateException("Invalid NE data. Check line " + currentLine);

        if (baseForms.size() != numPredicates)
            throw new IllegalStateException("Number of predicates incorrect. Check line " + currentLine
                    + ". Expected: " + numPredicates + ", found: " + baseForms.size());

        for (int i = 0; i < numPredicates; i++) {
            if (!validate(argumentLabels.get(i), argumentStart.get(i), argumentEnd.get(i)))
                throw new IllegalStateException("Invalid argument data. Check line " + currentLine
                        + ", argument #" + (i + 1));
        }

        TextAnnotation ta =
                textAnnotationBuilder.createTextAnnotation(this.corpusName, section + ":"
                        + currentAnnotationId, sentence.toString().trim());

        // POS
        TokenLabelView posView = new TokenLabelView(ViewNames.POS, "GoldStandard", ta, 1.0);
        for (int i = 0; i < pos.size(); i++) {
            posView.addTokenLabel(i, pos.get(i), 1.0);
        }
        ta.addView(ViewNames.POS, posView);

        // full parse
        TreeView parseView = new TreeView(ViewNames.PARSE_CHARNIAK, "GoldStandard", ta, 1.0);
        Tree<String> parseTree = TreeParserFactory.getStringTreeParser().parse(parse.toString());

        parseView.setParseTree(0, parseTree);

        ta.addView(ViewNames.PARSE_CHARNIAK, parseView);

        // chunk
        ta.addView(ViewNames.SHALLOW_PARSE,
                makeSpanLabeledView(chunkLabels, chunkStart, chunkEnd, ta, ViewNames.SHALLOW_PARSE));

        // NE
        ta.addView(ViewNames.NER_CONLL,
                makeSpanLabeledView(neLabels, neStart, neEnd, ta, ViewNames.NER_CONLL));

        // predicate argument
        if (predicatePositions.size() > 0) {
            PredicateArgumentView pav =
                    getPredicateArgumentView(argumentLabels, argumentStart, argumentEnd, ta,
                            verbSenses, baseForms, predicatePositions);

            ta.addView(predicateArgumentViewName, pav);
        }

        currentAnnotationId++;
        return ta;
    }



    protected PredicateArgumentView getPredicateArgumentView(List<List<String>> argumentLabels,
            List<List<Integer>> argumentStart, List<List<Integer>> argumentEnd, TextAnnotation ta,
            List<String> verbSenses, List<String> baseForms, List<Integer> predicatePositions) {
        int numPredicates = argumentLabels.size();

        PredicateArgumentView pav =
                new PredicateArgumentView(this.predicateArgumentViewName, "GoldStandard", ta, 1.0);

        for (int predicateId = 0; predicateId < numPredicates; predicateId++) {
            List<Constituent> args = new ArrayList<>();
            List<String> relations = new ArrayList<>();

            int predicatePos = predicatePositions.get(predicateId);

            for (int argId = 0; argId < argumentLabels.get(predicateId).size(); argId++) {
                String label = argumentLabels.get(predicateId).get(argId);
                if (!label.equals("V")) {
                    int start = argumentStart.get(predicateId).get(argId);
                    int end = argumentEnd.get(predicateId).get(argId);

                    Constituent arg =
                            new Constituent(label, 1.0, predicateArgumentViewName, ta, start, end);

                    args.add(arg);
                    relations.add(label);
                }
            }

            Constituent predicate =
                    new Constituent("Predicate", 1.0, predicateArgumentViewName, ta, predicatePos,
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
        return pav;
    }

    protected SpanLabelView makeSpanLabeledView(List<String> chunkLabels, List<Integer> chunkStart,
            List<Integer> chunkEnd, TextAnnotation ta, String viewName) {
        SpanLabelView view = new SpanLabelView(viewName, "GoldStandard", ta, 1.0);
        for (int i = 0; i < chunkLabels.size(); i++) {
            view.addSpanLabel(chunkStart.get(i), chunkEnd.get(i), chunkLabels.get(i), 1.0);
        }

        return view;
    }

    protected boolean validate(List<String> chunkLabels, List<Integer> chunkStart,
            List<Integer> chunkEnd) {
        return chunkLabels.size() == chunkStart.size() && chunkLabels.size() == chunkEnd.size();
    }

    protected void initializeReader() {

    }

    /**
     * TODO: generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */

    public String generateReport() {
        throw new UnsupportedOperationException("ERROR: generateReport() Not yet implemented.");
    }

}
