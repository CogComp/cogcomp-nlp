/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054E8DCA02C030148F556D080905D2E9D6D38061CB8A0DA054A4F05BDD6C04D4439D2822EBBB9AF38CE167999F668D649BAD221E812736B9D22DAFE7E4E648A987A8D554AC860B202E92C30232BA4B48ADC9F8A43630B00B1A44ACC4F6B64E268B794E9936F48C95EBB475BD46FBE3C68988EB9D2A4096D4F71F1DF5B48EE2D922EC62305DA1B0C596205E98954E75CF9048D1A69472FA40188FFB2EC36F6FB055A7FA50E32712E5BFAA37C9131309E05B34F54370711C3789710094AC94C3F000000

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


public class WordNetHymExtent extends Classifier
{
  public WordNetHymExtent()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "WordNetHymExtent";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'WordNetHymExtent(Relation)' defined on line 156 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String combined = r.getSource().getAttribute("WORDNETHYM");
    String[] group = combined.split(",");
    for (int i = 0; i < group.length; i++)
    {
      String s = group[i];
      if (!s.equals(""))
      {
        __id = "" + (i);
        __value = "" + (s);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WordNetHymExtent(Relation)' defined on line 156 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordNetHymExtent".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordNetHymExtent; }
}

