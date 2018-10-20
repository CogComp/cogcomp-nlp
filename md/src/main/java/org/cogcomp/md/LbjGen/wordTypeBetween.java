/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D715D5B6201301CFB2B8058C12A8D7DADE345150961D2DB3AD7C29EDDA6375D44295F3AD2DFFEDDD441D26D21809DCCCEC662359905A742CB08D83F551F1B4CE12D601DAA74C9B623EC28FC0EAB50F506C21402DE9645D61E60E2BB1F60D659AEE07D3505EBD334ACDDAC798A2B876969C79ADA26D188071AD3FE72076C34E860C982D0C65988DD3C4DFF4FAB0FD083F08F78CE177C959D33E24465C9B6B5EB93FC4EDB87716AC5B21CCCFAB63C6C3943F9B9D41F97F6201C4153DAC360627F643916AA60B52FAB4271F90FCBB0564695A7113F83DF1E207B47B6DA3A487CBB7238B152BD85F2034BB89CDD06C9744AFEC60234B24B41AFE6D8ED8D951EED931731175D9C2B327257A86B66B0BF7E6B389F449F78A397970D6C3C2997A4E49D1872A6533F272912062E7DAA45560B41C786C7021DA757E6E8AD2CA5FC584DB45EE859369EC6524B086930515A6243E5A1CBB230DAD56191C594FB404CBE7008770766EBD200000

package org.cogcomp.md.LbjGen;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.infer.ilp.OJalgoHook;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.MyString;
import java.util.*;
import org.cogcomp.md.*;


public class wordTypeBetween extends Classifier
{
  public wordTypeBetween()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "wordTypeBetween";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'wordTypeBetween(Relation)' defined on line 211 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    int startIdx = 1;
    int endIdx = 0;
    if (r.getSource().getStartSpan() < r.getTarget().getStartSpan())
    {
      startIdx = r.getSource().getEndSpan();
      endIdx = r.getTarget().getStartSpan();
    }
    else
    {
      startIdx = r.getTarget().getEndSpan();
      endIdx = r.getSource().getStartSpan();
    }
    int anchor = startIdx;
    for (int i = startIdx; i < endIdx; i++)
    {
      List features = BIOFeatureExtractor.getWordTypeInformation(r.getSource().getTextAnnotation().getView(ViewNames.TOKENS).getConstituentsCoveringToken(i).get(0));
      for (int j = 0; j < features.size(); j++)
      {
        Pair p = (Pair) features.get(j);
        String idx = (String) p.getFirst();
        boolean val = (Boolean) p.getSecond();
        __id = "" + (idx + "_" + (i - anchor));
        __value = "" + (val);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'wordTypeBetween(Relation)' defined on line 211 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "wordTypeBetween".hashCode(); }
  public boolean equals(Object o) { return o instanceof wordTypeBetween; }
}

