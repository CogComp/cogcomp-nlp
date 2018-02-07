package edu.illinois.cs.cogcomp.finetyper.finer.components.mention;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.FineTypeConstituent;

import java.util.List;

/**
 * Created by haowu4 on 1/15/17.
 */
public interface MentionDetecter {
    List<FineTypeConstituent> getMentionCandidates(TextAnnotation ta, Sentence sentence);
}
