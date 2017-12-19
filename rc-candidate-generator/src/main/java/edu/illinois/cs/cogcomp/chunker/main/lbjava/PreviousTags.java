/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D8D814E62013C054FA2F950866486645579408D0B06B80DC502D6C08540705291609A2EEE83504A2BBACB2BFFBFFCE83D774AC436C622D5834F9A3BBF45571E842812447536ED0E70C291C60FCB361FB991CE24445CABE781036986E313990686FCF49164B084FEDB143D07DAE925D78DBEF3891CDE1A23A0C8E52F77658EA9B818778A6B46D1BCE751E4A6830942E02B2B2E8D9DC4B621A863788D27AE2A56152B41392740E9E41335D97BF54EBCF52FA7D7F4FF7AFA68AB4F57EE08CD173EA54100000

package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.chunker.utils.Constants;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;
import edu.illinois.cs.cogcomp.pos.lbjava.POSWindow;


/**
  * Feature generator that senses the chunk tags of the previous two words.
  * During training, labels are present, so the previous two chunk tags are
  * simply read from the data.  Otherwise, the prediction of the
  * {@link ReadingComprehensionCandidateGenerator} is used.
  *
  * @author Nick Rizzolo
 **/
public class PreviousTags extends Classifier
{
  private static final ReadingComprehensionCandidateGenerator __ReadingComprehensionCandidateGenerator = new ReadingComprehensionCandidateGenerator();

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
      System.err.println("Classifier 'PreviousTags(Token)' defined on line 20 of ReadingComprehensionCandidateGenerator.lbj received '" + type + "' as input.");
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
      if (__ReadingComprehensionCandidateGenerator.isTraining)
      {
        __id = "" + (i++);
        __value = "" + (w.label);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
      else
      {
        __id = "" + (i++);
        __value = "" + (__ReadingComprehensionCandidateGenerator.discreteValue(w));
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
      System.err.println("Classifier 'PreviousTags(Token)' defined on line 20 of ReadingComprehensionCandidateGenerator.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTags".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTags; }
}

