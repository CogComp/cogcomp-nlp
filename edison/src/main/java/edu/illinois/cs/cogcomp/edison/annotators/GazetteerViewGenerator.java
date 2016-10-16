/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.SentenceUtils;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Use SimpleGazetteerAnnotator instead
 * Use this class to create a gazetteer view for your text.
 *
 * Use this in combination with cogcomp-common-resources, and pass in
 * 'resources/gazetteers/gazetteers' as the directory (note the lack of a trailing slash -- this is
 * important!)
 *
 *
 * @author Vivek Srikumar
 */
@Deprecated
public class GazetteerViewGenerator extends Annotator {
    public static final GazetteerViewGenerator gazetteersInstance, cbcInstance;
    private static final Logger log = LoggerFactory.getLogger(GazetteerViewGenerator.class);
    private static final String PEOPLE_FAMOUS = "People.Famous";
    private static final String STATES = "Locations.States";
    private static final String MAN_MADE_OBJECT = "ManMadeObjects";
    private static final String ART_WORK = "ArtWork";
    private static final String FILMS = "Films";
    private static final String CLOTHES = "Clothes";
    private static final Set<String> PEOPLE;

    static {
        try {
            PEOPLE =
                    getSet("People", "People.Famous", "People.FirstNames", "People.Gender.Female",
                            "People.Gender.Male", "People.Politicians", "People.Politicians.US",
                            "People.Politicians.US.Presidents",
                            "People.Politicians.US.VicePresidents");

            gazetteersInstance =
                    new GazetteerViewGenerator("resources/gazetteers/gazetteers",
                            ViewNames.GAZETTEER + "Gazetteers");

            GazetteerViewGenerator.addGazetteerFilters(gazetteersInstance);

            cbcInstance =
                    new GazetteerViewGenerator("resources/cbcData/lists", ViewNames.GAZETTEER
                            + "CBC");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final List<int[]> lengths;
    private final List<Predicate<Pair<Constituent, SpanLabelView>>> gazetteerFilters;
    private final String viewName;
    private List<String> names;
    private List<int[]> patterns;
    private int maxLength;
    private String directory;

    private boolean gzip;

    private boolean loaded;

    private Set<String> ignore;

    public GazetteerViewGenerator(String directory, String viewName) throws Exception {
        this(directory, true, viewName);
    }

    public GazetteerViewGenerator(String directory, boolean gzip, String viewName) throws Exception {
        super(viewName, new String[] {});
        this.directory = directory;
        this.gzip = gzip;
        this.viewName = viewName;

        // this.gazetteers = new HashMap<String, List<String>>();
        this.names = new ArrayList<>();
        this.patterns = new ArrayList<>();
        this.lengths = new ArrayList<>();

        this.gazetteerFilters = new ArrayList<>();

        this.loaded = false;

        this.ignore = new LinkedHashSet<>();

    }

    @SuppressWarnings("serial")
    private static Predicate<Pair<Constituent, SpanLabelView>> getPOSFilter(
            final Set<String> types, final Set<String> p) {
        return new Predicate<Pair<Constituent, SpanLabelView>>() {

            @Override
            public Boolean transform(Pair<Constituent, SpanLabelView> input) {
                Constituent c = input.getFirst();
                TextAnnotation ta = c.getTextAnnotation();
                if (!ta.hasView(ViewNames.POS))
                    return true;

                if (types != null && !types.contains(c.getLabel()))
                    return true;

                if (c.size() == 1) {

                    int startSpan = c.getStartSpan();
                    String pos = WordHelpers.getPOS(ta, startSpan);

                    if (p.contains(pos))
                        return false;
                }

                return true;
            }
        };
    }

    @SuppressWarnings("serial")
    public static void addGazetteerFilters(GazetteerViewGenerator gazetteers) {
        // the last word of clothes should be an noun

        gazetteers.addFilter(new Predicate<Pair<Constituent, SpanLabelView>>() {
            @Override
            public Boolean transform(Pair<Constituent, SpanLabelView> input) {

                if (!input.getFirst().getLabel().equals(CLOTHES))
                    return true;

                TextAnnotation ta = input.getFirst().getTextAnnotation();
                int last = input.getFirst().getEndSpan() - 1;

                return !ta.hasView(ViewNames.POS)
                        || POSUtils.isPOSNoun(WordHelpers.getPOS(ta, last));
            }
        });

        // film names, art work names, and famous people should be an NP in the
        // parse tree.
        gazetteers.addFilter(getNPFilter(getSet(FILMS, ART_WORK, PEOPLE_FAMOUS)));

        Predicate<Pair<Constituent, SpanLabelView>> prepFilter =
                getPOSFilter(getSet(FILMS, ART_WORK), getSet("IN", "TO", "PRP", "PRP$"));

        gazetteers.addFilter(prepFilter);

        // gazetteers.addFilter(getPOSFilter(getSet(LOCATIONS), getSet("IN")));

        gazetteers.addFilter(getPOSFilter(getSet(STATES), getSet("IN", "CC")));

        // the following should be filtered:
        // 1. Temporal labels that are contained in people
        // 2. any People label that are contained in art work
        // 3. any People that are contained in man-made-objects
        // 4. Any man made objects that are contained in people

        gazetteers.addFilter(containedInSetFilter(getSet("Temporal", MAN_MADE_OBJECT), PEOPLE));

        gazetteers.addFilter(containedInSetFilter(
                PEOPLE,
                getSet(ART_WORK, MAN_MADE_OBJECT, "Organizations", "Organizations.Terrorist",
                        "Temporal")));

        // Nothing can be a verb
        gazetteers.addFilter(getPOSFilter(null, getSet("VBD", "VBG", "VB", "VBP")));
    }

    private static Set<String> getSet(String... strings) {
        return new LinkedHashSet<>(Arrays.asList(strings));
    }

    private static Predicate<Pair<Constituent, SpanLabelView>> getNPFilter(final Set<String> types) {
        @SuppressWarnings("serial")
        Predicate<Pair<Constituent, SpanLabelView>> npFilter =
                new Predicate<Pair<Constituent, SpanLabelView>>() {

                    @Override
                    public Boolean transform(Pair<Constituent, SpanLabelView> input) {

                        if (!types.contains(input.getFirst().getLabel()))
                            return true;

                        Constituent c = input.getFirst();
                        TextAnnotation ta = c.getTextAnnotation();

                        TreeView parse;
                        if (ta.hasView(ViewNames.PARSE_CHARNIAK))
                            parse = (TreeView) ta.getView(ViewNames.PARSE_CHARNIAK);
                        else if (ta.hasView(ViewNames.PARSE_BERKELEY))
                            parse = (TreeView) ta.getView(ViewNames.PARSE_BERKELEY);
                        else if (ta.hasView(ViewNames.PARSE_STANFORD))
                            parse = (TreeView) ta.getView(ViewNames.PARSE_STANFORD);
                        else
                            return true;

                        boolean foundNP = false;
                        for (Constituent parseConstituent : parse.where(Queries
                                .sameSpanAsConstituent(c))) {
                            if (ParseTreeProperties.isNonTerminalNoun(parseConstituent.getLabel())) {
                                foundNP = true;
                                break;
                            }
                        }

                        // do not include
                        return foundNP;
                    }
                };
        return npFilter;
    }

    @SuppressWarnings("serial")
    private static Predicate<Pair<Constituent, SpanLabelView>> containedInSetFilter(
            final Set<String> typesToFilter, final Set<String> otherSet) {

        assert typesToFilter != null;
        assert otherSet != null;

        return new Predicate<Pair<Constituent, SpanLabelView>>() {

            @Override
            public Boolean transform(Pair<Constituent, SpanLabelView> input) {
                Constituent candidate = input.getFirst();
                SpanLabelView view = input.getSecond();

                assert candidate != null;

                if (!typesToFilter.contains(candidate.getLabel()))
                    return true;

                for (Constituent c : view.where(Queries.containsConstituent(candidate))) {

                    if (c == candidate)
                        continue;

                    if (otherSet.contains(c.getLabel())) {

                        return false;
                    }
                }

                return true;
            }
        };
    }

    public static void main(String[] args) throws EdisonException, AnnotatorException {
        gazetteersInstance.ignoreGazetteer("Weapons.gz");
        gazetteersInstance.ignoreGazetteer("Weapons.Missile.gz");

        List<String[]> sentences =
                Arrays.asList("I live in Chicago , Illinois .".split("\\s+"),
                        "I met George Bush .".split("\\s+"));
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(sentences);

        ta.addView(gazetteersInstance);

        System.out.println(ta);

        System.out.println(ta.getView(gazetteersInstance.getViewName()));
    }

    private void lazyLoadGazetteers(String directory, boolean gzip) throws URISyntaxException,
            IOException {
        log.info("Loading all gazetteers from {}", directory);

        for (URL url : IOUtils.lsResources(GazetteerViewGenerator.class, directory)) {
            String file = IOUtils.getFileName(url.getPath());

            // ignore any dot files
            if (file.startsWith("."))
                continue;

            if (ignore.contains(file))
                continue;

            int max = -1;

            TIntArrayList list = new TIntArrayList();
            TIntArrayList lenList = new TIntArrayList();

            this.names.add(file);

            log.debug("Loading {} from {}", file, url.getPath());

            InputStream stream = url.openStream();
            if (gzip)
                stream = new GZIPInputStream(stream);

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while ((line = reader.readLine()) != null) {

                line = StringUtils.normalizeUnicodeDiacritics(line);

                line = line.replaceAll("&amp;", "&");

                line = line.replaceAll("'", " '");
                line = line.replaceAll(",", " ,");
                line = line.replaceAll(";", " ;");
                line = line.replaceAll("\\s+", " ");

                line = line.trim();

                list.add(line.hashCode());

                int len = line.split("\\s+").length;

                lenList.add(len);

                if (len > max)
                    max = len;
            }

            this.patterns.add(list.toArray());
            this.lengths.add(lenList.toArray());

            this.maxLength = Math.max(max, this.maxLength);

            reader.close();

            log.debug("Found {} elements of type {}", list.size(), file);
        }

        this.loaded = true;
        log.info("Finished loading  {} gazetteers from {}", names.size(), directory);
    }

    /**
     * Add a filter for the labels. Only constituents that pass *ALL* filters will be added to the
     * view.
     */
    public void addFilter(Predicate<Pair<Constituent, SpanLabelView>> filter) {
        this.gazetteerFilters.add(filter);
    }

    /**
     * Adds a label to the set of gazetteers to be ignored. Use this to speed up the process of
     * adding the view.
     */
    public void ignoreGazetteer(String label) {
        this.ignore.add(label);
    }

    /**
     * Returns the names of all the gazetteers known to this view generator.
     */
    public List<String> getGazetteerNames() {
        return Collections.unmodifiableList(names);

    }

    /**
     * noop. Uses own lazy initialization.
     *
     * @param rm configuration parameters
     */
    @Override
    public void initialize(ResourceManager rm) {}

    @Override
    public void addView(TextAnnotation ta) {

        if (!this.loaded) {
            synchronized (this) {
                if (!this.loaded) {
                    try {
                        lazyLoadGazetteers(directory, gzip);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        SpanLabelView view = new SpanLabelView(ViewNames.GAZETTEER, "Gazetteers", ta, 1.0, true);

        TIntObjectHashMap<ArrayList<IntPair>> allSpans = hashAllSpans(ta);

        for (int i = 0; i < names.size(); i++) {
            String label = names.get(i);
            if (!this.ignore.contains(label)) {
                addView(label, i, view, allSpans);
            }
        }

        SpanLabelView newView = new SpanLabelView(ViewNames.GAZETTEER, "Gazetteers", ta, 1.0, true);

        for (Constituent c : view) {
            Pair<Constituent, SpanLabelView> input = new Pair<>(c, view);

            boolean allow = true;
            for (Predicate<Pair<Constituent, SpanLabelView>> filter : this.gazetteerFilters) {
                if (!filter.transform(input)) {
                    allow = false;
                    log.debug("Not labeling constituent {} as {} because it failed a filter", c,
                            c.getLabel());
                    break;
                }
            }

            if (allow)
                newView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), c.getLabel(),
                        c.getConstituentScore());
        }

        ta.addView(getViewName(), newView);
    }

    @Override
    public String[] getRequiredViews() {
        return new String[0];
    }

    private TIntObjectHashMap<ArrayList<IntPair>> hashAllSpans(TextAnnotation ta) {
        TIntObjectHashMap<ArrayList<IntPair>> allSpans = new TIntObjectHashMap<>();

        for (int start = 0; start < ta.size() - 1; start++) {

            int last = Math.min(ta.size(), start + maxLength);

            StringBuilder sb = new StringBuilder();

            for (int end = start; end < last; end++) {

                String token = ta.getToken(end);

                token = token.replaceAll("``", "\"").replaceAll("''", "\"");
                token = SentenceUtils.convertFromPTBBrackets(token);

                sb.append(token).append(" ");

                int hash = sb.toString().trim().hashCode();

                if (!allSpans.containsKey(hash))
                    allSpans.put(hash, new ArrayList<IntPair>());
                List<IntPair> object = allSpans.get(hash);
                object.add(new IntPair(start, end + 1));
            }
        }

        return allSpans;
    }

    private void addView(String label, int labelId, SpanLabelView view,
            TIntObjectHashMap<ArrayList<IntPair>> allSpans) {
        log.debug("Adding gazetteer {}", label);

        List<IntPair> matches = new ArrayList<>();

        int[] pattern = this.patterns.get(labelId);
        int[] len = this.lengths.get(labelId);

        for (int i = 0; i < pattern.length; i++) {
            int hashCode = pattern[i];
            int length = len[i];

            if (allSpans.containsKey(hashCode)) {
                List<IntPair> list = allSpans.get(hashCode);

                for (IntPair pair : list) {
                    if (pair.getSecond() - pair.getFirst() == length)
                        matches.add(pair);
                }

            }
        }
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
                view.addSpanLabel(p.getFirst(), p.getSecond(), label, 1.0);
                added.add(p);
            }
        }

    }
}
