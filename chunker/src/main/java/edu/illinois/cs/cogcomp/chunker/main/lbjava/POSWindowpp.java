// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D519F6B43C03016CFBAC93074A4C6998FECC6EB0FB082C60EB8154AEA7DE2D594A4A99514CFEEE5EF4D8B15863779BBFD3FCD5B693E1C0A5CB587D79DEB9455BE96818DE4F9051C4AD4D91CA378F609AC2C7063AD02C60E148F8BA6C2A941E92D15A08840AC93ACD14F5D86360208830C4258B2AA785F622A69BC05673B1057EEB710C9BCC84C36F607599F0B058F5650CFC51AE902F8E2985351381CF49AFC36226E9B72E4FF079ACC1F6B6D845DEEB40B55B325592C96EC5423F8E693CD79E43DB4B7B1288B4A4ECF98B290767D90B3AEE527E52DD2D2C7755BD2A163569641A5F57189D1CA1E44F1ECD57E5DECC39A8E155B6F84980531C8E3F9B968CF5371EE342956229CC14B2B46B2027E08E7647E697296EBFBCD5A85C470949D00B9BE866F597BA5453C08AA66B87F5489C6E4481F31AB4F7D32AA11DB22530DBB3874A61BA38376EB68E9F50E823EF4ABA200000

package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.chunker.utils.Constants;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;
import edu.illinois.cs.cogcomp.pos.lbjava.POSWindow;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


public class POSWindowpp extends Classifier
{
  private static final POSTagger __POSTagger = new POSTagger();

  public POSWindowpp()
  {
    containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
    name = "POSWindowpp";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'POSWindowpp(Token)' defined on line 106 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    int before = 3;
    int after = 3;
    int k = 3;
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
    i = 0;
    for (; w != last; w = (Token) w.next)
    {
      tags[i++] = __POSTagger.discreteValue(w);
    }
    for (int j = 0; j < k; j++)
    {
      for (i = 0; i < tags.length; i++)
      {
        StringBuffer f = new StringBuffer();
        for (int context = 0; context <= j && i + context < tags.length; context++)
        {
          if (context != 0)
          {
            f.append("_");
          }
          f.append(tags[i + context]);
        }
        __id = "" + (i + "_" + j);
        __value = "" + (f.toString());
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'POSWindowpp(Token)' defined on line 106 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "POSWindowpp".hashCode(); }
  public boolean equals(Object o) { return o instanceof POSWindowpp; }
}

