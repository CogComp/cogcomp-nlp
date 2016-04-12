package edu.illinois.cs.cogcomp.srl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.infer.ilp.ILPConstraint;
import edu.illinois.cs.cogcomp.infer.ilp.InferenceVariableLexManager;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPConstraintGenerator;
import edu.illinois.cs.cogcomp.srl.learn.SRLPredicateInstance;
import edu.illinois.cs.cogcomp.srl.learn.SRLSentenceInstance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This constraint says that a label can be SUP (or C-SUP) ONLY if it is either
 * a verb or a Nominal predicate. We assume that the predicates are fixed.
 *
 * @author svivek
 */
public class SupportVerbConstraint extends SRLILPConstraintGenerator {

  public static final String name = "supportVerbConstraint";

  public SupportVerbConstraint(SRLManager manager) {
    super(manager, name, false);
  }

  @Override
  public List<ILPConstraint> getILPConstraints(IInstance ins,
                                               InferenceVariableLexManager variables) {

    SRLSentenceInstance x = (SRLSentenceInstance) ins;

    List<ILPConstraint> list = new ArrayList<>();

    Set<Integer> predicatePosition = new HashSet<>();
    for (int predicateId = 0; predicateId < x.numPredicates(); predicateId++) {
      predicatePosition.add(x.predicates.get(predicateId)
              .getSenseInstance().getSpan().getFirst());
    }

    for (int predicateId = 0; predicateId < x.numPredicates(); predicateId++) {
      SRLPredicateInstance xp = x.predicates.get(predicateId);
      TextAnnotation ta = xp.getSenseInstance().getConstituent()
              .getTextAnnotation();

      int predicate = xp.getSenseInstance().getSpan().getFirst();
      for (int candidateId = 0; candidateId < xp.getCandidateInstances()
              .size(); candidateId++) {

        int candidatePosition = xp.getCandidateInstances()
                .get(candidateId).getSpan().getFirst();

        String pos = WordHelpers.getPOS(ta, candidatePosition);
        if (POSUtils.isPOSVerb(pos))
          continue;

        if (predicatePosition.contains(candidatePosition)
                && candidatePosition != predicate)
          continue;

        int supportVar = getArgumentVariable(variables,
                manager.getPredictedViewName(), predicateId,
                candidateId, "SUP");

        // if there is no support, then there cannot be C-support, so
        // don't bother.
        if (supportVar < 0)
          continue;

        int cSupportVar = getArgumentVariable(variables,
                manager.getPredictedViewName(), predicateId,
                candidateId, "C-SUP");

        if (cSupportVar >= 0) {

          list.add(new ILPConstraint(new int[]{supportVar,
                  cSupportVar}, new double[]{1, 1}, 0,
                  ILPConstraint.EQUAL
          ));
        } else {
          list.add(new ILPConstraint(new int[]{supportVar},
                  new double[]{1}, 0, ILPConstraint.EQUAL));
        }

      }
    }

    return list;
  }

  @Override
  public List<ILPConstraint> getViolatedILPConstraints(IInstance x,
                                                       IStructure y, InferenceVariableLexManager variables) {
    // This constraint is always present
    return new ArrayList<>();
  }

}
