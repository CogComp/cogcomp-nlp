// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D825D4F63803C0DFB2E65A550AC04DEE8B4BB3CEE3D625B7BA0D44B8169695255803096ADF7F93F1D2D2BBC48408DE7E7E7E79412A9DB6438770F22A7C22AD8A32A48E49E22685620FD024A18D169A432CA0E18BB83F2D0AE12C3E0FB2834060AC9569B78A3F6C4808301F044282C931E169BA0453B9978D46502BDAB6E0C898896878E5FAA26716A21B73C1E76445F40940594C5D5A72D8F5245BDC0C8942E81BBB2AB10669F6D64B095D63303975D01A42677EC5066614D238546C70C35DE3FF3D014BA3DDC9480943BB01F1962BBF955A656C654645B7B7D5F62FAA24D157137F3EC71AB4D604CCC8D0E80D92C18A6B0E4F952C19E3C895EBB45B7FA3A7AA2D9A5FEBAB35AA6CC5285BBD8E61FFC1593D396D82B23F1E62ADE2FBEE37B5694B56973E18F46413F160FE59434BC97AC3704770E06FCC409B479CDDE0B096F3254901D9174E0ECDDA696E7A31AC22A9EBF436BE485091BCA4BBE813ED7D16A56EA30BE60FA151C5E13CCA8F98BBE7D0A4AE0B00A1A4FE30C3257B15E7BF8C1ADD3FB09DE19A7C26300000

package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.chunker.utils.Constants;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;
import edu.illinois.cs.cogcomp.pos.lbjava.POSWindow;


public class Mixed extends Classifier
{
  private static final POSTagger __POSTagger = new POSTagger();

  public Mixed()
  {
    containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
    name = "Mixed";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Mixed(Token)' defined on line 67 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

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
      for (int x = 0; x < 2; x++)
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
      System.err.println("Classifier 'Mixed(Token)' defined on line 67 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Mixed".hashCode(); }
  public boolean equals(Object o) { return o instanceof Mixed; }
}

