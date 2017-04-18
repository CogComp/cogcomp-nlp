/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B4ECFCB2E292A4CCCCB2150FFA84BCF2A417ECFCDCD44768986A6A876E5A5A615A6E527AAA417962565A6279494EB6407A6E59084841A85351AA51C12F11A153A853DA51A610C61E952455000000

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


public class OxfordCommaConstrainedInference$subjectto extends ParameterizedConstraint {
    private static final oxfordComma __oxfordComma = new oxfordComma();

    public OxfordCommaConstrainedInference$subjectto() {
        super("edu.illinois.cs.cogcomp.comma.lbj.OxfordCommaConstrainedInference$subjectto");
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.comma.datastructures.Sentence";
    }

    public String discreteValue(Object __example) {
        if (!(__example instanceof Sentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'OxfordCommaConstrainedInference$subjectto(Sentence)' defined on line 256 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Sentence s = (Sentence) __example;

        {
            boolean LBJava$constraint$result$0;
            LBJava$constraint$result$0 = __oxfordComma.discreteValue(s).equals("true");
            if (!LBJava$constraint$result$0)
                return "false";
        }

        return "true";
    }

    public FeatureVector[] classify(Object[] examples) {
        if (!(examples instanceof Sentence[])) {
            String type = examples == null ? "null" : examples.getClass().getName();
            System.err
                    .println("Classifier 'OxfordCommaConstrainedInference$subjectto(Sentence)' defined on line 256 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        return super.classify(examples);
    }

    public int hashCode() {
        return "OxfordCommaConstrainedInference$subjectto".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof OxfordCommaConstrainedInference$subjectto;
    }

    public FirstOrderConstraint makeConstraint(Object __example) {
        if (!(__example instanceof Sentence)) {
            String type = __example == null ? "null" : __example.getClass().getName();
            System.err
                    .println("Constraint 'OxfordCommaConstrainedInference$subjectto(Sentence)' defined on line 256 of CommaClassifier.lbj received '"
                            + type + "' as input.");
            new Exception().printStackTrace();
            System.exit(1);
        }

        Sentence s = (Sentence) __example;
        FirstOrderConstraint __result = new FirstOrderConstant(true);

        {
            FirstOrderConstraint LBJava$constraint$result$0 = null;
            LBJava$constraint$result$0 = __oxfordComma.makeConstraint(s);
            __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
        }

        return __result;
    }
}
