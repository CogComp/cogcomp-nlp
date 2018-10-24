/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D719FCA02C03C06CF552711A3462A757A705050FAE6F2056D8A54D452B88288FEE66D92E46AE5A4EFEFEBC74D9FAA4641CE1C520BB560E3EC15E288466D870B2E30107203941E60E940A21BC2B2775892C82B851427D4E34DC7306870B549C3C99B44394C8BE59CF469C827026BB0BCAF67AD98A84B40A34A509B6603B7BAEF3E560770C3458F50BDED8F98DF1E2A6C6DECD259BB0CA3CF28760B1DCD4DD3FF156FA6CB14B6CDFE74F984A7891F09E39390C8BBAA50E556644142EF4C3B616FAFB3E3E917F70DFACD5DAFB100000

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


public class wordFormBetween extends Classifier
{
  public wordFormBetween()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "wordFormBetween";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'wordFormBetween(Relation)' defined on line 194 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    int startIdx = 1;
    int endIdx = 0;
    if (r.getSource().getStartSpan() < r.getTarget().getStartSpan())
    {
      startIdx = r.getSource().getEndSpan();
      endIdx = r.getTarget().getStartSpan();
    }
    else
    {
      startIdx = r.getTarget().getEndSpan();
      endIdx = r.getSource().getStartSpan();
    }
    int anchor = startIdx;
    for (int i = startIdx; i < endIdx; i++)
    {
      __id = "" + ((i - anchor));
      __value = "" + (r.getSource().getTextAnnotation().getToken(i));
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'wordFormBetween(Relation)' defined on line 194 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordFormBetween".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordFormBetween; }
}

