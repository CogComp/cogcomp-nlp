// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000057D813E02C030140FB2B4AA8B02F100A1A5092841F70C83713616D91D913E201F77C1982CD05EA6643B3BF4658E94806EA41646A32D2158A7D1FE4C82A06D8DB1DC8DF61F68F50D791E124F2F1372C6E00EC128AF19BC83963EC19C98361E1C72D26C3B767BA05F32B03AA37E5F8A9C8AD1E3D0E621F74AEABA3EBE29FDFEC8B000000

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
  * the training of {@link POSTaggerKnown}, these labels are present in the
  * data, so the value of {@link POSLabel} is simply returned.  Otherwise, the
  * prediction made by {@link POSTaggerKnown} is returned.
  *
  * @author Nick Rizzolo
 **/
public class labelOneBefore extends Classifier
{
  private static final POSLabel __POSLabel = new POSLabel();
  private static final POSTaggerKnown __POSTaggerKnown = new POSTaggerKnown();

  private static ThreadLocal __cache = new ThreadLocal(){ };
  private static ThreadLocal __exampleCache = new ThreadLocal(){ };
  public static void clearCache() { __exampleCache = new ThreadLocal(){ }; }

  public labelOneBefore()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "labelOneBefore";
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
      System.err.println("Classifier 'labelOneBefore(Token)' defined on line 92 of POSKnown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelOneBefore(Token)' defined on line 92 of POSKnown.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'labelOneBefore(Token)' defined on line 92 of POSKnown.lbj received '" + type + "' as input.");
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
      if (__POSTaggerKnown.isTraining)
      {
        return "" + (__POSLabel.discreteValue(w.previous));
      }
      return "" + (__POSTaggerKnown.discreteValue(w.previous));
    }
    return "";
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'labelOneBefore(Token)' defined on line 92 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "labelOneBefore".hashCode(); }
  public boolean equals(Object o) { return o instanceof labelOneBefore; }
}

