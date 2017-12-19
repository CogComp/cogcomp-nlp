/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4D81CA02C04C044F75278A0A0BE1CBB719282E14A26B0D37039A60D62BCEEA65DFA77B2A0E96899733C094C360E4C318303298E9A4BED70E33B6413D215948031F68593062B0541B89636716581C20DC0693785B58E3A3821DB42CBACB035E5A38633F6E9E97BAD5E2C73C557D9C3837B7F18FE267B8D0E964755DDA829D0E067993DF52EDBBFC3C8A575FB9938731283E2EE9B000000

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


public class ReadingComprehensionCandidateGenerator$$1 extends Classifier
{
  private static final Forms __Forms = new Forms();
  private static final Capitalization __Capitalization = new Capitalization();
  private static final WordTypeInformation __WordTypeInformation = new WordTypeInformation();
  private static final Affixes __Affixes = new Affixes();
  private static final PreviousTags __PreviousTags = new PreviousTags();
  private static final POSWindow __POSWindow = new POSWindow();
  private static final Mixed __Mixed = new Mixed();
  private static final POSWindowpp __POSWindowpp = new POSWindowpp();
  private static final Formpp __Formpp = new Formpp();
  private static final SOPrevious __SOPrevious = new SOPrevious();

  public ReadingComprehensionCandidateGenerator$$1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
    name = "ReadingComprehensionCandidateGenerator$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'ReadingComprehensionCandidateGenerator$$1(Token)' defined on line 170 of ReadingComprehensionCandidateGenerator.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeatures(__Forms.classify(__example));
    __result.addFeatures(__Capitalization.classify(__example));
    __result.addFeatures(__WordTypeInformation.classify(__example));
    __result.addFeatures(__Affixes.classify(__example));
    __result.addFeatures(__PreviousTags.classify(__example));
    __result.addFeatures(__POSWindow.classify(__example));
    __result.addFeatures(__Mixed.classify(__example));
    __result.addFeatures(__POSWindowpp.classify(__example));
    __result.addFeatures(__Formpp.classify(__example));
    __result.addFeatures(__SOPrevious.classify(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'ReadingComprehensionCandidateGenerator$$1(Token)' defined on line 170 of ReadingComprehensionCandidateGenerator.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "ReadingComprehensionCandidateGenerator$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof ReadingComprehensionCandidateGenerator$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__Forms);
    result.add(__Capitalization);
    result.add(__WordTypeInformation);
    result.add(__Affixes);
    result.add(__PreviousTags);
    result.add(__POSWindow);
    result.add(__Mixed);
    result.add(__POSWindowpp);
    result.add(__Formpp);
    result.add(__SOPrevious);
    return result;
  }
}

