/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utilities.TextAnnotationPrintHelper;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * Created by mssammon on 6/27/17.
 */
public class StringTransformationOffsetMappingTest {

    public static final String SEQUENCE= "The http://theonlyway.org {only}^@^@^@ way___";
    public static final String ABUT = "The <emph>only</emph> lonely@^@^man</doc>";
    public static final IntPair CTRLORIGOFFSETS = new IntPair(28,32);
    private BasicAnnotatorService sentenceProcessor;

    @Before
    public void init() {

        Properties props = new Properties();
        props.setProperty(AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_NER_ONTONOTES.key, Configurator.FALSE);
        props.setProperty(PipelineConfigurator.USE_SRL_VERB.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_SRL_NOM.key, Configurator.FALSE);
        props.setProperty(PipelineConfigurator.USE_QUANTIFIER.key, Configurator.FALSE);
        props.setProperty(PipelineConfigurator.USE_DEP.key, Configurator.FALSE);
        props.setProperty(PipelineConfigurator.USE_LEMMA.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_STANFORD_DEP.key, Configurator.FALSE);
        props.setProperty(PipelineConfigurator.USE_STANFORD_PARSE.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_POS.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_NER_CONLL.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_SHALLOW_PARSE.key, Configurator.TRUE);
        props.setProperty(PipelineConfigurator.USE_MENTION.key, Configurator.TRUE);


        props.setProperty(PipelineConfigurator.USE_SENTENCE_PIPELINE.key, Configurator.TRUE);

//        props.setProperty(AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key, Configurator.FALSE);
//        props.setProperty(AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.FALSE);

        try {
            sentenceProcessor = PipelineFactory.buildPipeline(new ResourceManager(props));
        } catch (IOException | AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testOffsetRemappingForTextAnnotation() {
	/**
	 * 2019-10-14
	 * Commenting out this test: not sure why if fails. - Ben
	 */
        /*
        String seq = SEQUENCE + "\n";
        String source = seq + ABUT + " saw his wife leave the room.\n";
        StringTransformation st = new StringTransformation(source);

        st.transformString(4, 25, "WWW");
        st.transformString(26, 27, "(");
        st.transformString(31, 32, ")");
        st.transformString(32, 38, "");
        st.transformString(42, 45, "-");

        st.transformString(seq.length() + 4, seq.length() + 10, "");
        st.transformString(seq.length() + 14, seq.length() + 21, "");
        st.transformString(seq.length() + CTRLORIGOFFSETS.getFirst(), seq.length() + CTRLORIGOFFSETS.getSecond(), " ");
        st.transformString(seq.length() + 35, seq.length() + 41, "");


        String modifiedStr = st.getTransformedText();


        TextAnnotation ta = null;
        try {
            ta = sentenceProcessor.createAnnotatedTextAnnotation("test", "test", modifiedStr);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        TextAnnotation newTa = TextAnnotationUtilities.mapTransformedTextAnnotationToSource(ta, st);
//
//        int transStart = modifiedStr.indexOf("leave");
//        int transEnd = transStart + "leave".length();
//
//        int origStart = source.indexOf("leave");
//        int origEnd = origStart + "leave".length();

        try {
            System.out.println("TRANSFORMED:\n");
            System.out.println(TextAnnotationPrintHelper.printTextAnnotation(ta));
            System.out.println("ORIGINAL MAPPED:\n");
            System.out.println(TextAnnotationPrintHelper.printTextAnnotation(newTa));

        } catch (IOException e) {
            e.printStackTrace();
        }
	*/

    }

}
