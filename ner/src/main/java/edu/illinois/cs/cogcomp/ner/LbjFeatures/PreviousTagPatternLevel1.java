// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000DC25D4F4201301DFB2343194B12638C150D4C81CB84094C55F86A0B309A92D5C6BBC60D0FFDD9DDEE2F110EE6639E166EDB997FEDE4AADFCC160CB28983C5BEC27F98A5C445808ECE807D86E68F878F199B41A0A740C0EA1E714FC18F44935B422A3FF499B1D4FBE13B415EC277E0D68D7F4EC1558CD1AF42B73F4DFCC605ADAF76CD0767961231286743B51EE0A6502A29027553DAF0FE83B099385542310D2615719B8E3CB607ADE20C8A92A1A62B716DFA49710D2286E6C88ADAC878421618E2EA79AD72E844217B404347EB75124A5C1595EE3C610D87CBC0B463B2C836B4CE90AB4515636E4638B6B104351BB4231600D3867BB606DA7775098FD9B23E939522F9AB3D92586D1384596A3438B4AFD0CB09438794093E41F022CF7A97C6B2D1D832FAF709B08B78EE9D84A57299C1AEC8D4F9F4D757E0CB7BFBC0A3BC2FE28EC2EC5EF7CBD77137421F8324CE058132F780C5B08E00BF464F216B473192150D15BC3A52BFC81CD6305AFEF0014FBB08629300000

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
      System.err.println("Classifier 'PreviousTagPatternLevel1(NEWord)' defined on line 330 of LbjTagger.lbj received '" + type + "' as input.");
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
      }
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof NEWord[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'PreviousTagPatternLevel1(NEWord)' defined on line 330 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "PreviousTagPatternLevel1".hashCode(); }
  public boolean equals(Object o) { return o instanceof PreviousTagPatternLevel1; }
}

