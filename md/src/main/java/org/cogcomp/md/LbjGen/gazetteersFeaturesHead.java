/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056DC15B02804C0700FFA2B790EE094A7E4B7902B280A8CF4078E4918C92BB51268FDDBBB2B42A7AD6CE7BFFA427503A0E20A6330A8022BBC0D8CD81DD10D49AEA8D811A6D20B684790F08319318A6220B18DE1FC3D1CEA7163584BC17D82BFF4CDF4A1BE40AA6914195102F7DBA4C794FB971B3A1059744154ADF3BB81268ECB351A3D3B3FF1419779B039DA18ACE30A7F4A1AB0BFC88D98A9DCDD43F7627C2A5B5604E0DA3C75ECA38C406481F9EC7F6D95D1100000

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


public class gazetteersFeaturesHead extends Classifier
{
  public gazetteersFeaturesHead()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "gazetteersFeaturesHead";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'gazetteersFeaturesHead(Relation)' defined on line 53 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getGazetteerFeaturesHead(r);
    for (int i = 0; i < features.size(); i++)
    {
      Pair p = (Pair) features.get(i);
      String idx = (String) p.getFirst();
      String val = (String) p.getSecond();
      __id = "" + (idx);
      __value = "" + (val);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'gazetteersFeaturesHead(Relation)' defined on line 53 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "gazetteersFeaturesHead".hashCode(); }
  public boolean equals(Object o) { return o instanceof gazetteersFeaturesHead; }
}

