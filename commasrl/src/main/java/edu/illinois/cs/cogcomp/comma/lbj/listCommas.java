// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000057EC1BA02C0401400DF59125DD53ADB2AD4AD8224A5B9373B90B0B9B38BD35401FFDD01D225896766E1C05A8652709850A265AE43E81CC5BC1B07426897CB0D7AC14513770284483BDC0C5E42D57ACDACD452E0F39ED3C9B62150D938A538994F2C9FB4E299F129EE6B44E8624BBD1AA66A30516B75CABF33F3BCAB5F83C1FF36A5EA6F87F708A1D25D37F000000

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


public class listCommas extends ParameterizedConstraint
{
  private static final LocalCommaClassifier __LocalCommaClassifier = new LocalCommaClassifier();

  public listCommas() { super("edu.illinois.cs.cogcomp.comma.lbj.listCommas"); }

  public String getInputType() { return "edu.illinois.cs.cogcomp.comma.datastructures.Sentence"; }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof Sentence))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Constraint 'listCommas(Sentence)' defined on line 244 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Sentence s = (Sentence) __example;

    {
      boolean LBJava$constraint$result$0;
      {
        LBJava$constraint$result$0 = true;
        for (java.util.Iterator __I0 = (s.getMiddleSiblingCommas()).iterator(); __I0.hasNext() && LBJava$constraint$result$0; )
        {
          Comma c = (Comma) __I0.next();
          {
            boolean LBJava$constraint$result$1;
            {
              boolean LBJava$constraint$result$2;
              LBJava$constraint$result$2 = ("" + (__LocalCommaClassifier.discreteValue(s.getPreviousSiblingComma(c)))).equals("" + ("List"));
              if (LBJava$constraint$result$2)
                LBJava$constraint$result$1 = ("" + (__LocalCommaClassifier.discreteValue(s.getNextSiblingComma(c)))).equals("" + ("List"));
              else LBJava$constraint$result$1 = false;
            }
            if (LBJava$constraint$result$1)
              LBJava$constraint$result$0 = ("" + (__LocalCommaClassifier.discreteValue(c))).equals("" + ("List"));
            else LBJava$constraint$result$0 = true;
          }
        }
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
      System.err.println("Classifier 'listCommas(Sentence)' defined on line 244 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "listCommas".hashCode(); }
  public boolean equals(Object o) { return o instanceof listCommas; }

  public FirstOrderConstraint makeConstraint(Object __example)
  {
    if (!(__example instanceof Sentence))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Constraint 'listCommas(Sentence)' defined on line 244 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Sentence s = (Sentence) __example;
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
            FirstOrderConstraint LBJava$constraint$result$3 = null;
            {
              EqualityArgumentReplacer LBJ$EAR =
                new EqualityArgumentReplacer(LBJ$constraint$context, true)
                {
                  public Object getLeftObject()
                  {
                    Sentence s = (Sentence) context[0];
                    Comma c = (Comma) quantificationVariables.get(0);
                    return s.getPreviousSiblingComma(c);
                  }
                };
              LBJava$constraint$result$3 = new FirstOrderEqualityWithValue(true, new FirstOrderVariable(__LocalCommaClassifier, null), "" + ("List"), LBJ$EAR);
            }
            FirstOrderConstraint LBJava$constraint$result$4 = null;
            {
              EqualityArgumentReplacer LBJ$EAR =
                new EqualityArgumentReplacer(LBJ$constraint$context, true)
                {
                  public Object getLeftObject()
                  {
                    Sentence s = (Sentence) context[0];
                    Comma c = (Comma) quantificationVariables.get(0);
                    return s.getNextSiblingComma(c);
                  }
                };
              LBJava$constraint$result$4 = new FirstOrderEqualityWithValue(true, new FirstOrderVariable(__LocalCommaClassifier, null), "" + ("List"), LBJ$EAR);
            }
            LBJava$constraint$result$2 = new FirstOrderConjunction(LBJava$constraint$result$3, LBJava$constraint$result$4);
          }
          FirstOrderConstraint LBJava$constraint$result$5 = null;
          {
            EqualityArgumentReplacer LBJ$EAR =
              new EqualityArgumentReplacer(LBJ$constraint$context, true)
              {
                public Object getLeftObject()
                {
                  Comma c = (Comma) quantificationVariables.get(0);
                  return c;
                }
              };
            LBJava$constraint$result$5 = new FirstOrderEqualityWithValue(true, new FirstOrderVariable(__LocalCommaClassifier, null), "" + ("List"), LBJ$EAR);
          }
          LBJava$constraint$result$1 = new FirstOrderImplication(LBJava$constraint$result$2, LBJava$constraint$result$5);
        }
        LBJava$constraint$result$0 = new UniversalQuantifier("c", s.getMiddleSiblingCommas(), LBJava$constraint$result$1);
      }
      __result = new FirstOrderConjunction(__result, LBJava$constraint$result$0);
    }

    return __result;
  }
}

