package edu.illinois.cs.cogcomp.finetyper.finer.components.typers;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.FinerType;
import edu.illinois.cs.cogcomp.finetyper.finer.wordnet.WordNetUtils;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Synset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by haowu4 on 5/21/17.
 */
public class HypernymTyper implements IFinerTyper {
    Map<String, List<FinerType>> typeToSynsets;
    WordNetUtils wordNetUtils;

    public HypernymTyper(Map<String, List<FinerType>> typeToSynsets) {
        this.typeToSynsets = typeToSynsets;
        try {
            wordNetUtils = WordNetUtils.getInstance();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public void annotateOneMention(FineTypeConstituent mention) throws JWNLException {
        FinerType coarseType = mention.getCoarseType();
        int start = mention.getConstituent().getStartSpan();
        int end = mention.getConstituent().getEndSpan();
        if (start > 0) {
            Constituent word_before = mention.getConstituent().getTextAnnotation().getView(ViewNames.TOKENS).getConstituents().get(start - 1);
            if (word_before.getSentenceId() == mention.getConstituent().getSentenceId()) {
                start = start - 1;
            }
        }

        View wsdView = mention.getConstituent().getTextAnnotation().getView(ViewNames.FINE_NER_TYPE_WSD);
        View posView = mention.getConstituent().getTextAnnotation().getView(ViewNames.POS);

        for (Constituent c : wsdView.getConstituentsCoveringSpan(start, end)) {
            if (posView.getConstituentsCovering(c).get(0).getLabel().startsWith("N")) {
                if (c.getLabel().isEmpty()) {
                    continue;
                }
                Synset synset;
                try {
                    synset = wordNetUtils.getSynsetByOffset(c.getLabel());
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println(c.getLabel() + " Not Found...");
                    continue;
                }
                String synset_offset_pos = synset.getOffset() + "" + synset.getPOS();
                List<FinerType> infered = typeToSynsets.getOrDefault(synset_offset_pos, new ArrayList<>());
                for (FinerType t : infered) {
                    if (t.isChildOf(coarseType)) {
                        mention.addFineType(t);
                    }
                }
            }
        }
    }

    @Override
    public void annotate(List<FineTypeConstituent> mentions, Sentence sentence) {
        for (FineTypeConstituent m : mentions) {
            try {
                annotateOneMention(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
