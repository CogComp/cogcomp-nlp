/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D7091CA430130168F556A1824278592B74B658AD3514058E2ED3EAFB5281625399CA288FEE663BE16D25427A9CCC73FDFCCB8B4D74806947A88BBDF3CD3608F65FE207217291C24DB1AB968EB8CDB29EEFAE809388D82783956D68A1A69E69656A4FFF03F81130D68AEC778F49D23701B2E24186CFB7278F0DAA8055D276C6AD58101D1F1BB0F60EB86CA88E5995F499AAB61B12ECEDB9970DDA8447FC950A59B4D54BE8BCAD224CB76BE3965213349911940E4025D4BA8EA7A0F76C7D4AB6AF628F238393617E299F9076A9B4A8F79C1D3FBFE700845386190A100000

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


public class preBIOLevel2 extends Classifier
{
  public preBIOLevel2()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "preBIOLevel2";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'preBIOLevel2(Constituent)' defined on line 125 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (c.getStartSpan() - 2 > 0)
    {
      Constituent cPrev = c.getTextAnnotation().getView("BIO").getConstituentsCoveringToken(c.getStartSpan() - 2).get(0);
      if (cPrev != null)
      {
        if (c.getAttribute("isTraining").equals("true"))
        {
          __id = "-2";
          __value = "" + (cPrev.getAttribute("BIO"));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        else
        {
          if (!c.getAttribute("preBIOLevel2").equals(""))
          {
            __id = "-2";
            __value = "" + (c.getAttribute("preBIOLevel2"));
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
          }
        }
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'preBIOLevel2(Constituent)' defined on line 125 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "preBIOLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof preBIOLevel2; }
}

