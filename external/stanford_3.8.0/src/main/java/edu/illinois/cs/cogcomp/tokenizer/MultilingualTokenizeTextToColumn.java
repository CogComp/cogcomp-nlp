/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.tokenizer;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Simple package to tokenize a file, or directory of files, and generate a new file/directory of files
 *    in column format.
 *
 * @author mssammon
 */
public class MultilingualTokenizeTextToColumn {

    private static final Logger logger = LoggerFactory.getLogger(MultiLingualTokenizer.class);

    private TextAnnotationBuilder taBldr;

    public MultilingualTokenizeTextToColumn(String lang) {
        taBldr = MultiLingualTokenizer.getTokenizer(lang);
    }

    public static void main(String[] args) {

        String inDir = args[0];
        String outDir = args[1];
        String lang = args[2];

        MultilingualTokenizeTextToColumn mft = new MultilingualTokenizeTextToColumn(lang);

        try {
            mft.processDir("dummycorpus", inDir, outDir);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * given an input containing plain text, tokenize and write to named output file.
     *
     * @param corpus name of corpus
     * @param in file to tokenize
     * @param out output file for tokenized text
     */
    public void processFile(String corpus, File in, String out) throws IOException {

        if (!in.exists())
            throw new IOException("File '" + in.getAbsolutePath() + "' doesn't exist.");
        if (!in.isFile())
            throw new IOException("File '" + in.getAbsolutePath() + "' exists but is not a file.");

        Scanner scanner = new Scanner(new FileInputStream(in), StandardCharsets.UTF_8.name()); //Charset.defaultCharset().name());//

        StringBuilder sb = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sb.append(line).append("\n");
        }

        scanner.close();
        String str = sb.toString();

        TextAnnotation ta = taBldr.createTextAnnotation(corpus, in.getName(), str);
        View sents = ta.getView(ViewNames.SENTENCE);

        logger.info("processing file '{}'; input length is {}", in.getAbsolutePath(), str.length());
//        System.err.println("processing file '" + in.getAbsolutePath() + "'..." + " input length: " + str.length());
        List<Constituent> toks = ta.getView(ViewNames.TOKENS).getConstituents();
//        List<String> outputs = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();
        for (Constituent sent : sents) {
            int index = 1;
            for (Constituent tok : toks) {
                if (tok.getStartCharOffset() >= sent.getStartCharOffset() && tok.getEndCharOffset() <= sent.getEndCharOffset()) {
                    bldr.append(Integer.toString(index++)).append("\t").append(tok.getSurfaceForm()).append("\t").
                            append(tok.getStartCharOffset()).append("\t").append(tok.getEndCharOffset()).append(System.lineSeparator());
                }
            }
            bldr.append(System.lineSeparator()); // empty line to separate sentences
        }

        System.err.println("output length: " + bldr.toString().length());
//        LineIO.write(out, outputs);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(out)), StandardCharsets.UTF_8.name())) {
            writer.write(bldr.toString());
        } catch (IOException e) {
            logger.error("Can't write to file {}: {}", out, e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Process a directory containing plain text files (possibly in subdirectories).
     * Create a comparable structure rooted at outDir containing processed files. Create the root directory
     *     if it doesn't yet exist.
     * @param corpusName name of corpus
     * @param inDir directory of files to process
     * @param outDir output directory for processed files
     * @throws IOException if the input or output directories are invalid
     */
    public void processDir(String corpusName, String inDir, String outDir) throws IOException {

        if (inDir.equals(outDir))
            throw new IllegalArgumentException("Input directory and output directory must have different names ('" + inDir + "'");

        if (!IOUtils.isDirectory(inDir)) {
            throw new IOException("input directory '" + inDir + "' is not a directory.");
        }

        if (IOUtils.exists(outDir)) {
            if (!IOUtils.isDirectory(outDir))
               throw new IOException("output directory '" + outDir + "' is not a directory.");
        }
        else
            IOUtils.mkdir(outDir);

        FilenameFilter filter = (dir, name) -> !(new File(name).isDirectory());

        // get list of files -- absolute paths
        String[] inFiles = IOUtils.lsFilesRecursive(inDir, filter);

        for (String inFile : inFiles) {
            File in = new File(inFile);
            String name = in.getName();
            String outFile = outDir + "/" + name;
            processFile(corpusName, in, outFile);
        }
    }
}
