/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B4ECFCB2E292A4CCCCB21582E2D4A2E29CC292D294D084CCC22D80E4DCB294DCB4E455826D458A650D07045967BCC2A2E217ECFCDCD44D00ACBE7C820A9CBF666A4A4E4A285141305586A5B24D2000F04A912EB6000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.comma.readers.CommaParser;
import edu.illinois.cs.cogcomp.comma.readers.PrettyCorpusReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.ArrayList;
import java.util.List;


public class substitutePair extends ParameterizedConstraint {
    private static final substitutePairFirstComma __substitutePairFirstComma =
            new substitutePairFirstComma();
    private static final substitutePairMiddleCommas __substitutePairMiddleCommas =
            new substitutePairMiddleCommas();

    public substitutePair() {
        super("edu.illinois.cs.cogcomp.comma.lbj.substitutePair");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.Sentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof Sentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'substitutePair(Sentence)' defined on line 209 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Sentence s = (Sentence) __example;

        {
            boolean LBJava$constraint$result$0;
            {
                boolean LBJava$constraint$result$1;
                LBJava$constraint$result$1 =
                        __substitutePairFirstComma.discreteValue(s).equals("true");
                if (LBJava$constraint$result$1)
                    LBJava$constraint$result$0 =
                            __substitutePairMiddleCommas.discreteValue(s).equals("true");
                else
                    LBJava$constraint$result$0 = false;
            }
            if (!LBJava$constraint$result$0)
                return "false";
        }

        return "true";
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Sentence[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'substitutePair(Sentence)' defined on line 209 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "substitutePair".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof substitutePair;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof Sentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'substitutePair(Sentence)' defined on line 209 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Sentence s = (Sentence) __example;
        FirstOrderConstraint __result = new FirstOrderConstant(true);

        {
            FirstOrderConstraint LBJava$constraint$result$0 = null;
            {
                FirstOrderConstraint LBJava$constraint$result$1 = null;
                LBJava$constraint$result$1 = __substitutePairFirstComma.makeConstraint(s);
                FirstOrderConstraint LBJava$constraint$result$2 = null;
                LBJava$constraint$result$2 = __substitutePairMiddleCommas.makeConstraint(s);
                LBJava$constraint$result$0 =
                        new FirstOrderConjunction(LBJava$constraint$result$1,
                                LBJava$constraint$result$2);
            }
            __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
        }

        return __result;
    }
}
