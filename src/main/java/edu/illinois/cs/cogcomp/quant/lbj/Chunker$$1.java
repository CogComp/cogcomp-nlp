// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D4D81CA02C030144F756F051412E1CBB719241C3845840A7E866B679A663C621BAE7DB1B8287A1666E133E82E95031E20AE12BF115AAA63B43C32A78985CDA06BB68685E61514D63052B75A79D44CE51475A73FC08B7FD710EB1EEAFE9E1858E3A0ED9837436F25C9EC7A148B14B92BCCD6BAB32FE87250782CBBFB424053F3E7457BFB9150F60A0851F8BAA000000

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


public class Chunker$$1 extends Classifier
{
  private static final Forms __Forms = new Forms();
  private static final Capitalization __Capitalization = new Capitalization();
  private static final WordTypeInformation __WordTypeInformation = new WordTypeInformation();
  private static final Affixes __Affixes = new Affixes();
  private static final PreviousTags __PreviousTags = new PreviousTags();
  private static final SubhroFeatures __SubhroFeatures = new SubhroFeatures();
  private static final POSWindow __POSWindow = new POSWindow();
  private static final Mixed __Mixed = new Mixed();
  private static final POSWindowpp __POSWindowpp = new POSWindowpp();
  private static final Formpp __Formpp = new Formpp();
  private static final SOPrevious __SOPrevious = new SOPrevious();

  public Chunker$$1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.quant.lbj";
    name = "Chunker$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Chunker$$1(Token)' defined on line 265 of chunk.lbj received '" + type + "' as input.");
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
    __result.addFeatures(__SubhroFeatures.classify(__example));
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
      System.err.println("Classifier 'Chunker$$1(Token)' defined on line 265 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Chunker$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof Chunker$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__Forms);
    result.add(__Capitalization);
    result.add(__WordTypeInformation);
    result.add(__Affixes);
    result.add(__PreviousTags);
    result.add(__SubhroFeatures);
    result.add(__POSWindow);
    result.add(__Mixed);
    result.add(__POSWindowpp);
    result.add(__Formpp);
    result.add(__SOPrevious);
    return result;
  }
}

