/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6E814E02804C054FA2DD89CC480E104171688EAC5907189045C988D267A44313CDDD10D0AB073D4AFFFBEFFDAD78A4051710764757EEA3374C6D9A76201B095A0F48C9928A7DE194FD01C60469D0A69E42E4367D0726103E3AFE3A73133AF58A31D2A374322E12BFA3B3AA76D394C6C29245C353055F2B769BEF6A498F0DD211BE4F591B396C71423E3E9704A081BB12DFBDB616506E31AB4CB5FEAD03FAAC70EB3A4EE20640366D003CB004B09912AF0100000

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


public class headCap extends Classifier
{
  public headCap()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "headCap";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Relation))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'headCap(Relation)' defined on line 76 of extent.lbj received '" + type + "' as input.");
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
      String curForm = headC.getTextAnnotation().getToken(i);
      __id = "" + ((i - headC.getStartSpan()));
      __value = "" + ((curForm.equals(curForm.toLowerCase())));
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Relation[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'headCap(Relation)' defined on line 76 of extent.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "headCap".hashCode(); }
  public boolean equals(Object o) { return o instanceof headCap; }
}

