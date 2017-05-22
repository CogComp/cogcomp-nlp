package edu.illinois.cs.cogcomp.entitySimilarity.compare;

public class WebWrapperTest {
	
	public static void main (String [] args) {
		WebWrapper test = new WebWrapper();
		
		test.compare("west virginia", "w. virginia");
		System.out.println(test.getScore());
		System.out.println(test.getReason());
		
		test.compare("schwartzenegger", "schwarzennegger");
		System.out.println(test.getScore());
		System.out.println(test.getReason());
	}
}
