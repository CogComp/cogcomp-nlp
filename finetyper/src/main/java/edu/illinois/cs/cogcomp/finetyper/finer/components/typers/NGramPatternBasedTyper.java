package edu.illinois.cs.cogcomp.finetyper.finer.components.typers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.FineTypeConstituent;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.FinerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by haowu4 on 5/15/17.
 */
public class NGramPatternBasedTyper implements IFinerTyper {

    private Map<NGramPattern, List<FinerType>> allPatterns;
    private int windowSize = 5;

    public NGramPatternBasedTyper(Map<NGramPattern, List<FinerType>> allPatterns) {
        this(allPatterns, 5);
    }

    public NGramPatternBasedTyper(Map<NGramPattern, List<FinerType>> allPatterns, int windowSize) {
        this.allPatterns = allPatterns;
        this.windowSize = windowSize;
    }

    public List<NGramPattern> extractAllPattern(String[] surface) {
        List<NGramPattern> ret = new ArrayList<>();
        for (int i = 0; i < surface.length; i++) {
            String w = surface[i];
            int word_before = i;
            for (int j = 0; j < this.windowSize; j++) {
                int word_after = surface.length - i - j;
                if (word_after < 0) {
                    break;
                }

                NGramPattern sp = new NGramPattern(word_before, word_after, Arrays.copyOfRange(surface, i, i + j));
                ret.add(sp);
            }
        }
        return ret;
    }

    public void annotateOneMention(FineTypeConstituent c) {
        FinerType coarseType = c.getCoarseType();
        int start = c.getConstituent().getStartSpan();
        int end = c.getConstituent().getEndSpan();
        String[] surface = new String[end - start];
        TextAnnotation ta = c.getConstituent().getTextAnnotation();
        for (int i = 0; i < surface.length; i++) {
            surface[i] = ta.getToken(start + i);
        }

        List<NGramPattern> existing_patterns = extractAllPattern(surface);

        for (NGramPattern sp : existing_patterns) {
            for (FinerType t : allPatterns.getOrDefault(sp, new ArrayList<>())) {
                if (t.isChildOf(coarseType)) {
                    c.addFineType(t);
                }
            }
        }
    }

    @Override
    public void annotate(List<FineTypeConstituent> mentions, Sentence sentence) {
        for (FineTypeConstituent mention : mentions) {
            annotateOneMention(mention);
        }
    }
}
