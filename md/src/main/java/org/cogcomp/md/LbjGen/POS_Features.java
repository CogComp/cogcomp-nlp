/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054DC1CA0280401081E7599B40B22947E4BB4190240A0E3048CA3AC04CA2BB36841EBB7335267ADD16FB9F7B5A06D323EE0AA2BEB7E8D0F4E1389BC0E2031F48E81C6409DE1ED073A0C0DDA20E407E2AC5DF5766FD85E1C72D32B476BC8D8258E60F0684A342B37845E8C6BA4218E5864015C1742F545D097815C91DB54F772D534AE4B3DECA2A07C8D3A0A15F5372F185B453B727D3C3B9782AFD4BA9A1DE0EA55410D50CF68E8A23585069F00F88AB5F9A0100000

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


public class POS_Features extends Classifier
{
  public POS_Features()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "POS_Features";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POS_Features(Constituent)' defined on line 67 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getPOSFeatures(c);
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
      System.err.println("Classifier 'POS_Features(Constituent)' defined on line 67 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "POS_Features".hashCode(); }
  public boolean equals(Object o) { return o instanceof POS_Features; }
}

