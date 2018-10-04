/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D7DC13B02C04C0681EFB2F1D9EE61DDB2AB47DA258EAE27E53D2184FE0E276114CFFE69AD1448A3684ED72E360DC9C178C098ED56E98A17C9ECCD572455C1747A6A5A09928728A5C31D7C4E440BCE0E1C106477305EF42D2FD483C0B696D2C4D33B2BCC598355EE99291F6156982AE5F56183E963E271BF24F8CFD2FCDFF207DD36BBF621D4C1FEAFFD0B6F087DB10DDFB9D2A11100000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;


public class locativePairMiddleCommas extends ParameterizedConstraint {
    private static final LocalCommaClassifier __LocalCommaClassifier = new LocalCommaClassifier();

    public locativePairMiddleCommas() {
        super("edu.illinois.cs.cogcomp.comma.lbj.locativePairMiddleCommas");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'locativePairMiddleCommas(CommaSRLSentence)' defined on line 220 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        CommaSRLSentence s = (CommaSRLSentence) __example;

        {
            boolean LBJava$constraint$result$0;
            {
                LBJava$constraint$result$0 = true;
                for (java.util.Iterator __I0 = (s.getMiddleSiblingCommas()).iterator(); __I0
                        .hasNext() && LBJava$constraint$result$0;) {
                    Comma c = (Comma) __I0.next();
                    {
                        boolean LBJava$constraint$result$1;
                        LBJava$constraint$result$1 =
                                ("" + (__LocalCommaClassifier.discreteValue(c))).equals(""
                                        + ("Locative"));
                        if (LBJava$constraint$result$1) {
                            boolean LBJava$constraint$result$2;
                            LBJava$constraint$result$2 =
                                    ("" + (__LocalCommaClassifier.discreteValue(s
                                            .getNextSiblingComma(c)))).equals("" + ("Locative"));
                            if (!LBJava$constraint$result$2)
                                LBJava$constraint$result$0 =
                                        ("" + (__LocalCommaClassifier.discreteValue(s
                                                .getPreviousSiblingComma(c)))).equals(""
                                                + ("Locative"));
                            else
                                LBJava$constraint$result$0 = true;
                        } else
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
                    .println("Classifier 'locativePairMiddleCommas(CommaSRLSentence)' defined on line 220 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "locativePairMiddleCommas".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof locativePairMiddleCommas;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'locativePairMiddleCommas(CommaSRLSentence)' defined on line 220 of CommaClassifier.lbj received '"
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
                                    public Object getLeftObject() {
                                        Comma c = (Comma) quantificationVariables.get(0);
                                        return c;
                                    }
                                };
                        LBJava$constraint$result$2 =
                                new FirstOrderEqualityWithValue(true, new FirstOrderVariable(
                                        __LocalCommaClassifier, null), "" + ("Locative"), LBJ$EAR);
                    }
                    FirstOrderConstraint LBJava$constraint$result$3 = null;
                    {
                        FirstOrderConstraint LBJava$constraint$result$4 = null;
                        {
                            EqualityArgumentReplacer LBJ$EAR =
                                    new EqualityArgumentReplacer(LBJ$constraint$context, true) {
                                        public Object getLeftObject() {
                                            CommaSRLSentence s = (CommaSRLSentence) context[0];
                                            Comma c = (Comma) quantificationVariables.get(0);
                                            return s.getNextSiblingComma(c);
                                        }
                                    };
                            LBJava$constraint$result$4 =
                                    new FirstOrderEqualityWithValue(true, new FirstOrderVariable(
                                            __LocalCommaClassifier, null), "" + ("Locative"),
                                            LBJ$EAR);
                        }
                        FirstOrderConstraint LBJava$constraint$result$5 = null;
                        {
                            EqualityArgumentReplacer LBJ$EAR =
                                    new EqualityArgumentReplacer(LBJ$constraint$context, true) {
                                        public Object getLeftObject() {
                                            CommaSRLSentence s = (CommaSRLSentence) context[0];
                                            Comma c = (Comma) quantificationVariables.get(0);
                                            return s.getPreviousSiblingComma(c);
                                        }
                                    };
                            LBJava$constraint$result$5 =
                                    new FirstOrderEqualityWithValue(true, new FirstOrderVariable(
                                            __LocalCommaClassifier, null), "" + ("Locative"),
                                            LBJ$EAR);
                        }
                        LBJava$constraint$result$3 =
                                new FirstOrderDisjunction(LBJava$constraint$result$4,
                                        LBJava$constraint$result$5);
                    }
                    LBJava$constraint$result$1 =
                            new FirstOrderImplication(LBJava$constraint$result$2,
                                    LBJava$constraint$result$3);
                }
                LBJava$constraint$result$0 =
                        new UniversalQuantifier("c", s.getMiddleSiblingCommas(),
                                LBJava$constraint$result$1);
            }
            __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
        }

        return __result;
    }
}
