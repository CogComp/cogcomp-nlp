package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class SpanLabelsHelper {
    public static List<Constituent> getConstituentsInBetween(SpanLabelView view, int start, int end) {

        List<Constituent> output = view.getConstituentsCoveringSpan(start, end);
        List<Constituent> restrictedOutput = new ArrayList<>();

        for (Constituent c : output) {
            if ((c.getStartSpan() >= start) && (c.getEndSpan() <= end))
                restrictedOutput.add(c);
        }

        return restrictedOutput;
    }

}
