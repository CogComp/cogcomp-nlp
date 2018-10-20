/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000055D814B028040164FFACC1C0A06B38D9BB4A01901418140D5462D9DA10DD599D13BCF5FDE1A38DDEB70FE1F53C1A6125A50CD9D7557B8120B5629AC1679429E237FE282BE04E41AE51C67D0936FCA8D2F48ACED91870E44AA4421A2B48A3805030C1AC8509BA9A45415303A7966FE5AB993B3B69FD3B17B3818B64DE2F9E9A0763ACFB78C4CF8E2F6780A42764D76C4A71ACA83D19E54D6AF74B130F50F5E4BA863D000000

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


public class bio_classifier_nam$$1 extends Classifier
{
  private static final Capitalization __Capitalization = new Capitalization();
  private static final gazetteers_features __gazetteers_features = new gazetteers_features();
  private static final isSentenceStart __isSentenceStart = new isSentenceStart();
  private static final wordForm_features __wordForm_features = new wordForm_features();
  private static final Affixes __Affixes = new Affixes();
  private static final AffixesZH __AffixesZH = new AffixesZH();
  private static final WordTypeInformation __WordTypeInformation = new WordTypeInformation();
  private static final BrownClusterPaths __BrownClusterPaths = new BrownClusterPaths();
  private static final preBIOLevel1 __preBIOLevel1 = new preBIOLevel1();
  private static final preBIOLevel2 __preBIOLevel2 = new preBIOLevel2();

  public bio_classifier_nam$$1()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "bio_classifier_nam$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'bio_classifier_nam$$1(Constituent)' defined on line 205 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__Capitalization.classify(__example));
    __result.addFeatures(__gazetteers_features.classify(__example));
    __result.addFeatures(__isSentenceStart.classify(__example));
    __result.addFeatures(__wordForm_features.classify(__example));
    __result.addFeatures(__Affixes.classify(__example));
    __result.addFeatures(__AffixesZH.classify(__example));
    __result.addFeatures(__WordTypeInformation.classify(__example));
    __result.addFeatures(__BrownClusterPaths.classify(__example));
    __result.addFeatures(__preBIOLevel1.classify(__example));
    __result.addFeatures(__preBIOLevel2.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'bio_classifier_nam$$1(Constituent)' defined on line 205 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "bio_classifier_nam$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof bio_classifier_nam$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__Capitalization);
    result.add(__gazetteers_features);
    result.add(__isSentenceStart);
    result.add(__wordForm_features);
    result.add(__Affixes);
    result.add(__AffixesZH);
    result.add(__WordTypeInformation);
    result.add(__BrownClusterPaths);
    result.add(__preBIOLevel1);
    result.add(__preBIOLevel2);
    return result;
  }
}

