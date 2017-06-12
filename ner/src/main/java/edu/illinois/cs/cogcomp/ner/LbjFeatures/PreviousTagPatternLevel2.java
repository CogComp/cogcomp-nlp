// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC3515B62C0301EFB276162902B02E3E473813AEBC4C90BE6B7C11DE9291D6AE294D2E68FFD7796A6D98AF0B7B15804EEEBBFEEBBF27D459D5814787503338B155496319BA9947E0D8E90E603B10B96CF658941A2A383C8EA1EB14D218DC4A199351A1B3E2CC46EF1F054A82615A138ADD12726982D5960D625CB85AC71AD945ADE32E695479A164C935F875C58B2C0CA3460E614365D4091F12C3B33A4FA023937CC8291D354348DB05AB732A97DAA5CAB9E43CA59750D126B23BC8733AC436290B2431ADB05631342298ED306FD18D1829B0D831F1E12CE0033B879169C67D8186D36F40DE558F4C296256A4B30541CE39C4811C00ABDD608D8ED63020F3B4999564353951AA7D3F24B17A48C43D833CC9E9185528883728C1B52FFFDF8D5DA36F31955F58C83CD14FFCA52D931F4E7BEC094B5ECD6DB13C60785E1A5B4FBA34B937ECFF3EEEDADC1A8850A0ED2A0385E7FE892E0D388ED32A390BE7A9052140D15AC2A6A1F3A80E62CDFF0FF559617C1B7D3183AE53F4B8C9ED96A401FD6A56DD58FE70BC478EBAAE300000

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
      System.err.println("Classifier 'PreviousTagPatternLevel2(NEWord)' defined on line 588 of LbjTagger.lbj received '" + type + "' as input.");
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
        if (ParametersForLbjCode.currentParameters.useFE)
        {
          __id = "" + (word.domainName + "");
          __value = "" + (res);
          __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
        }
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'PreviousTagPatternLevel2(NEWord)' defined on line 588 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTagPatternLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTagPatternLevel2; }
}

