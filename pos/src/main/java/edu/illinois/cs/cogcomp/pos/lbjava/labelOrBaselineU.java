// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000053CC14A02C0301581EBACB562B0B710D59B61A503D30C43D13D1A16A039A4612EDDD8457BFEDFC73B4986CB3323D4C9B7BB251EC2AC3AB0BDACA8AE1192E2C33E272CB02F08B1AFB70A498D64D557BAAD9490642A2A9C7B4A96F4354BAE6F52D55F76CBFFBE4F3E30952EDF8FE30D0729E1738000000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.*;


/**
  * During the training of {@link POSTaggerUnknown}, return the value of
  * {@link POSLabel}; otherwise, return the value of {@link baselineTarget}.
  *
  * @author Nick Rizzolo
 **/
public class labelOrBaselineU extends Classifier
{
  private static final POSLabel __POSLabel = new POSLabel();
  private static final baselineTarget __baselineTarget = new baselineTarget();

  private static ThreadLocal __cache = new ThreadLocal(){ };
  private static ThreadLocal __exampleCache = new ThreadLocal(){ };
  public static void clearCache() { __exampleCache = new ThreadLocal(){ }; }

  public labelOrBaselineU()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "labelOrBaselineU";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete"; }

  private Feature cachedFeatureValue(Object __example)
  {
    if (__example == __exampleCache.get()) return (Feature) __cache.get();
    __exampleCache.set(__example);
    String __cachedValue = _discreteValue(__example);
    Feature __result = new DiscretePrimitiveStringFeature(containingPackage, name, "", __cachedValue, valueIndexOf(__cachedValue), (short) allowableValues().length);
    __cache.set(__result);
    return __result;
  }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'labelOrBaselineU(Token)' defined on line 34 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return new FeatureVector(cachedFeatureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'labelOrBaselineU(Token)' defined on line 34 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return cachedFeatureValue(__example);
  }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'labelOrBaselineU(Token)' defined on line 34 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return cachedFeatureValue(__example).getStringValue();
  }

  private String _discreteValue(Object __example)
  {
    Token w = (Token) __example;

    if (POSTaggerUnknown.isTraining)
    {
      return "" + (__POSLabel.discreteValue(w));
    }
    return "" + (__baselineTarget.discreteValue(w));
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'labelOrBaselineU(Token)' defined on line 34 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "labelOrBaselineU".hashCode(); }
  public boolean equals(Object o) { return o instanceof labelOrBaselineU; }
}

