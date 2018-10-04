/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class WordNetHelper {

    public static String wordNetPropertiesFile = null;
    private static Logger log = LoggerFactory.getLogger(WordNetHelper.class);

    // public static String wordNetPropertiesFile = "file_properties.xml";
    private static WordNetManager wnManager = null;

    private static synchronized void initialize() {
        if (wnManager != null)
            return;

        try {
            wnManager = WordNetManager.getInstance();
        } catch (Exception e) {
            log.error("Error loading WordNet using properties file: " + wordNetPropertiesFile, e);
        }

    }

    public static String getLemma(String word, POS pos) throws JWNLException {
        initialize();
        return wnManager.getLemma(word, pos);
    }

    public static String getLemma(String word, String pos) throws JWNLException {
        return getLemma(word, getWNPOS(pos));
    }

    public static String[] getSynset(String word, POS pos) throws JWNLException {
        initialize();

        List<String> syns = wnManager.getSynonyms(word, pos, true);

        return syns.toArray(new String[syns.size()]);
    }

    public static String[] getSynset(String word, String pos) throws JWNLException {
        return getSynset(word, getWNPOS(pos));
    }

    public static String getMorph(String word, POS pos) throws JWNLException {
        initialize();

        ArrayList<String> l = wnManager.getMorphs(pos, word);

        if (l.size() > 1)
            return wnManager.getMorphs(pos, word).get(0);
        else
            return "<NULL-MORPH>";
    }

    public static String getMorph(String word, String pos) throws JWNLException {
        return getMorph(word, getWNPOS(pos));
    }

    /**
     * Converts a PennTreebank style POS tag to a JWNL POS object. If the POS is not a noun, verb,
     * adjective or an adverb, then it returns null.
     */
    public static net.didion.jwnl.data.POS getWNPOS(String pos) {
        if (pos.startsWith("NN"))
            return net.didion.jwnl.data.POS.NOUN;
        else if (pos.startsWith("V"))
            return net.didion.jwnl.data.POS.VERB;
        else if (pos.startsWith("JJ"))
            return net.didion.jwnl.data.POS.ADJECTIVE;
        else if (pos.startsWith("RB"))
            return net.didion.jwnl.data.POS.ADVERB;
        else {
            return null;
        }
    }
}
