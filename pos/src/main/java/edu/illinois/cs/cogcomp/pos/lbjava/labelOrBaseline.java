// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000053CC14A02C0301581EBACB562B8A710D59BDA0D289B0C43D13E0D03589452B09EDDD8857BFEDFC73B4986CB1323D4C970BB251EC2ACE2CAB0B2AA744A8F4E9179E0F68C30E6C1EE182526BE57DAA72921C8445439F6143AE56A86DCDEBE9BAEFC8DFFBE478D18C21F6FBFE3060AED85A08000000

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
  * During the training of {@link POSTaggerKnown}, return the value of
  * {@link POSLabel}; otherwise, return the value of {@link baselineTarget}.
  *
  * @author Nick Rizzolo
 **/
public class labelOrBaseline extends Classifier
{
  private static final POSLabel __POSLabel = new POSLabel();
  private static final baselineTarget __baselineTarget = new baselineTarget();

  private static ThreadLocal __cache = new ThreadLocal(){ };
  private static ThreadLocal __exampleCache = new ThreadLocal(){ };
  public static void clearCache() { __exampleCache = new ThreadLocal(){ }; }

  public labelOrBaseline()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "labelOrBaseline";
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
      System.err.println("Classifier 'labelOrBaseline(Token)' defined on line 59 of POSKnown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelOrBaseline(Token)' defined on line 59 of POSKnown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelOrBaseline(Token)' defined on line 59 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return cachedFeatureValue(__example).getStringValue();
  }

  private String _discreteValue(Object __example)
  {
    Token w = (Token) __example;

    if (POSTaggerKnown.isTraining)
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
      System.err.println("Classifier 'labelOrBaseline(Token)' defined on line 59 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "labelOrBaseline".hashCode(); }
  public boolean equals(Object o) { return o instanceof labelOrBaseline; }
}

