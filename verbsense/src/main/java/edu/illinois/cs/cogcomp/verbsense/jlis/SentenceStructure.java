package edu.illinois.cs.cogcomp.verbsense.jlis;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVector;
import edu.illinois.cs.cogcomp.verbsense.Constants;
import edu.illinois.cs.cogcomp.verbsense.core.SenseManager;

import java.util.List;

public class SentenceStructure implements IStructure {

	public final SentenceInstance x;
	public final List<SenseStructure> ys;

	public SentenceStructure(SentenceInstance instance, List<SenseStructure> ys) {
		this.x = instance;
		this.ys = ys;
	}

	@Override
	public FeatureVector getFeatureVector() {
		throw new RuntimeException("Not yet implemented!");
	}

	public TokenLabelView getView(SenseManager manager, TextAnnotation ta) {
		String viewName = SenseManager.getPredictedViewName();
		TokenLabelView view = new TokenLabelView(viewName, Constants.systemIdentifier, ta, 1.0);

		for (SenseStructure y : this.ys) {
			SenseInstance senseInstance = y.getInstance();
			IntPair predicateSpan = senseInstance.getConstituent().getSpan();

			String sense = manager.getSense(y.getLabel());
			view.addTokenLabel(predicateSpan.getFirst(), sense, 1.0);
		}
		return view;
	}
}
