// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D825D4F6280401DFB23A94D046B01DE1BBAD34FED4B98EDC8960BC08B8477D0C25C4A9EF7FECE78CA2DB43422ECCCB973F6ED09B86F3A14D8770F22EC8974B1574490DBA62F816990C73809A1AF346A53CA061CD68B7C2453841F38B83B2436312C368FB2838724AC912DB78A3B65BF08301F044282C931E169B2F453B9938D46502BBAB6E0C898894B8FE572236B16A21FCA93CFC88AE9021FA298BAF4F4D0E7905D5B181394C236F754710668F6DAB112BCDEE047656B48298DF527E999975BC061B3E10F45BFCFF4387DA6573721242DCC24C74A9CCE7649A191B51B3AADBDBEA73959526315F13773EC51AF4D404CCC850748EA059B3D550B4832DB8133C7345FC38DF6FA6EAAB75AA6CC4281BB573D1EF93595D396D82B4D781ABCDABFCD5150D695CD870E2915CC3C0EF05253D27E82F210D730599B99027968CDDE03F9673254101D5074E0ECDEA69667A31ACC3A9EBF4363E4810963CA4BBE813EC7D03D676B30BE61FA15ED5E13CC88F98DFCFA5494D16004349E7B2874AE6DACD6F1954BD7E71472D07A017300000

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


public class Mixed extends Classifier
{
  private static final POSTagger __POSTagger = new POSTagger();

  public Mixed()
  {
    containingPackage = "edu.illinois.cs.cogcomp.quant.lbj";
    name = "Mixed";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Mixed(Token)' defined on line 68 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    int whatt = 1;
    int before = 2;
    int after = 2;
    int k = 2;
    int i;
    Token w = word, last = word;
    for (i = 0; i <= after && last != null; ++i)
    {
      last = (Token) last.next;
    }
    for (i = 0; i > -before && w.previous != null; --i)
    {
      w = (Token) w.previous;
    }
    String[] tags = new String[before + after + 1];
    String[] forms = new String[before + after + 1];
    i = 0;
    for (; w != last; w = (Token) w.next)
    {
      tags[i] = __POSTagger.discreteValue(w);
      forms[i] = w.form;
      i++;
    }
    for (int j = 1; j < k; j++)
    {
      for (int x = 0; i < 2; i++)
      {
        boolean t = true;
        for (i = 0; i < tags.length; i++)
        {
          StringBuffer f = new StringBuffer();
          for (int context = 0; context <= j && i + context < tags.length; context++)
          {
            if (context != 0)
            {
              f.append("_");
            }
            if (t && x == 0)
            {
              f.append(tags[i + context]);
            }
            else
            {
              f.append(forms[i + context]);
            }
            t = !t;
          }
          __id = "" + (i + "_" + j);
          __value = "" + (f.toString());
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
      System.err.println("Classifier 'Mixed(Token)' defined on line 68 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Mixed".hashCode(); }
  public boolean equals(Object o) { return o instanceof Mixed; }
}

