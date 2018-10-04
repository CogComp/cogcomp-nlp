/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4D81BA02C040144F756B880A07616F63A1809A4301B2B936DBCE5C584EE46F63443F5F6419067F891EDC43C9C9092DA0E6C1DABE03526FC42634CE3BC67BEC3684ACA3050507B18DF616C82D4115A7BE905701A460E8217C0977342529A05FE374C9AE951A0E8A6541530D2E44AA4429EF4CC1F1CA8D1F48AC13818387FCFCF41F38B696EBF7722DB06BB079FAED0457EAD61BC2DB13F9702198C000000

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


public class bio_classifier_nom$$1 extends Classifier
{
  private static final wordForm_features __wordForm_features = new wordForm_features();
  private static final BrownClusterPaths __BrownClusterPaths = new BrownClusterPaths();
  private static final isSentenceStart __isSentenceStart = new isSentenceStart();
  private static final gazetteers_features __gazetteers_features = new gazetteers_features();
  private static final Capitalization __Capitalization = new Capitalization();
  private static final Affixes __Affixes = new Affixes();
  private static final AffixesZH __AffixesZH = new AffixesZH();
  private static final wordNetTag __wordNetTag = new wordNetTag();
  private static final wordNetHym __wordNetHym = new wordNetHym();
  private static final POS_Features __POS_Features = new POS_Features();

  public bio_classifier_nom$$1()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "bio_classifier_nom$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'bio_classifier_nom$$1(Constituent)' defined on line 218 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__wordForm_features.classify(__example));
    __result.addFeatures(__BrownClusterPaths.classify(__example));
    __result.addFeatures(__isSentenceStart.classify(__example));
    __result.addFeatures(__gazetteers_features.classify(__example));
    __result.addFeatures(__Capitalization.classify(__example));
    __result.addFeatures(__Affixes.classify(__example));
    __result.addFeatures(__AffixesZH.classify(__example));
    __result.addFeatures(__wordNetTag.classify(__example));
    __result.addFeatures(__wordNetHym.classify(__example));
    __result.addFeatures(__POS_Features.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'bio_classifier_nom$$1(Constituent)' defined on line 218 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "bio_classifier_nom$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof bio_classifier_nom$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__wordForm_features);
    result.add(__BrownClusterPaths);
    result.add(__isSentenceStart);
    result.add(__gazetteers_features);
    result.add(__Capitalization);
    result.add(__Affixes);
    result.add(__AffixesZH);
    result.add(__wordNetTag);
    result.add(__wordNetHym);
    result.add(__POS_Features);
    return result;
  }
}

