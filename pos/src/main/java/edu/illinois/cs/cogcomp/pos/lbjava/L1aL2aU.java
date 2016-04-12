// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// discrete L1aL2aU(Token w) <- labelOneAfterU && labelTwoAfterU

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
  * The classifier conjunction of {@link labelOneAfterU} and
  * {@link labelTwoAfterU}.
  *
  * @author Nick Rizzolo
 **/
public class L1aL2aU extends Classifier
{
  private static final labelOneAfterU left = new labelOneAfterU();
  private static final labelTwoAfterU right = new labelTwoAfterU();

  public L1aL2aU()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "L1aL2aU";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete"; }

  public Feature featureValue(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'L1aL2aU(Token)' defined on line 122 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Feature __result;
    __result = left.featureValue(__example).conjunction(right.featureValue(__example), this);
    return __result;
  }

  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public String discreteValue(Object __example)
  {
    return featureValue(__example).getStringValue();
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'L1aL2aU(Token)' defined on line 122 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "L1aL2aU".hashCode(); }
  public boolean equals(Object o) { return o instanceof L1aL2aU; }
}

