/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D719D4F43C03C068FFA8F2849AAAD43EA41E0362E0848001DA8B786DB1615C149874721ADF77C94631B9AC6717CF9F83FACD1586D3A0E50C3F35D7B823022B971CEDA09360F50C5F40E7088502885F27FDD60E60E2BA4914EE27C33D87906CF475825BB5BF61D419CF8325F79563AC1845E6CA75B3A271AB4E061C88477CD5E6CAEF6BE93E550B50CE30EF38D3C98398D31A22263A27BCDEBB3FADCB78750B4D8D4CA1D15A9458764BAF56994722BE7CC01C467C920EA6CB5B1CD8CC99D94AB44ECDB21E062A974BF9816AA71B49E583E0242B64690B07FD8E9875D8BF0D3425AAB995A7E1CEB16F953C6F71DBFD23FB7F100000

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


public class POSBetween extends Classifier
{
  public POSBetween()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "POSBetween";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POSBetween(Relation)' defined on line 177 of extent.lbj received '" + type + "' as input.");
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
      __id = "" + ((i - anchor));
      __value = "" + (r.getSource().getTextAnnotation().getView(ViewNames.POS).getConstituentsCoveringToken(i).get(0).getLabel());
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'POSBetween(Relation)' defined on line 177 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "POSBetween".hashCode(); }
  public boolean equals(Object o) { return o instanceof POSBetween; }
}

