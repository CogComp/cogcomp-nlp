/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import org.cogcomp.Datastore;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordLists;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Checks for the following patterns in the input constituent:
 * <ul>
 * <li>Constituent ends with a currency symbol followed by a number</li>
 * <li>Constituent ends with a number (in words) followed by a currency symbol</li>
 * </ul>
 *
 * @author Vivek Srikumar
 */
public class CurrencyIndicator implements FeatureExtractor {

    public static final CurrencyIndicator instance;
    private static final String VIEW_NAME = "CURRENCY_INDICATOR";
    private static String[] currencies;
    private static int[] lengths;
    private static boolean loaded = false;
    private static Feature CURRENCY = DiscreteFeature.create("Y");

    static {
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File f = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            instance = new CurrencyIndicator(f.getAbsolutePath() + File.separator + "gazetteers" + File.separator + "Currency.gz", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final boolean gzip;
    private final String file;
    private URL currencyListFile;

    /**
     * Loads the list of currencies from the classpath
     */
    public CurrencyIndicator(String file, boolean gzip) throws IOException, URISyntaxException {
        this.file = file;
        this.gzip = gzip;
        this.currencyListFile = null;
    }

    private void loadCurrency(boolean gzip, boolean loadFromDatastore) throws Exception {
        InputStream stream = null;

        if (currencyListFile == null) {
            assert file != null;
            if (!loadFromDatastore) {
                currencyListFile = IOUtils.lsResources(CurrencyIndicator.class, file).get(0);
                stream = currencyListFile.openStream();
            } else {
                stream = new FileInputStream(file);
            }
        }

        if (gzip) {
            stream = new GZIPInputStream(stream);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        List<String> l = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0)
                l.add(line);
        }
        currencies = new String[l.size()];
        lengths = new int[l.size()];

        for (int i = 0; i < l.size(); i++) {
            currencies[i] = l.get(i);
            lengths[i] = currencies[i].split("\\s+").length;
        }

        loaded = true;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        try {
            if (!loaded)
                synchronized (this) {
                    // danielk: we used to load the resource from classpath.
                    // now its changed to be loaded from datastore.
                    if (!loaded)
                        loadCurrency(gzip, true);
                }
        } catch (Exception ex) {
            throw new EdisonException(ex);
        }

        TextAnnotation ta = c.getTextAnnotation();

        if (!ta.hasView(VIEW_NAME)) {
            try {
                addCurrencyView(ta);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SpanLabelView view = (SpanLabelView) ta.getView(VIEW_NAME);
        Set<Feature> features = new LinkedHashSet<>();

        for (Constituent cc : view.where(Queries.containedInConstituent(c))) {
            if (cc.getEndSpan() == c.getEndSpan()) {
                if (cc.getStartSpan() - 1 > c.getEndSpan()) {
                    // check if this is a number
                    if (WordLists.NUMBERS
                            .contains(ta.getToken(cc.getStartSpan() - 1).toLowerCase())) {
                        features.add(CURRENCY);
                        break;
                    }
                }
            } else if (WordFeatureExtractorFactory.numberNormalizer.getWordFeatures(ta,
                    cc.getEndSpan()).size() > 0) {
                features.add(CURRENCY);
                break;
            }
        }

        return features;
    }

    private void addCurrencyView(TextAnnotation ta) throws Exception {

        if (!loaded)
            synchronized (this) {
                // danielk: we used to load the resource from classpath.
                // now its changed to be loaded from datastore.
                if (!loaded)
                    loadCurrency(gzip, true);
            }

        synchronized (ta) {

            if (ta.hasView(VIEW_NAME))
                return;
            List<String> tokens = new ArrayList<>();
            Collections.addAll(tokens, ta.getTokens());

            List<IntPair> matches = new ArrayList<>();
            for (String pattern : currencies) {

                List<IntPair> list = ta.getSpansMatching(pattern);

                matches.addAll(list);
            }

            SpanLabelView view = new SpanLabelView(VIEW_NAME, "Gazetteer", ta, 1.0);
            Set<IntPair> added = new LinkedHashSet<>();
            for (IntPair p : matches) {

                // don't add nested constituents of the same type
                boolean foundContainer = false;
                for (IntPair p1 : added) {
                    if (p1 == p)
                        continue;

                    if (p1.getFirst() <= p.getFirst() && p1.getSecond() >= p.getSecond()) {
                        foundContainer = true;
                        break;
                    }
                }

                if (!foundContainer) {
                    view.addSpanLabel(p.getFirst(), p.getSecond(), "CURRENCY", 1.0);
                    added.add(p);
                }
            }

            ta.addView(VIEW_NAME, view);

        }
    }

    @Override
    public String getName() {
        return "#currency?";
    }
}
