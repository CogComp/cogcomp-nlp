/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.cogcomp.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * A BIG dictionary of words (and multi-word phrases) and their synonyms.
 * Probably useful for many applications, e.g. QA, paraphrase, etc.
 */
public class RogetThesaurusFeatures implements FeatureExtractor {

    public static final RogetThesaurusFeatures INSTANCE = new RogetThesaurusFeatures();

    private final static Logger log = LoggerFactory.getLogger(RogetThesaurusFeatures.class);

    private final static String fileName = "rogetThesaurus/index.txt";

    Map<String, int[]> map;

    Map<String, Integer> classNamesToIds;
    List<String> id2ClassName;

    private boolean loaded;

    public RogetThesaurusFeatures() {
        loaded = false;
        loaded = false;
    }

    // old way: loading the resourcres from classpath
    private synchronized void loadFromClassPath() throws Exception {
        if (loaded)
            return;

        List<URL> urls = IOUtils.lsResources(RogetThesaurusFeatures.class, fileName);
        if (urls.size() == 0)
            throw new EdisonException("Cannot find " + fileName + " in the classpath");

        loadWithURL(urls.get(0));
    }


    private synchronized void loadFromDatastore() throws Exception {
        if (loaded)
            return;
        Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
        File f = dsNoCredentials.getFile("org.cogcomp.roget.thesaurus", "rogetThesaurus", 1.3);
        loadWithURL(f.toURI().toURL());
    }

    private synchronized void loadWithURL(URL url) throws Exception {
        id2ClassName = new ArrayList<>();
        classNamesToIds = new HashMap<>();
        map = new HashMap<>();

        log.info("Loading Roget's thesaurus from {}", fileName);
        Scanner scanner = new Scanner(url.openStream());


        String word = scanner.nextLine();

        while (scanner.hasNextLine()) {
            String type = scanner.nextLine();

            List<Integer> list = new ArrayList<>();
            while (type.startsWith(" ")) {

                type = type.trim();
                int space = type.lastIndexOf(" ");
                if (space > 0) {
                    type = type.substring(0, space).trim();
                }

                if (!this.classNamesToIds.containsKey(type)) {
                    int id = this.classNamesToIds.size();
                    this.classNamesToIds.put(type, id);
                    this.id2ClassName.add(type);
                }

                list.add(this.classNamesToIds.get(type));

                if (!scanner.hasNextLine())
                    break;
                type = scanner.nextLine();
            }

            if (list.size() > 0) {
                word = word.trim();
                map.put(word, ArrayUtilities.asIntArray(list));
            }
            word = type;
        }

        log.info("Loaded {} words", map.size());

        scanner.close();
        loaded = true;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        if (!loaded) {
            try {
                // not load the data from classpath; instead using the datastore
                // loadFromClassPath();
                loadFromDatastore();
            } catch (Exception e) {
                throw new EdisonException(e);
            }
        }

        String s = c.getTokenizedSurfaceForm().trim();
        Set<Feature> features = new LinkedHashSet<>();

        if (map.containsKey(s)) {
            for (int i : map.get(s)) {
                features.add(DiscreteFeature.create(this.id2ClassName.get(i)));
            }
        } else if (map.containsKey(s.toLowerCase())) {
            for (int i : map.get(s.toLowerCase())) {
                features.add(DiscreteFeature.create(this.id2ClassName.get(i)));
            }
        }

        return features;
    }

    @Override
    public String getName() {
        return "#roget";
    }

}
