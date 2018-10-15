/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A feature extractor that simply checks if the surface string of the constituent is in a list or
 * not. If it is present, then it emits the name of the list as a feature.
 *
 * @author Vivek Srikumar
 */
public class ListFeatureExtractor implements FeatureExtractor {

    private final Feature listName;
    private final Set<String> list;
    private final boolean caseSensitive;

    /**
     * Creates a new ListFeatureExtractor
     *
     * @param name The name of the list, which is emitted as the feature
     * @param list The list of items against which the constituent should be checked.
     * @param caseSensitive Should the checking be case sensitive?
     */
    public ListFeatureExtractor(String name, Collection<String> list, boolean caseSensitive) {
        this.listName = DiscreteFeature.create(name);
        this.list = new LinkedHashSet<>();

        this.caseSensitive = caseSensitive;
        for (String l : list)
            if (caseSensitive)
                this.list.add(l);
            else
                this.list.add(l.toLowerCase());

    }

    /**
     * Creates a new ListFeatureExtractor
     *
     * @param listName The name of the list, which is emitted as the feature
     * @param listFile A file containing the elements of the list
     * @param caseSensitive Should the checking be case sensitive?
     * @throws java.io.FileNotFoundException
     */
    public ListFeatureExtractor(String listName, String listFile, boolean caseSensitive)
            throws FileNotFoundException {
        this(listName, LineIO.read(listFile), caseSensitive);
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        String surface = c.getTokenizedSurfaceForm().trim();

        if (!caseSensitive)
            surface = surface.toLowerCase();
        Set<Feature> features = new LinkedHashSet<>();
        if (list.contains(surface))
            features.add(listName);
        return features;
    }

    public String getName() {
        return "#list#";
    }
}
