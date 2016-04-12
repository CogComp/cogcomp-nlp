// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056CC13E02C0301441DBAC69BB5427108A1922A0090370072E1C2B20B696DE0E201777CD5A0A89AE96E7845613450D5F2737E36481BBCB249A90D170AF05A701FCEB0E9941ECB544D1BF62D25DBE206913FC506F640E69D2C92BDB8B9884F76FCE66AB78FCA9B967C30D7FFCEEABEEAF3445BD5C449000000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.*;


/**
  * After {@link POSTaggerKnown} and {@link POSTaggerUnknown} are trained,
  * this classifier will return the prediction of {@link POSTaggerKnown} if
  * the input word was observed during training or of {@link POSTaggerUnknown}
  * if it wasn't.
  *
  * @author Nick Rizzolo
 **/
public class POSTagger extends Classifier
{
  private static final POSTaggerKnown __POSTaggerKnown = new POSTaggerKnown();
  private static final POSTaggerUnknown __POSTaggerUnknown = new POSTaggerUnknown();
  private static final wordForm __wordForm = new wordForm();

  public POSTagger()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "POSTagger";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete"; }


  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    String result = discreteValue(__example);
    return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result), (short) allowableValues().length);
  }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POSTagger(Token)' defined on line 18 of POS.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token w = (Token) __example;

    if (baselineTarget.getInstance().observed(__wordForm.discreteValue(w)))
    {
      return "" + (__POSTaggerKnown.discreteValue(w));
    }
    return "" + (__POSTaggerUnknown.discreteValue(w));
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'POSTagger(Token)' defined on line 18 of POS.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "POSTagger".hashCode(); }
  public boolean equals(Object o) { return o instanceof POSTagger; }
}

