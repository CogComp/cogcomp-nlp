// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000055C814E028030154FA2F9584A4849817965CD871071C500560C90D4B6A501313EDDD650319CCA6EFF7FF53CEFAE860AD0ECE8E1C674F55DD97159DE9C062BEA9C07098718DC0065852749FDE41A5BE0283CBB5506C112778435C45CD71912921664DA514A4EC2872E476B76F7892CB7659A0042F39FA9534FC12A18B5883DD643D39B28D75EA663C6AB8D8723E90C97E8D70E57D712D15C4A34ABE679DB892B8048BF067E8318F70100000

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


/**
  * Feature generator that senses the chunk tags of the previous two words.
  * During training, labels are present, so the previous two chunk tags are
  * simply read from the data.  Otherwise, the prediction of the
  * {@link Chunker} is used.
  *
  * @author Nick Rizzolo
 **/
public class PreviousTags extends Classifier
{
  private static final Chunker __Chunker = new Chunker();

  public PreviousTags()
  {
    containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
    name = "PreviousTags";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTags(Token)' defined on line 20 of chunk.lbj received '" + type + "' as input.");
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
    for (; w != word; w = (Token) w.next)
    {
      if (__Chunker.isTraining)
      {
        __id = "" + (i++);
        __value = "" + (w.label);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
      else
      {
        __id = "" + (i++);
        __value = "" + (__Chunker.discreteValue(w));
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'PreviousTags(Token)' defined on line 20 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTags".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTags; }
}

