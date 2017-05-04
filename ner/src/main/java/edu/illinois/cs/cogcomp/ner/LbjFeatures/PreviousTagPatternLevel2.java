// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC351CE4201301DF59163194B1263483A8A981387190298BAE1D41670253BBD26BD563868F777ABD56180077F2D4A3FEDCCCB97D966ACDCD2A7CB289A5C5B235AB45E27A2DB74BA7C8B6CC70C623AF036338A8E0E07B7D0FB0A610C6A2DAC2821DAB72367C3BFA74391A89796D2A6FB7C4C205AF2D2AB4DCB932CD86F25967FC8B169C5A68907E4D3ED17EED85855440EE04365D4091F12CBA7BA4F2127933CC90C4E52912CE482DDB11DCB65D265D47A16DACB28E055B23FC9733AC464421698636B71AC5A6944259F048D5706BF29C586C1780F016B089B3CBC4B473B2C856307F48D14540061439235AD382A06F9462CD2C00ABDD688D8ED63020FBB499B364353951AA7DB024B17A48CC2B15E850D330BA4051E28827C69CFF7F36B5BE8DD44E4DF023E07F0DF3B6947E4C3934D911475ECC5DB13C60BF5E1A5BC0BA34B937ECFF3EEEDADC13985C21CB56164BCF1C335C1A709C762472173C431352A0A328C1A6A1F3940E6E0FE5FAC666A02FC724FD824A4BC806DC6FF0BDC153384B300000

package edu.illinois.cs.cogcomp.ner.LbjFeatures;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.WordEmbeddings;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.WordTopicAndLayoutFeatures;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.*;
import java.util.*;


public class PreviousTagPatternLevel2 extends Classifier
{
  public PreviousTagPatternLevel2()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "PreviousTagPatternLevel2";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTagPatternLevel2(NEWord)' defined on line 530 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PreviousTagPatternLevel2"))
    {
      Vector pattern = new Vector();
      String label = "O";
      NEWord w = (NEWord) word.previous;
      if (w != null)
      {
        if (NETaggerLevel2.isTraining)
        {
          label = ((NEWord) w).neLabel;
        }
        else
        {
          label = ((NEWord) w).neTypeLevel2;
        }
      }
      else
      {
        label = null;
      }
      for (int i = 0; i < 2 && label != null && label.equals("O"); i++)
      {
        pattern.addElement(w.form);
        w = (NEWord) w.previous;
        if (w != null)
        {
          if (NETaggerLevel2.isTraining)
          {
            label = ((NEWord) w).neLabel;
          }
          else
          {
            label = ((NEWord) w).neTypeLevel2;
          }
        }
        else
        {
          label = null;
        }
      }
      if (pattern.size() > 0 && label != null && !label.equals("O"))
      {
        label = label.substring(2);
        String res = "";
        for (int i = 0; i < pattern.size(); i++)
        {
          res = (String) pattern.elementAt(i) + "_" + res;
        }
        res = label + "_" + res;
        __id = "";
        __value = "" + (res);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        __id = "" + (word.domainName + "");
        __value = "" + (res);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'PreviousTagPatternLevel2(NEWord)' defined on line 530 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTagPatternLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTagPatternLevel2; }
}

