/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.spelling.ashwin;

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
	/*
	private void play() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.equals("end"))
					break;
				String[] parts = line.split("----");
				if (parts.length != 2)
					continue;
				float score = score(parts[0].trim().toLowerCase(), parts[1].trim().toLowerCase());
				System.out.println(score);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}