/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.ner;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureCreatorUtil;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* needs reimplementation of Gazetteer classes to work with Constituents.
 * Probably makes sense to work with a Gazetteer view that stores equivalent information to
 *    the corresponding field, NEWord.gazetteers
 * Not yet sure how gazetteers populate NEWord fields: the fex looks like it sweeps a window
 *    of +/- 2 and adds feat for size of window (could be <5 if at end of sentence) plus name
 *    of gazetteers matched at that position
 */
public class GazetteerWindowTwo implements FeatureExtractor {
    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        Set<Feature> feats = new HashSet<>();

        int i=0;
        View gazView = c.getTextAnnotation().getView( ViewNames.GAZETTEER_NE );

        // suppose for now that gaz-ne view has a constituent for each gazetteer match -- either
        //  a single constituent for each word for each match, or a single constituent for each complete match

        List<Constituent> overlapCons = gazView.getConstituentsCovering( c );

        for ( Constituent oc : overlapCons )
        {
            /**
             * assumes we are dealing with multi-token Gazetteer constituents; otherwise, must track match position
             *    of gazetteer entry in the single-token Constituent as a parameter (e.g. attributes are
             *    keyed on name of matched gazetteer, and value is the position of the gazetteer entry matched
             *    by the current token)
             */
            int relativePosition = c.getStartSpan() - oc.getStartSpan();
            String[] pieces = { getName(), ":", "(", Integer.toString(relativePosition ), ")", oc.getLabel() };
            feats.add( FeatureCreatorUtil.createFeatureFromArray( pieces ));
        }
//        NEWord w = word, last = (NEWord)word.next;
//
//        for (i = 0; i < 2 && last != null; ++i) last = (NEWord) last.next;
//        for (i = 0; i > -2 && w.previous != null; --i) w = (NEWord) w.previous;
//
//        do
//        {
//            if(w.gazetteers!=null)
//                for(int j=0;j<w.gazetteers.size();j++)
//                    sense i: w.gazetteers.get(j);
//            i++;
//            w = (NEWord) w.next;
//        }while(w != last);

        return feats;
    }

    @Override
    public String getName() {
        return GazetteerWindowTwo.class.getSimpleName();
    }
}
