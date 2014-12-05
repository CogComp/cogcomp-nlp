// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945507EC82DCBC6F94C4A4DC1D809CFCE4DC3582FCF2A41D450B1D558A6500A282D228888E5E084985B24D2000A070A76793000000

package edu.illinois.cs.cogcomp.quant.lbj;

import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
import edu.illinois.cs.cogcomp.lbj.pos.POSWindow;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;
import java.util.regex.*;


/**
  * Simply returns the value of this <code>Token</code>'s <code>label</code>
  * field.
  *
  * @author Nick Rizzolo
 **/
public class ChunkLabel extends Classifier
{
  public ChunkLabel()
  {
    containingPackage = "edu.illinois.cs.cogcomp.quant.lbj";
    name = "ChunkLabel";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
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
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'ChunkLabel(Token)' defined on line 63 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    return "" + (word.label);
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'ChunkLabel(Token)' defined on line 63 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "ChunkLabel".hashCode(); }
  public boolean equals(Object o) { return o instanceof ChunkLabel; }
}

