package edu.illinois.cs.cogcomp.nesim.utils;


public class DameraoLevenstein {
	
	private int INF = 100;
	private String A, B;
	private int AL, BL;
	private int[][] dp;
	
	public DameraoLevenstein() {
		A = "";
		B = "";
		AL = 0;
		BL = 0;
	}
	
	public int ED(int i, int j) {
		try {
			if (i == AL && j == BL)
				return 0;
			else if (i == AL)
				return (BL-j);
			else if (j == BL)
				return (AL-i);
			else {
				if (dp[i][j] != -1)
					return dp[i][j];
				int cost = 0;
				if (A.charAt(i) != B.charAt(j))
					cost++;
				dp[i][j] = Math.min(ED(i+1, j+1)+cost, 1+Math.min(ED(i+1, j), ED(i, j+1)));
				if (i+1 < AL && j+1 < BL && A.charAt(i) == B.charAt(j+1) && A.charAt(i+1) == B.charAt(j))
					dp[i][j] = Math.min(dp[i][j], 1+ED(i+2, j+2));
				return dp[i][j];
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return INF;
		}
	}
	
	/**
	 * Returns the damerao-levenstein distance between @A and @B.
	 * Damerao-Levenstein distance is a variant of Levenstein distance that allows transposition of two adjacent characters.
	 * Read http://en.wikipedia.org/wiki/Damerauâ€“Levenshtein_distance for more details.
	 * @param A
	 * @param B
	 * @return
	 * @author ashwink
	 */
	public float score(String A, String B) {
		try {
			this.A = A.trim().toLowerCase();
			this.B = B.trim().toLowerCase();
			AL = A.length();
			BL = B.length();
			dp = new int[AL][BL];
			for (int i = 0; i < AL; i++)
				for (int j = 0; j < BL; j++)
					dp[i][j] = -1;
			return (float)ED(0, 0);
		}
		catch (Exception e) {
			e.printStackTrace();
			return (float)INF;
		}
	}
	
	public static void main(String[] args) {
		DameraoLevenstein ed = new DameraoLevenstein();
		System.out.println(ed.score("Obama", "'bama"));
	}
}