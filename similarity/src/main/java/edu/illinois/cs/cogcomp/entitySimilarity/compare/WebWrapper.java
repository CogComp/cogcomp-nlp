package edu.illinois.cs.cogcomp.entitySimilarity.compare;

import java.util.HashMap;

public class WebWrapper {
	
	private static EntityComparison ec = null;
	private float score;
	private String reason = ""; 
	
	public WebWrapper() {
		try {
			ec = new EntityComparison();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void compare(String str1, String str2) {
		HashMap <String, String> items = new HashMap <String, String>();
		items.put("FIRST_STRING", str1);
		items.put("SECOND_STRING", str2);
		HashMap <String, String> result = ec.compare(items);
		score = Float.parseFloat(result.get("SCORE"));
		reason = result.get("REASON");
	}
	
	public float getScore() {
		return score;
	}
	
	public String getReason() {
		return reason;
	}
}
