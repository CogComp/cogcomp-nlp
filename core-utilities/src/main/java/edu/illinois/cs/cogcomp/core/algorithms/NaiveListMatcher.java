/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.utilities.AvoidUsing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a naive list searcher, only for debugging purposes. For any application, use
 * {@link BoyerMooreHorspoolMatch}. If you are using this, then you are doing something wrong.
 *
 * @author Vivek Srikumar
 */
@AvoidUsing(reason = "", alternative = "BoyerMooreHorspoolMatch")
public class NaiveListMatcher<T> extends ListMatch<T> {
    private static Logger logger = LoggerFactory.getLogger(NaiveListMatcher.class);

    public NaiveListMatcher(List<T> pattern) {
        super(pattern);
    }

    public List<Integer> matches(List<T> text) {
        logger.info(text.toString());
        List<Integer> results = new ArrayList<>();

        for (int textId = 0; textId < text.size() - pattern.size() + 1; textId++) {
            logger.info("Searching at " + text.subList(textId, text.size()));

            boolean found = true;
            for (int patternId = 0; patternId < pattern.size(); patternId++) {
                if (!text.get(textId + patternId).equals(pattern.get(patternId))) {
                    logger.info("...Not found");
                    found = false;
                    break;
                }
            }
            if (found) {
                logger.info("...Found");
                results.add(textId);
            }
        }
        return results;
    }

}
