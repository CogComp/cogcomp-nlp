// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC351DE42C0341DF59BC2194B12E28C3A3431360F542842E4D74358D584DC6D26BD1B0A1EFDDBD57C0800FEEB439EDB7E4FE937677994B3738E0FA06A607D25796351BC9A07E0D8A13EA13FB16391D78639145470781E53CF28C500B9A032A022A1BF4ADC876F5F8A33C87E5A138ACDEB71F2058B2D0AD45FB95AEB65E48456F9173C2AB43032EC966CE6A2CD143A087D212E5534B40ED17EE4B1855023105165D4191F40E5D919A6909B8916E4DC8E52A4A65E514780A56E93F6CAC46442169862C8F85AD4D09842EA70CE8EC6FA287CA07CEBC90C61037B879169E66581E95F8D314B751EB1B02B1C4A27029A8D729903481047BBD00B1DBD6126CFE254E6919522F92B7D3F24B9812619563AC1B0AFC0CAA89E1E28027C92E1448FF43F8D6DA367E8CACF146C1EE1AF76329EC946278A3343D6933B5FA30B1CE73386D2DFE50DA5C9BCFF87A7B137021B0F40F6158122F707C4278E144F911D9485FE6205280A3A6954546F3A80E6F0FE5F6976AB0ACC724FB197A4B80F1DC6FF0699811064B300000

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


public class PreviousTagPatternLevel1 extends Classifier
{
  public PreviousTagPatternLevel1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "PreviousTagPatternLevel1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTagPatternLevel1(NEWord)' defined on line 374 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PreviousTagPatternLevel1"))
    {
      NEWord w = (NEWord) word.previous;
      Vector pattern = new Vector();
      String label = "O";
      if (w != null)
      {
        if (NETaggerLevel1.isTraining)
        {
          label = ((NEWord) w).neLabel;
        }
        else
        {
          label = ((NEWord) w).neTypeLevel1;
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
          if (NETaggerLevel1.isTraining)
          {
            label = ((NEWord) w).neLabel;
          }
          else
          {
            label = ((NEWord) w).neTypeLevel1;
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
      System.err.println("Classifier 'PreviousTagPatternLevel1(NEWord)' defined on line 374 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTagPatternLevel1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTagPatternLevel1; }
}

