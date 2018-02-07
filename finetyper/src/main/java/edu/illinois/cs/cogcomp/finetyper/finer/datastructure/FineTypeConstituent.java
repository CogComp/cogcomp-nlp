package edu.illinois.cs.cogcomp.finetyper.finer.datastructure;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.finetyper.finer.datastructure.types.FinerType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by haowu4 on 5/17/17.
 */
public class FineTypeConstituent {

    private Constituent constituent;
    private FinerType coarseType;
    private Set<FinerType> fineTypes;
    private Map<FinerType, Double> labelsToScores;

    private static Map<String, Double> getLablToScore() {
        Map<String, Double> ret = new HashMap<String, Double>();
        ret.put("FinerType", 1.0);
        return ret;
    }

    public FineTypeConstituent(Constituent constituent) {
        this.constituent = constituent;
        this.fineTypes = new HashSet<>();
        this.labelsToScores = new HashMap<>();
    }


    public void addFineType(FinerType t) {
        this.fineTypes.add(t);
    }

    public void addCoarseType(FinerType t) {
        this.coarseType = t;
    }


    public FinerType getCoarseType() {
        return coarseType;
    }

    public void finish() {
        this.labelsToScores.clear();
        for (FinerType t : this.fineTypes) {
            if (t.isVisible()) {
                this.labelsToScores.put(t, 1.0);
            }
        }

        if (this.coarseType.isVisible()) {
            this.labelsToScores.put(this.coarseType, 1.0);
        }
    }


    public Optional<Constituent> toConstituent(String viewName) {
        this.finish();
        if (labelsToScores.isEmpty()) {
            return Optional.empty();
        } else {
            Map<String, Double> labelStrToScores = labelsToScores
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(x -> x.getKey().getType(), Map.Entry::getValue));
            return Optional.of(new Constituent(labelStrToScores,
                    viewName,
                    this.constituent.getTextAnnotation(),
                    constituent.getStartSpan(), constituent.getEndSpan()));
        }

    }

    public Constituent getConstituent() {
        return constituent;
    }
}
