// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005705DDA6380381D759351654115AABBB56DDDCE10A50DBB2168B535BF1D0174C83703C77F521596B2BDD424ECF39325BF1A5811F08267B7D2EB8A9EAD0BC6E2241AF64F132C3728F109230268907C6D37C07A643242BFCD0301E5094E85FA1D7AF9350165B58AE4A468421A8C6E83BE89E1DD8C8160416439AAF0C16AAAB5BA252AF91B74ECEA4BCAED58C58027EEAEDFA0FB889DE95DCB1FEB2598F63E688B693017BCDE7754955D5B0D16F19D89312C7D377AE2247A4D69AB2256B1C936C2E1D3D7AEF5E67B5C4C28D9CFE3F6005C1BBBEFB2C6582B10184910E96E49D074C80ED20B7E4846CFAA439DFD25FD0772CCF6489D248EEFC68F5EA3C5253DF100000

package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.chunker.utils.Constants;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;
import edu.illinois.cs.cogcomp.pos.lbjava.POSWindow;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


public class SOPrevious extends Classifier
{
  private static final Chunker __Chunker = new Chunker();
  private static final POSTagger __POSTagger = new POSTagger();

  public SOPrevious()
  {
    containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
    name = "SOPrevious";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'SOPrevious(Token)' defined on line 33 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    int i;
    Token w = word;
    for (i = 0; i > -2 && w.previous != null; --i)
    {
      w = (Token) w.previous;
    }
    String[] tags = new String[3];
    String[] labels = new String[2];
    i = 0;
    for (; w != word; w = (Token) w.next)
    {
      tags[i] = __POSTagger.discreteValue(w);
      if (__Chunker.isTraining)
      {
        labels[i] = w.label;
      }
      else
      {
        labels[i] = __Chunker.discreteValue(w);
      }
      i++;
    }
    tags[i] = __POSTagger.discreteValue(w);
    __id = "ll";
    __value = "" + (labels[0] + "_" + labels[1]);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "lt1";
    __value = "" + (labels[0] + "_" + tags[1]);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    __id = "lt2";
    __value = "" + (labels[1] + "_" + tags[2]);
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'SOPrevious(Token)' defined on line 33 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "SOPrevious".hashCode(); }
  public boolean equals(Object o) { return o instanceof SOPrevious; }
}

