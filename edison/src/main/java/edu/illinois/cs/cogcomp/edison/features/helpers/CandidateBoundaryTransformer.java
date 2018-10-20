/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;

import java.util.Collections;
import java.util.List;

public class CandidateBoundaryTransformer extends FeatureInputTransformer {

    @Override
    public List<Constituent> transform(Constituent input) {
        TextAnnotation ta = input.getTextAnnotation();

        Constituent ce = new Constituent("", "", ta, input.getEndSpan() - 1, input.getEndSpan());

        Constituent cs =
                new Constituent("", "", ta, input.getStartSpan(), input.getStartSpan() + 1);

        new Relation("", cs, ce, 0);

        return Collections.singletonList(ce);
    }

    @Override
    public String name() {
        return "#b:";
    }

}
