/**
 * 
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.Interval;

import java.util.Calendar;
import java.util.Date;

/**
 * @author dxquang Nov 19, 2011
 */
public class TimexNormalizer {
	protected static String defaultcountry = "UNITED_STATES";
	protected static String defaultyear = "2011";
	protected static String deyear = "2011";
	protected static String demonth = "11";
	protected static String deday = "16";
	protected static String dehour = "12";
	protected static String deminute = "30";
	protected static String desecond = "00";
	protected static String dems = "00";

	public void setCountry(String country) {
		this.defaultcountry = country;
	}

	public void setTime(Date dct) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dct);
		this.defaultyear = String.valueOf(cal.get(Calendar.YEAR));
		this.deyear = String.valueOf(cal.get(Calendar.YEAR));
		this.demonth = String.valueOf(cal.get(Calendar.MONTH) + 1);
		this.deday = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		this.dehour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		this.deminute = String.valueOf(cal.get(Calendar.MINUTE));
		this.desecond = String.valueOf(cal.get(Calendar.SECOND));
		this.dems = String.valueOf(cal.get(Calendar.MILLISECOND));
	}

	public static Interval normalize(String timex, Date dct) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(dct);
		String defaultyear = String.valueOf(cal.get(Calendar.YEAR));
		String deyear = String.valueOf(cal.get(Calendar.YEAR));
		String demonth = String.valueOf(cal.get(Calendar.MONTH) + 1);
		String deday = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		String dehour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		String deminute = String.valueOf(cal.get(Calendar.MINUTE));
		String desecond = String.valueOf(cal.get(Calendar.SECOND));
		String dems = String.valueOf(cal.get(Calendar.MILLISECOND));

		String p = timex;
		try {
			p = ConvertWordToNumber.ConvertWTN(timex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		p = p.toLowerCase();
		Interval canonicalTime = phrase.canonize(p, deyear,
				demonth, deday, dehour, deminute, desecond, dems,
				defaultyear, defaultcountry);
		return canonicalTime;
	}

	public static Interval normalize(TemporalPhrase timex) {
		String p = timex.getPhrase();
		try {
			p = ConvertWordToNumber.ConvertWTN(timex.getPhrase());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//TODO: remember update timex using p
		p = p.toLowerCase();
		timex.setPhrase(p);
		Interval canonicalTime = phrase.canonize(timex, TimexNormalizer.deyear,
				TimexNormalizer.demonth, TimexNormalizer.deday,
				TimexNormalizer.dehour, TimexNormalizer.deminute,
				TimexNormalizer.desecond, TimexNormalizer.dems,
				TimexNormalizer.defaultyear, TimexNormalizer.defaultcountry);
		return canonicalTime;
	}
}
