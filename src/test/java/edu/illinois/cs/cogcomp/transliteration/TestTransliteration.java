package edu.illinois.cs.cogcomp.transliteration;

import org.junit.Test;

import java.util.ArrayList;
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

        model.Train(1);

        System.out.println(model.Probability("this", "this"));

    }

}
