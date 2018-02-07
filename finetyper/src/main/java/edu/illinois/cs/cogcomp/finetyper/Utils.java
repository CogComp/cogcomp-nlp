package edu.illinois.cs.cogcomp.finetyper;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haowu4 on 2/7/18.
 */
public class Utils {
    public static List<Constituent> getSentenceConstituents(Sentence sentence, TextAnnotation ta, String viewName) {
        List<Constituent> ret = new ArrayList<>();
        int start = sentence.getStartSpan();
        int end = sentence.getEndSpan();
        View view = ta.getView(viewName);

        for (Constituent ct : view.getConstituents()) {
            if (ct.getStartSpan() >= start && ct.getEndSpan() < end) {
                ret.add(ct);
            }
        }
        return ret;
    }
}
