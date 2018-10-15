/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import java.util.*;

/**
 * This class contains a hash map between a date string and a number
 */
public class DateMapping {

	public static DateMapping instance;

	// Create a hash map
	HashMap<String, String> hm = new HashMap<String, String>();
	HashMap<String, String> hm_month = new HashMap<String, String>();
	HashMap<String, String> hm_dayOfWeek = new HashMap<String, String>();
	HashSet<String> hm_dayOfMonth = new HashSet<>();

	public static DateMapping getInstance(){
		if(instance==null)
			instance = new DateMapping();
		return instance;
	}
	DateMapping() {
		hm_month.put("jan", "1");
		hm_month.put("jan.", "1");
		hm_month.put("january", "1");
		hm_month.put("feb", "2");
		hm_month.put("feb.", "2");
		hm_month.put("february", "2");
		hm_month.put("mar", "3");
		hm_month.put("mar.", "3");
		hm_month.put("march", "3");
		hm_month.put("apr", "4");
		hm_month.put("apr.", "4");
		hm_month.put("april", "4");
		hm_month.put("may", "5");
		hm_month.put("jun", "6");
		hm_month.put("jun.", "6");
		hm_month.put("june", "6");
		hm_month.put("jul", "7");
		hm_month.put("jul.", "7");
		hm_month.put("july", "7");
		hm_month.put("aug", "8");
		hm_month.put("aug.", "8");
		hm_month.put("august", "8");
		hm_month.put("sep", "9");
		hm_month.put("sep.", "9");
		hm_month.put("sept", "9");
		hm_month.put("sept.", "9");
		hm_month.put("september", "9");
		hm_month.put("oct", "10");
		hm_month.put("oct.", "10");
		hm_month.put("october", "10");
		hm_month.put("nov", "11");
		hm_month.put("nov.", "11");
		hm_month.put("november", "11");
		hm_month.put("dec", "12");
		hm_month.put("dec.", "12");
		hm_month.put("december", "12");
		hm_dayOfWeek.put("mon", "1");
		hm_dayOfWeek.put("monday", "1");
		hm_dayOfWeek.put("tues", "2");
		hm_dayOfWeek.put("tuesday", "2");
		hm_dayOfWeek.put("wed", "3");
		hm_dayOfWeek.put("wednesday", "3");
		hm_dayOfWeek.put("thur", "4");
		hm_dayOfWeek.put("thursday", "4");
		hm_dayOfWeek.put("fri", "5");
		hm_dayOfWeek.put("friday", "5");
		hm_dayOfWeek.put("sat", "6");
		hm_dayOfWeek.put("saturday", "6");
		hm_dayOfWeek.put("sun", "7");
		hm_dayOfWeek.put("sunday", "7");
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

		hm.putAll(hm_month);
		hm.putAll(hm_dayOfWeek);

		hm_dayOfMonth.add("1st");
		hm_dayOfMonth.add("2nd");
		hm_dayOfMonth.add("3rd");
		hm_dayOfMonth.add("4th");
		hm_dayOfMonth.add("5th");
		hm_dayOfMonth.add("6th");
		hm_dayOfMonth.add("7th");
		hm_dayOfMonth.add("8th");
		hm_dayOfMonth.add("9th");
		hm_dayOfMonth.add("10th");
		hm_dayOfMonth.add("11th");
		hm_dayOfMonth.add("12th");
		hm_dayOfMonth.add("13th");
		hm_dayOfMonth.add("14th");
		hm_dayOfMonth.add("15th");
		hm_dayOfMonth.add("16th");
		hm_dayOfMonth.add("17th");
		hm_dayOfMonth.add("18th");
		hm_dayOfMonth.add("19th");
		hm_dayOfMonth.add("20th");
		hm_dayOfMonth.add("21st");
		hm_dayOfMonth.add("22nd");
		hm_dayOfMonth.add("23rd");
		hm_dayOfMonth.add("24th");
		hm_dayOfMonth.add("25th");
		hm_dayOfMonth.add("26th");
		hm_dayOfMonth.add("27th");
		hm_dayOfMonth.add("28th");
		hm_dayOfMonth.add("29th");
		hm_dayOfMonth.add("30th");
		hm_dayOfMonth.add("31st");
	}

	public HashMap<String, String> getHm() {
		return hm;
	}

	public HashMap<String, String> getHm_month() {
		return hm_month;
	}

	public HashMap<String, String> getHm_dayOfWeek() {
		return hm_dayOfWeek;
	}

	public HashSet<String> getHm_dayOfMonth() {
		return hm_dayOfMonth;
	}
}
