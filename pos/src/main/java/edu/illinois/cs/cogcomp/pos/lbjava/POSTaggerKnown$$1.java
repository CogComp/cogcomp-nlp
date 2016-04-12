// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5AC14A02C0301641EBACC2A2824D54AB673AB07360A289B0C4CEFD05C83303D04EAF648280EAE3E1CB9695F96828D1DD7C740E4906731DA2D57E6F14F501AA70A3D19AAAD4755B77F419754E54018D21A4F49932278AA71CCA68DA741CF7F97E20BFDEB5AF12A771B9EA9CF59DF0DCF00CC6BF8F189000000

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


public class POSTaggerKnown$$1 extends Classifier
{
  private static final wordForm __wordForm = new wordForm();
  private static final baselineTarget __baselineTarget = new baselineTarget();
  private static final labelTwoBefore __labelTwoBefore = new labelTwoBefore();
  private static final labelOneBefore __labelOneBefore = new labelOneBefore();
  private static final labelOneAfter __labelOneAfter = new labelOneAfter();
  private static final labelTwoAfter __labelTwoAfter = new labelTwoAfter();
  private static final L2bL1b __L2bL1b = new L2bL1b();
  private static final L1bL1a __L1bL1a = new L1bL1a();
  private static final L1aL2a __L1aL2a = new L1aL2a();

  public POSTaggerKnown$$1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "POSTaggerKnown$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POSTaggerKnown$$1(Token)' defined on line 165 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeature(__wordForm.featureValue(__example));
    __result.addFeature(__baselineTarget.featureValue(__example));
    __result.addFeature(__labelTwoBefore.featureValue(__example));
    __result.addFeature(__labelOneBefore.featureValue(__example));
    __result.addFeature(__labelOneAfter.featureValue(__example));
    __result.addFeature(__labelTwoAfter.featureValue(__example));
    __result.addFeature(__L2bL1b.featureValue(__example));
    __result.addFeature(__L1bL1a.featureValue(__example));
    __result.addFeature(__L1aL2a.featureValue(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'POSTaggerKnown$$1(Token)' defined on line 165 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "POSTaggerKnown$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof POSTaggerKnown$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__wordForm);
    result.add(__baselineTarget);
    result.add(__labelTwoBefore);
    result.add(__labelOneBefore);
    result.add(__labelOneAfter);
    result.add(__labelTwoAfter);
    result.add(__L2bL1b);
    result.add(__L1bL1a);
    result.add(__L1aL2a);
    return result;
  }
}

