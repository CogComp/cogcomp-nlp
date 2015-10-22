package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetHelper;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

/**
 * If the word is present in WordNet, the lemma from WordNet is used. Otherwise,
 * it defaults to the word itself (with case).
 * <p/>
 * This lemmatizer lowercases words before finding their lemma. This could lead
 * to errors.
 *
 * @author Vivek Srikumar
 * @deprecated Use {@code illinois-lemmatizer} instead
 */
public class WordNetLemmaViewGenerator implements Annotator {

	private WordNetManager wn;

	public WordNetLemmaViewGenerator(WordNetManager wn) {
		this.wn = wn;
	}

	@Override
	public String getViewName() {
		return ViewNames.LEMMA;
	}

	@Override
	public View getView(TextAnnotation ta) {
		TokenLabelView view = new TokenLabelView(ViewNames.LEMMA, "WordNet", ta, 1.0);
		for (int i = 0; i < ta.size(); i++) {
			String word = ta.getToken(i).toLowerCase().trim();

			POS wnPOS = WordNetHelper.getWNPOS(WordHelpers.getPOS(ta, i));

			String lemma;
			if (wnPOS == null) lemma = ta.getToken(i);
			else {

				try {
					lemma = wn.getLemma(word, wnPOS);
				} catch (JWNLException e) {
					lemma = ta.getToken(i);
				}
			}
			view.addTokenLabel(i, lemma, 1.0);
		}

		return view;
	}

	@Override
	public String[] getRequiredViews() {
		return new String[0];
	}
}
