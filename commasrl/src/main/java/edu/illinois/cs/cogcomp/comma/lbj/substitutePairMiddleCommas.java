/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000058DC13B02C0301500EFB2F8E49C2AB75479EAA258EAE296A7D27096209BB6114CFFEA451114A83E1FEDBFEC7A82A9D17458C4DA82BE4A45B3EC72EEAB0455A174726A1A8A41D314C2E68E356712069C0E1C116463309EB62D07B183E0FE5A5B037C4ED585EEA28311EE992B1F6156982A9FC3D20BF3CA4771DFC475DF6BF92CF2179D26D58A33DCC9629F7A85BB3CDF10D771F1F91100000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;


public class substitutePairMiddleCommas extends ParameterizedConstraint {
    private static final LocalCommaClassifier __LocalCommaClassifier = new LocalCommaClassifier();

    public substitutePairMiddleCommas() {
        super("edu.illinois.cs.cogcomp.comma.lbj.substitutePairMiddleCommas");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'substitutePairMiddleCommas(CommaSRLSentence)' defined on line 200 of CommaClassifier.lbj received '"
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
                                        + ("Substitute"));
                        if (LBJava$constraint$result$1) {
                            boolean LBJava$constraint$result$2;
                            LBJava$constraint$result$2 =
                                    ("" + (__LocalCommaClassifier.discreteValue(s
                                            .getNextSiblingComma(c)))).equals("" + ("Substitute"));
                            if (!LBJava$constraint$result$2)
                                LBJava$constraint$result$0 =
                                        ("" + (__LocalCommaClassifier.discreteValue(s
                                                .getPreviousSiblingComma(c)))).equals(""
                                                + ("Substitute"));
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
                    .println("Classifier 'substitutePairMiddleCommas(CommaSRLSentence)' defined on line 200 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "substitutePairMiddleCommas".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof substitutePairMiddleCommas;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'substitutePairMiddleCommas(CommaSRLSentence)' defined on line 200 of CommaClassifier.lbj received '"
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
                                        __LocalCommaClassifier, null), "" + ("Substitute"), LBJ$EAR);
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
                                            __LocalCommaClassifier, null), "" + ("Substitute"),
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
                                            __LocalCommaClassifier, null), "" + ("Substitute"),
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
