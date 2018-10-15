/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DAE814A02C04C054FA21A02CC05AD3086D5841501C59DEE560B15705319C44414A7777A328DD8B47395CF7F3F29EC78DB0A2E40E48EAB51BC5DC61F2E4D3318858AA08714DC414DBED19435BA6893849744D6D94C96CEC0E0C206C74EE32B4D91873AE44B9B93A1A2E1AA19C29ABF6E97E63E19084103A30A8F9BF616A36ED2E357144CA9EF436356C76423E3A0F36AC6D5FE238CFF06C25973CF049AD50C1E1EE1AF730E23C946C63100000

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


public class headForm extends Classifier
{
  public headForm()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "headForm";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'headForm(Relation)' defined on line 37 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Relation r = (Relation) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    Constituent headC = r.getTarget();
    for (int i = headC.getStartSpan(); i < headC.getEndSpan(); i++)
    {
      __id = "" + ((i - headC.getStartSpan()));
      __value = "" + (headC.getTextAnnotation().getToken(i));
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      __id = "" + ("IC_" + (i - headC.getStartSpan()));
      __value = "" + (headC.getTextAnnotation().getToken(i).toLowerCase());
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'headForm(Relation)' defined on line 37 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "headForm".hashCode(); }
  public boolean equals(Object o) { return o instanceof headForm; }
}

