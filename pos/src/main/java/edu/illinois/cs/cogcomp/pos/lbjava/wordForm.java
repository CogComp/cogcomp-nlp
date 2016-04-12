// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945582FCF2A417BCF2AC5D809CFCE4DC35827D450B1D558A658CC4350D827DB430A48E5E4A6E5A79468686A28DADA281A6245E49432ABA594F233F2525B2CF3D06A2393321B8C1B443C043535141D651471CA8186949615E92829EAF40939EA295B24D244BB66C6D296AD3806ADB6162601D504120003AB6F6C5CC000000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.*;


/**
  * Returns the <i>form</i> of the word, i.e, the raw text that represents it.
  * The only exceptions are the brackets <code>'('</code>, <code>'['</code>,
  * and <code>'{'</code> which are translated to <code>'-LRB-'</code> and
  * <code>')'</code>, <code>']'</code>, <code>'}'</code> which are translated
  * to <code>'-RRB-'</code>.
  *
  * @author Nick Rizzolo
 **/
public class wordForm extends Classifier
{
  public wordForm()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "wordForm";
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
      System.err.println("Classifier 'wordForm(Token)' defined on line 20 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token w = (Token) __example;

    if (w.form.length() == 1)
    {
      if ("([{".indexOf(w.form.charAt(0)) != -1)
      {
        return "-LRB-";
      }
      if (")]}".indexOf(w.form.charAt(0)) != -1)
      {
        return "-RRB-";
      }
    }
    return "" + (w.form);
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'wordForm(Token)' defined on line 20 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordForm".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordForm; }
}

