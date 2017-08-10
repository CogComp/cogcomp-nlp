// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D6D8D4B62014C004FFA4E22CC0659E9BBA715A280586141C3F83B9590C6766942B5CF06FFBB99E61DB4F43974E5ED4D4219151F6D48650F5049B7C162078CC5FEF2D1E635399FB3825E4E6D9398296F89412A785CC0E60F142A0D060DE9150690BADE7E664A7F3B2788A997E724DFF271D750640E8CA7467BFA59D3B874D6E247547621D47AEDEBAFA04C0D997EAC4EF9E95FD1997B356A4720AAFC54A19C34756FB1261D2DA36ECD26840F31AD22DA64CF3B678137AAB8628940F7B4F654DA06081EEFAEB80BFA2100000

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


public class WordTypeInformation extends Classifier
{
  public WordTypeInformation()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "WordTypeInformation";
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
      System.err.println("Classifier 'WordTypeInformation(Constituent)' defined on line 91 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getWordTypeInformation(c);
    for (int i = 0; i < features.size(); i++)
    {
      Pair p = (Pair) features.get(i);
      String idx = (String) p.getFirst();
      boolean val = (Boolean) p.getSecond();
      __id = "" + (idx);
      __value = "" + (val);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WordTypeInformation(Constituent)' defined on line 91 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordTypeInformation".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordTypeInformation; }
}

