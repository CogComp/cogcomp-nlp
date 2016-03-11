package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class IllinoisLemmatizer extends Annotator {
    private static final String NAME = IllinoisLemmatizer.class.getCanonicalName();
    private static final String verbLemmaFile = "verb-lemDict.txt";
    private static Map<String, String> verbLemmaMap;
    private static Map<String, String> verbBaseMap;
    private final static SnowballStemmer stemmer = new englishStemmer();
    private static Map<String, String> contractions;
    private static MorphaStemmer morphaStemmer = new MorphaStemmer();
    private WordnetLemmaReader wnLemmaReader;

    public IllinoisLemmatizer() {
        super(ViewNames.LEMMA, new String[] {ViewNames.POS});
        initialize(new LemmatizerConfigurator().getDefaultConfig());
    }

    public IllinoisLemmatizer(ResourceManager rm) {
        super(ViewNames.LEMMA, new String[] {ViewNames.POS});
        initialize(new LemmatizerConfigurator().getConfig(rm));
    }

    public void initialize(ResourceManager rm) {
        wnLemmaReader = new WordnetLemmaReader(rm.getString(LemmatizerConfigurator.WN_PATH.key));
        verbLemmaMap = loadMap();

        contractions = new HashMap<>();
        contractions.put("’d", "have");
        contractions.put("’ll", "will");
        contractions.put("’s", "’s");
        contractions.put("’re", "be");
        contractions.put("’m", "be");
        contractions.put("’ve", "have");
        contractions.put("'d", "have");
        contractions.put("'ll", "will");
        contractions.put("'s", "'s");
        contractions.put("'re", "be");
        contractions.put("'m", "be");
        contractions.put("'ve", "have");
        contractions.put("ca", "can");
    }

    // load verb resources
    private Map<String, String> loadMap() {
        Map<String, String> mm = new HashMap<>();
        verbBaseMap = new HashMap<>();
        InputStream in = ClassLoader.getSystemResourceAsStream(verbLemmaFile);

        Scanner scanner = new Scanner(in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.length() == 0)
                continue;
            String[] parts = line.split("\\s+");

            String lemma = parts[0];
            verbBaseMap.put(lemma, lemma);
            for (int i = 1; i < parts.length; i++)
                mm.put(parts[i], lemma);
        }
        scanner.close();

        return mm;
    }

    /**
     * create a Lemma view in the TextAnnotation argument, and return a reference to that View.
     */
    public View createLemmaView(TextAnnotation inputTa) throws IOException {
        String[] toks = inputTa.getTokens();
        TokenLabelView lemmaView = new TokenLabelView(ViewNames.LEMMA, NAME, inputTa, 1.0);

        for (int i = 0; i < toks.length; ++i) {
            String lemma = getLemma(inputTa, i);
            Constituent lemmaConstituent =
                    new Constituent(lemma, ViewNames.LEMMA, inputTa, i, i + 1);
            lemmaView.addConstituent(lemmaConstituent);
        }

        inputTa.addView(ViewNames.LEMMA, lemmaView);

        return lemmaView;
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException {
        View v = null;

        try {
            v = this.createLemmaView(textAnnotation);
        } catch (IOException e) {
            e.printStackTrace();
            String msg =
                    NAME + ".getView(): caught IOException trying to create view: "
                            + e.getMessage();
            throw new AnnotatorException(msg);
        }

        textAnnotation.addView(getViewName(), v);
    }

    /**
     * get a lemma for the token at index tokIndex in TextAnnotation ta.
     *
     * @param ta TextAnnotation to query for lemma; MUST have POS view.
     * @param tokIndex token index for word to lemmatize
     * @return a String representing a lemma with the POS found for the corresponding word
     */
    public String getLemma(TextAnnotation ta, int tokIndex) throws IOException {
        if (tokIndex >= ta.getTokens().length) {
            String msg =
                    "ERROR: " + NAME + ".getLemma(): index '" + tokIndex
                            + "' is out of range of textAnnotation, " + "which has '"
                            + ta.getTokens().length + "' tokens.";
            System.err.println(msg);
            throw new IllegalArgumentException(msg);
        }

        String word = ta.getToken(tokIndex).toLowerCase().trim();
        String pos = WordHelpers.getPOS(ta, tokIndex);
        return getLemma(word, pos);
    }

    /**
     * included for backward compatibility: wraps getLemma().
     *
     * @param ta TextAnnotation to query. MUST have POS view.
     * @param tokIndex index of word to lemmatize.
     * @return a String representing the lemma of the queried word.
     */

    @Deprecated
    public String getSingleLemma(TextAnnotation ta, int tokIndex) throws IOException {
        return getLemma(ta, tokIndex);
    }

    /**
     * gets the lemma (root form) corresponding to the specified word having the specified part of
     * speech.
     *
     * @param word Word to lemmatize
     * @param pos Part-of-speech of word
     * @return String representing lemma (root form) of word
     */
    public String getLemma(String word, String pos) {
        boolean posVerb = POSUtils.isPOSVerb(pos) || pos.startsWith("VB");
        boolean knownLemma = verbLemmaMap.containsKey(word);
        boolean contraction = contractions.containsKey(word);

        String replaceRE = word.replace("re-", "");
        boolean knownTrimmedLemma = word.startsWith("re-") && verbLemmaMap.containsKey(replaceRE);

        String lemma = null;

        if (word.indexOf('@') >= 0) {
            return word;
        }

        if (pos.startsWith("V") && (word.equals("'s") || word.equals("’s"))) {
            return "be";
        }

        if (contraction) {
            lemma = contractions.get(word);
            return lemma;
        }

        if (pos.equals("NNP") || pos.equals("NNPS")) {
            return word.toLowerCase();
        }

        if (pos.startsWith("JJ") && word.endsWith("ed"))
            return word;

        if (pos.equals("JJR") || pos.equals("JJS") || pos.equals("RBR") || pos.equals("RBS")
                || pos.equals("RB")) {
            return word.toLowerCase();
        }

        if (posVerb && knownLemma) {
            if (pos.equals("VB"))
                lemma = verbBaseMap.get(word);
            else
                lemma = verbLemmaMap.get(word);

            if (lemma != null) {
                if (lemma.equals("xmodal"))
                    return word;
                return lemma;
            }
        } else if (knownTrimmedLemma) {
            lemma = verbLemmaMap.get(replaceRE);
            if (lemma.equals("xmodal"))
                return word;
            return lemma;
        }

        if (pos.startsWith("N") || pos.startsWith("J") || pos.startsWith("R")
                || pos.startsWith("V")) {
            lemma = wnLemmaReader.getLemma(word, pos);

            if (lemma != null)
                return lemma;
            if (word.endsWith("men")) {
                lemma = word.substring(0, word.length() - 3) + "man";
                return lemma;
            }
        } else {
            // function word
            return word;
        }

        if (word.endsWith("s") || pos.endsWith("S")) {
            lemma = morphaStemmer.stem(word);
            return lemma;
        }
        return word;

    }

    public static void main(String[] args) {
        IllinoisLemmatizer lem = new IllinoisLemmatizer();

        System.out.println("Getting lemma for 'media': ");
        String lemma = lem.getLemma("media", "NNS");
        System.out.println(lemma);

        System.out.println("Getting lemma for 'men': ");
        lemma = lem.getLemma("men", "NNS");
        System.out.println(lemma);

        System.out.println("Getting lemmas for 'retakes': ");
        lemma = lem.getLemma("retakes", "VBZ");
        System.out.println(lemma);

        System.out.println("Getting lemmas for 'putting': ");
        lemma = lem.getLemma("putting", "VBG");
        System.out.println(lemma);

        System.out.println("Getting lemmas for 'me': ");
        lemma = lem.getLemma("me", "PRP");
        System.out.println(lemma);
    }
}
