/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.core.algorithms.Mappers;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * FeatureManifest represents a parsed manifest file (.fex format).
 */
public class FeatureManifest {
    private final static Logger log = LoggerFactory.getLogger(FeatureManifest.class);
    private ManifestParser parser;
    private boolean compressedName;

    /**
     * Load the file.
     * 
     * @param path path to .fex file
     * @throws Exception
     */
    public FeatureManifest(String path) throws Exception {
        this(IOUtils.lsResources(FeatureManifest.class, path).get(0).openStream());
    }

    /**
     * Alternate constructor for {@link InputStream}
     * 
     * @param file
     * @throws Exception
     */
    public FeatureManifest(InputStream file) throws Exception {

        parser = new ManifestParser(file);
        compressedName = false;

        log.debug("Features: \n{}", getIncludedFeatures());
    }

    public static void setFeatureExtractor(String name, FeatureExtractor fex) {
        KnownFexes.fexes.put(name, fex);
    }

    public static void setTransformer(String name, FeatureInputTransformer t) {
        KnownTransformers.transformers.put(name, t);
    }

    public static Set<String> getKnownFeatureExtractors() {

        Set<String> f = new LinkedHashSet<>();
        f.addAll(KnownFexes.getKnownFeatureExtractors());
        f.addAll(WordNetClasses.getKnownFeatureExtractors());
        f.addAll(ParameterizedFeatureExtractors.getKnownFeatureExtractors());

        f.add("bigram");
        f.add("trigram");

        return f;
    }

    public void setVariable(String key, String value) {
        parser.setVariable(key, value);
    }

    /**
     * This parses the body of a define statement: (define name body).
     * 
     * @param tree
     * @param cf used for memoization, can be empty when passed in
     * @return
     * @throws EdisonException
     */
    private FeatureExtractor createFex(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {
        String label = tree.getLabel();

        if (tree.isLeaf()) {
            if (label.startsWith("wn"))
                return getWordNetFeatureExtractor(Collections.singletonList(label), cf);
            else if (cf.containsKey(definition(label)))
                return cf.get(definition(label));
            else
                return getLeafFeature(label, cf);
        } else if (label.equals("list"))
            return processList(tree, cf);
        else if (label.equals("conjoin"))
            return processConjunction(tree, cf);
        else if (ParameterizedFeatureExtractors.fexes.containsKey(label))
            return getParameterizedFex(tree, cf);
        else if (label.equals("conjoin-and-include"))
            return processIncludeWithPrefix(tree, cf);
        else if (label.equals("bigram"))
            return bigrams(tree, cf);
        else if (label.equals("trigram"))
            return trigrams(tree, cf);
        else if (label.equals("define"))
            throw new EdisonException("'define' can only be a top level statement "
                    + "or at the beginning of a feature descriptor\n" + tree);
        else if (label.equals("transform-input"))
            return processTransform(tree, cf);
        else if (label.equals("if"))
            return processQuery(tree, cf);
        else
            throw new EdisonException("Invalid feature description: " + tree);
    }

    /**
     * Alternate version, calls {@link FeatureManifest#populateFex(FeatureCollection)}
     * 
     * @return
     * @throws EdisonException
     */
    public FeatureExtractor createFex() throws EdisonException {
        String name = parser.getName();
        if (this.compressedName) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i >= 0 && i < name.length()) {
                sb.append(name.charAt(i));

                i = name.indexOf("-", i);
                if (i >= 0)
                    i++;
            }
            name = sb.toString();
        }

