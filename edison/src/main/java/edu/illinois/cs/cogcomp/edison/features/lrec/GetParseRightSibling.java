/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.List;

public class GetParseRightSibling extends FeatureInputTransformer {
    private final String parseViewName;

    public GetParseRightSibling(String parseViewName) {
        this.parseViewName = parseViewName;
    }

    @Override
    public List<Constituent> transform(Constituent input) {
        TextAnnotation ta = input.getTextAnnotation();

        TreeView parse = (TreeView) ta.getView(parseViewName);

        List<Constituent> siblings = new ArrayList<>();
        try {
            Constituent phrase = parse.getParsePhrase(input);
            List<Relation> in = phrase.getIncomingRelations();

            if (in.size() > 0) {
                List<Relation> outgoingRelations = in.get(0).getSource().getOutgoingRelations();
                int id = -1;
                for (int i = 0; i < outgoingRelations.size(); i++) {
                    Relation r = outgoingRelations.get(i);
                    if (r.getTarget() == phrase) {
                        id = i;
                        break;
                    }
                }

                if (id >= 0 && id + 1 < outgoingRelations.size())
                    siblings.add(outgoingRelations.get(id + 1).getTarget());
            }

        } catch (EdisonException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return siblings;
    }

    @Override
    public String name() {
        return "#rsis";
    }

}
