/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054DC1CA02C03C0081E759C548E015C3B3DB82E4602828F0025ABC6404AB194622A8FEE63EC9E9AD09FAF7B2290C8A83186CF30551195E253A7DE915CD6BD82A4AD36458091CA660F4830982C8206D0B92F85C03DEEEACE386BC3F6057F36EA813612BC1AE691C15AA15A79B8C3D1BAF5B6E24F047901D47A95AF8E4E981AB4EC9DD2BFBB457746ECA35DDD449151B1C48A3BD614C2A65A3B235C60E6EFA68689EB6EC81AD859121C828F90D2D46E0F287DB10A51975C71100000

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


public class gazetteers_features extends Classifier
{
  public gazetteers_features()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "gazetteers_features";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'gazetteers_features(Constituent)' defined on line 13 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getGazetteerFeatures(c);
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
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'gazetteers_features(Constituent)' defined on line 13 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "gazetteers_features".hashCode(); }
  public boolean equals(Object o) { return o instanceof gazetteers_features; }
}

