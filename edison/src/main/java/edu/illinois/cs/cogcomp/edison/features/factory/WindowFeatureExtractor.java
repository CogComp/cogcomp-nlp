/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
//package edu.illinois.cs.cogcomp.edison.features.factory;
//
//import edu.illinois.cs.cogcomp.core.datastructures.Pair;
//import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
//import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
//import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
//import edu.illinois.cs.cogcomp.edison.features.Feature;
//import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
//import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
//import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///** Given a feature extractor, it extractors a window
// * @author daniel
// */
//public class WindowFeatureExtractor extends WordFeatureExtractor {
//
//    FeatureExtractor<Constituent> fe = null;
//    int width;
//    int shift;
//    String viewName;
//
//    /**
//     *
//     * @param shift how much the window is shifted to the right of the anchor word
//     * @param width the windows of the window
//     * @param fe the base feature extractor
//     */
//    public WindowFeatureExtractor(int shift, int width, String viewName, FeatureExtractor<Constituent> fe) {
//        this.width = width;
//        this.shift = shift;
//        this.fe = fe;
//        this.viewName = viewName;
//    }
//
//    @Override
//    public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {
//        Set<Constituent> constituents = new HashSet<>();
//        int center = wordPosition + shift;
//        int start = center - width;
//        if(start < 0) start = 0;
//        int end = center + width;
//        if(end > ta.size()) end = ta.size();
//        for(int i = start; i <= end; i++) {
//            constituents.addAll(ta.getView(viewName).getConstituentsCoveringToken(wordPosition));
//        }
//        Set<Feature> features = new HashSet<>();
//        for(Constituent c : constituents) {
//            features.addAll(fe.getFeatures(c));
//        }
//        return features;
//    }
//
//    @Override
//    public String getName() {
//        return "#wndw-ftr";
//    }
//}