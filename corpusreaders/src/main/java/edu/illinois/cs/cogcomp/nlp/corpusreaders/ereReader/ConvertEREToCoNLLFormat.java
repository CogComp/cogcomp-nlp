/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;

import static edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader.*;

/**
 * Read the ERE data and produce, in CoNLL format, gold standard data including named and nominal
 * named entities, but excluding pronouns.
 * 
 * @author redman
 * @author mssammon
 */
public class ConvertEREToCoNLLFormat {

    private static final String NAME = ConvertEREToCoNLLFormat.class.getCanonicalName();

    /**
     * @param args command line arguments: corpus directory, include Nominals or not, and output
     *        directory.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 5) {
            System.err.println("Usage: " + NAME + " ERECorpusVal corpusRoot includeNominals<true|false> outDir\n\nSee " +
            "module README or ERECorpusReader.EreCorpus enumeration for possible values.");
            System.exit(-1);
        }

        final String ereCorpusVal = args[0];
        final String corpusRoot = args[1];

        final boolean includeNominals = Boolean.parseBoolean(args[2]);

        final String conllDir = args[3];


        if (IOUtils.exists(conllDir))
            if (!IOUtils.isDirectory(conllDir)) {
                System.err.println("Output directory '" + conllDir
                        + "' exists and is not a directory.");
                System.exit(-1);
            } else
                IOUtils.mkdir(conllDir);

        boolean throwExceptionOnXmlTagMismatch = true;
        ERENerReader reader =
                new ERENerReader(EreCorpus.valueOf(ereCorpusVal), corpusRoot, throwExceptionOnXmlTagMismatch, includeNominals, includeNominals);

        while (reader.hasNext()) {
            XmlTextAnnotation xmlTa = reader.next();
            TextAnnotation ta = xmlTa.getTextAnnotation();
            View nerView = ta.getView(reader.getMentionViewName());
            CoNLL2002Writer.writeViewInCoNLL2003Format(nerView, ta,
                    conllDir + "/" + ta.getCorpusId() + ".txt");
        }
    }
}
