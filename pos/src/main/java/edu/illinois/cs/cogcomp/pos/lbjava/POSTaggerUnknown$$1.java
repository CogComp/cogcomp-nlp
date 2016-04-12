// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056CC1BA02C0381400E759F78A0A017847671D1A351A289E30CF1B7905A5318F3921F1FD6541707BFEE0EE6813DD5091B3AB67733CED34AF03588524559EDB9831205930D9E8452AC0D4497822BC903F810685C32B2A99D266352E50E2A0AFF41D50CF5176797DFFFD2EB9BDAD6BADE60DB28FD0E6BED09617E6C7630ECB80292A7105F3FAAE71B000000

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


public class POSTaggerUnknown$$1 extends Classifier
{
  private static final wordForm __wordForm = new wordForm();
  private static final baselineTarget __baselineTarget = new baselineTarget();
  private static final labelTwoBeforeU __labelTwoBeforeU = new labelTwoBeforeU();
  private static final labelOneBeforeU __labelOneBeforeU = new labelOneBeforeU();
  private static final labelOneAfterU __labelOneAfterU = new labelOneAfterU();
  private static final labelTwoAfterU __labelTwoAfterU = new labelTwoAfterU();
  private static final L2bL1bU __L2bL1bU = new L2bL1bU();
  private static final L1bL1aU __L1bL1aU = new L1bL1aU();
  private static final L1aL2aU __L1aL2aU = new L1aL2aU();
  private static final suffixFeatures __suffixFeatures = new suffixFeatures();

  public POSTaggerUnknown$$1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "POSTaggerUnknown$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POSTaggerUnknown$$1(Token)' defined on line 170 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeature(__wordForm.featureValue(__example));
    __result.addFeature(__baselineTarget.featureValue(__example));
    __result.addFeature(__labelTwoBeforeU.featureValue(__example));
    __result.addFeature(__labelOneBeforeU.featureValue(__example));
    __result.addFeature(__labelOneAfterU.featureValue(__example));
    __result.addFeature(__labelTwoAfterU.featureValue(__example));
    __result.addFeature(__L2bL1bU.featureValue(__example));
    __result.addFeature(__L1bL1aU.featureValue(__example));
    __result.addFeature(__L1aL2aU.featureValue(__example));
    __result.addFeatures(__suffixFeatures.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'POSTaggerUnknown$$1(Token)' defined on line 170 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "POSTaggerUnknown$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof POSTaggerUnknown$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__wordForm);
    result.add(__baselineTarget);
    result.add(__labelTwoBeforeU);
    result.add(__labelOneBeforeU);
    result.add(__labelOneAfterU);
    result.add(__labelTwoAfterU);
    result.add(__L2bL1bU);
    result.add(__L1bL1aU);
    result.add(__L1aL2aU);
    result.add(__suffixFeatures);
    return result;
  }
}

