/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4D8DCA02C040138F556E250585FD0CB925A7014151C3F8DD45714BB5662284F9EDF954CED29C790989CB530159C51A1B682F160F9DE17356ACDB8DC569B8F2BD8A3F49D260933ABC686ED9501DE80B22F35A32E9C2AAEC67F90EDD6F014E2A32840CCF774FF5EA3935BF615CDA47802F2BA661AEFC9000000

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


public class headFeatures extends Classifier
{
  private static final headLastWord __headLastWord = new headLastWord();
  private static final beforeHead __beforeHead = new beforeHead();
  private static final afterHead __afterHead = new afterHead();
  private static final headText __headText = new headText();
  private static final headForm __headForm = new headForm();
  private static final afterHeadPOS __afterHeadPOS = new afterHeadPOS();
  private static final gazetteersFeaturesHead __gazetteersFeaturesHead = new gazetteersFeaturesHead();
  private static final headDistance __headDistance = new headDistance();
  private static final headCap __headCap = new headCap();

  public headFeatures()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "headFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'headFeatures(Relation)' defined on line 84 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeature(__headLastWord.featureValue(__example));
    __result.addFeature(__beforeHead.featureValue(__example));
    __result.addFeature(__afterHead.featureValue(__example));
    __result.addFeature(__headText.featureValue(__example));
    __result.addFeatures(__headForm.classify(__example));
    __result.addFeature(__afterHeadPOS.featureValue(__example));
    __result.addFeatures(__gazetteersFeaturesHead.classify(__example));
    __result.addFeatures(__headDistance.classify(__example));
    __result.addFeatures(__headCap.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'headFeatures(Relation)' defined on line 84 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "headFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof headFeatures; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__headLastWord);
    result.add(__beforeHead);
    result.add(__afterHead);
    result.add(__headText);
    result.add(__headForm);
    result.add(__afterHeadPOS);
    result.add(__gazetteersFeaturesHead);
    result.add(__headDistance);
    result.add(__headCap);
    return result;
  }
}

