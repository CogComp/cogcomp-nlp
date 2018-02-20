package org.cogcomp;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.ner.NerAnnotatorManager;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by daniel on 2/19/18.
 */
public class main {
    public static void main(String[] args) throws Exception {
        Properties p = new Properties();
        ResourceManager rm = new ResourceManager(p);
        NERAnnotator nerConll = NerAnnotatorManager.buildNerAnnotator(rm, ViewNames.NER_CONLL);
        nerConll.doInitialize();

        ServerClientAnnotator annotator = new ServerClientAnnotator();
        annotator.setUrl("http://austen.cs.illinois.edu", "5800"); // set the url and port name of your server here
        annotator.setViews(ViewNames.POS, ViewNames.LEMMA); // specify the views that you want
        TextAnnotation ta = annotator.annotate("This is the best sentence ever.");
        System.out.println(ta.getAvailableViews()); // here you should see that the required views are added.
        //ta.addView(nerConll);
        nerConll.getView(ta);
        System.out.println(ta.getAvailableViews());

    }
}




