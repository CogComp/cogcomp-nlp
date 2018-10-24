/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.edison.features.factory.LevinVerbClassFeature;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

/**
 * A concreteness scorer based on the ratings of <a
 * href="http://crr.ugent.be/archives/1330">Brysbaert et al. (2014)</a>
 * <p>
 * <p>
 * Value ranges from <b>1--5</b> where 5 is most concrete
 */
public class ConcretenessScorer {

    public static final String FILE = "Concreteness_ratings_Brysbaert_et_al_BRM.txt";
    public static HashMap<String, Double> ratingMap;

    private static void readRatingsFile() throws IOException, URISyntaxException {
        ratingMap = new HashMap<>();
        InputStream resource =
                IOUtils.lsResources(LevinVerbClassFeature.class, FILE).get(0).openStream();
        List<String> lines =
                LineIO.read(resource, Charset.defaultCharset().name(),
                        new ITransformer<String, String>() {
                            public String transform(String line) {
                                return line;
                            }
                        });
        // Remove the title line
        lines.remove(0);
        for (String line : lines) {
            String[] splits = line.split("\\t");
            // 0: Word
            // 1: Bigram
            // 2: Conc.Mean
            // 3: Conc.SD
            // 4: Unknown
            // 5: Total
            // 6: Percent_known
            // 7: SUBTLEX
            // 8: Dom_Pos
            ratingMap.put(splits[0], Double.parseDouble(splits[2]));
        }
    }

    /**
     * Returns the concreteness rating for a given word.
     *
     * @param word The word to be scored
     * @return The concreteness rating from <b>1--5</b> or <b>0</b> is not found
     * @throws java.io.FileNotFoundException
     */
    public static double getRating(String word) throws IOException, URISyntaxException {
        if (ratingMap == null)
            readRatingsFile();
        if (ratingMap.containsKey(word))
            return ratingMap.get(word);
        return 0;
    }
}
