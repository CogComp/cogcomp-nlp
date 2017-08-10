// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056DC1CA02C03C0081E759C548E81A87673F2A82802828F405AB83302DA4299E05C777B172A70F4D68C7DFBD0940645C11C283DD2E2FCD9822FEDBE94CD2354152DE03A24820AE13C306B42A0744FAD13A0CC161B9DDA78965DB2BF0A9872D2AEF7C24151C131383AC53ACF27A55E3AEF6B62247747911595619F3ADB7268B467ECE65CFC5EAB32376D96A731B98A8D266471BDEA9854DA47056A8D2C5DF9D0D03D7CC1034A8D8121C828FE0DCC4650F487EB00E6F81A6B51100000

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


public class BrownClusterPaths extends Classifier
{
  public BrownClusterPaths()
  {
    containingPackage = "org.cogcomp.md.LbjGen";
    name = "BrownClusterPaths";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Constituent))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BrownClusterPaths(Constituent)' defined on line 101 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Constituent c = (Constituent) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    List features = BIOFeatureExtractor.getBrownClusterPaths(c);
    for (int i = 0; i < features.size(); i++)
    {
      Pair p = (Pair) features.get(i);
      int idx = (Integer) p.getFirst();
      String val = (String) p.getSecond();
      __id = "" + (idx);
      __value = "" + (val);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Constituent[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'BrownClusterPaths(Constituent)' defined on line 101 of md.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BrownClusterPaths".hashCode(); }
  public boolean equals(Object o) { return o instanceof BrownClusterPaths; }
}

