/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056E8D4B02C03C068FFA4C280D2A61DBAF170141F6A0A30F037789B565B03B5D633014CFFE66E70E1427848CBFCBF625AD854042CE2CED7827D8494E66973247427B857E46DB380A06A308B3CE8285760A0F7E3A57852CC0286384B3FD48205AA67E931337C6805A8DF66BB85F23946EB21A62F1F6A91890EB9B0B5FB92A3E5A6B4254FB5AA2F104A574069981E48BD4FD60D53A33472ED4FA7AE7FA446CE5A7A6336DA209D98A1FAD4E5749248A69C88E2228C8FDB378386612D16603EFFDE8835E1D6D31068DDF77771100000

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


public class WordNetTagExtent extends Classifier
{
  public WordNetTagExtent()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "WordNetTagExtent";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'WordNetTagExtent(Relation)' defined on line 145 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String combined = r.getSource().getAttribute("WORDNETTAG");
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
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WordNetTagExtent(Relation)' defined on line 145 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordNetTagExtent".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordNetTagExtent; }
}

