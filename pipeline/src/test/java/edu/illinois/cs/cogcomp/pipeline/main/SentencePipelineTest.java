/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;
import edu.illinois.cs.cogcomp.annotation.BasicAnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Sanity check for sentence-level pipeline.
 */
public class SentencePipelineTest {

    private static BasicAnnotatorService sentenceProcessor;
    private static BasicAnnotatorService normalProcessor;

    @BeforeClass
    public static void init() throws IOException, AnnotatorException {

        Properties props = new Properties();
        props.setProperty( PipelineConfigurator.USE_NER_ONTONOTES.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_SRL_VERB.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_SRL_NOM.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_QUANTIFIER.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_DEP.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_LEMMA.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_STANFORD_DEP.key, Configurator.FALSE );
        props.setProperty( PipelineConfigurator.USE_STANFORD_PARSE.key, Configurator.TRUE );
        props.setProperty( PipelineConfigurator.USE_POS.key, Configurator.TRUE );
        props.setProperty( PipelineConfigurator.USE_NER_CONLL.key, Configurator.TRUE );


        props.setProperty( PipelineConfigurator.USE_SENTENCE_PIPELINE.key, Configurator.TRUE );

        props.setProperty( AnnotatorServiceConfigurator.FORCE_CACHE_UPDATE.key, Configurator.FALSE );
        props.setProperty( AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.TRUE );

        sentenceProcessor = PipelineFactory.buildPipeline(new ResourceManager(props));

        props.setProperty( PipelineConfigurator.USE_SENTENCE_PIPELINE.key, Configurator.FALSE );

        normalProcessor = PipelineFactory.buildPipeline(new ResourceManager(props));
    }


    @Test
    public void testSentencePipeline() {
        String[] viewsToAdd = new String[]{};
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);

        String[] viewsToAnnotate = {ViewNames.POS, ViewNames.NER_CONLL, ViewNames.PARSE_STANFORD};
        Set<String> viewSet = new HashSet<>();
        for (String viewName : viewsToAnnotate)
            viewSet.add(viewName);

        try {
            sentenceProcessor.addViewsAndCache(ta, viewSet, false);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(ta.hasView(ViewNames.POS));
        assertEquals(29, ta.getView(ViewNames.POS).getConstituents().size());
        assertTrue(ta.hasView(ViewNames.NER_CONLL));
        assertEquals(1, ta.getView(ViewNames.NER_CONLL).getConstituents().size());
        assertTrue(ta.hasView(ViewNames.PARSE_STANFORD));
        assertEquals(84, ta.getView(ViewNames.PARSE_STANFORD).getConstituents().size());

        TextAnnotation normalTa = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);

        try {
            normalProcessor.addViewsAndCache(normalTa, viewSet, false);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(normalTa.hasView(ViewNames.POS));
        assertEquals(29, normalTa.getView(ViewNames.POS).getConstituents().size());
        assertTrue(ta.hasView(ViewNames.NER_CONLL));
        assertEquals(1, ta.getView(ViewNames.NER_CONLL).getConstituents().size());
        assertTrue(ta.hasView(ViewNames.PARSE_STANFORD));
        assertEquals(84, ta.getView(ViewNames.PARSE_STANFORD).getConstituents().size());


        List<Constituent> sentPos = ta.getView(ViewNames.POS).getConstituents();
        List<Constituent> normalPos = normalTa.getView(ViewNames.POS).getConstituents();
        for (int i = 0; i < sentPos.size(); ++i)
            assertEquals(normalPos.get(i), sentPos.get(i));
    }
}
