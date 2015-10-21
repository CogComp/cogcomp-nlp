package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.core.datastructures.Option;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;

import java.util.ArrayList;
import java.util.List;

/**
 * A skeleton for a predicate detector. This could be implemented either by
 * heuristics (both verb and nominalization SRL systems come with heuristics)
 * and also a learned predicate detector.
 * 
 * @author Vivek Srikumar
 * 
 */
public abstract class AbstractPredicateDetector {

	private final SRLManager manager;

	public AbstractPredicateDetector(SRLManager manager) {
		this.manager = manager;
	}

	public boolean debug = false;

	public abstract Option<String> getLemma(TextAnnotation ta, int tokenId)
			throws Exception;

	public List<Constituent> getPredicates(TextAnnotation ta) throws Exception {
		List<Constituent> list = new ArrayList<>();

		for (int i = 0; i < ta.size(); i++) {
            Option<String> opt = getLemma(ta, i);

			if (opt.isPresent()) {
				Constituent c = new Constituent("", "", ta, i, i + 1);
				c.addAttribute(CoNLLColumnFormatReader.LemmaIdentifier, opt.get());
				list.add(c);
			}
		}

		return list;
	}

	public SRLManager getManager() {
		return manager;
	}

}
