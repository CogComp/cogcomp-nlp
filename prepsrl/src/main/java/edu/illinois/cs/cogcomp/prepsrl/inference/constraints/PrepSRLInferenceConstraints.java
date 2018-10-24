/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.lbjava.infer.FirstOrderConstraint;
import edu.illinois.cs.cogcomp.lbjava.infer.ParameterizedConstraint;
import edu.illinois.cs.cogcomp.prepsrl.inference.PrepSRLInference;

/**
 * A definition of the collection of constraints used for {@link PrepSRLInference}. Here only using
 * {@link LegalRoles}.
 */
public class PrepSRLInferenceConstraints extends ParameterizedConstraint {
    private static final LegalRoles legalRoles = new LegalRoles();

    public PrepSRLInferenceConstraints() {
        super(
                "edu.illinois.cs.cogcomp.esrl.prepsrl.inference.constraints.PrepSRLInferenceConstraints");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent";
    }

    public String discreteValue(Object example) {
        Constituent phrase = (Constituent) example;

        boolean constraint = legalRoles.discreteValue(phrase).equals("true");
        if (!constraint)
            return "false";

        return "true";
    }

    public int hashCode() {
        return "PrepSRLInferenceConstraints".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof PrepSRLInferenceConstraints;
    }

    public FirstOrderConstraint makeConstraint(Object example) {
        Constituent phrase = (Constituent) example;
        return legalRoles.makeConstraint(phrase);
    }
}
