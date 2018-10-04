/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B4ECFCB2E292A4CCCCB2158C9CF4E4C29CC2B4D084CCC22D80E4DCB294DCB4E455826D458A6507046937BCC2A2E217ECFCDCD44D00A4AE7C0AACAF666A4A4E4A285AB8182F6DA05B009759B23E36000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;


public class locativePair extends ParameterizedConstraint {
    private static final locativePairFirstComma __locativePairFirstComma =
            new locativePairFirstComma();
    private static final locativePairMiddleCommas __locativePairMiddleCommas =
            new locativePairMiddleCommas();

    public locativePair() {
        super("edu.illinois.cs.cogcomp.comma.lbj.locativePair");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'locativePair(CommaSRLSentence)' defined on line 229 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        CommaSRLSentence s = (CommaSRLSentence) __example;

        {
            boolean LBJava$constraint$result$0;
            {
                boolean LBJava$constraint$result$1;
                LBJava$constraint$result$1 =
                        __locativePairFirstComma.discreteValue(s).equals("true");
                if (LBJava$constraint$result$1)
                    LBJava$constraint$result$0 =
                            __locativePairMiddleCommas.discreteValue(s).equals("true");
                else
                    LBJava$constraint$result$0 = false;
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
                    .println("Classifier 'locativePair(CommaSRLSentence)' defined on line 229 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "locativePair".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof locativePair;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof CommaSRLSentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'locativePair(CommaSRLSentence)' defined on line 229 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        CommaSRLSentence s = (CommaSRLSentence) __example;
        FirstOrderConstraint __result = new FirstOrderConstant(true);

        {
            FirstOrderConstraint LBJava$constraint$result$0 = null;
            {
                FirstOrderConstraint LBJava$constraint$result$1 = null;
                LBJava$constraint$result$1 = __locativePairFirstComma.makeConstraint(s);
                FirstOrderConstraint LBJava$constraint$result$2 = null;
                LBJava$constraint$result$2 = __locativePairMiddleCommas.makeConstraint(s);
                LBJava$constraint$result$0 =
                        new FirstOrderConjunction(LBJava$constraint$result$1,
                                LBJava$constraint$result$2);
            }
            __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
        }

        return __result;
    }
}
