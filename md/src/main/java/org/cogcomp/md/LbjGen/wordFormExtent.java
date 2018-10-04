/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054D8BCA02C04C054F752B1166015C5BDAB11C280282671EA78636908EC8462E305AFFE62A547597CD393969273C828328B72E6BA4C7E5D3403A8BD3E92805A80CE1AC90CB0634950E8814EAC891610BC5F6BAFE4A71C1A194C3DE05E038668C2BBFC6BE475E60D97F50C131383A8204A29951A5ACF967A99E9A49E27C36FAF3771881E2AC93BECFF9355A323ECC3DE3C885741CE051AB85A511761335D2C41B38B583914FD9606A6C625C6D0AC8133E7443732B08E1AF730143ED185A1100000

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


public class wordFormExtent extends Classifier
{
  public wordFormExtent()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "wordFormExtent";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'wordFormExtent(Relation)' defined on line 87 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getWordFormFeatures(r.getSource());
    for (int i = 0; i < features.size(); i++)
    {
      Pair p = (Pair) features.get(i);
      int idx = (Integer) p.getFirst();
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
      System.err.println("Classifier 'wordFormExtent(Relation)' defined on line 87 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordFormExtent".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordFormExtent; }
}

