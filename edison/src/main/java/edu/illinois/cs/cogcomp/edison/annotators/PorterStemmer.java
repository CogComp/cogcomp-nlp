package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 * Adds a {@link ViewNames#LEMMA} view using the Porter stemming algorithm
 *
 * @author Vivek Srikumar
 * @deprecated Use {@code illinois-lemmatizer} instead
 */
public class PorterStemmer implements Annotator {

	public static final PorterStemmer instance = new PorterStemmer();

	private final static SnowballStemmer stemmer = new englishStemmer();

	@Override
	public String getViewName() {
		return ViewNames.LEMMA;
	}

	@Override
	public View getView(TextAnnotation input) {
		TokenLabelView view = new TokenLabelView(ViewNames.LEMMA, "PorterStemmer", input, 1.0);

		synchronized (instance) {
			for (int i = 0; i < input.size(); i++) {
				stemmer.setCurrent(input.getToken(i));

				stemmer.stem();

				view.addTokenLabel(i, stemmer.getCurrent(), 1.0);
			}
		}
		return view;
	}

	@Override
	public String[] getRequiredViews() {
		return new String[0];
	}

}
