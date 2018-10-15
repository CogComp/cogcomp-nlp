/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D3C81BA02C04C004F752345058A38BB3958283958B283E17E537818A94942741FFEDB9C5F1FEDB58C2B2A3E10E54213FA9CC8A01A6CD45ABEE27C148DC9CB22B34E31C5FC0BB8E23A8E7261C4E551DA78B9ACEC3CA5DC157A4EFE686A74883EF5876B66EFE6877E2DAC4E42CD309586754EC81C39A7F0F30097C6108C8000000

package org.cogcomp.md.LbjGen;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;
import org.cogcomp.md.*;


public class bio_classifier_pro$$1 extends Classifier
{
  private static final wordForm_features __wordForm_features = new wordForm_features();
  private static final BrownClusterPaths __BrownClusterPaths = new BrownClusterPaths();
  private static final POS_Features __POS_Features = new POS_Features();
  private static final WordTypeInformation __WordTypeInformation = new WordTypeInformation();
  private static final isSentenceStart __isSentenceStart = new isSentenceStart();

  public bio_classifier_pro$$1()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "bio_classifier_pro$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'bio_classifier_pro$$1(Constituent)' defined on line 231 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__wordForm_features.classify(__example));
    __result.addFeatures(__BrownClusterPaths.classify(__example));
    __result.addFeatures(__POS_Features.classify(__example));
    __result.addFeatures(__WordTypeInformation.classify(__example));
    __result.addFeatures(__isSentenceStart.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'bio_classifier_pro$$1(Constituent)' defined on line 231 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "bio_classifier_pro$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof bio_classifier_pro$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__wordForm_features);
    result.add(__BrownClusterPaths);
    result.add(__POS_Features);
    result.add(__WordTypeInformation);
    result.add(__isSentenceStart);
    return result;
  }
}

