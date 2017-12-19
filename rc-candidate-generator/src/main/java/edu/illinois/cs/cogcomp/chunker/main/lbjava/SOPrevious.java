/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D809DCE43C030148F556A198A21549AAD073C49B4F0C1B519EDAA809966B16540B193E21424D777CECF8AD6402171B5BBBFDECC8D507DB73469E609DA7368E395F1BE07BAFD841A1D6A8807F90EB1CAC2850AFE369DEC40E0AD024675EC5081F08425C47A8666F1DB0162B48A365590429074E47CFA67AE1D5062072466D0BA27793CAC2B67492A668EDD6E2EC3EA4EB0553202DCDBD7B92AD442C9FC468C87D69A8EBCAF02ED567C9BB9D66D9D656952990B98C9CC101E319C2C9CEA4FBBB08FA4AA66DA652551C584B4F84A8C84BADCC8BEDA19CAC19E50BBC5D94633B6B2FFC27D3A1D4FF72FD6A93183E8DF5F75CDADDA3128AAA0077389CC374C80E930776FD954E762DE2E774B578B203DB0071320DFF8F9E703E71799DB3200000

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


public class SOPrevious extends Classifier
{
  private static final ReadingComprehensionCandidateGenerator __ReadingComprehensionCandidateGenerator = new ReadingComprehensionCandidateGenerator();
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
      System.err.println("Classifier 'SOPrevious(Token)' defined on line 33 of ReadingComprehensionCandidateGenerator.lbj received '" + type + "' as input.");
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
      if (__ReadingComprehensionCandidateGenerator.isTraining)
      {
        labels[i] = w.label;
      }
      else
      {
        labels[i] = __ReadingComprehensionCandidateGenerator.discreteValue(w);
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
      System.err.println("Classifier 'SOPrevious(Token)' defined on line 33 of ReadingComprehensionCandidateGenerator.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "SOPrevious".hashCode(); }
  public boolean equals(Object o) { return o instanceof SOPrevious; }
}

