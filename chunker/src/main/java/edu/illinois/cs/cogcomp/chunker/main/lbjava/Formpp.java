/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D519DDE448030158F556CD4CD04A2465F2D2871E58F20A77B126079167B0C6B4A411313EBBB3DF1CEAB12106E033FD9335A113D143A1CB58715AFC3E89CB9AE152C2A473924191C7380960E30B55A11A4870EEAEAB538A369D7C751C1201843B49B381AE9C48283017021145EE8AF1A8230A6BB5FD673528C97818303622523F03BE355AEACC52E791E0F3758A728C24A426D29F8A1F358A97A84CC2374C5E2071BDC2FE5D8612F8BFAC28FC315B94C565130A9588BC0EEB2B6AECBD5E0E4062B2B109F5978DCB6D9D167F281BA2C964E6598B90D9E57E98D1410D3D381B03779746E939F082F86E44A8F62F93F97E6B5A87DEFB0FE5C425E1D7E0A4A1A45E99B614F33A3B787286DBF3DEACD28EED354B09CAD84BBFEC58DCBE17449D42B97FD4AE67B5580BE74875EEBF4827247E943147FE0E1966C82F9D317D347DF264DD20578A200000

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


public class Formpp extends Classifier
{
  public Formpp()
  {
    containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
    name = "Formpp";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'Formpp(Token)' defined on line 131 of chunk.lbj received '" + type + "' as input.");
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
    String[] forms = new String[before + after + 1];
    i = 0;
    for (; w != last; w = (Token) w.next)
    {
      forms[i++] = word.form;
    }
    for (int j = 0; j < k; j++)
    {
      for (i = 0; i < forms.length; i++)
      {
        StringBuffer f = new StringBuffer();
        for (int context = 0; context <= j && i + context < forms.length; context++)
        {
          if (context != 0)
          {
            f.append("_");
          }
          f.append(forms[i + context]);
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
      System.err.println("Classifier 'Formpp(Token)' defined on line 131 of chunk.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "Formpp".hashCode(); }
  public boolean equals(Object o) { return o instanceof Formpp; }
}

