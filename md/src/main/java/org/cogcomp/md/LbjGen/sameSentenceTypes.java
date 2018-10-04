/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC19D4B82C030168FFACC604849A617FABAE1A88EDABD5CA7B2561D836D04D4B676A7854CFFEB31F30F6E1C359C16066E94ED978CEC29961917004B932668E81D91C5FF5384A7E5B326BCD9431C4003D11C9123E6DAB21C4D7CDA578B3891898A44E895AFBDE815BAC29FE839F95D245039B3D971056B577D878FE7F22A6A2BCA5D0D35BFAB50D6524CA013E98499EDE24451AB29F02D903C0E91E428D57E9BD2466B70DF1411E9ABD454A59A0E94AF06F9A4F88B19F832474807BD3F751482C4E3E20EBC7DF4970F227E5BF5AF0FBE7A65AF6A65C94CBC57F75BEABEDBBF16DB63B4F599D5EF14AF0D1963A300000

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


public class sameSentenceTypes extends Classifier
{
  public sameSentenceTypes()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "sameSentenceTypes";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'sameSentenceTypes(Constituent)' defined on line 165 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    String combined = c.getAttribute("SMNAMPRE");
    String[] group = combined.split(",");
    for (int i = 0; i < group.length; i++)
    {
      String s = group[i];
      if (!s.equals(""))
      {
        String[] gp = s.split("-");
        __id = "" + ("SMNAM_" + gp[0]);
        __value = "" + (gp[1]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    combined = c.getAttribute("SMNOMPRE");
    group = combined.split(",");
    for (int i = 0; i < group.length; i++)
    {
      String s = group[i];
      if (!s.equals(""))
      {
        String[] gp = s.split("-");
        __id = "" + ("SMNOM_" + gp[0]);
        __value = "" + (gp[1]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    combined = c.getAttribute("SMNAMAFT");
    group = combined.split(",");
    for (int i = 0; i < group.length; i++)
    {
      String s = group[i];
      if (!s.equals(""))
      {
        String[] gp = s.split("-");
        __id = "" + ("SMNAMAFT_" + gp[0]);
        __value = "" + (gp[1]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    combined = c.getAttribute("SMNOMAFT");
    group = combined.split(",");
    for (int i = 0; i < group.length; i++)
    {
      String s = group[i];
      if (!s.equals(""))
      {
        String[] gp = s.split("-");
        __id = "" + ("SMNOMAFT_" + gp[0]);
        __value = "" + (gp[1]);
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
      System.err.println("Classifier 'sameSentenceTypes(Constituent)' defined on line 165 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "sameSentenceTypes".hashCode(); }
  public boolean equals(Object o) { return o instanceof sameSentenceTypes; }
}

