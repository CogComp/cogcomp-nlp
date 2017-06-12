// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B880000000000000005C0914B620130158FFAC47148D05CC16F8DDA7A2BE5C561F0B5C37477C52213199944192EF773BAB52E5A582E1AC04E23F2FEB7F6A3DCB124F8318521E15BB0CDAAEB8A1F886A8CB9A65E8A3835C740CBF41EB04F612F5A225BF8F5887EE8AE5FEE3C5782731880DAF4B39B545E30127BEE393EED95FA4B5E50E93FCE115990138FA5F0AB48F602CCE60D2F6CB39C3C8A7879918D06C881394E3624109432E74573554E5F847F272537B413196BDF02064BC809D4B8C0ED029FEFE4012D26DA6D86ACFBC16203ECBA4CC1A8ACECDE366962A40E5F98417983862A9E3158AD3F10FE72AF7F65F815E296EA0E43EDE784B200000

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


public class PreviousTag2Level2 extends Classifier
{
  public PreviousTag2Level2()
  {
    containingPackage = "edu.illinois.cs.cogcomp.ner.LbjFeatures";
    name = "PreviousTag2Level2";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'PreviousTag2Level2(NEWord)' defined on line 499 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    NEWord word = (NEWord) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PreviousTag2"))
    {
      int i;
      NEWord w = word;
      if (w.previous != null)
      {
        if (((NEWord) w.previous).previous != null)
        {
          if (NETaggerLevel2.isTraining)
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neLabel);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            if (ParametersForLbjCode.currentParameters.useFE)
            {
              __id = "" + (word.domainName + "-2");
              __value = "" + (((NEWord) ((NEWord) w.previous).previous).neLabel);
              __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            }
          }
          else
          {
            __id = "-2";
            __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel2);
            __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            if (ParametersForLbjCode.currentParameters.useFE)
            {
              __id = "" + (word.domainName + "-2");
              __value = "" + (((NEWord) ((NEWord) w.previous).previous).neTypeLevel2);
              __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
            }
          }
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
      System.err.println("Classifier 'PreviousTag2Level2(NEWord)' defined on line 499 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTag2Level2".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTag2Level2; }
}

