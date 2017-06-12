// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC3515B62C0301EFB276162902B2EC7C5DD06C8AF23172CABDE1744B7A464B9AB425B8B1EF7FD5A9A5515F16F63A018EDD7FDDD77F5E29A4337D861FA06AA17D2B82D422693516D2A6536C536673C621FB71A358A8E0E03CB68F109B006351A54E444D66458E1FCE3F1B8413C979A53A2BBFC58B0416B4D86292E5D05EB05658456E9073C28B4D030EC9A7CEAB2CD143A087D212C5534B80ED07E6B0D0B2F4620A2CAA9023E11CB85D25D2123133CC8291C37015DACB28E014BCC23EDC82398942C215BF6F1A439862194C5708D1D9DE550F051E8D58328D206660F23C29DCA0D79578D314B351E21B0A138945614250BF4231680308E67B1063A7BD0488F55A8CC03A1986E49DBE931AD8D01A843D833CC9EA18551251EC9027CE2E1858FF4DF8D6DA36731919FD8C83CD34FFCA52D931F4E057AF4A92766AE570638DF660DA5ABDB0A5B837EFF177F6D66F426EB40F6158ED2F70BC4278E140F110D948573D87A8714749238A86CF0208B5FFFF787755A1C15CE4F8FA15FBC84B8C9EE9624017D6ADAAB5FFDF20ACC9B51BAE300000

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
      System.err.println("Classifier 'PreviousTagPatternLevel1(NEWord)' defined on line 418 of LbjTagger.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'PreviousTagPatternLevel1(NEWord)' defined on line 418 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTagPatternLevel1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTagPatternLevel1; }
}

