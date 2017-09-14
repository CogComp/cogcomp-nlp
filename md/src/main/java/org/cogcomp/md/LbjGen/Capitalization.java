// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D61914B43C040158FFAC4B02C688D051F6635143824140F0D2ED7B9E4BC26C99ABB3B65C6BFFDD9D4A196146F03B9CB9FEDCCB465E24DE1507FB6BD40CB601F11F875059DDA31BD8BF1B2E89C45C414C9444218A3382740B787378B385A3E6BE41AE26382B0CF69702269E0BC2DBB4AB91E3ECE5789D406DCE1C835717A8C8E662A5B48B5D297E99A7625AE8E76BAED07E26DBCC7B65DB02707AD8B603D574903683C1E4DE7F3DE75944D31DAA362976DC42E822E40E807193AA8E1E20B335345CF58ED1D661CFE846EFBD5AD0766C9D967A4E6389205C6A94357E298E678C33BFF84974FA580772815C69CCD0A528ADE6556338EB7B0CFC8AF33CCFD3B0FB0FE0D75630A64252302504DF477798D49B4FCF2014C095569C100000

package org.cogcomp.md.LbjGen;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;
import org.cogcomp.md.*;


public class Capitalization extends Classifier
{
  public Capitalization()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "Capitalization";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  private static String[] __allowableValues = DiscreteFeature.BooleanValues;
  public static String[] getAllowableValues() { return __allowableValues; }
  public String[] allowableValues() { return __allowableValues; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Capitalization(Constituent)' defined on line 37 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    View bioView = c.getTextAnnotation().getView("BIO");
    for (int i = -1; i < 3; i++)
    {
      int curId = c.getStartSpan() + i;
      if (curId < 0 || curId >= bioView.getEndSpan())
      {
        continue;
      }
      Constituent cCur = bioView.getConstituentsCoveringToken(c.getStartSpan() + i).get(0);
      if (cCur != null)
      {
        String cCurForm = cCur.toString();
        boolean cap = (!cCurForm.equals(cCurForm.toLowerCase()));
        __id = "" + (i);
        __value = "" + (cap);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'Capitalization(Constituent)' defined on line 37 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Capitalization".hashCode(); }
  public boolean equals(Object o) { return o instanceof Capitalization; }
}

