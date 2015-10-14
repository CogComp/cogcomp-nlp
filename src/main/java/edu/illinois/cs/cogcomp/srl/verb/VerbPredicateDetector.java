package edu.illinois.cs.cogcomp.srl.verb;

import edu.illinois.cs.cogcomp.core.datastructures.Option;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.annotators.WordNetPlusLemmaViewGenerator;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.srl.core.AbstractPredicateDetector;

public class VerbPredicateDetector extends AbstractPredicateDetector {

	public VerbPredicateDetector(VerbSRLManager manager) {
		super(manager);
	}

	@Override
	public Option<String> getLemma(TextAnnotation ta, int tokenId) {
		String pos = POSUtils.getPOS(ta, tokenId);
		String token = ta.getToken(tokenId).toLowerCase();
		TokenLabelView lemmaView = (TokenLabelView) ta.getView(ViewNames.LEMMA);
		String lemma = lemmaView.getConstituentAtToken(tokenId).getLabel();
		boolean predicate = false;

		// any token that is a verb is a predicate
		if (POSUtils.isPOSVerb(pos) && !pos.equals("AUX")) {

			if (token.equals("'s") || token.equals("'re") || token.equals("'m"))
				lemma = "be";
			else if (token.equals("'d") || lemma.equals("wo")
					|| lemma.equals("'ll"))
				lemma = "xmodal";

			predicate = true;

			if (lemma.equals("xmodal") || pos.equals("MD") || token.equals("'ve")) // modals and some
				predicate = false;

			// ignore all instances of has + "to be" if they are followed by a
			// verb or if the token is "be" followed by a verb

			boolean doVerb = lemma.equals("do");
			boolean be = lemma.equals("be");
			boolean have = lemma.equals("have");

			if (tokenId < ta.size() - 1) {

				if (be) {
					SpanLabelView chunk = (SpanLabelView) ta.getView(ViewNames.SHALLOW_PARSE);
					for (Constituent c : chunk.getConstituentsCoveringToken(tokenId)) {
						// if the token under consideration is not the last
						// token, then there is another verb here
						if (c.getEndSpan() - 1 != tokenId) {
							predicate = false;
							break;
						}
					}
				}

				// ignore "have + be"
				if (have && lemmaView.getConstituentAtToken(tokenId+1).getLabel().equals("be")) {
					predicate = false;
				}

				// ignore "have/do + verb"
				if ((have || doVerb) && POSUtils.isPOSVerb(POSUtils.getPOS(ta, tokenId + 1)))
					predicate = false;

				// for some reason "according" in 'according to' is tagged as a
				// verb. we want to avoid this.

				if (token.equals("according") && ta.getToken(tokenId + 1).toLowerCase().equals("to"))
					predicate = false;
			}

			if (tokenId < ta.size() - 2) {

				// ignore don't + V or haven't + V
				if (doVerb || have) {
					String nextToken = ta.getToken(tokenId + 1).toLowerCase();

					if ((nextToken.equals("n't") || nextToken.equals("not"))
							&& POSUtils.isPOSVerb(POSUtils.getPOS(ta, tokenId + 2)))
						predicate = false;

				}
			}

			// NOTE: Not treating "have to" cases as exceptions because there
			// are some instances annotated as verbs and some that aren't.

			// else if (has.contains(token)) {
			// have to

			// except when the token is a variant of "has to"
			// if (tokenId < ta.size() - 1
			// && ta.getToken(tokenId + 1).toLowerCase().equals("to"))
			// predicate = false;
			// } else {
			// or when there is another VP following the token (Not sure why
			// this would happen, though...)
			// predicate &= checkVPAmongSiblings(ta, tokenId);

			// }

		} else if (token.startsWith("re-")) {
			String trim = token.replace("re-", "");
			predicate = WordNetPlusLemmaViewGenerator.lemmaDict.contains(trim);
		}

		if (predicate) {

			return new Option<>(lemma);
		} else {
			return Option.empty();
		}
	}
}
