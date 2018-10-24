/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000058D8BBE02C03C054F75CAA4849AA01133D230F858981A365D18603522549451B331AEFB3EE38999C6D5D937F64C78BD047634D624445790E98D2724FC06278A670F18A93A5FD30D404D8C07403AB74E31B4ED526459D9F67F957AC2F275CB96711840FF99A960769556BD90B75880ACACC85126F5AC9A6534B34FD3FB52A2A8C56FF91257E01C4A83F880E7D931D3C3221E2FBA54D6C6B25D48E905A0F0FF0C116CF20F28D079700100000

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


public class bioScore extends Classifier
{
  public bioScore()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "bioScore";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'bioScore(Constituent)' defined on line 243 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    double __value;

    String scoreSet = c.getAttribute("BIOScores");
    String[] scores = scoreSet.split(",");
    for (int i = 0; i < scores.length; i++)
    {
      double curScore = Double.parseDouble(scores[i]);
      __id = "" + (i);
      __value = Double.parseDouble(scores[i]);
      __result.addFeature(new RealPrimitiveStringFeature(this.containingPackage, this.name, __id, __value));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'bioScore(Constituent)' defined on line 243 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "bioScore".hashCode(); }
  public boolean equals(Object o) { return o instanceof bioScore; }
}

