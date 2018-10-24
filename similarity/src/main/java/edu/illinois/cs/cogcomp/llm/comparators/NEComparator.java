/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.llm.comparators;

import edu.illinois.cs.cogcomp.mrcs.comparators.Comparator;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.EntailmentResult;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import edu.illinois.cs.cogcomp.sim.Metric;
import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.NESim;

public class NEComparator implements Comparator<String, EntailmentResult> {

	private Metric NEComparator;

	public NEComparator() {

		NEComparator = new NESim();
	}

	@Override
	public EntailmentResult compare(String specific_, String general_) throws Exception {
		String genTok = StringCleanup.normalizeToLatin1(general_);
		String specTok = StringCleanup.normalizeToLatin1(specific_);

		double score = 0.0;
		String reason = "default reason";
		String source = WordComparator.class.getSimpleName();
		boolean isEntailed = false;
		boolean isPositivePolarity = true;

		if (specTok.equalsIgnoreCase(genTok)) {
			score = 1.0;
			reason = "Identity";
		} else {
			MetricResponse result = NEComparator.compare(specific_, general_);

			score = result.score;
			reason = result.reason;

			isEntailed = (Math.abs(score) > 0.5);
			isPositivePolarity = (score >= 0);

		}

		return new EntailmentResult(source, (float) score, reason, isEntailed, isPositivePolarity, true, null);
	}

}
