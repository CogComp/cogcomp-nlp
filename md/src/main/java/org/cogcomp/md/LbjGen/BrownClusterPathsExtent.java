/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DAE813B02C04C058FFAC3F048B3AA5475BA3861771B3A834DA96D38A7D29B41509EF77FA8B939849C094E52F29FEA6D71C42435CE8B978B4BEECB01F1279B9FDF358C98E325D9B8D6C18D06D37CB0990B575D9EC82A36C60C17542953D1714ADC05F652C2C5A312DA679A231BF6B6B2A5DC49940563C0D6D90C607E229096D3C998B62759CD24F15462CB0B524709F46FC89C60A4D0A9727E90B0CA0F99428E34CF2C07F5E8D413F5EF181DE8E81FD41DFB13DD2B7A2EC100000

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


public class BrownClusterPathsExtent extends Classifier
{
  public BrownClusterPathsExtent()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "BrownClusterPathsExtent";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BrownClusterPathsExtent(Relation)' defined on line 124 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String[] cur = r.getSource().getAttribute("BC").split(",");
    for (int i = 0; i < cur.length; i++)
    {
      if (cur[i] != "")
      {
        __id = "0";
        __value = "" + (cur[i]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    cur = r.getSource().getAttribute("BCm1").split(",");
    for (int i = 0; i < cur.length; i++)
    {
      if (cur[i] != "")
      {
        __id = "" + (-1);
        __value = "" + (cur[i]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    cur = r.getSource().getAttribute("BCp1").split(",");
    for (int i = 0; i < cur.length; i++)
    {
      if (cur[i] != "")
      {
        __id = "1";
        __value = "" + (cur[i]);
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
      System.err.println("Classifier 'BrownClusterPathsExtent(Relation)' defined on line 124 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BrownClusterPathsExtent".hashCode(); }
  public boolean equals(Object o) { return o instanceof BrownClusterPathsExtent; }
}

