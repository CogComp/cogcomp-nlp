/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054D814B028040158FFAC4B01CA452D93DE0151879C0A28801F09A3AD28EEADEC84444FFDB5B48893C387FEB73FA0549B546C13C3C8D2264E8E9D8C5B1D4CA8B34DC09B701EC0E50746BA47509B96235A1B08524EE758CB267E76D13A417EDF1631F6F41D5672CB060E3941A2B6AB6B7C786AF4D6DA85A896F4596C2845E6649326E18390F750FB64D51FDC9339987FF97278D73F445AE2B214E88C7CB777DA94A01E5F421A6247F8610410CBBFBF00C953E2754E000000

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


public class wordNetHym extends Classifier
{
  public wordNetHym()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "wordNetHym";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'wordNetHym(Constituent)' defined on line 154 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String combined = c.getAttribute("WORDNETHYM");
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
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'wordNetHym(Constituent)' defined on line 154 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordNetHym".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordNetHym; }
}

