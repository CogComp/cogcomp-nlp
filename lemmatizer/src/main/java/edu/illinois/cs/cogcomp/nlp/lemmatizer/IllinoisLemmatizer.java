/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IllinoisLemmatizer extends Annotator {

    private static final String NAME = IllinoisLemmatizer.class.getCanonicalName();

    private static String verbLemmaFile;
    private static String exceptionsFile;
    private static boolean useStanford_default = false;
    private Map<String, String> verbLemmaMap;
    private Map<String, String> verbBaseMap;
    private Map<String, String> exceptionsMap;
    private WordnetLemmaReader wnLemmaReader;
    private Map<String, String> contractions;
    private Map<String, String> toStanford;
    private boolean useStanford;
    private static Logger logger = LoggerFactory.getLogger(IllinoisLemmatizer.class);

    /**
     * default configuration, lazily initialized
     */
    public IllinoisLemmatizer() {
        this(true);
    }

    /**
     * default parameters, but set whether lazily initialized or not
     * 
     * @param isLazilyInitialized if 'true', defer loading resources until getView() is called.
     */
    public IllinoisLemmatizer(boolean isLazilyInitialized) {

        super(ViewNames.LEMMA, new String[] {ViewNames.POS}, isLazilyInitialized,
                new LemmatizerConfigurator().getDefaultConfig());
    }

    /**
     * Override default config parameters with properties in rm. Is lazily initialized by default.
     * 
     * @param nonDefaultRm non-default configuration params
     */
    public IllinoisLemmatizer(ResourceManager nonDefaultRm) {
        super(ViewNames.LEMMA, new String[] {ViewNames.POS}, nonDefaultRm.getBoolean(
                AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, Configurator.TRUE),
                new LemmatizerConfigurator().getConfig(nonDefaultRm));
    }

    public static List<String> readFromClasspath(String filename) {
        List<String> lines = null;
        try {
            InputStream resource =
                    IOUtils.lsResources(IllinoisLemmatizer.class, filename).get(0).openStream();
            lines =
                    LineIO.read(resource, Charset.defaultCharset().name(),
                            new ITransformer<String, String>() {
                                public String transform(String line) {
                                    return line;
                                }
                            });
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error while trying to read " + filename + ".");
            System.exit(-1);
        }
        return lines;
    }

    // main
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

    }

    /**
     * loads resources used by lemmatizer. By default, is called by Annotator superclass with
     * ResourceManager passed in at construction time.
     */
    public void initialize(ResourceManager rm) {
        this.useStanford = rm.getBoolean(LemmatizerConfigurator.USE_STNFRD_CONVENTIONS.key);
        wnLemmaReader = new WordnetLemmaReader(rm.getString(LemmatizerConfigurator.WN_PATH.key));
        verbLemmaFile = rm.getString( LemmatizerConfigurator.VERB_LEMMA_FILE.key );
        exceptionsFile = rm.getString( LemmatizerConfigurator.EXCEPTIONS_FILE.key );
        loadVerbMap();
        loadExceptionMap();
        contractions = new HashMap<>();
        contractions.put("'d", "have");
        contractions.put("'ll", "will");
        contractions.put("'s", "'s");
        contractions.put("'re", "be");
        contractions.put("'m", "be");
        contractions.put("'ve", "have");
        // contractions.put("ca", "can");

        toStanford = new HashMap<>();

        toStanford.put("her", "she");
        toStanford.put("him", "he");
        toStanford.put("is", "be");
        toStanford.put("their", "they");
        toStanford.put("them", "they");
        toStanford.put("me", "i");
        toStanford.put("an", "a");
    }

    private void loadExceptionMap() {
        exceptionsMap = new HashMap<>();

        for (String line : readFromClasspath(exceptionsFile)) {
            String[] parts = line.split("\\s+");
            exceptionsMap.put(parts[0], parts[1]);
        }
    }

    // load verb resources
    private void loadVerbMap() {
        verbLemmaMap = new HashMap<>();
        verbBaseMap = new HashMap<>();

        for (String line : readFromClasspath(verbLemmaFile)) {
            String[] parts = line.split("\\s+");

            String lemma = parts[0];
            this.verbBaseMap.put(lemma, lemma);
            for (int i = 1; i < parts.length; i++)
                verbLemmaMap.put(parts[i], lemma);
        }
    }

    /**
     * get a lemma for the token at index tokIndex in TextAnnotation ta.
     *
     * @param ta TextAnnotation to query for lemma; MUST have POS view.
     * @param tokIndex token index for word to lemmatize
     * @return a String representing a lemma with the POS found for the corresponding word
     */

    public String getLemma(TextAnnotation ta, int tokIndex) {
        if (tokIndex >= ta.getTokens().length) {
            String msg =
                    "ERROR: " + NAME + ".getLemma(): index '" + tokIndex
                            + "' is out of range of textAnnotation, " + "which has '"
                            + ta.getTokens().length + "' tokens.";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String word = ta.getToken(tokIndex).toLowerCase().trim();
        String pos = ta.getView(ViewNames.POS).getLabelsCoveringToken(tokIndex).get(0);
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
    public String getSingleLemma(TextAnnotation ta, int tokIndex) {
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

        if (!isInitialized())
            super.doInitialize();
        word = word.toLowerCase();

        // look at file
        boolean posVerb = POSUtils.isPOSVerb(pos) || pos.startsWith("VB");
        boolean knownLemma = verbLemmaMap.containsKey(word);
        boolean contraction = contractions.containsKey(word);
        boolean exception = exceptionsMap.containsKey(word);

        // first try exceptions
        if (exception) {
            return exceptionsMap.get(word);
        }

        // narrow re case
        // all res are verbs
        String replaceRE = word.replace("re-", "");
        boolean knownTrimmedLemma = word.startsWith("re-") && verbLemmaMap.containsKey(replaceRE);

        String lemma;

        if (word.indexOf('@') >= 0) {
            return word;
        }

        if (pos.startsWith("V") && (word.equals("'s") || word.equals("’s"))) {
            return "be";
        }

        if (useStanford && toStanford.containsKey(word)) {
            return toStanford.get(word);
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
            return word;
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
            lemma = MorphaStemmer.stem(word);
            return lemma;
        }
        return word;
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

}
