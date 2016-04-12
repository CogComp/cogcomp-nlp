// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000057D813E02C030140FB2B4AA8B02F100A1A64A021EC30C8371365CA33A336C502EFE83215482827533A9D1D72B24F0240375A0D33D196A82434BA3EC4C82A06D8DB1D88DF61F28F90D69EEE24F4F1372C6E00EC128AF2937F71D6C93291876E858B3F94B81FCE9DD2A4DB9C2C8A6D969FA5584DE0FE51EF98CF1DA96AE8F013CA4A68DB000000

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
  * Produces the POS tag label of the word one before the target word.  During
  * the training of {@link POSTaggerUnknown}, these labels are present in the
  * data, so the value of {@link POSLabel} is simply returned.  Otherwise, the
  * prediction made by {@link POSTaggerUnknown} is returned.
  *
  * @author Nick Rizzolo
 **/
public class labelOneBeforeU extends Classifier
{
  private static final POSLabel __POSLabel = new POSLabel();
  private static final POSTaggerUnknown __POSTaggerUnknown = new POSTaggerUnknown();

  private static ThreadLocal __cache = new ThreadLocal(){ };
  private static ThreadLocal __exampleCache = new ThreadLocal(){ };
  public static void clearCache() { __exampleCache = new ThreadLocal(){ }; }

  public labelOneBeforeU()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "labelOneBeforeU";
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
      System.err.println("Classifier 'labelOneBeforeU(Token)' defined on line 67 of POSUnknown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelOneBeforeU(Token)' defined on line 67 of POSUnknown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelOneBeforeU(Token)' defined on line 67 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return cachedFeatureValue(__example).getStringValue();
  }

  private String _discreteValue(Object __example)
  {
    Token w = (Token) __example;

    if (w.previous != null)
    {
      if (__POSTaggerUnknown.isTraining)
      {
        return "" + (__POSLabel.discreteValue(w.previous));
      }
      return "" + (__POSTaggerUnknown.discreteValue(w.previous));
    }
    return "";
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'labelOneBeforeU(Token)' defined on line 67 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "labelOneBeforeU".hashCode(); }
  public boolean equals(Object o) { return o instanceof labelOneBeforeU; }
}

