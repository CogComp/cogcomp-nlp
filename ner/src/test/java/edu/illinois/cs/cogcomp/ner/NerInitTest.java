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
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 *  A test that excludes big resource files for a quick assessment of basic NER behavior,
 *     specifically initialization of models
 *  Created by mssammon on 4/14/16.
 *
 */
public class NerInitTest
{
    final static String TESTSTR = "NOHO Ltd. partners with Telford International Company Inc";

    @Test
    public void testInit()
    {
        Properties props = new Properties();
        props.setProperty(NerBaseConfigurator.GAZETTEER_FEATURES, "0" );
        props.setProperty( NerBaseConfigurator.BROWN_CLUSTER_PATHS, "0" );

        ResourceManager rm = ( new NerBaseConfigurator() ).getConfig( new ResourceManager( props ) );

        NERAnnotator ner = NerAnnotatorManager.buildNerAnnotator( rm, ViewNames.NER_CONLL );

        assertNotNull( ner );

        TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder( new StatefulTokenizer() );
        TextAnnotation ta = tab.createTextAnnotation(TESTSTR);

        try {
            ner.getView( ta );
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        assert( ta.hasView( ViewNames.NER_CONLL ) );
        assertEquals( ta.getView( ViewNames.NER_CONLL ).getConstituents().size(), 2 );
    }


}
