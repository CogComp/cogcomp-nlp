/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.prepsrl.inference;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.infer.ILPInference;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Normalizer;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;
import edu.illinois.cs.cogcomp.prepsrl.inference.constraints.PrepSRLInferenceConstraints;

public class PrepSRLInference extends ILPInference {
    public PrepSRLInference() {}

    public PrepSRLInference(Constituent head) {
        super(head, new OJalgoHook());
        constraint = new PrepSRLInferenceConstraints().makeConstraint(head);
    }

    public static Constituent findHead(Constituent c) {
        return c;
    }

    public String getHeadType() {
        return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent";
    }

    public String[] getHeadFinderTypes() {
        return new String[] {"edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"};
    }

    public Normalizer getNormalizer(Learner c) {
        return new Softmax();
    }
}
