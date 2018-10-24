/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetHelper;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This is an extended version of the WordNetLemmaViewGenerator. If the word is a verb, then it
 * first looks up the lemma dictionary. If found, it returns the lemma. Otherwise, it checks against
 * a known list of contractions. Finally, it goes to WordNet. If the word is present in WordNet, the
 * lemma from WordNet is used. Otherwise, it defaults to the word itself (with case).
 * <p>
 * This lemmatizer lowercases words before finding their lemma. This could lead to errors. This
 * requires the file resources/verb-lemDict.txt in the classpath, which can be obtained from
 * cogcomp-common-resources.
 *
 * @author Vivek Srikumar
 * @deprecated Use {@code illinois-lemmatizer} instead
 */
public class WordNetPlusLemmaViewGenerator extends Annotator {
    public final static VerbLemmaDictionary lemmaDict;
    private final static Logger log = LoggerFactory.getLogger(WordNetPlusLemmaViewGenerator.class);
    private final static Map<String, String> contractions;

    static {
        try {
            log.info("Loading verb lemma dictionary.");
            lemmaDict = new VerbLemmaDictionary(log);
            log.info("Finished loading verb lemma dictionary");

            contractions = new HashMap<>();
            contractions.put("'d", "xmodal");
            contractions.put("'ll", "xmodal");
            contractions.put("'s", "be");
            contractions.put("'re", "be");
            contractions.put("'m", "be");
            contractions.put("'ve", "have");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private WordNetManager wn;

    public WordNetPlusLemmaViewGenerator(WordNetManager wn) {
        super(ViewNames.LEMMA, new String[] {});
        this.wn = wn;
    }


    /**
     * noop
     * 
     * @param rm configuration parameters
     */
    @Override
    public void initialize(ResourceManager rm) {}

    @Override
    public void addView(TextAnnotation ta) {
        TokenLabelView view = new TokenLabelView(getViewName(), "WordNetPlus", ta, 1.0);
        for (int i = 0; i < ta.size(); i++) {
            String word = ta.getToken(i).toLowerCase().trim();

            String pos = WordHelpers.getPOS(ta, i);
            POS wnPOS = WordNetHelper.getWNPOS(pos);
            String lemma;
            boolean posVerb = POSUtils.isPOSVerb(pos);
            boolean knownLemma = lemmaDict.contains(word);
            boolean contraction = contractions.containsKey(word);

            String replaceRE = word.replace("re-", "");
            boolean knownTrimmedLemma = word.startsWith("re-") && lemmaDict.contains(replaceRE);

            if (posVerb && knownLemma) {
                lemma = lemmaDict.get(word);
            } else if (posVerb && contraction) {
                lemma = contractions.get(word);
            } else if (knownTrimmedLemma) {
                lemma = lemmaDict.get(replaceRE);
            } else if (wnPOS != null) {
                try {
                    lemma = wn.getLemma(word, wnPOS);
                } catch (JWNLException e) {
                    lemma = ta.getToken(i);
                }
            } else if (POSUtils.isPOSClosedSet(pos))
                lemma = word;
            else
                lemma = ta.getToken(i);
            view.addTokenLabel(i, lemma, 1.0);
        }

        ta.addView(getViewName(), view);

    }

    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }

    public static class VerbLemmaDictionary {
        final Map<String, String> map;

        VerbLemmaDictionary(Logger log) throws IOException, URISyntaxException, EdisonException {

            Map<String, String> mm = new HashMap<>();

            String file = "verb-lemDict.txt";

            InputStream in =
                    IOUtils.lsResources(VerbLemmaDictionary.class, file).get(0).openStream();

            log.info("Found file {} in the classpath", file);

            Scanner scanner = new Scanner(in);

            int count = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.length() == 0)
                    continue;
                String[] parts = line.split("\\s+");

                String lemma = parts[0];
                for (String p : parts) {
                    mm.put(p, lemma);
                }
                count++;
            }

            log.info("{} verb lemmas found", count);
            map = Collections.unmodifiableMap(mm);
        }

        public boolean contains(String w) {
            return map.containsKey(w);
        }

        String get(String w) {
            return map.get(w);
        }
    }
}
