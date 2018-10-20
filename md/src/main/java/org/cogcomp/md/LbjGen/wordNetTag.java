/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056D8B4B82C040148FFA4B30B0314770DBAF8388B2ED41430E126E0629676702EC8E4770F02EF77B3E30F02D7828AAFAABA27456446CF18B4885D2193DDB5D3B0E98D1738E91AC406CFB0758D0747ED2491E8583F851C40A436197AC2E71D03A65BD5DAFF693F43D9E24523A71F95E063686E4D2EFA6A1A35DE85BAE5B4D12440DE466C901DF198C8F950353A7BCFF2E47BB9C76E940B74E99BC52B308EE091C373BFA94B2594B421A7240D4FED9DDEC84095F3781E7BB309F27B6FEE6C7BEB3F80100000

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


public class wordNetTag extends Classifier
{
  public wordNetTag()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "wordNetTag";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'wordNetTag(Constituent)' defined on line 143 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String combined = c.getAttribute("WORDNETTAG");
    String[] group = combined.split(",");
    for (int i = 0; i < group.length; i++)
    {
      String s = group[i];
      if (!s.equals(""))
      {
        __id = "" + ((s.split("\\."))[0]);
        __value = "" + ((s.split("\\."))[1]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'wordNetTag(Constituent)' defined on line 143 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordNetTag".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordNetTag; }
}

