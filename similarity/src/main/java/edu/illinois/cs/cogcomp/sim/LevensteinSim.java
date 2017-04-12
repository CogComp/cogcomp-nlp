package edu.illinois.cs.cogcomp.sim;


import edu.illinois.cs.cogcomp.nesim.utils.DameraoLevenstein;


public class LevensteinSim implements Metric<String> {
	
	private static DameraoLevenstein leven = new DameraoLevenstein();
	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException {
		double score=doComparison(arg1,arg2);
		String reason="DameraoLevenstein";
		return new MetricResponse(score, reason);
	}

	
	private double doComparison(String first, String second) {
		
		String concat = first + second;
		return   1.0f - leven.score(first, second) / concat.length();

	}
}
