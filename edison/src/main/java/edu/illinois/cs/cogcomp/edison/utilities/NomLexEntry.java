/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static edu.illinois.cs.cogcomp.edison.utilities.NomLexEntry.NomLexClasses.*;

/**
 * @author Vivek Srikumar
 */
public class NomLexEntry {
    public final static Set<NomLexClasses> VERBAL = new LinkedHashSet<>(Arrays.asList(NOM, NOMLIKE,
            NOMING, ABLE_NOM));

    public final static Set<NomLexClasses> ADJECTIVAL = new LinkedHashSet<>(Arrays.asList(NOMADJ,
            NOMADJLIKE));

    public final static Set<NomLexClasses> NON_VERB_ADJ = new LinkedHashSet<>(Arrays.asList(
            ABILITY, ATTRIBUTE, CRISSCROSS, ENVIRONMENT, EVENT, FIELD, GROUP, HALLMARK, ISSUE, JOB,
            PARTITIVE, RELATIONAL, SHARE, TYPE, VERSION, WORK_OF_ART));
    // this is the root of the nomlex record
    public NomLexClasses nomClass;
    public String orth;
    public String plural;
    public String verb;
    public String adj;

    /**
     * These are the possible NOMBANK classes. (See Section 5.3 in
     * "Those Other Nombank Dictionaries"
     */
    public enum NomLexClasses {
        ABILITY, ABLE_NOM, ATTRIBUTE, CRISSCROSS,

        ENVIRONMENT, EVENT, FIELD, GROUP, HALLMARK,

        ISSUE, JOB, NOM, NOMADJ, NOMADJLIKE, NOMING,

        NOMLIKE, PARTITIVE, RELATIONAL, SHARE, TYPE,

        VERSION, WORK_OF_ART, UNKNOWN_CLASS
    }

    // this is the entry referenced by :NOMTYPE. This could be a structured
    // entry. So for now, I'll ignore it.
    // public String nom_type;

}
