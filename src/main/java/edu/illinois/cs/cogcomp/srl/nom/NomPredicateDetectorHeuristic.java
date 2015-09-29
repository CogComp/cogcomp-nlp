package edu.illinois.cs.cogcomp.srl.nom;

import edu.illinois.cs.cogcomp.core.datastructures.Option;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.NomLexEntry;
import edu.illinois.cs.cogcomp.edison.utilities.NomLexEntry.NomLexClasses;
import edu.illinois.cs.cogcomp.edison.utilities.NomLexReader;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.srl.core.AbstractPredicateDetector;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.*;

public class NomPredicateDetectorHeuristic extends AbstractPredicateDetector {

	private static final SnowballStemmer stemmer = new englishStemmer();
	private final static Map<String, String> nonStandard;
	private final static Set<String> pluralLemmas;

	private static final Set<NomLexClasses> classes;

	static {

		nonStandard = new HashMap<String, String>();

		nonStandard.put("bondholder", "holder");
		nonStandard.put("earthquake", "quake");
		nonStandard.put("spokesperson", "person");
		nonStandard.put("allies", "ally");
		nonStandard.put("liabilities", "liability");
		nonStandard.put("reelection", "election");
		nonStandard.put("coauthor", "author");
		nonStandard.put("people", "person");
		nonStandard.put("supressor", "suppressor");
		nonStandard.put("buyout", "buy-out");
		nonStandard.put("hookup", "hook-up");
		nonStandard.put("ceasefire", "cease-fire");
		nonStandard.put("startup", "start-up");
		nonStandard.put("eurobond", "bond");

		pluralLemmas = new HashSet<String>();
		pluralLemmas.addAll(Arrays.asList("filers", "hundreds", "thousands",
				"millions", "billions", "tens"));

		// All NomLex classes are accounted for here.
		classes = new HashSet<NomLexClasses>();
		classes.addAll(NomLexEntry.VERBAL);
		classes.addAll(NomLexEntry.ADJECTIVAL);
		classes.addAll(NomLexEntry.NON_VERB_ADJ);

	}
	private final NomLexReader nomLex;

	public NomPredicateDetectorHeuristic(NomSRLManager manager)
			throws EdisonException {
		super(manager);
		nomLex = NomLexReader.getInstance();
	}

	@Override
	public Option<String> getLemma(TextAnnotation ta, int tokenId) {

		String pos = POSUtils.getPOS(ta, tokenId);

		boolean isNoun = POSUtils.isPOSNoun(pos);
		if (!isNoun) {
			return Option.empty();
		} else {

			Option<String> opt = Option.empty();
			String token = ta.getToken(tokenId).toLowerCase();
			if (pluralLemmas.contains(token)) {
				opt = testTokenVariations(token);
			} else {

				TokenLabelView lemmaView = (TokenLabelView) ta.getView(ViewNames.LEMMA);
				String lemma = lemmaView.getConstituentAtToken(tokenId).getLabel();

				opt = testTokenVariations(lemma);

				if (!opt.isPresent() && !lemma.matches("-*")) {
					opt = testWithDelim(lemma, '-');
				}

				if (!opt.isPresent()) {
					opt = testWithDelim(lemma, ' ');
				}
			}

			return opt;
		}
	}

	private Option<String> testWithDelim(String token, char delim) {
		Option<String> found = Option.empty();

		if (debug) {
			System.out.println("Testing with delimiter: " + delim + ". Token="
					+ token);
		}

		if (token.indexOf(delim) >= 0) {
			String[] split = token.split("" + delim);
			String lastElement = split[split.length - 1];

			if (debug) {

				System.out.println(lastElement);
			}

			found = testTokenVariations(lastElement);
		}
		return found;
	}

	private Option<String> testTokenVariations(String token) {

		Option<String> opt = Option.empty();
		if (nomLex.isPlural(token)) {
			token = nomLex.getSingular(token);
		}

		opt = testTokenInstance(token);

		if (!opt.isPresent()) {
			if (token.endsWith("s")) {
				String prefix = token.substring(0, token.length() - 1);
				opt = testTokenInstance(prefix);
			}
		}

		if (!opt.isPresent()) {

			if (debug) {
				System.out.println("Testing hard-coded suffixes");
			}
			// hard coded lemma for bookmaker, steelmaker, downpayments, etc
			if (token.endsWith("maker") || token.endsWith("makers"))
				opt = new Option<String>("maker");
			else if (token.endsWith("payment") || token.endsWith("payments"))
				opt = new Option<String>("payment");
		}

		if (!opt.isPresent()) {
			if (debug)
				System.out.println("Testing for counter+X");

			if (token.startsWith("counter")) {
				String suffix = token.replace("counter", "");
				opt = testTokenInstance(suffix);
			}
		}

		if (!opt.isPresent()) {
			token = getPorterLemma(token);
			opt = testTokenInstance(token);
		}

		return opt;
	}

	private Option<String> testTokenInstance(String token) {
		if (debug) {
			System.out.println("Testing token " + token);
		}

		if (nonStandard.containsKey(token)) {
			token = nonStandard.get(token);
			if (debug) {
				System.out.println("Non standard lemma for token: " + token);
			}
		}

		Option<String> found = Option.empty();
		if (nomLex.containsEntry(token)) {

			List<NomLexEntry> entry = nomLex.getNomLexEntry(token);

			if (debug) {
				System.out.println("Nomlex entry found: ");
				for (NomLexEntry e : entry) {
					System.out.println("\t" + e.nomClass);
				}
			}

			for (NomLexEntry e : entry) {
				if (classes.contains(e.nomClass)) {
					found = new Option<String>(token);
					break;
				}
			}
		}
		return found;
	}

	private static synchronized String getPorterLemma(String token) {
		stemmer.setCurrent(token);
		stemmer.stem();
		return stemmer.getCurrent();
	}
}
