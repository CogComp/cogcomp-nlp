// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005909F4B43013015CFBAC850B427860C6D39FF04A0E5C2D61CD2D376BB3BBB1ABE407231750BE7773ABB51928A59C52339799FDB7358B0B1641C35801BC2DDBCD1A598C81456E7B840DA68BA11CB283218609A29A1EA1A535A7E743D5DA4F524EED73869022D69C7B4942BC5C3466BAA0975D5BC8B091B574E8A281E0180B58CD60C6C116669B2415AD8FC302F336135F1944570948FFC1633B9363854F3D6DE9B85A5E427325723B5BF6A08DDE0E4EF0CE102B5D60F92AD628A1CA6E7F3F5CA7E38416C590A6F142DF9E35FD0C4E3AA96D69DE64218D5891A4AB4F64DC62DBCDA8AE5E32833DA57A5C504A08BFD85889701E466EBBCC88F99F6197A9CC92D07102C11B3B17C3D6C7C1D6CFB1DEB29E9FFF29E4E836F4E726FB577ED10D3CF5D5A8200000

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
  * When {@link baselineTarget} has not observed the given word during
  * training, this classifier extracts suffixes of the word of various
  * lengths.
  *
  * @author Nick Rizzolo
 **/
public class suffixFeatures extends Classifier
{
  private static final baselineTarget __baselineTarget = new baselineTarget();

  public suffixFeatures()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "suffixFeatures";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'suffixFeatures(Token)' defined on line 133 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token w = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    int length = w.form.length();
    boolean unknown = POSTaggerUnknown.isTraining && new baselineTarget().observedCount(w.form) <= POSLabeledUnknownWordParser.threshold || !POSTaggerUnknown.isTraining && __baselineTarget.discreteValue(w).equals("UNKNOWN");
    if (unknown && length > 3 && Character.isLetter(w.form.charAt(length - 1)))
    {
      __id = "" + (w.form.substring(length - 1).toLowerCase());
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      if (Character.isLetter(w.form.charAt(length - 2)))
      {
        __id = "" + (w.form.substring(length - 2).toLowerCase());
        __value = "true";
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        if (length > 4 && Character.isLetter(w.form.charAt(length - 3)))
        {
          __id = "" + (w.form.substring(length - 3).toLowerCase());
          __value = "true";
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'suffixFeatures(Token)' defined on line 133 of POSUnknown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "suffixFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof suffixFeatures; }
}

