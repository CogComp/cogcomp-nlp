package edu.illinois.cs.cogcomp.edison.features.lrec;

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

		Constituent ce = new Constituent("", "", ta,
				input.getEndSpan() - 1, input.getEndSpan());

		Constituent cs = new Constituent("", "", ta, input.getStartSpan(),
				input.getStartSpan() + 1);

		new Relation("", cs, ce, 0);

		return Collections.singletonList(ce);
	}
	
	@Override
	public String name() {
		return "#b:";
	}

}
