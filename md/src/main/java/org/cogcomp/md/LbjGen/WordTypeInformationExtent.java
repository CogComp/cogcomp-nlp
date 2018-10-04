/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6D814B43C040158FFACBB80BB45B4FC6AE5A061A0285C80E97D4625602DDD03B319A69CF7776311DB87AD973FEB9F6B5ECD8092D5BB0D76AB58AC843DD0ED294BFAF930D126794EC14935C7CB825457F24DFC112E1BDB3C51F4C951D150D15823E10BB3C3FE79467121A1D42BE319EF364737157A15A127ED750B60E83A28DC3B9ACE9DEFA97D99FBC82B5EA65EDEBD36061C06C9B239FF3EC49E8D8BA5583E90CDE5A04B42F81A4FB769CA5C5FE925F4122E324F50ADD21F78AA9A941BD2856A899663DD714B2C489EB15D1081D093100000

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


public class WordTypeInformationExtent extends Classifier
{
  public WordTypeInformationExtent()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "WordTypeInformationExtent";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  private static String[] __allowableValues = DiscreteFeature.BooleanValues;
  public static String[] getAllowableValues() { return __allowableValues; }
  public String[] allowableValues() { return __allowableValues; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'WordTypeInformationExtent(Relation)' defined on line 104 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getWordTypeInformation(r.getSource());
    for (int i = 0; i < features.size(); i++)
    {
      Pair p = (Pair) features.get(i);
      String idx = (String) p.getFirst();
      boolean val = (Boolean) p.getSecond();
      __id = "" + (idx);
      __value = "" + (val);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WordTypeInformationExtent(Relation)' defined on line 104 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordTypeInformationExtent".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordTypeInformationExtent; }
}

