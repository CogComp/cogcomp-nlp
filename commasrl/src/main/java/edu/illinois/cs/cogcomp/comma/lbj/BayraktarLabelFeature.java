// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945507A4CAC2A4CCE294C22F94C4A4DC17B4D4C292D2A45D07ECFCDCD445846D450B1D558A6500A2C2D2AC35846DB4F4D21455FA1A96DA05B000C677E39AC4000000

package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.comma.readers.CommaParser;
import edu.illinois.cs.cogcomp.comma.readers.PrettyCorpusReader;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.ArrayList;
import java.util.List;


public class BayraktarLabelFeature extends Classifier
{
  public BayraktarLabelFeature()
  {
    containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
    name = "BayraktarLabelFeature";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.comma.datastructures.Comma"; }
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
    if (!(__example instanceof Comma))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BayraktarLabelFeature(Comma)' defined on line 173 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Comma c = (Comma) __example;

    return "" + (c.getBayraktarLabel());
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Comma[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'BayraktarLabelFeature(Comma)' defined on line 173 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BayraktarLabelFeature".hashCode(); }
  public boolean equals(Object o) { return o instanceof BayraktarLabelFeature; }
}

