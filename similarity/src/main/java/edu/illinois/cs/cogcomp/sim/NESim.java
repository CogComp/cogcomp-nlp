/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;
import edu.illinois.cs.cogcomp.nesim.compare.EntityComparison;

/**
 * Name Entity Comparison Metric
 * 
 * @author shaoshi
 *
 */

public class NESim implements Metric<String> {
	EntityComparison entityComp;

	public NESim() {
		try {
			entityComp = new EntityComparison();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException {

		entityComp.compare(arg1, arg2);
		float score = entityComp.getScore();
		String reason = entityComp.getReason();
		return new MetricResponse(score, reason);

	}

    public MetricResponse compare(String arg1, String arg2, String type1, String type2) {
		entityComp.compare(arg1, arg2, type1, type2);
		float score = entityComp.getScore();
		String reason = entityComp.getReason();
		return new MetricResponse(score, reason);
	}

}
