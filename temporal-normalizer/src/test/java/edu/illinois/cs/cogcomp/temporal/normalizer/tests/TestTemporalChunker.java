/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.tests;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerAnnotator;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by zhilifeng on 11/1/16.
 * This is the test class for TemporalChunkerAnnotator
 * In addition, users can follow the test to set up annotators.
 * Also we provide two evaluations: 1) normalizing with gold chunks, and
 * 2) normalizing with our extraction (using chunker)
 */
public class TestTemporalChunker {
    private TemporalChunkerAnnotator tca;

    @Before
    public void setUp() throws IOException, ParserConfigurationException, SAXException {
        Properties rmProps = new TemporalChunkerConfigurator().getDefaultConfig().getProperties();
        rmProps.setProperty("useHeidelTime", "False");
        tca = new TemporalChunkerAnnotator(new ResourceManager(rmProps));
    }

    @Test
    public void testTemporalChunkerWithPlainText() throws Exception{
        String text = "The flu season is winding down, and it has killed 105 children so far - about the average toll.\n" +
                "\n" +
                "The season started about a month earlier than usual, sparking concerns it might turn into the worst in " +
                "a decade. It ended up being very hard on the elderly, but was moderately severe overall, according to " +
                "the Centers for Disease Control and Prevention.\n" +
                "\n" +
                "Six of the pediatric deaths were reported in the last week, and it's possible there will be more, said " +
                "the CDC's Dr. Michael Jhung said Friday.\n" +
                "\n" +
                "Roughly 100 children die in an average flu season. One exception was the swine flu pandemic of " +
                "2009-2010, when 348 children died.\n" +
                "\n" +
                "The CDC recommends that all children ages 6 months and older be vaccinated against flu each season, " +
                "though only about half get a flu shot or nasal spray.\n" +
                "\n" +
                "All but four of the children who died were old enough to be vaccinated, but 90 percent of them did " +
                "not get vaccinated, CDC officials said.\n" +
                "\n" +
                "This year's vaccine was considered effective in children, though it didn't work very well in older " +
                "people. And the dominant flu strain early in the season was one that tends to " +
                "cause more severe illness.\n" +
                "\n" +
                "The government only does a national flu death count for children. But it does track hospitalization " +
                "rates for people 65 and older, and those statistics have been grim.\n" +
                "\n" +
                "In that group, 177 out of every 100,000 were hospitalized with flu-related illness in the past " +
                "several months. That's more than 2 1/2 times higher than any other recent season.\n" +
                "\n" +
                "This flu season started in early December, a month earlier than usual, and peaked by the end " +
                "of year. Since then, flu reports have been dropping off throughout the country.\n" +
                "\n" +
                "\"We appear to be getting close to the end of flu season,\" Jhung said.";
        TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = tab.createTextAnnotation("corpus", "id", text);
        POSAnnotator annotator = new POSAnnotator();
        try {
            annotator.getView(ta);
        } catch (AnnotatorException e) {
            fail("AnnotatorException thrown!\n" + e.getMessage());
        }
        tca.addView(ta);
        View temporalViews = ta.getView(ViewNames.TIMEX3);
        List<Constituent> constituents = temporalViews.getConstituents();
        assertEquals("<TIMEX3 type=\"DURATION\" value=\"P1M\">", constituents.get(0).getLabel());
    }
}
