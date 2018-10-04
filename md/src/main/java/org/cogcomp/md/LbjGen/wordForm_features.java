/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000054DCD4B02C03C0081EFB29B80D22A8767A715C180282CE0E1564B17404B59433D1ACEFBBD8F97A634E9EB5F49C13A0E00E619D7919FC78326D2D23623BC8129094B8140C9589D80E10B1A420F5103785CA7B5EB7A5572C5B398C3E6056FF9859FD693B50C132381AC13ACF07254E366FB4D831DD1D46443C1ADCFFCEA6268B46764F66FFE27CD09A3DE8FE45CA38063891D547B521721D2552C41A18B6D7254FE9E36A24713875490342C7586AA2B08E1AF7208C7AE12E41100000

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


public class wordForm_features extends Classifier
{
  public wordForm_features()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "wordForm_features";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'wordForm_features(Constituent)' defined on line 27 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getWordFormFeatures(c);
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
      System.err.println("Classifier 'wordForm_features(Constituent)' defined on line 27 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordForm_features".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordForm_features; }
}

