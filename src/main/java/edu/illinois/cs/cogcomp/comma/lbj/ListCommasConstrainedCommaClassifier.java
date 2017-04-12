// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// discrete ListCommasConstrainedCommaClassifier(Comma c) <- ListCommasConstrainedInference(LocalCommaClassifier)

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


public class ListCommasConstrainedCommaClassifier extends Classifier
{
  private static final LocalCommaClassifier __LocalCommaClassifier = new LocalCommaClassifier();

  public ListCommasConstrainedCommaClassifier()
  {
    containingPackage = "edu.illinois.cs.cogcomp.comma.lbj";
    name = "ListCommasConstrainedCommaClassifier";
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
      System.err.println("Classifier 'ListCommasConstrainedCommaClassifier(Comma)' defined on line 284 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Sentence head = ListCommasConstrainedInference.findHead((Comma) __example);
    ListCommasConstrainedInference inference = (ListCommasConstrainedInference) InferenceManager.get("edu.illinois.cs.cogcomp.comma.lbj.ListCommasConstrainedInference", head);

    if (inference == null)
    {
      inference = new ListCommasConstrainedInference(head);
      InferenceManager.put(inference);
    }

    String result = null;

    try { result = inference.valueOf(__LocalCommaClassifier, __example); }
    catch (Exception e)
    {
      System.err.println("LBJava ERROR: Fatal error while evaluating classifier ListCommasConstrainedCommaClassifier: " + e);
      e.printStackTrace();
      System.exit(1);
    }

    return result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Comma[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'ListCommasConstrainedCommaClassifier(Comma)' defined on line 284 of CommaClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "ListCommasConstrainedCommaClassifier".hashCode(); }
  public boolean equals(Object o) { return o instanceof ListCommasConstrainedCommaClassifier; }
}

