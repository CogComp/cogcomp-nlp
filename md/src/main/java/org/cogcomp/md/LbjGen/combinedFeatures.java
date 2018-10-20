/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D294555584ECFCD4ACCCB4D417B4D4C292D2A4D26D80A4DC94C29CCCFC35822D450B1D5C097515130D1C228688D40D80B90A13631431C6286A8D40DC47410052EABF80DB000000

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


public class combinedFeatures extends Classifier
{
  private static final combinedFeatures$$0 __combinedFeatures$$0 = new combinedFeatures$$0();
  private static final combinedFeatures$$1 __combinedFeatures$$1 = new combinedFeatures$$1();
  private static final combinedFeatures$$2 __combinedFeatures$$2 = new combinedFeatures$$2();
  private static final combinedFeatures$$3 __combinedFeatures$$3 = new combinedFeatures$$3();
  private static final combinedFeatures$$4 __combinedFeatures$$4 = new combinedFeatures$$4();
  private static final combinedFeatures$$5 __combinedFeatures$$5 = new combinedFeatures$$5();
  private static final combinedFeatures$$6 __combinedFeatures$$6 = new combinedFeatures$$6();

  public combinedFeatures()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "combinedFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'combinedFeatures(Relation)' defined on line 237 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__combinedFeatures$$0.classify(__example));
    __result.addFeatures(__combinedFeatures$$1.classify(__example));
    __result.addFeatures(__combinedFeatures$$2.classify(__example));
    __result.addFeatures(__combinedFeatures$$3.classify(__example));
    __result.addFeatures(__combinedFeatures$$4.classify(__example));
    __result.addFeatures(__combinedFeatures$$5.classify(__example));
    __result.addFeatures(__combinedFeatures$$6.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'combinedFeatures(Relation)' defined on line 237 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "combinedFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof combinedFeatures; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__combinedFeatures$$0);
    result.add(__combinedFeatures$$1);
    result.add(__combinedFeatures$$2);
    result.add(__combinedFeatures$$3);
    result.add(__combinedFeatures$$4);
    result.add(__combinedFeatures$$5);
    result.add(__combinedFeatures$$6);
    return result;
  }
}

