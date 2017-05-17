package edu.illinois.cs.cogcomp.verbsense.jlis;

import edu.illinois.cs.cogcomp.sl.core.IInstance;

import java.util.List;

public class SentenceInstance implements IInstance {

    public final List<SenseInstance> predicates;
    private int size;

    public SentenceInstance(List<SenseInstance> predicates) {
        this.predicates = predicates;
        size = predicates.size();
    }

    @Override
    public double size() {
        return size;
    }


    public int numPredicates() {
        return predicates.size();
    }
}
