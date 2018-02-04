package edu.illinois.cs.cogcomp.finetyper.typers;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;

import java.util.List;
import java.util.Map;

/**
 * Created by haowu4 on 2/4/18.
 */
public interface Typer {
    /**
     * Determine types for the given
     *
     * @param query
     * @return list of constituents with type and their confidence added as labels.
     */
    List<Constituent> getTypes(List<Constituent> query);
}
