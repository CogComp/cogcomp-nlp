/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D7091CA430130168F556A182427859EE5DA51A6F450141AB87F8BEF6906894D462BA02EBBB9DCA785B419C96233FDC7F33F2E25F112852D922EE6FF07F810EB5DBB0C94C9460B05F68E6A1AF227FA4ABFBA324E0263AC1E4695B1A68A5AB5A5992DFF3CC364C04B1AA3FD1E356BCC14CA8B050A1FFE9C1E34BA22455BC91B967160447C7CE2CB18F2A1B22A7566D356AAEA5C6883B7F66E147B221DD3761865E2571DA3E2B6B801FE9DAF4A594CC056644218318453DA2ABE92CF91F539EA9EB90EBC0E4E85C9B46E72C996E292EF5274FCFEBF16924EDFD0A100000

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


public class preBIOLevel1 extends Classifier
{
  public preBIOLevel1()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "preBIOLevel1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'preBIOLevel1(Constituent)' defined on line 111 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (c.getStartSpan() - 1 > 0)
    {
      Constituent cPrev = c.getTextAnnotation().getView("BIO").getConstituentsCoveringToken(c.getStartSpan() - 1).get(0);
      if (cPrev != null)
      {
        if (c.getAttribute("isTraining").equals("true"))
        {
          __id = "-1";
          __value = "" + (cPrev.getAttribute("BIO"));
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
        else
        {
          if (!c.getAttribute("preBIOLevel1").equals(""))
          {
            __id = "-1";
            __value = "" + (c.getAttribute("preBIOLevel1"));
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
      System.err.println("Classifier 'preBIOLevel1(Constituent)' defined on line 111 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "preBIOLevel1".hashCode(); }
  public boolean equals(Object o) { return o instanceof preBIOLevel1; }
}

