/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D294555584DA8294DCB217B4D4C292D2A4D26D80A4DC94C29CCCFC35822D450B1D5582FCF2A417BCF2AC5750BA2D1584F4CAA4D29294D4D2A2689E089C4830556845614AA76E5A105D38DC0894508F7032BA2FB4D29094C474311F8A48BD1EC98509952989399558268000DC833D692B000000

package org.cogcomp.md.LbjGen;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.MyString;
import java.util.*;
import org.cogcomp.md.*;


public class extentFeatures extends Classifier
{
  private static final wordFormExtent __wordFormExtent = new wordFormExtent();
  private static final gazetteersFeaturesExtent __gazetteersFeaturesExtent = new gazetteersFeaturesExtent();
  private static final WordTypeInformationExtent __WordTypeInformationExtent = new WordTypeInformationExtent();
  private static final POSExtent __POSExtent = new POSExtent();
  private static final WordNetTagExtent __WordNetTagExtent = new WordNetTagExtent();
  private static final WordNetHymExtent __WordNetHymExtent = new WordNetHymExtent();
  private static final CapitalizationExtent __CapitalizationExtent = new CapitalizationExtent();

  public extentFeatures()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "extentFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'extentFeatures(Relation)' defined on line 174 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__wordFormExtent.classify(__example));
    __result.addFeatures(__gazetteersFeaturesExtent.classify(__example));
    __result.addFeatures(__WordTypeInformationExtent.classify(__example));
    __result.addFeatures(__POSExtent.classify(__example));
    __result.addFeatures(__WordNetTagExtent.classify(__example));
    __result.addFeatures(__WordNetHymExtent.classify(__example));
    __result.addFeature(__CapitalizationExtent.featureValue(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'extentFeatures(Relation)' defined on line 174 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "extentFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof extentFeatures; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__wordFormExtent);
    result.add(__gazetteersFeaturesExtent);
    result.add(__WordTypeInformationExtent);
    result.add(__POSExtent);
    result.add(__WordNetTagExtent);
    result.add(__WordNetHymExtent);
    result.add(__CapitalizationExtent);
    return result;
  }
}

