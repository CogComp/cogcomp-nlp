/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D7F4BCA02C04C0CF590E96B882A76BE1C745405AB2675FA2BAD4BC26DCAC63E304CF777B51F0E30F239039994629A9277E0911476C8E6A8A3D58C44C21B0DC6C218B002C61CD041E59704469F943B68E38B67E8CA4B3F8228ABF731980A7062301F52B882D4E8ADFC124FB8A266D60F22A2885F10BC6B293B82E4E34BD28E60E388FCA7274FBD7E397D6A195A29D0F9098BC19D33A3349B2BB74AF795A9E7F75C88E4D56EA7B8545F2CDF572B1275A63272B91AC55C3E68F1C308DA03217B3100000

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


public class afterHeadPOS extends Classifier
{
  public afterHeadPOS()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "afterHeadPOS";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete"; }


  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    String result = discreteValue(__example);
    return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result), (short) allowableValues().length);
  }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'afterHeadPOS(Relation)' defined on line 45 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    TextAnnotation ta = r.getTarget().getTextAnnotation();
    if (r.getTarget().getEndSpan() < ta.getView(ViewNames.TOKENS).getEndSpan() - 1)
    {
      return "" + (ta.getView(ViewNames.POS).getConstituentsCoveringToken(r.getTarget().getEndSpan() + 1).get(0).getLabel());
    }
    return "OUT_OF_BOUND";
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'afterHeadPOS(Relation)' defined on line 45 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "afterHeadPOS".hashCode(); }
  public boolean equals(Object o) { return o instanceof afterHeadPOS; }
}

