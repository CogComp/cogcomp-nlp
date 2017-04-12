// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B4ECFCB2E292A4CCCCB2158C9CF4E4C29CC2B4D084CCC22D80E4DCB294DCB4E455826D458A6507046937BCC2A2E217ECFCDCD44D00A4AE7C0AACAF666A4A4E4A285AB8182F6DA05B009759B23E36000000

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


public class locativePair extends ParameterizedConstraint
{
  private static final locativePairFirstComma __locativePairFirstComma = new locativePairFirstComma();
  private static final locativePairMiddleCommas __locativePairMiddleCommas = new locativePairMiddleCommas();

  public locativePair() { super("edu.illinois.cs.cogcomp.comma.lbj.locativePair"); }

  public String getInputType() { return "edu.illinois.cs.cogcomp.comma.datastructures.Sentence"; }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof Sentence))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Constraint 'locativePair(Sentence)' defined on line 229 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Sentence s = (Sentence) __example;

    {
      boolean LBJava$constraint$result$0;
      {
        boolean LBJava$constraint$result$1;
        LBJava$constraint$result$1 = __locativePairFirstComma.discreteValue(s).equals("true");
        if (LBJava$constraint$result$1)
          LBJava$constraint$result$0 = __locativePairMiddleCommas.discreteValue(s).equals("true");
        else LBJava$constraint$result$0 = false;
      }
      if (!LBJava$constraint$result$0) return "false";
    }

    return "true";
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Sentence[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'locativePair(Sentence)' defined on line 229 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "locativePair".hashCode(); }
  public boolean equals(Object o) { return o instanceof locativePair; }

  public FirstOrderConstraint makeConstraint(Object __example)
  {
    if (!(__example instanceof Sentence))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Constraint 'locativePair(Sentence)' defined on line 229 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Sentence s = (Sentence) __example;
    FirstOrderConstraint __result = new FirstOrderConstant(true);

    {
      FirstOrderConstraint LBJava$constraint$result$0 = null;
      {
        FirstOrderConstraint LBJava$constraint$result$1 = null;
        LBJava$constraint$result$1 = __locativePairFirstComma.makeConstraint(s);
        FirstOrderConstraint LBJava$constraint$result$2 = null;
        LBJava$constraint$result$2 = __locativePairMiddleCommas.makeConstraint(s);
        LBJava$constraint$result$0 = new FirstOrderConjunction(LBJava$constraint$result$1, LBJava$constraint$result$2);
      }
      __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
    }

    return __result;
  }
}

