/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant;

import java.util.Calendar;

import org.junit.Test;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.quant.standardize.Date;
import edu.illinois.cs.cogcomp.quant.standardize.DateRange;
import edu.illinois.cs.cogcomp.quant.standardize.Normalizer;

import static org.junit.Assert.assertTrue;

public class QuantTest {

    @Test
    public void testQuantifierView() throws Exception {
        Quantifier quantifier = new Quantifier();
        quantifier.doInitialize();

        // sentence 1
        TextAnnotation ta = Quantifier.taBuilder.createTextAnnotation("It was May 2008.");
        quantifier.addView(ta);
        assertTrue(ta.hasView(ViewNames.QUANTITIES));
        assertTrue(isEqual("[= Date(05/XX/2008)]", ta.getView(ViewNames.QUANTITIES)
                .getConstituents().get(0).getLabel()));

        // sentence 2
        ta =
                Quantifier.taBuilder
                        .createTextAnnotation("This month is the 20th of the year. Everything is above 20$.");
        quantifier.addView(ta);
        assertTrue(ta.hasView(ViewNames.QUANTITIES));
        assertTrue(isEqual("[= 1.0 year]", ta.getView(ViewNames.QUANTITIES).getConstituents()
                .get(0).getLabel()));
        assertTrue(isEqual("[= 20.0 US$]", ta.getView(ViewNames.QUANTITIES).getConstituents()
                .get(1).getLabel()));

        // a long paragraph
        String paragraph =
                "Super Bowl 50 was an American football game to determine the champion of the National "
                        + "Football League (NFL) for the 2015 season. The American Football Conference (AFC) champion Denver "
                        + "Broncos defeated the National Football Conference (NFC) champion Carolina Panthers 24–10 to earn "
                        + "their third Super Bowl title. The game was played on February 7, 2016, at Levi's Stadium in "
                        + "the San Francisco Bay Area at Santa Clara, California. As this was the 50th Super Bowl, the league "
                        + "emphasized the \"golden anniversary\" with various gold-themed initiatives, as well as temporarily "
                        + "suspending the tradition of naming each Super Bowl game with Roman numerals (under which the game "
                        + "would have been known as \"Super Bowl L\"), so that the logo could prominently feature the Arabic "
                        + "numerals 50.";

        ta = Quantifier.taBuilder.createTextAnnotation(paragraph);
        quantifier.addView(ta);
        assertTrue(ta.hasView(ViewNames.QUANTITIES));
        assertTrue(isEqual("[= 2015.0  season  ]", ta.getView(ViewNames.QUANTITIES)
                .getConstituents().get(0).getLabel()));
        assertTrue(isEqual("[= 24.0  – 10  ]", ta.getView(ViewNames.QUANTITIES).getConstituents()
                .get(1).getLabel()));
        assertTrue(isEqual("[= Date(02/07/XXXX)]", ta.getView(ViewNames.QUANTITIES)
                .getConstituents().get(2).getLabel()));
        assertTrue(isEqual("[daterange[= Date(01/01/2016)][= Date(12/31/2016)]]",
                ta.getView(ViewNames.QUANTITIES).getConstituents().get(3).getLabel()));
        assertTrue(isEqual("[= 50.0   ]", ta.getView(ViewNames.QUANTITIES).getConstituents().get(4)
                .getLabel()));

        // another long paragraph
        paragraph =
                "The annual NFL Experience was held at the Moscone Center in San Francisco. In addition, "
                        + "\"Super Bowl City\" opened on January 30 at Justin Herman Plaza on The Embarcadero, featuring games "
                        + "and activities that will highlight the Bay Area's technology, culinary creations, and cultural diversity. "
                        + "More than 1 million people are expected to attend the festivities in San Francisco during Super Bowl Week. San Francisco mayor Ed Lee said of the highly visible homeless presence in this area \"they are going to have to leave\". San Francisco city supervisor Jane Kim unsuccessfully lobbied for the NFL to reimburse San Francisco for city services in the amount of $5 million.";
        ta = Quantifier.taBuilder.createTextAnnotation(paragraph);
        quantifier.addView(ta);
        assertTrue(ta.hasView(ViewNames.QUANTITIES));
        // TODO: make sure the extracted quantities are correct.
    }

