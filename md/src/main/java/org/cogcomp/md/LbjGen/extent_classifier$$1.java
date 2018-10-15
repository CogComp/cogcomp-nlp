/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D294555584DA8294DCB298F4EC94C2E2ECC4BCC4D22515134D80A4DC94C29CCCFC35822D450B1D558C84D4C417B4D4C292D2A4D26D18AE040F3925B4AC35353F01209C9F9B9499979A84A500C9C6394CF6000000

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


public class extent_classifier$$1 extends Classifier
{
  private static final headFeatures __headFeatures = new headFeatures();
  private static final extentFeatures __extentFeatures = new extentFeatures();
  private static final betweenFeatures __betweenFeatures = new betweenFeatures();
  private static final combinedFeatures __combinedFeatures = new combinedFeatures();

  public extent_classifier$$1()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "extent_classifier$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'extent_classifier$$1(Relation)' defined on line 243 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__headFeatures.classify(__example));
    __result.addFeatures(__extentFeatures.classify(__example));
    __result.addFeatures(__betweenFeatures.classify(__example));
    __result.addFeatures(__combinedFeatures.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'extent_classifier$$1(Relation)' defined on line 243 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "extent_classifier$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof extent_classifier$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__headFeatures);
    result.add(__extentFeatures);
    result.add(__betweenFeatures);
    result.add(__combinedFeatures);
    return result;
  }
}

