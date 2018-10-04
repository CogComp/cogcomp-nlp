/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6D81CA02C030150F75E1D35271F30A2A0250F45A80D387E53C6BD584318CE22288FFE690E541FC333C48C94DA094234CC18C4EEC712927692A6D5E5752732723E4181AE1F4C49B05C88A8010290E47733B5D0649B54943758A7D542C27A2C346BE94DC97F07DF6B88517719455621E2E2874BD2A9EF3BF607832EFA857430F3EBE3B5EF3DBFD3E5F67C55AC197D000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;


public class locativePairFirstComma extends ParameterizedConstraint {
    private static final LocalCommaClassifier __LocalCommaClassifier = new LocalCommaClassifier();

    public locativePairFirstComma() {
        super("edu.illinois.cs.cogcomp.comma.lbj.locativePairFirstComma");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'locativePairFirstComma(CommaSRLSentence)' defined on line 214 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        CommaSRLSentence s = (CommaSRLSentence) __example;

        {
            boolean LBJava$constraint$result$0;
            {
                LBJava$constraint$result$0 = true;
                for (java.util.Iterator __I0 =
                        (s.getFirstSiblingCommasWhichAreNotLast()).iterator(); __I0.hasNext()
                        && LBJava$constraint$result$0;) {
                    Comma c = (Comma) __I0.next();
                    {
                        boolean LBJava$constraint$result$1;
                        LBJava$constraint$result$1 =
                                ("" + (__LocalCommaClassifier.discreteValue(c))).equals(""
                                        + ("Locative"));
                        if (LBJava$constraint$result$1)
                            LBJava$constraint$result$0 =
                                    ("" + (__LocalCommaClassifier.discreteValue(s
                                            .getNextSiblingComma(c)))).equals("" + ("Locative"));
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
                    .println("Classifier 'locativePairFirstComma(CommaSRLSentence)' defined on line 214 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "locativePairFirstComma".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof locativePairFirstComma;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'locativePairFirstComma(CommaSRLSentence)' defined on line 214 of CommaClassifier.lbj received '"
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
                        EqualityArgumentReplacer LBJ$EAR =
                                new EqualityArgumentReplacer(LBJ$constraint$context, true) {
                                    public Object getLeftObject() {
                                        CommaSRLSentence s = (CommaSRLSentence) context[0];
                                        Comma c = (Comma) quantificationVariables.get(0);
                                        return s.getNextSiblingComma(c);
                                    }
                                };
                        LBJava$constraint$result$3 =
                                new FirstOrderEqualityWithValue(true, new FirstOrderVariable(
                                        __LocalCommaClassifier, null), "" + ("Locative"), LBJ$EAR);
                    }
                    LBJava$constraint$result$1 =
                            new FirstOrderImplication(LBJava$constraint$result$2,
                                    LBJava$constraint$result$3);
                }
                LBJava$constraint$result$0 =
                        new UniversalQuantifier("c", s.getFirstSiblingCommasWhichAreNotLast(),
                                LBJava$constraint$result$1);
            }
            __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
        }

        return __result;
    }
}
