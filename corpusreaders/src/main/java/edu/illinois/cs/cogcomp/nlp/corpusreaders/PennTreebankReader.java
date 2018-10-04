/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.nlp.utilities.POSFromParse;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Vivek Srikumar
 */
public class PennTreebankReader extends AnnotationReader<TextAnnotation> {

    public static final String PENN_TREEBANK_WSJ = "PennTreebank-WSJ";

    private static final String TOP_LABEL = "S1";

    protected final String[] sections;

    protected final String combinedWSJHome;
    private final String parseViewName;
    protected int currentSectionId;
    String[] currentSectionFiles;
    int currentFileId;
    int currentLineId;

    int treeInFile;
    private ArrayList<String> lines;

    /**
     * Reads all the sections of the combined annotation from penn treebank
     *
     * @param treebankHome The directory that points to the merged (mrg) files of the WSJ portion
     */
    public PennTreebankReader(String treebankHome) throws Exception {
        this(treebankHome, null, ViewNames.PARSE_GOLD, ".mrg", ".mrg");
    }

    /**
     * Reads all the sections of the combined annotation from penn treebank
     *
     * @param treebankHome The directory that points to the merged (mrg) files of the WSJ portion
     * @param parseViewName The name of the parse view which is to be added
     */
    public PennTreebankReader(String treebankHome, String parseViewName) throws Exception {
        this(treebankHome, null, parseViewName, ".mrg", ".mrg");
    }

    /**
     * Reads the specified sections from penn treebank
     *
     * @param treebankHome The directory that points to the merged (mrg) files of the WSJ portion
     */
    public PennTreebankReader(String treebankHome, String[] sections) throws Exception {
        this(treebankHome, sections, ViewNames.PARSE_GOLD, ".mrg", ".mrg");
    }

    /**
     * Reads the specified sections from penn treebank
     *
     * @param treebankHome The directory that points to the merged (mrg) files of the WSJ portion
     */
    public PennTreebankReader(String treebankHome, String[] sections, String parseViewName, String sourceFileExtension, String annotationFileExtension)
            throws Exception {
        super(CorpusReaderConfigurator.buildResourceManager(PENN_TREEBANK_WSJ, treebankHome, treebankHome, sourceFileExtension, annotationFileExtension));
        this.parseViewName = parseViewName;
        combinedWSJHome = treebankHome;

        if (sections == null)
            this.sections = IOUtils.lsDirectories(combinedWSJHome);
        else {
            this.sections = new String[sections.length];
            System.arraycopy(sections, 0, this.sections, 0, sections.length);
        }

        updateCurrentFiles();
    }

    protected void initializeReader() {
        currentSectionId = 0;
        currentFileId = 0;

        currentLineId = 0;
        treeInFile = 0;
    }

    /**
     * @throws Exception
     */
    private void updateCurrentFiles() throws Exception {
        currentSectionFiles = IOUtils.ls(combinedWSJHome + "/" + sections[currentSectionId]);

        currentSectionId++;
    }

    @Override
    public boolean hasNext() {
        if (lines == null)
            return true;

        if (currentLineId < lines.size())
            return true;

        if (currentSectionId < sections.length)
            return true;

        return currentFileId < currentSectionFiles.length;

    }

    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     *
     * @return an annotation object.
     */
    @Override
    public TextAnnotation next() {
        // first check if we don't have any more lines
        if (lines == null || currentLineId == lines.size()) {
            // check if the current section has no more files
            if (currentFileId == currentSectionFiles.length) {

                // check if there are more sections
                if (currentSectionId == sections.length) {
                    return null;
                }

                try {
                    updateCurrentFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentFileId = 0;
            }

            try {
                lines =
                        LineIO.read(combinedWSJHome + "/" + sections[currentSectionId - 1] + "/"
                                + currentSectionFiles[currentFileId++]);
                treeInFile = 0;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            currentLineId = 0;

        }

        TextAnnotation ta = null;

        try {
            ta = findNextTree();
        } catch (AnnotatorException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        return ta;
    }


    private TextAnnotation findNextTree() throws AnnotatorException {

        StringBuilder sb = new StringBuilder();
        int numParen = 0;
        boolean first = true;

        while (true) {
            String line = lines.get(currentLineId++);

            if (line.length() == 0)
                continue;

            if (first) {
                first = false;

                line =
                        line.substring(0, line.indexOf("(") + 1) + TOP_LABEL
                                + line.substring(line.indexOf("(") + 1);
            }

            int numOpenParen = line.replaceAll("[^\\(]", "").length();
            int numCloseParen = line.replaceAll("[^\\)]", "").length();

            numParen += (numOpenParen - numCloseParen);

            sb.append(line);

            if (numParen == 0)
                break;
        }

        Tree<String> tree =
                TreeParserFactory.getStringTreeParser().parse(
                        sb.toString().replaceAll("\\\\/", "/"));

        String[] text = ParseUtils.getTerminalStringSentence(tree);

        String id =
                "wsj/" + sections[currentSectionId - 1] + "/"
                        + currentSectionFiles[currentFileId - 1] + ":" + treeInFile;

        treeInFile++;

        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(PENN_TREEBANK_WSJ, id,
                        Collections.singletonList(text));

        TreeView parse = new TreeView(parseViewName, "PTB-GOLD", ta, 1.0);
        parse.setParseTree(0, tree);

        ta.addView(parseViewName, parse);

        POSFromParse pos = new POSFromParse(parseViewName);
        ta.addView(pos);

        return ta;
    }


    public void remove() {}


    /**
     * TODO: generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */
    public String generateReport() {
        throw new UnsupportedOperationException("ERROR: generateReport() Not yet implemented.");
    }

}
