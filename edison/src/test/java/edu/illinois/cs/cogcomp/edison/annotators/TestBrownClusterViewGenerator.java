/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.config.BrownClusterViewGeneratorConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Chen-Tse Tsai
 */
public class TestBrownClusterViewGenerator {
    private static Logger logger = LoggerFactory.getLogger(TestBrownClusterViewGenerator.class);


    /**
     * Test the configuration of normalizing tokens in the brown clusters
     */
    @Test
    public final void testCharniakParseViewGenerator() {
        String sentence = "a test .";

        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(sentence);

        // The default configuration: do normalization
        BrownClusterViewGenerator viewGenerator = null;
        try {
            viewGenerator = new BrownClusterViewGenerator(BrownClusterViewGenerator.file100, BrownClusterViewGenerator.file100);
            viewGenerator.addView(ta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpanLabelView view = (SpanLabelView) ta.getView(viewGenerator.getViewName());
        assertEquals("a", view.getConstituents().get(0).getSurfaceForm());
        assertEquals("111011111", view.getConstituents().get(0).getLabel());
        assertEquals("a", view.getConstituents().get(1).getSurfaceForm());
        assertEquals("10010", view.getConstituents().get(1).getLabel());
        assertEquals("test", view.getConstituents().get(2).getSurfaceForm());
        assertEquals("001110", view.getConstituents().get(2).getLabel());

        // Don't normalize tokens in the brown clusters
        Properties props = new Properties();
        props.setProperty(BrownClusterViewGeneratorConfigurator.NORMALIZE_TOKEN.key,
                Configurator.FALSE);
        ResourceManager rm = new ResourceManager(props);
        try {
            viewGenerator = new BrownClusterViewGenerator(BrownClusterViewGenerator.file100, BrownClusterViewGenerator.file100, rm);
            viewGenerator.addView(ta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        view = (SpanLabelView) ta.getView(viewGenerator.getViewName());
        assertEquals("a", view.getConstituents().get(0).getSurfaceForm());
        assertEquals("10010", view.getConstituents().get(0).getLabel());
        assertEquals("test", view.getConstituents().get(1).getSurfaceForm());
        assertEquals("001110", view.getConstituents().get(1).getLabel());
    }
}
