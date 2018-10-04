/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl.inference.constraints;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLConfigurator;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLClassifier;
import edu.illinois.cs.cogcomp.prepsrl.data.PrepSRLDataReader;

/**
 * An ILP constraint that enforces a specific set allowed roles to each preposition. The list of
 * allowed roles is read on the fly from `sense2role.csv` (in the resources folder).
 */
public class LegalRoles extends ParameterizedConstraint {
    private static ResourceManager rm = PrepSRLConfigurator.defaults();
    private static String modelsDir = rm.getString(PrepSRLConfigurator.MODELS_DIR);
    private static String modelName = modelsDir + "/" + PrepSRLClassifier.CLASS_NAME;
    private static final PrepSRLClassifier prepSRLClassifier = new PrepSRLClassifier(modelName
            + ".lc", modelName + ".lex");

    LegalRoles() {
        super("edu.illinois.cs.cogcomp.esrl.prepsrl.inference.constraints.LegalRoles");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent";
    }

    public String discreteValue(Object example) {
        Constituent phrase = (Constituent) example;

        boolean constraint = false;
        for (String role : PrepSRLDataReader.getLegalRoles(phrase)) {
            constraint = prepSRLClassifier.discreteValue(phrase).equals(role);
        }

        if (!constraint)
            return "false";

        return "true";
    }

    public int hashCode() {
        return "LegalRoles".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof LegalRoles;
    }

    public FirstOrderConstraint makeConstraint(Object example) {
        Constituent phrase = (Constituent) example;

        Object[] context = new Object[1];
        context[0] = phrase;

        EqualityArgumentReplacer roleEAR = new EqualityArgumentReplacer(context, false) {
            public String getRightValue() {
                return (String) quantificationVariables.get(0);
            }
        };
        FirstOrderConstraint isRole =
                new FirstOrderEqualityWithValue(true, new FirstOrderVariable(prepSRLClassifier,
                        phrase), null, roleEAR);
        FirstOrderConstraint existsRole =
                new ExistentialQuantifier("role", PrepSRLDataReader.getLegalRoles(phrase), isRole);

        return new FirstOrderConjunction(new FirstOrderConstant(true), existsRole);
    }
}
