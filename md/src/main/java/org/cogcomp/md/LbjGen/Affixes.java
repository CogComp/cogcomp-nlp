// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000059D8D5A0280501587B270128B7135A8A7A4D02A524B38C6EAD04D53C9112827FED890140DB4CC3C183F3FDC985AEE849668D580C77217BF63A82B6F41515B74991E183867C1B1486BBB2A24D9B6B393EC713A907C6566B86558949516D6A96AED6AC10E6CD5E71A8D8E9D97C61C3602415809CDE99063F627E2DF1562CE26E6D92038DD71E79F73E5E71EFB709D7E7D0F24C403EECE0100000

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


public class Affixes extends Classifier
{
  public Affixes()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "Affixes";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Affixes(Constituent)' defined on line 53 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String form = c.toString();
    for (int i = 3; i <= 4; i++)
    {
      if (form.length() > i)
      {
        __id = "p|";
        __value = "" + (form.substring(0, i));
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    for (int i = 1; i <= 4; i++)
    {
      if (form.length() > i)
      {
        __id = "s|";
        __value = "" + (form.substring(form.length() - i));
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
      System.err.println("Classifier 'Affixes(Constituent)' defined on line 53 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Affixes".hashCode(); }
  public boolean equals(Object o) { return o instanceof Affixes; }
}

