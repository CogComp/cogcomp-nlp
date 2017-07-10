package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xuanyu on 7/9/2017.
 */
public class BIOFeatureExtractor {
    public static List<String> getGazetteerFeatures(Constituent c){
        List<String> ret_features = new ArrayList<>();
        String label = "";
        TextAnnotation ta = c.getTextAnnotation();
        View gazetteerView = ta.getView(ViewNames.GAZETTEER);
        Constituent gazetteerConstituent = gazetteerView.getConstituentsCovering(c).get(0);
        if (gazetteerConstituent != null){
            label = gazetteerConstituent.getLabel();
        }
        System.out.println(label);
        return ret_features;
    }
}
