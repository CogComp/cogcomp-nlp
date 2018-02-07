package edu.illinois.cs.cogcomp.finetyper.finer.components.mention;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.finetyper.Utils;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.FinerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by haowu4 on 1/15/17.
 * <p>
 * A FINER mention detection using Ontonotes NER output.
 */
public class BasicMentionDetection implements MentionDetecter {
    TypeMapper mapper;

    public BasicMentionDetection(TypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<FineTypeConstituent> getMentionCandidates(TextAnnotation ta, Sentence sentence) {
        List<FineTypeConstituent> ret = new ArrayList<>();
        List<Constituent> ner = Utils.getSentenceConstituents(sentence, ta, ViewNames.NER_ONTONOTES);
        for (Constituent c : ner) {
            FinerType coarseType = mapper.getType(c.getLabel());
            if (coarseType == null) {
                continue;
            }
            String typeName = coarseType.toString();
            Map<String, Double> l2s = new HashMap<>();
            l2s.put(typeName, 1.0);
            FineTypeConstituent mention = new FineTypeConstituent(c);
            mention.addCoarseType(coarseType);
            ret.add(mention);
        }
        return ret;
    }
}
