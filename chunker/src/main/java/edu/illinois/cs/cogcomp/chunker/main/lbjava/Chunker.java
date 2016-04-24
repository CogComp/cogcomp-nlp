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
// F1B88000000000000000D8251CE4B11301DB7FB2648834211CE60A7B290502D2215540114A5EC38D3BBB6DA36B56B731202AFDED1B7305ADB4BB71BD3BF6EDCB73335E864F1006403724F684280D8128AA419788D06407E94A2113002C5C5DD0886A33D244CA1A65B623C1DD85F21C313E209984233B162CF4485947AB4BD2999499FE500C3F9965CC0B0F4B656BB0B4CA3CB045EDEA27E5C64500782A5CA900D8C738FDCDDDB232DE6A7C23E823E7280D87AA6AB7D4C8EE3595AE36F5880547AA31509CEACF92C6D2CEAC5981C651738E9A07D8BFC1A5ADE4F27E71FD8977FE49FCC46CF84ECA66BEB3903921F43979D096D9ECED1D5A5FBA0B37470DBC8C7F91A35115BA72C8ACEBA5E38DDFCB7E63D27BE8EAC45C9EF7228D8EB3A93FAA25F84FA50EDCF20BC40DAE1C3AFD20B118E20F8B85455481B3EEE373779CDEBCC4FE6A82054341041F80773B4E2D0AC46EC7732A222BAB7B2AA84BCFD2C4548D82E61405B27A7BC3D6E28D31CA1577406BAE72793D296AFBBA5598267ADC853786A3DAB84FAC1933CE2636D3C5B21D2CDAA7A72BADE7081D8A409A280EA6403B4B774E7095A6ED9B16FEC4AAEF995E3C4E0147A54EE1F37C702DCD7256AE7528DFDF326B21B18B378E30D5354E4E6BF5E7FFCBB8E9FA9C3F4F5E28CB0271DB535C20D3EA858876F5035034B9FF1CE068720EA8CA8853CD22B19924174928CDF21DA1A018327C720F08186EBCFEFF0AE18362C7188412E56A5B848D953A4B4F46250E7098869BC6E8BDBE9FCF87C3E1F22179F1CCCA9011D4C054A4FF2811783C433AFAF81D3A88051AE04C10B53FE258B9E2ABE22C7C17AF08C8CF502A76F33E82400000

package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.chunker.utils.Constants;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.lbjava.POSWindow;

/**
 * Learned classifier that predicts a BIO chunk tag given a word represented
 * as a <code>Token</code>.  {@link PreviousTags} from
 * this package and {@link POSWindow} from the
 * <a href="http://l2r.cs.uiuc.edu/~cogcomp/asoftware.php?skey=FLBJPOS">LBJ
 * POS tagger package</a> as well as <code>Forms</code>,
 * <code>Capitalization</code>, <code>WordTypeInformation</code>, and
 * <code>Affixes</code> from the LBJ library are used as features.  This
 * classifier caches its prediction in the <code>Token.type</code> field, and
 * it will simply return the value of this field as its prediction if it is
 * non-null.
 *
 * @author Nick Rizzolo
 **/


public class Chunker extends SparseNetworkLearner {
  private static ResourceManager rm = new ChunkerConfigurator().getDefaultConfig();
  private static String modelFile = rm.getString("modelPath");
  private static String modelLexFile = rm.getString("modelLexPath");

  public static boolean isTraining;

  public Chunker(){
    this(new Parameters(), modelFile, modelLexFile);
  }


  public Chunker(String modelPath, String lexiconPath) {
    this(new Parameters(), modelPath, lexiconPath);
  }

  public Chunker(Parameters p, String modelPath, String lexiconPath) {
    super(p);
    try {
      lcFilePath = new java.net.URL("file:" + modelPath);
      lexFilePath = new java.net.URL("file:" + lexiconPath);
    } catch (Exception e) {
      System.err.println("ERROR: Can't create model or lexicon URL: " + e);
      e.printStackTrace();
      System.exit(1);
    }

    if (new java.io.File(modelPath).exists()) {
      readModel(lcFilePath);
      readLexiconOnDemand(lexFilePath);
    } else if (IOUtilities.existsInClasspath(Chunker.class, modelPath)) {
      readModel(IOUtilities.loadFromClasspath(Chunker.class, modelPath));
      readLexiconOnDemand(IOUtilities.loadFromClasspath(Chunker.class, lexiconPath));
    }
    containingPackage = "edu.illinois.cs.cogcomp.chunker.main.lbjava";
    name = "Chunker";
    setLabeler(new ChunkLabel());
    setExtractor(new Chunker$$1());
  }

  public String getInputType() {
    return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
  }

  public String getOutputType() {
    return "discrete";
  }

  private Feature cachedFeatureValue(Object __example) {
    Token word = (Token) __example;
    String __cachedValue = word.type;

    if (__cachedValue != null) {
      Feature result = new DiscretePrimitiveStringFeature(containingPackage, name, "", __cachedValue, valueIndexOf(__cachedValue), (short) allowableValues().length);
      return result;
    }

    Feature __result;
    __result = super.featureValue(__example);
    __cachedValue = word.type = __result.getStringValue();
    return __result;
  }

  public FeatureVector classify(Object __example) {
    if (__example instanceof Object[]) {
      Object[] a = (Object[]) __example;
      if (a[0] instanceof int[]) return super.classify((int[]) a[0], (double[]) a[1]);
    }
    return new FeatureVector(cachedFeatureValue(__example));
  }

  public Feature featureValue(Object __example) {
    if (__example instanceof Object[]) {
      Object[] a = (Object[]) __example;
      if (a[0] instanceof int[]) return super.featureValue((int[]) a[0], (double[]) a[1]);
    }
    return cachedFeatureValue(__example);
  }

  public String discreteValue(Object __example) {
    if (__example instanceof Object[]) {
      Object[] a = (Object[]) __example;
      if (a[0] instanceof int[]) return super.discreteValue((int[]) a[0], (double[]) a[1]);
    }
    return cachedFeatureValue(__example).getStringValue();
  }

  public int hashCode() {
    return "Chunker".hashCode();
  }

  public boolean equals(Object o) {
    return o instanceof Chunker;
  }

  public static class Parameters extends SparseNetworkLearner.Parameters {
    public Parameters() {
      SparseAveragedPerceptron.Parameters p = new SparseAveragedPerceptron.Parameters();
      p.learningRate = .1;
      p.thickness = 2;
      baseLTU = new SparseAveragedPerceptron(p);
    }
  }
}