/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054D81DA028040154F7560128D5421F93DE528C802802FB061B156046716664803CF7F680DA7A18B7FCD33F429E815177034891551195A1C0A313AC9F5A815DD307C0A49220B78A7F0F68B19824FB2407483D5FEBE4C61C1A3D4C5C08A79D48B9FA5A83C88EEB56B962EE0D97F514F981C154502375959D9AF76F248663AC2CC37F6FA503A0246566078F336A3746CC20BC709FA63994FC000000

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


public class gazetteersFeaturesExtent extends Classifier
{
  public gazetteersFeaturesExtent()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "gazetteersFeaturesExtent";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'gazetteersFeaturesExtent(Relation)' defined on line 97 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getGazetteerFeaturesSingle(r.getSource());
    for (int i = 0; i < features.size(); i++)
    {
      __id = "0";
      __value = "" + (features.get(i));
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'gazetteersFeaturesExtent(Relation)' defined on line 97 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "gazetteersFeaturesExtent".hashCode(); }
  public boolean equals(Object o) { return o instanceof gazetteersFeaturesExtent; }
}

