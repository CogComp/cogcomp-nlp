// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000058E814A02C034144FA2367152958DB08AB17D28289E1026AFB5343CF494C8958877735014541C5ECCCB916A3B1D40AB01C9E319359DF69A7F18A51ACF84C8C2164B9335785F21738DE12273350ABA5F92261B10727E057D879BF51BC761F0BF3A2D3C041A5E19D76E6C64514B56BC3CC849719203A05BB9FA88F1B82758BFB17F1B6FF8FAAA22E10B0E4D479EE000000

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
  * Produces the POS tag label of the word two before the target word.  During
  * the training of {@link POSTaggerUnknown}, these labels are present in the
  * data, so the value of {@link POSLabel} is simply returned.  Otherwise, the
  * prediction made by {@link POSTaggerUnknown} is returned.
  *
  * @author Nick Rizzolo
 **/
public class labelTwoBeforeU extends Classifier
{
  private static final POSLabel __POSLabel = new POSLabel();
  private static final POSTaggerUnknown __POSTaggerUnknown = new POSTaggerUnknown();

  private static ThreadLocal __cache = new ThreadLocal(){ };
  private static ThreadLocal __exampleCache = new ThreadLocal(){ };
  public static void clearCache() { __exampleCache = new ThreadLocal(){ }; }

  public labelTwoBeforeU()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "labelTwoBeforeU";
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
      System.err.println("Classifier 'labelTwoBeforeU(Token)' defined on line 48 of POSUnknown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelTwoBeforeU(Token)' defined on line 48 of POSUnknown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelTwoBeforeU(Token)' defined on line 48 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return cachedFeatureValue(__example).getStringValue();
  }

  private String _discreteValue(Object __example)
  {
    Token w = (Token) __example;

    if (w.previous != null && w.previous.previous != null)
    {
      if (__POSTaggerUnknown.isTraining)
      {
        return "" + (__POSLabel.discreteValue(w.previous.previous));
      }
      return "" + (__POSTaggerUnknown.discreteValue(w.previous.previous));
    }
    return "";
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'labelTwoBeforeU(Token)' defined on line 48 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "labelTwoBeforeU".hashCode(); }
  public boolean equals(Object o) { return o instanceof labelTwoBeforeU; }
}

