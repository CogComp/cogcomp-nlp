/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000053B8B3A020130168FA2F39A296C30CA863B065B592E1068CEC6C1846209911401FEEAC28DE7F8CD5DC7098A3AFB6BE36D9B7B641F6CAECA991690F6CF835DA8D51234411DE058D770613524CBBA4914DEA25E1E7162F7E0E89316A901847D083D91B4FC457F56EA4662B90F8F73B88978474C7EBA568F66409000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;


public class oxfordComma extends ParameterizedConstraint {
    private static final UnigramRightFeature __UnigramRightFeature = new UnigramRightFeature();
    private static final LocalCommaClassifier __LocalCommaClassifier = new LocalCommaClassifier();

    public oxfordComma() {
        super("edu.illinois.cs.cogcomp.comma.lbj.oxfordComma");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'oxfordComma(CommaSRLSentence)' defined on line 236 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        CommaSRLSentence s = (CommaSRLSentence) __example;

        {
            boolean LBJava$constraint$result$0;
            {
                LBJava$constraint$result$0 = true;
                for (java.util.Iterator __I0 = (s.getCommas()).iterator(); __I0.hasNext()
                        && LBJava$constraint$result$0;) {
                    Comma c = (Comma) __I0.next();
                    {
                        boolean LBJava$constraint$result$1;
                        LBJava$constraint$result$1 =
                                ("" + (__UnigramRightFeature.discreteValue(c)))
                                        .equals("" + ("and"));
                        if (LBJava$constraint$result$1)
                            LBJava$constraint$result$0 =
                                    ("" + (__LocalCommaClassifier.discreteValue(c))).equals(""
                                            + ("List"));
                        else
                            LBJava$constraint$result$0 = true;
                    }
                }
            }
            if (!LBJava$constraint$result$0)
                return "false";
        }

        return "true";
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof CommaSRLSentence[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'oxfordComma(CommaSRLSentence)' defined on line 236 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "oxfordComma".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof oxfordComma;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'oxfordComma(CommaSRLSentence)' defined on line 236 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        CommaSRLSentence s = (CommaSRLSentence) __example;
        FirstOrderConstraint __result = new FirstOrderConstant(true);

        {
            Object[] LBJ$constraint$context = new Object[1];
            LBJ$constraint$context[0] = s;
            FirstOrderConstraint LBJava$constraint$result$0 = null;
            {
                FirstOrderConstraint LBJava$constraint$result$1 = null;
                {
                    FirstOrderConstraint LBJava$constraint$result$2 = null;
                    {
                        EqualityArgumentReplacer LBJ$EAR =
                                new EqualityArgumentReplacer(LBJ$constraint$context, true) {
                                    public String getLeftValue() {
                                        Comma c = (Comma) quantificationVariables.get(0);
                                        return "" + (__UnigramRightFeature.discreteValue(c));
                                    }
                                };
                        LBJava$constraint$result$2 =
                                new FirstOrderEqualityTwoValues(true, null, "" + ("and"), LBJ$EAR);
                    }
                    FirstOrderConstraint LBJava$constraint$result$3 = null;
                    {
                        EqualityArgumentReplacer LBJ$EAR =
                                new EqualityArgumentReplacer(LBJ$constraint$context, true) {
                                    public Object getLeftObject() {
                                        Comma c = (Comma) quantificationVariables.get(0);
                                        return c;
                                    }
                                };
                        LBJava$constraint$result$3 =
                                new FirstOrderEqualityWithValue(true, new FirstOrderVariable(
                                        __LocalCommaClassifier, null), "" + ("List"), LBJ$EAR);
                    }
                    LBJava$constraint$result$1 =
                            new FirstOrderImplication(LBJava$constraint$result$2,
                                    LBJava$constraint$result$3);
                }
                LBJava$constraint$result$0 =
                        new UniversalQuantifier("c", s.getCommas(), LBJava$constraint$result$1);
            }
            __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
        }

        return __result;
    }
}
