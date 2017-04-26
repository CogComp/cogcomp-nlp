// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC2516B62C0301DFB276062902B02E7C5DD06C07F562E4857BD7C11DE9291135794A6173CFFEBB63DAE44DFE3A018EDB7977FEE5E23DE7EE030E51C4D1E647E58F45D27AA2404767C8B14330E391DB7EE23829E0103CB68F10DB00E355E4DA8E2A3FF89BB1FCE3F12FC05ECB07E0D683062718A241E0D7A9FBA72C37B1496BEF907BC9D5A68C480AE1F683F09B3857440E61C269D419B840E5283D679064DC0D018CE99520DA15AFF6C4B8AD5BC573D929AD9790D125B2C811DC82391195852AB8DE5A6F9A323942F511ADE0CF02924A5C1755E4067086C3E56A5ABD53649DA8B72CEAC5450C28625EAD600D45CE39D4812C00ABDD688D8FDD7142E751AC87E4353541AB7DBAC863949459563238B2A760E5A421E5115E8329FFF97CEA6F1DE44E5F732710770DF3B1947E4239FBE3328EB899FA7368F0E0BC34B695DAE0D6EC9BCFF8BBFE36E879874901B7616C8CFE307D20A70CE381D94CDA6A98752A1A328C3A5A1F9138B969A4FDF207BB5A8029300000

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
      System.err.println("Classifier 'PreviousTagPatternLevel2(NEWord)' defined on line 474 of LbjTagger.lbj received '" + type + "' as input.");
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
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'PreviousTagPatternLevel2(NEWord)' defined on line 474 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTagPatternLevel2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTagPatternLevel2; }
}

