// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005AD4D4B0280441CFB23801CE26A457C4D02AB87FE6E13B7B6F027D0FD31282FFB7B6240147B893C0CC7E995AEE949618DB37C7729AA4378ECB82BE04E515B54E90E183A6FCEB18BEAF6150AE45BB951363B94481E016E06DA3B049718D40E836B1AACE066AE5A752FD8E5C85CE0C39124E5801555627B7648DEB7C3591E423F2FA69126916C08FFF14E7D3C77729FCDD8F201E3B51C061100000

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


public class AffixesZH extends Classifier
{
  public AffixesZH()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "AffixesZH";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'AffixesZH(Constituent)' defined on line 77 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String form = c.toString();
    for (int i = 1; i <= 2; i++)
    {
      if (form.length() > i)
      {
        __id = "ZH-p|";
        __value = "" + (form.substring(0, i));
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    for (int i = 1; i <= 2; i++)
    {
      if (form.length() > i)
      {
        __id = "ZH-s|";
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
      System.err.println("Classifier 'AffixesZH(Constituent)' defined on line 77 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "AffixesZH".hashCode(); }
  public boolean equals(Object o) { return o instanceof AffixesZH; }
}

