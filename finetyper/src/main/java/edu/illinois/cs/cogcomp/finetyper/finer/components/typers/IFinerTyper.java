package edu.illinois.cs.cogcomp.finetyper.finer.components.typers;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.FineTypeConstituent;

import java.util.List;

/**
 * Created by haowu4 on 5/15/17.
 */
public interface IFinerTyper {
    void annotate(List<FineTypeConstituent> mentions, Sentence sentence);
}
