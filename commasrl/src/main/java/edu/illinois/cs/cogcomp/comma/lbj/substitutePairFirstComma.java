/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6E81CA02C030144F7568E929B8F1051509287A2224F0E97B16BDE24A90467B0288FFEA4E0288E9FDCCB990939A5129460DD64531BDC8FA425EC254DABCBEA4E60E46C92034D3E189271A81151120421C9EE666BA58146C8296EA05F6B8485E458F29DA72537ED3C5F9305CA8BB84AA23907171C3AD61DC0F930D0E074CFD86D9AB0FDFB69ED28F138FD3E9F2018E4A9E9DD000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;


public class substitutePairFirstComma extends ParameterizedConstraint {
    private static final LocalCommaClassifier __LocalCommaClassifier = new LocalCommaClassifier();

    public substitutePairFirstComma() {
        super("edu.illinois.cs.cogcomp.comma.lbj.substitutePairFirstComma");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'substitutePairFirstComma(CommaSRLSentence)' defined on line 194 of CommaClassifier.lbj received '"
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
                                        + ("Substitute"));
                        if (LBJava$constraint$result$1)
                            LBJava$constraint$result$0 =
                                    ("" + (__LocalCommaClassifier.discreteValue(s
                                            .getNextSiblingComma(c)))).equals("" + ("Substitute"));
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
                    .println("Classifier 'substitutePairFirstComma(CommaSRLSentence)' defined on line 194 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "substitutePairFirstComma".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof substitutePairFirstComma;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'substitutePairFirstComma(CommaSRLSentence)' defined on line 194 of CommaClassifier.lbj received '"
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
                                        __LocalCommaClassifier, null), "" + ("Substitute"), LBJ$EAR);
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
