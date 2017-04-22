package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;

public class LLMStringSim implements Metric<String>{
	private  LlmStringComparator llm;
	private static final String config = "config/LlmConfig.txt";
	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException {
		String reason = "";
		try {
			llm = new LlmStringComparator( new ResourceManager( config ) );
			double score = llm.compareStrings( arg1, arg2 );
			return new MetricResponse(score,  reason);
		} catch (Exception e) {

		}
		return null;

	}

}