        return populateFex(new FeatureCollection(name));
    }

    public FeatureExtractor createFex(FeatureInputTransformer transformer) throws EdisonException {
        return populateFex(new FeatureCollection(parser.getName(), transformer));
    }


    /**
     * This deals with if statements.
     * 
     * @param tree
     * @param cf
     * @return
     * @throws EdisonException
     */
    public FeatureExtractor processQuery(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {

        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        if (tree.getNumberOfChildren() != 3) {
            throw new EdisonException(
                    "Invalid query. Expecting (if <query> <if-true> <if-false>).\n" + tree);
        }

        Tree<String> condition = tree.getChild(0);

        Predicate<Constituent> predicate;
        if (condition.getLabel().equals("exists")) {
            predicate = processExists(condition);
        } else {
            throw new EdisonException("Unknown query '" + condition.getLabel() + "'");
        }

        FeatureExtractor ifTrue = createFex(tree.getChild(1), cf);
        FeatureExtractor ifFalse = createFex(tree.getChild(2), cf);

        ConditionalFeatureExtractor fex =
                new ConditionalFeatureExtractor(predicate, ifTrue, ifFalse);

        CachedFeatureCollection cfx = new CachedFeatureCollection("", fex);
        cf.put(uniqueLabel, cfx);
        return cfx;
    }

    @SuppressWarnings("serial")
    private Predicate<Constituent> processExists(Tree<String> condition) throws EdisonException {
        if (condition.getNumberOfChildren() != 4) {
            throw new EdisonException("Invalid syntax for exists. Expecting "
                    + "(exists :view <view-name> :such-that <predicate>).\n" + condition);
        }

        final HashMap<String, String> variables = parser.getVariables();

        String viewName = null;
        Tree<String> query = null;

        int id = 0;
        while (id != 4) {
            Tree<String> child = condition.getChild(id++);
            if (child.getLabel().equals(":view")) {
                child = condition.getChild(id++);
                viewName = child.getLabel();
                if (variables.containsKey(viewName))
                    viewName = variables.get(viewName);
            } else if (child.getLabel().equals(":such-that")) {
                query = condition.getChild(id++);

                // parse the query for now, just so that we catch syntax errors
                QueryGenerator.generateQuery(query, null, variables);

            }
        }

        final Tree<String> tree = Mappers.mapTree(query, new ITransformer<Tree<String>, String>() {

            @Override
            public String transform(Tree<String> input) {
                return input.getLabel();
            }
        });
        final String name = viewName;

        return new Predicate<Constituent>() {
            @Override
            public Boolean transform(Constituent c) {
                Predicate<Constituent> q;
                try {
                    q = QueryGenerator.generateQuery(tree, c, variables);

                    return c.getTextAnnotation().getView(name).where(q).iterator().hasNext();
                } catch (EdisonException e) {
                    // this should never happen because we have already checked
                    // for errors before hand. This can happen only at runtime,
                    // given the data.
                    throw new RuntimeException(e);
                }

            }
        };
    }

    private FeatureExtractor processTransform(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {

        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        if (tree.getNumberOfChildren() != 2)
            throw new EdisonException("transform-input requires two arguments.\n" + tree);

        String transformer = tree.getChild(0).getLabel();

        if (!KnownTransformers.transformers.containsKey(transformer))
            throw new EdisonException("Unknown input transformer '" + transformer
                    + "'. Expecting one of " + KnownTransformers.transformers.keySet());

        FeatureInputTransformer fit = KnownTransformers.transformers.get(transformer);

        CachedFeatureCollection cfx =
                new CachedFeatureCollection("", fit, createFex(tree.getChild(1), cf));
        cf.put(uniqueLabel, cfx);

        return cfx;

    }

    private FeatureExtractor getNonAttributeFeatureExtractors(Tree<String> tree,
            Map<String, FeatureExtractor> cf) throws EdisonException {

        FeatureCollection f = new FeatureCollection("");
        int childId = 0;

        boolean found = false;
        while (childId < tree.getNumberOfChildren()) {
            Tree<String> child = tree.getChild(childId++);
            if (child.getLabel().startsWith(":")) {
                childId++;
            } else {
                f.addFeatureExtractor(createFex(child, cf));
                found = true;
            }
        }

        if (found)
            return f;
        else
            return null;

    }

    private FeatureExtractor getParameterizedFex(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {
        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        FeatureExtractor fex =
                ParameterizedFeatureExtractors.getParameterizedFeatureExtractor(tree,
                        getNonAttributeFeatureExtractors(tree, cf), parser.getVariables());

        CachedFeatureCollection cfx = new CachedFeatureCollection("", fex);
        cf.put(uniquify(tree), cfx);

        return cfx;
    }

    private WordFeatureExtractor getWordFex(final FeatureExtractor fex) {
        return new WordFeatureExtractor() {

            @Override
            public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition)
                    throws EdisonException {
                return fex.getFeatures(new Constituent("", "", ta, wordPosition, wordPosition + 1));
            }
        };
    }

    private FeatureExtractor bigrams(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {

        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        if (tree.getNumberOfChildren() != 1) {
            throw new EdisonException("bigrams takes exactly one argument\n" + tree);
        }

        FeatureExtractor fex =
                NgramFeatureExtractor.bigrams(getWordFex(createFex(tree.getChild(0), cf)));

        CachedFeatureCollection cfx = new CachedFeatureCollection("", fex);
        cf.put(uniquify(tree), cfx);

        return cfx;
    }

    private FeatureExtractor trigrams(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {

        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        if (tree.getNumberOfChildren() != 1) {
            throw new EdisonException("trigrams takes exactly one argument\n" + tree);
        }

        FeatureExtractor fex =
                NgramFeatureExtractor.trigrams(getWordFex(createFex(tree.getChild(0), cf)));

        CachedFeatureCollection cfx = new CachedFeatureCollection("", fex);
        cf.put(uniquify(tree), cfx);

        return cfx;
    }

    private FeatureExtractor processIncludeWithPrefix(Tree<String> tree,
            Map<String, FeatureExtractor> cf) throws EdisonException {
        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        FeatureCollection fex = new FeatureCollection("");

        if (tree.getNumberOfChildren() == 0)
            throw new EdisonException("Invalid declaration for conjoin-and-include\n" + tree);

        FeatureExtractor firstChild = createFex(tree.getChild(0), cf);
        fex.addFeatureExtractor(firstChild);

        if (tree.getNumberOfChildren() > 1) {

            FeatureExtractor conjoin = new FeatureCollection("", firstChild);

            for (int childId = 1; childId < tree.getNumberOfChildren(); childId++) {
                FeatureExtractor ff = createFex(tree.getChild(childId), cf);

                conjoin = FeatureUtilities.conjoin(conjoin, ff);
                fex.addFeatureExtractor(ff);
            }

            fex.addFeatureExtractor(conjoin);
        }

        CachedFeatureCollection cfx = new CachedFeatureCollection("", fex);
        cf.put(uniquify(tree), cfx);

        return cfx;

    }

    private FeatureExtractor processConjunction(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {
        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        if (tree.getNumberOfChildren() == 0) {
            throw new EdisonException("Invalid conjunction " + tree);
        }

        FeatureExtractor fex = createFex(tree.getChild(0), cf);

        for (int i = 1; i < tree.getNumberOfChildren(); i++) {
            fex = FeatureUtilities.conjoin(fex, createFex(tree.getChild(i), cf));
        }

        CachedFeatureCollection f = new CachedFeatureCollection("", fex);

        cf.put(uniqueLabel, f);

        return f;
    }

    private FeatureExtractor processList(Tree<String> tree, Map<String, FeatureExtractor> cf)
            throws EdisonException {

        String uniqueLabel = uniquify(tree);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        CachedFeatureCollection fc = new CachedFeatureCollection("");

        List<String> wnLabels = new ArrayList<>();

        for (Tree<String> child : tree.getChildren()) {
            if (child.isLeaf() && child.getLabel().startsWith("wn")) {
                wnLabels.add(child.getLabel());
            } else {
                fc.addFeatureExtractor(createFex(child, cf));
            }

        }

        if (wnLabels.size() > 0)
            fc.addFeatureExtractor(getWordNetFeatureExtractor(wnLabels, cf));

        cf.put(uniquify(tree), fc);

        return fc;
    }

    /**
     * Given a leaf name, find the corresponding FeatureExtractor, as defined in
     * {@link KnownFexes#fexes}
     * 
     * @param label string, needs to be in {@link KnownFexes#fexes}
     * @param cf used for memoization, maps label to FeatureExtractor
     * @return the corresponding FeatureExtractor
     * @throws EdisonException
     */
    private FeatureExtractor getLeafFeature(String label, Map<String, FeatureExtractor> cf)
            throws EdisonException {

        String uniqueLabel = uniquify(label);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        if (!KnownFexes.fexes.containsKey(label))
            throw new EdisonException("Unknown feature extractor '" + label
                    + "', expecting one of " + KnownFexes.fexes.keySet());
        FeatureExtractor featureExtractor = KnownFexes.fexes.get(label);

        cf.put(uniqueLabel, featureExtractor);
        return featureExtractor;

    }

    private String uniquify(Tree<String> label) {
        return getUniqueList(label).toString().replaceAll("\\s+", "");
    }

    /**
     * This just removes all whitespace.
     * 
     * @param label any string
     * @return label without whitespace
     */
    private String uniquify(String label) {
        return label.replaceAll("\\s+", "");
    }

    private String uniquify(List<String> labels) {
        return new TreeSet<>(labels).toString().replaceAll("\\s+", "");
    }

    private FeatureExtractor getWordNetFeatureExtractor(List<String> wnLabels,
            Map<String, FeatureExtractor> cf) throws EdisonException {

        String uniqueLabel = uniquify(wnLabels);
        if (cf.containsKey(uniqueLabel))
            return cf.get(uniqueLabel);

        try {
            WordNetFeatureExtractor wn = new WordNetFeatureExtractor();

            for (String label : wnLabels) {
                if (!WordNetClasses.wnClasses.containsKey(label))
                    throw new EdisonException("Unknown wordnet feature extractor '" + label
                            + "', expecting one of " + WordNetClasses.wnClasses.keySet());
                wn.addFeatureType(WordNetClasses.wnClasses.get(label));
            }

            CachedFeatureCollection f = new CachedFeatureCollection("", wn);

            cf.put(uniqueLabel, f);
            return f;
        } catch (Exception e) {
            throw new EdisonException(e);
        }

    }

    public String getIncludedFeatures() {
        return parser.getIncludedFeatures();
    }

    private List<String> getUniqueList(Tree<String> tree) {
        List<String> f = new ArrayList<>();
        f.add(tree.getLabel());
        for (Tree<String> child : tree.getChildren()) {
            f.add(child.toString().replaceAll("\\s+", " "));
        }

        Collections.sort(f);

        return f;
    }

    private String definition(String label) {
        return "__DEF__" + label;
    }

    /**
     * This adds a FeatureExtractor to the input FeatureCollection. Typically the FeatureCollection
     * is empty, having only a name.
     * 
     * @param fex
     * @return
     * @throws EdisonException
     */
    private FeatureExtractor populateFex(FeatureCollection fex) throws EdisonException {

        // cached features.
        Map<String, FeatureExtractor> cf = new HashMap<>();

        // first manage all define statements
        for (Tree<String> defn : parser.getDefinitions()) {
            if (defn.getNumberOfChildren() != 2) {
                throw new EdisonException("Invalid definition. Expecting (define name body)\n"
                        + defn);
            }

            String name = defn.getChild(0).getLabel();

            FeatureExtractor body = this.createFex(defn.getChild(1), cf);

            cf.put(definition(name), new CachedFeatureCollection("", body));
        }

        fex.addFeatureExtractor(this.createFex(parser.getFeatureDescriptor(), cf));

        return fex;
    }

    public void useCompressedName() {
        this.compressedName = true;
    }


}
