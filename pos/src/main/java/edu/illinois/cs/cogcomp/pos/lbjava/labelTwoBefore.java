// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000058E814A02C034144FA2367152958DB08AB17BA0A06E2013DFD81C0F352136612EDDD4140B0A0E2766EDC03DBB4622DD80E5F99CBA216B4348842458B213A8481D6E24D36DB4C30E60882DD819EEE24E485C60C9DB74BD2E3EE75C2FD5C3E1E4A4B5B4177C1A077E29A8AD1B36B3105F24E8C8ACCE7AF12E7CE9C51E933E66B5FF8E6A9A2E5D4A06EB09E000000

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
  * the training of {@link POSTaggerKnown}, these labels are present in the
  * data, so the value of {@link POSLabel} is simply returned.  Otherwise, the
  * prediction made by {@link POSTaggerKnown} is returned.
  *
  * @author Nick Rizzolo
 **/
public class labelTwoBefore extends Classifier
{
  private static final POSTaggerKnown __POSTaggerKnown = new POSTaggerKnown();
  private static final POSLabel __POSLabel = new POSLabel();

  private static ThreadLocal __cache = new ThreadLocal(){ };
  private static ThreadLocal __exampleCache = new ThreadLocal(){ };
  public static void clearCache() { __exampleCache = new ThreadLocal(){ }; }

  public labelTwoBefore()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "labelTwoBefore";
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
      System.err.println("Classifier 'labelTwoBefore(Token)' defined on line 73 of POSKnown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelTwoBefore(Token)' defined on line 73 of POSKnown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelTwoBefore(Token)' defined on line 73 of POSKnown.lbj received '" + type + "' as input.");
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
      if (__POSTaggerKnown.isTraining)
      {
        return "" + (__POSLabel.discreteValue(w.previous.previous));
      }
      return "" + (__POSTaggerKnown.discreteValue(w.previous.previous));
    }
    return "";
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'labelTwoBefore(Token)' defined on line 73 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "labelTwoBefore".hashCode(); }
  public boolean equals(Object o) { return o instanceof labelTwoBefore; }
}

