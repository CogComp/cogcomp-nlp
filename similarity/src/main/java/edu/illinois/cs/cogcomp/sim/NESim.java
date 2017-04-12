package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;

import edu.illinois.cs.cogcomp.nesim.compare.EntityComparison;

public class NESim implements Metric<String> {

	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException{
		try {
			EntityComparison entityComp = new EntityComparison();		
			entityComp.compare(arg1, arg2);
			float score = entityComp.getScore();
			String reason = entityComp.getReason();
			return new MetricResponse(score, reason);
			} catch (IOException e) {

		}
		return null;
	}

}
