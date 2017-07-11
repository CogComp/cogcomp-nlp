/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import java.util.*;

public class DateMapping {

	// Create a hash map
	HashMap<String, String> hm = new HashMap<String, String>();

	DateMapping() {
		hm.put("jan", "1");
		hm.put("january", "1");
		hm.put("feb", "2");
		hm.put("february", "2");
		hm.put("mar", "3");
		hm.put("march", "3");
		hm.put("apr", "4");
		hm.put("april", "4");
		hm.put("may", "5");
		hm.put("jun", "6");
		hm.put("june", "6");
		hm.put("jul", "7");
		hm.put("july", "7");
		hm.put("aug", "8");
		hm.put("august", "8");
		hm.put("sept", "9");
		hm.put("september", "9");
		hm.put("oct", "10");
		hm.put("october", "10");
		hm.put("nov", "11");
		hm.put("november", "11");
		hm.put("dec", "12");
		hm.put("december", "12");
		hm.put("mon", "1");
		hm.put("monday", "1");
		hm.put("tues", "2");
		hm.put("tuesday", "2");
		hm.put("wed", "3");
		hm.put("wednesday", "3");
		hm.put("thur", "4");
		hm.put("thursday", "4");
		hm.put("fri", "5");
		hm.put("friday", "5");
		hm.put("sat", "6");
		hm.put("saturday", "6");
		hm.put("sun", "7");
		hm.put("sunday", "8");
		hm.put("one", "1");
		hm.put("two", "2");
		hm.put("three", "3");
		hm.put("four", "4");
		hm.put("five", "5");
		hm.put("six", "6");
		hm.put("seven", "7");
		hm.put("eight", "8");
		hm.put("nine", "9");
		hm.put("ten", "10");
		hm.put("eleven", "11");
		hm.put("twelve", "12");
		hm.put("thirteen", "13");
		hm.put("fourteen", "14");
		hm.put("fifteen", "15");
		hm.put("sixteen", "16");
		hm.put("seventeen", "17");
		hm.put("eighteen", "18");
		hm.put("nineteen", "19");
		hm.put("twenty", "20");
		hm.put("thirty", "30");
		hm.put("fourty", "40");
		hm.put("fifty", "50");
		hm.put("sixty", "60");
		hm.put("seventy", "70");
		hm.put("eighty", "80");
		hm.put("ninety", "90");
		hm.put("hundred", "100");
		hm.put("thousand", "1000");
		hm.put("million", "1000000");
		hm.put("billion", "1000000000");
		hm.put("an", "1");
		hm.put("a", "1");
		hm.put("this", "1");
	}

}
