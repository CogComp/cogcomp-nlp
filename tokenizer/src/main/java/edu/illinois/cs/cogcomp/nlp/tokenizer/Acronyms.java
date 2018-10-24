/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * this class manages a list of acronyms (with the trailing period removed). new Acronyms can be
 * added to the array known_acronyms, static initializers take care of organizing and constructing
 * an efficient search data structure from those. The {@link get} method will return an array of
 * acronyms starting with the given character. It is case sensitive.
 * 
 * @author redman
 */
public class Acronyms {
    /**
     * Given the character key, get the list of all acronyms starting with that character.
     * 
     * @param key the key is the first char of the acronym
     * @return the ist of strings that are the acronyms.
     */
    static ArrayList<String> get(char key) {
        return acronyms.get(key);
    }

    /**
     * This map keys a set of acronyms to the first letter of that acronym, for faster lookup. This
     * map is constructed from the list of strings below, so if you want to add a new acronym,
     * simply add it to the list below.
     */
    final static private HashMap<Character, ArrayList<String>> acronyms =
            new HashMap<Character, ArrayList<String>>();

    /** this is where we add acroyms. */
    final static private String[] known_acronyms = {"Ave", "ave", "AST", "CST", "Cos", "EST",
            "HST", "MST", "PST", "ADT", "CDT", "EDT", "HDT", "MDT", "PDT", "UTC", "LTD", "Ltd",
            "UTC-11", "Esq", "Jr", "Sr", "M.D", "m.o", "Ph.D", "APR", "AUG", "Adj", "Adm", "Adv",
            "Apr", "Asst", "Aug", "Bar", "Bldg", "Brig", "Bros", "Capt", "Cmdr", "Col", "Comdr",
            "Con", "Cpl", "DEC", "DR", "Dec", "Dr", "Ens", "etc", "FEB", "Feb", "Gen", "Gov",
            "Hon", "Hosp", "Insp", "Inc", "INC", "JAN", "JUL", "JUN", "Jan", "Jul", "Jun", "Lt",
            "MAR", "MM", "MR", "MRS", "MS", "MT", "Maj", "Mar", "Messrs", "Mlle", "Mme", "Mr",
            "Mrs", "Ms", "Msg", "Mt", "NO", "NOV", "No", "Nov", "OCT", "Oct", "Op", "Ord", "Pfc",
            "Ph", "Prof", "Pvt", "Rep", "Reps", "Res", "Rev", "Rt", "SEP", "SEPT", "ST", "Sen",
            "Sens", "Sep", "Sept", "Sfc", "Sgt", "Sr", "St", "Ft", "Supt", "Surg", "U.S", "al", "apr",
            "aug", "dec", "feb", "jan", "jul", "jun", "ma", "nov", "oct", "sep", "sept", "v", "vs",
            "a.m", "p.m", "A.M", "P.M", "e.g", "i.e", "Corp", "Ala", "AL", "AK", "AS", "Ariz",
            "AZ", "Ark", "AR", "Calif", "CA", "Colo", "CO", "Co", "co", "Conn", "CT", "Del", "DE",
            "D.C", "D.c", "DC", "Fla", "FL", "Ga", "GA", "GU", "HI", "ID", "Ill", "IL", "Ind",
            "IN", "IA", "Kans", "KS", "Ky", "KY", "La", "LA", "ME", "Md", "MD", "MH", "Mass", "MA",
            "Mich", "MI", "FM", "Minn", "MN", "Miss", "MS", "Mo", "MO", "Mont", "MT", "Nebr", "NE",
            "Nev", "NV", "N.H", "NH", "N.J", "NJ", "N.M", "NM", "N.Y", "NY", "N.C", "NC", "N.D",
            "ND", "MP", "OH", "Okla", "OK", "Ore", "OR", "pub", "PW", "Pa", "PA", "P.R", "PR",
            "R.I", "RI", "S.C", "SC", "S.D", "SD", "Tenn", "TN", "Tex", "TX", "UT", "Vt", "VT",
            "Va", "VA", "V.I", "VI", "Wash", "WA", "W.Va", "WV", "Wis", "WI", "Wyo", "WY", "Fr"};

    // init the abbr data structure.
    static {
        for (String t : known_acronyms) {
            Character key = t.charAt(0);
            ArrayList<String> abbrForCharacter = acronyms.get(key);
            if (abbrForCharacter == null) {
                abbrForCharacter = new ArrayList<String>();
                acronyms.put(key, abbrForCharacter);
            }
            abbrForCharacter.add(t);
        }
    }

}
