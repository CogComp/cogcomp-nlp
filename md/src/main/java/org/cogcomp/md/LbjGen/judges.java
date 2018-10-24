/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D709BCA620140154F75A8601668141F1BB8AB057595702B19144B7AC1A44BBC4F38C228FFE9A98D664BD14A715DCD37F25F8A8C96B8E1B7078055D8E2B51B17E9C7043E147E03BE3CF0C787B46AE57905547F7E6F8E06E060FBF6C625E4F18EA785A814F0A64FB0F269D50F899A5C6EC61B22DE98D8AC394C8AB21BC75981775265942C1A1780ACC6F4A0ED2D91B5CCFC874D2E3B5E72C361C4275ADD643709A3781E550A450EB716FC612BF3B5430CC06225A82279353DE12B78E245E00FB28CF4E61A2F6C21BF515C41E2087491FE57CEDFA13AE881FDB6DCB8BE6C61DCAC1FBFF790A6115288B54C73479F57D43182516200000

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


public class judges extends Classifier
{
  public judges()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "judges";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'judges(Constituent)' defined on line 252 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String[] discreteVals = new String[3];
    discreteVals[0] = c.getAttribute("A_prediction");
    discreteVals[1] = c.getAttribute("B_prediction");
    discreteVals[2] = c.getAttribute("C_prediction");
    __id = "nam";
    __value = "" + (discreteVals[0]);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "nom";
    __value = "" + (discreteVals[1]);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "pro";
    __value = "" + (discreteVals[2]);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    int b_count = 0;
    int i_count = 0;
    int o_count = 0;
    for (int i = 0; i < 3; i++)
    {
      if (discreteVals[i].equals("B"))
      {
        b_count++;
      }
      else
      {
        if (discreteVals[i].equals("I"))
        {
          i_count++;
        }
        else
        {
          o_count++;
        }
      }
    }
    __id = "b_count";
    __value = "" + (b_count);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "i_count";
    __value = "" + (i_count);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "o_count";
    __value = "" + (o_count);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'judges(Constituent)' defined on line 252 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "judges".hashCode(); }
  public boolean equals(Object o) { return o instanceof judges; }
}

