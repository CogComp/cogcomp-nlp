/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.NomLexEntry;
import edu.illinois.cs.cogcomp.edison.utilities.NomLexReader;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds NomLex based features. If the input constituent contains an attribute called
 * {@link PredicateArgumentView#LemmaIdentifier}, the corresponding value is used to identify the
 * features. Otherwise, the lemma of the last token is used. If the lemma is not an element of
 * NomLex, then the last token (lower cased) is tested. If NomLex does not contain the last token,
 * then an indicator feature is added that this token is not a member of NomLex.
 * <p>
 * When the NomLex entry is found, the following features are added:
 * <ul>
 * <li>Nom class</li>
 * <li>For verbal and adjectival nominalizations, the underlying verb (or adjective).</li>
 * </ul>
 * <b>Note</b>: To use this feature, NomLexReader.nomLexFile must be set.
 *
 * @keywords SRL, Nom, Nominal, Nominalization, NomLex
 * @author Vivek Srikumar
 */
public class NomLexClassFeature implements FeatureExtractor {

    private static final DiscreteFeature DE_ADJECTIVAL = DiscreteFeature.create("nom-adj");
    private static final DiscreteFeature DE_VERBAL = DiscreteFeature.create("nom-vb");
    public static NomLexClassFeature instance = new NomLexClassFeature();

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        int tokenId = c.getEndSpan() - 1;
        TextAnnotation ta = c.getTextAnnotation();
        String predicateWord = ta.getToken(tokenId).toLowerCase().trim();
        String predicateLemma;
        if (c.hasAttribute(PredicateArgumentView.LemmaIdentifier))
            predicateLemma = c.getAttribute(PredicateArgumentView.LemmaIdentifier);
        else
            predicateLemma = WordHelpers.getLemma(ta, tokenId);

        NomLexReader nomLex = NomLexReader.getInstance();

        List<NomLexEntry> nomLexEntries = nomLex.getNomLexEntries(predicateWord, predicateLemma);

        Set<Feature> features = new LinkedHashSet<>();
        if (nomLexEntries.size() > 0) {
            for (NomLexEntry e : nomLexEntries) {
                features.add(DiscreteFeature.create("nom-cls:" + e.nomClass));

                if (NomLexEntry.VERBAL.contains(e.nomClass)) {
                    features.add(DE_VERBAL);
                    features.add(DiscreteFeature.create("nom-vb:" + e.verb));
                } else if (NomLexEntry.ADJECTIVAL.contains(e.nomClass)) {
                    features.add(DE_ADJECTIVAL);
                    features.add(DiscreteFeature.create("nom-adj:" + e.adj));
                }
            }
        }

        return features;
    }

    @Override
    public String getName() {
        return "#nomlex#";
    }

}