    @Test
    public void testDateNormalization() {
        Normalizer normalizer = new Normalizer();
        assertTrue(isEqual("[= Date(03/11/1992)]", normalizer.parse("march 11th 1992 ", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(12/10/1992)]", normalizer.parse("12-10-1992 ", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(03/18/1986)]", normalizer.parse("1986.03.18", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(03/18/1986)]", normalizer.parse("03.18.1986", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(03/18/1986)]", normalizer.parse("3.18.86", "DATE").toString()));
        assertTrue(isEqual("[= Date(05/XX/2008)]", normalizer.parse("may 2008", "DATE").toString()));
        assertTrue(isEqual("[= Date(05/XX/2008)]", normalizer.parse("MAY 2008", "DATE").toString()));
        assertTrue(isEqual("[= Date(03/12/XXXX)]", normalizer.parse("march 12", "DATE").toString()));
        assertTrue(isEqual("[= Date(05/11/XXXX)]", normalizer.parse("11 may", "DATE").toString()));
        assertTrue(isEqual("[= Date(01/07/XXXX)]", normalizer.parse("7 of January", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(01/07/XXXX)]", normalizer.parse("7th of January", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(01/07/XXXX)]", normalizer.parse("7th January", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(03/18/2008)]", normalizer.parse("March 18, 2008", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/08/2004)]", normalizer.parse("July 8th, 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/08/2004)]", normalizer.parse("07/08/04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/08/2004)]", normalizer.parse("2004.07.08", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/08/2004)]", normalizer.parse("07/8/2004", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/08/2004)]", normalizer.parse("2004-07-08", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("July 1, 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("July 1,2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("Jul 1, 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("Jul 1,2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("July 01, 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("July 01,2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("Jul 01, 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("Jul 01,2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("July 01, 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("July 01,04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("Jul 01, 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("Jul 01,04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("July 1st 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/02/2004)]", normalizer.parse("July 2nd 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/03/2004)]", normalizer.parse("July 3rd 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/04/2004)]", normalizer.parse("July 4th 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("July 15th 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/21/2004)]", normalizer.parse("July 21st 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/22/2004)]", normalizer.parse("July 22nd 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/23/2004)]", normalizer.parse("July 23rd 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/31/2004)]", normalizer.parse("July 31st 2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("Jul 1st 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/02/2004)]", normalizer.parse("Jul 2nd 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/03/2004)]", normalizer.parse("Jul 3rd 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/04/2004)]", normalizer.parse("Jul 4th 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("Jul 15th 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/21/2004)]", normalizer.parse("Jul 21st 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/22/2004)]", normalizer.parse("Jul 22nd 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/23/2004)]", normalizer.parse("Jul 23rd 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/31/2004)]", normalizer.parse("Jul 31st 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("7/1/2004", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("07/01/2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("07/01/04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("7/1/04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("7/15/2004", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("07/15/2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("07/15/04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("7/15/04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("7-1-2004", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("07-01-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("07-01-04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("7 - 1 - 04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("7-15-2004", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("07-15-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("07-15-04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("7-15-04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("7.15.2004", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("07.15.2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("07.15.04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("7.15.04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("2004-07-15", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("7.1.2004", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("07.01.2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("07.01.04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("7.1.04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("2004-07-01", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("2004/7/1", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("2004/07/01", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("2004/7/15", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("2004/07/15", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("1-Jul-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("01-Jul-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("01-Jul-04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("1-July-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("01-July-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("01-July-04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(01/15/2004)]", normalizer.parse("15-Jan-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("15-July-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("15-July-04", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("jul-1-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("jul-1-04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("jul-01-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/01/2004)]", normalizer.parse("jul-01-04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("jul-15-2004", "DATE")
                .toString()));
        assertTrue(isEqual("[= Date(07/15/2004)]", normalizer.parse("jul-15-04", "DATE").toString()));
        assertTrue(isEqual("[= Date(07/08/2004)]", normalizer.parse("jul-08-04", "DATE").toString()));
        assertTrue(isEqual(
                new DateRange(
                        Date.getRelativeDate(
                                "year",
                                -1,
                                new Date(Date.presentDate.get(Calendar.YEAR), Date.presentDate
                                        .get(Calendar.MONTH), Date.presentDate
                                        .get(Calendar.DAY_OF_MONTH))), new Date(
                                Date.presentDate.get(Calendar.YEAR),
                                Date.presentDate.get(Calendar.MONTH),
                                Date.presentDate.get(Calendar.DAY_OF_MONTH))).toString(),
                normalizer.parse("Last year", "DATE").toString()));
        assertTrue(isEqual(
                new DateRange(
                        Date.getRelativeDate(
                                "year",
                                -1,
                                new Date(Date.presentDate.get(Calendar.YEAR), Date.presentDate
                                        .get(Calendar.MONTH), Date.presentDate
                                        .get(Calendar.DAY_OF_MONTH))), new Date(
                                Date.presentDate.get(Calendar.YEAR),
                                Date.presentDate.get(Calendar.MONTH),
                                Date.presentDate.get(Calendar.DAY_OF_MONTH))).toString(),
                normalizer.parse("last year", "DATE").toString()));
    }

    @Test
    public void testDateRangeNormalization() {
        Normalizer normalizer = new Normalizer();
        assertTrue(isEqual("[daterange[= Date(01/01/1982)][= Date(12/31/1982)]]",
                normalizer.parse("1982", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1980)][= Date(12/31/1989)]]",
                normalizer.parse("the 80s", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1980)][= Date(12/31/1989)]]",
                normalizer.parse("the '80s", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1980)][= Date(12/31/1989)]]",
                normalizer.parse("the 1980s", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/2001)][= Date(12/31/2100)]]",
                normalizer.parse("the 21st century", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1600)][= Date(12/31/1699)]]",
                normalizer.parse("1600s", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1920)][= Date(12/31/1929)]]",
                normalizer.parse("1920s", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1901)][= Date(12/31/2000)]]",
                normalizer.parse("20th cent", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/2001)][= Date(12/31/2100)]]",
                normalizer.parse("21st cent", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1965)][= Date(12/31/1975)]]",
                normalizer.parse("from 1965-1975", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1861)][= Date(12/31/1878)]]",
                normalizer.parse("1861-78", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1900)][= Date(12/31/1946)]]",
                normalizer.parse("from 1900-1946", "DATE").toString()));
        assertTrue(isEqual("[daterange[= Date(01/01/1900)][= Date(12/31/1946)]]",
                normalizer.parse("1900-46", "DATE").toString()));
    }

    @Test
    public void testRatioNormalization() {
        Normalizer normalizer = new Normalizer();
        assertTrue(isEqual("[ratio[= 5.0][= 6.0]]",
                normalizer.parse("Five of the six defendants", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 1034.0][~ 1400.0]]",
                normalizer.parse("1,034 of around 1,400 passengers", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 1.0][= 4.0]]",
                normalizer.parse("1 out of every four US bridges", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 1.0][= 119.0]]",
                normalizer.parse("one of 119 universities", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 1.0][= 119.0]]",
                normalizer.parse("one of the 119 universities", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 0.2][= 1.0]]",
                normalizer.parse("one-fifth of the company's revenues", "RATIO").toString()));
        assertTrue(isEqual("[ratio[~ 24.0][= 70.0]]",
                normalizer.parse("About two dozen of the 70 national parks", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 0.778][= 64000.0]]",
                normalizer.parse("77.8% of the 64,000 students", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 0.9][= 1.0]]", normalizer.parse("90% of dentists", "RATIO")
                .toString()));
        assertTrue(isEqual("[ratio[= 9.0][= 10.0]]",
                normalizer.parse("9 out of 10 dentists", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 1.0][= 10.0]]",
                normalizer.parse("100 percent of the 10 people", "RATIO").toString()));
        assertTrue(isEqual("[ratio[= 11.0][= 4.0]]", normalizer.parse("11 to 4", "RATIO")
                .toString()));
    }

    @Test
    public void testRangeNormalization() {
        Normalizer normalizer = new Normalizer();
        assertTrue(isEqual("[range[= 1000.0][= 9999.0]]",
                normalizer.parse("Thousands of blank British passports", "RANGE").toString()));
        assertTrue(isEqual("[range[= 500.0][= 600.0]]",
                normalizer.parse("between $500.00 and $600.00", "RANGE").toString()));
        assertTrue(isEqual("[range[= 500.0][= 600.0]]",
                normalizer.parse("between 500.00 and 600.00", "RANGE").toString()));
        assertTrue(isEqual("[range[= 1000.0][= 9999.0]]", normalizer.parse("thousands", "RANGE")
                .toString()));
    }

    @Test
    public void testQuantityNormalization() {
        Normalizer normalizer = new Normalizer();
        assertTrue(isEqual("[= 1250000.0]",
                normalizer.parse("one and a quarter million dollars", "NUMBER").toString()));
        assertTrue(isEqual("[= 48.0]", normalizer.parse("the 48-year-old motorist", "NUMBER")
                .toString()));
        assertTrue(isEqual("[= 100.0]", normalizer.parse("100 yrs old", "NUMBER").toString()));
        assertTrue(isEqual("[= 100.0]", normalizer.parse("100 years-old", "NUMBER").toString()));
        assertTrue(isEqual("[= 100.0]", normalizer.parse("100 dlrs", "NUMBER").toString()));
        assertTrue(isEqual("[> 100.0]", normalizer.parse("at least 100 dollars", "NUMBER")
                .toString()));
        assertTrue(isEqual("[~ 100.0]", normalizer.parse("close to 100 dollars", "NUMBER")
                .toString()));
        assertTrue(isEqual("[~ 100.0]", normalizer.parse("nearly 100 dollars", "NUMBER").toString()));
        assertTrue(isEqual("[~ 16.0]", normalizer.parse("approx $16 a barrel", "NUMBER").toString()));
        assertTrue(isEqual("[= 1200000.0]", normalizer.parse("1.2e6 US$", "NUMBER").toString()));
        assertTrue(isEqual("[= 1200000.0]", normalizer.parse("$1.2 million", "NUMBER").toString()));
        assertTrue(isEqual("[= 1200000.0]", normalizer.parse("$ 1.2 million", "NUMBER").toString()));
        assertTrue(isEqual("[>= 100.0]", normalizer.parse("100 or more", "NUMBER").toString()));
        assertTrue(isEqual("[= 1200000.0]", normalizer.parse("1.2 million US dollars", "NUMBER")
                .toString()));
        assertTrue(isEqual("[= 500000.0]", normalizer.parse("500,000 dollars", "NUMBER").toString()));
        assertTrue(isEqual("[> 600000.0]", normalizer.parse(
                "more than 600,000 cars, buses, trucks, sport utilities and "
                        + "other vehicles in a year", "NUMBER").toString()));
        assertTrue(isEqual("[~ 500.0]", normalizer.parse("approximately 500 times a day", "NUMBER")
                .toString()));
        assertTrue(isEqual("[= 3.9]", normalizer.parse("3.9 per cent a year", "NUMBER").toString()));
        assertTrue(isEqual("[= 7.0]", normalizer.parse("seven", "NUMBER").toString()));
        assertTrue(isEqual("[= 3.0]", normalizer.parse("3", "NUMBER").toString()));
        assertTrue(isEqual("[= 1340.0]",
                normalizer.parse("One thousand three hundred and forty dollars", "NUMBER")
                        .toString()));
    }

    boolean isEqual(String a, String b) {
        String arr[] = a.split("]");
        for (String item : arr) {
            if (!b.contains(item)) {
                return false;
            }
        }
        return true;
    }


}
