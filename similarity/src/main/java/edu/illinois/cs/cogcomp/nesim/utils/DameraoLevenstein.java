package edu.illinois.cs.cogcomp.nesim.utils;

import edu.illinois.cs.cogcomp.core.algorithms.LevensteinDistance;

public class DameraoLevenstein extends LevensteinDistance {
	public int score(String s1,String s2){
		return getLevensteinDistance(s1,s2);
	}
}