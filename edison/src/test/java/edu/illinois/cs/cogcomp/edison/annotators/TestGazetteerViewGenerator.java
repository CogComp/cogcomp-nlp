/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Daniel Khashbai
 */
public class TestGazetteerViewGenerator {
    private static Logger logger = LoggerFactory.getLogger(TestGazetteerViewGenerator.class);

    TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);
    GazetteerViewGenerator ge = GazetteerViewGenerator.cbcInstance;

    @Test
    public final void testCBCClusters() {
        ge.addView(ta);
        String gold = "[N305.gz construction ] [N323.gz construction ] [N521.gz construction ] [N31.gz John Smith ] [N38.gz John ] [N549.gz John ] [N1344.gz library ] [N155.gz library ] [N35.gz library ] [N624.gz library ] [A1467.gz finished ] [A1500.gz finished ] [A1708.gz finished ] [N1192.gz time ] [N134.gz time ] [N1358.gz time ] [N18.gz time ] [N477.gz time ] [N8.gz time ] ";
        System.out.println(ta.getView(ge.getViewName()).toString());
        assertEquals(ta.getView(ge.getViewName()).toString(), gold);
    }

    @Test
    public final void testGazetteers() {
        GazetteerViewGenerator.gazetteersInstance.ignoreGazetteer("Weapons.gz");
        GazetteerViewGenerator.gazetteersInstance.ignoreGazetteer("Weapons.Missile.gz");

        List<String[]> sentences = Arrays.asList("I live in Chicago , Illinois .".split("\\s+"), "I met George Bush .".split("\\s+"));
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(sentences);

        GazetteerViewGenerator.gazetteersInstance.addView(ta);

        String gold = "[Films.gz I ] [ArtWork.gz Chicago ] [Films.gz Chicago ] [Locations.Cities.gz Chicago ] [Locations.Cities.US.gz Chicago ] [Locations.gz Chicago ] [Languages.gz Illinois ] [Locations.gz Illinois ] [Locations.Regions.gz Illinois ] [Locations.States.gz Illinois ] [Films.gz I ] [Locations.Cities.gz George ] [Locations.gz George ] [ManMadeObjects.gz George ] [People.Famous.gz George ] [People.Famous.gz George ] [People.Famous.gz George Bush ] [People.Famous.gz George Bush ] [People.FirstNames.gz George ] [People.gz George ] [People.gz George ] [People.gz George Bush ] [People.gz George Bush ] [TV.Programs.gz George ] [Locations.Cities.gz Bush ] [Locations.gz Bush ] [People.FirstNames.gz Bush ] [People.gz Bush ] ";
        assertEquals(ta.getView(GazetteerViewGenerator.gazetteersInstance.getViewName()).toString(), gold);
    }

}
