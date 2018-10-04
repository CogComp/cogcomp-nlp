/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by mayhew2 on 11/5/15.
 */
public class TestTransliteration {

    /**
     * This is just a test to make sure that we can load and run everything.
     */
    @Test
    public void testModelLoad()
    {
        List<Example> examples = new ArrayList<>();
        examples.add(new Example("this", "this"));

        SPModel model = new SPModel(examples);

        boolean rom = false;
        model.Train(1,rom, examples);

        System.out.println(model.Probability("this", "this"));

    }

    /**
     * Test to ensure that Example equality is working correctly.
     */
    @Test
    public void testExampleEquality(){

        HashSet<Example> ee = new HashSet<>();

        Example e = new Example("John", "Smith");
        Example e2 = new Example("John", "Smith");

        ee.add(e);
        ee.add(e2);

        assert(ee.size() == 1);

        Example e3 = new Example("Smith", "John");
        ee.add(e3);

        assert(ee.size() == 2);
    }

    /**
     * Test to ensure that MultiExample equality is working correctly.
     */
    @Test
    public void testMultiExampleEquality(){

        HashSet<MultiExample> ee = new HashSet<>();

        List<String> l = new ArrayList<>();
        l.add("wut");
        l.add("why");
        MultiExample me = new MultiExample("John", l);

        List<String> l2 = new ArrayList<>();
        l2.add("wut");
        l2.add("why");
        MultiExample me2 = new MultiExample("John", l2);

        ee.add(me);
        ee.add(me2);

        assert(ee.size() == 1);

        List<String> l3 = new ArrayList<>();
        // these are in the opposite order.
        l3.add("why");
        l3.add("wut");
        MultiExample me3 = new MultiExample("John", l3);
        ee.add(me3);

        assert(ee.size() == 2);

    }

}
