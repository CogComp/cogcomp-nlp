// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D845D4F6AD0401DB7E75CC12700406A07D09A245A6F25B20A045D3F8DE1BDB269D5B676D8BD44CF7FECEAD4862A65D2704E995FB73F1FEDA7E399CD00C406BE8A059B756A20F531438EC38D218B12ACBE0F412B59A31918ECAB26A21B268C5323BA2519305C197491F46A0A20CB58C800B964B298CA1DF35EF22691ABAD231496DD15E99C118DC89CD94EDB2A572D24462FE05919021087970DACC1228FB808EC3DB4623462970AD3ABA8CF97A1183C996CC84FEB3BF984A01D51313FB134F7BCFAB4F4EEFC453C1D52D9E23B4719DF61F24264981D417DC16AB4C37463499415B68695625468290D7BE88562C5B70F4CE76E5D196A177DA90D581F5015957AC7D7CBCA8501ED9865DF29D1907810486605056BC5A3B7C842C41554F851FB3101A3CE9DE575FD94D004FFAB7F8DB84B6BF0678B27705D13FA500AC40EC842BACD614F1FBB0F621C93B92771D73BA97CC38814471DFA52471724DD25FE929A71F05AAB6D19A85D1B11FA8877AF9C24765391A350F89F05ED8533BEB0A965BE1A64210C190E10B5F5B570F8A2F30C39A7E76BADED0C4623782417E24A40BDDCE6F85554EEBA1BD991DEDE12C656C3C0623757F66C085DCA7932003D0E501903843F697E6F671DF21772066A742F2B8E34A1084EE568CEA56E0CA8A8D29BC9A1FECA946B8E0F82D6936860EE1C057FF3FEE86C770D427179C31AC857F09C22425EA66E7034CC2995ED5CB429EEFBDF38A74D801E918288318C2A84B9274DD4FDDD2B44C2C6B3CC4B4C51F8E279274DD87C3E0649FCF34C149B8EA8A93837C652685ED4BEB96D3C2F34CF18C732E71199D8BB0A7400000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.*;


/**
  * Predicting the part of speech of the given word, this classifier is
  * intended to be applied only on words whose forms were observed during
  * training.  {@link wordForm}, {@link baselineTarget},
  * {@link labelTwoBefore}, {@link labelOneBefore}, {@link labelOneAfter},
  * {@link labelTwoAfter}, {@link L2bL1b}, {@link L1bL1a}, and {@link L1aL2a}
  * are all used as features.  At test-time, the learning algorithm is
  * restricted to return a prediction from the set of tags the given word was
  * observed with at training-time.  The prediction is cached in the
  * <code>Word.partOfSpeech</code> field, and the value of that field will
  * simply be returned whenever it is non-<code>null</code>.
  *
  * @author Nick Rizzolo
 **/


public class POSTaggerKnown extends SparseNetworkLearner
{
  private static java.net.URL _lcFilePath;
  private static java.net.URL _lexFilePath;

  static
  {
    _lcFilePath = POSTaggerKnown.class.getResource("POSTaggerKnown.lc");

    if (_lcFilePath == null)
    {
      System.err.println("ERROR: Can't locate POSTaggerKnown.lc in the class path.");
      System.exit(1);
    }

    _lexFilePath = POSTaggerKnown.class.getResource("POSTaggerKnown.lex");

    if (_lexFilePath == null)
    {
      System.err.println("ERROR: Can't locate POSTaggerKnown.lex in the class path.");
      System.exit(1);
    }
  }

  private static void loadInstance()
  {
    if (instance == null)
    {
      instance = (POSTaggerKnown) Learner.readLearner(_lcFilePath);
      instance.readLexiconOnDemand(_lexFilePath);
    }
  }

  public static Parser getParser() { return null; }
  public static Parser getTestParser() { return null; }

  public static boolean isTraining;
  public static POSTaggerKnown instance;

  public static POSTaggerKnown getInstance()
  {
    loadInstance();
    return instance;
  }

  private static final wordForm __wordForm = new wordForm();
  private static final baselineTarget __baselineTarget = new baselineTarget();

  private POSTaggerKnown(boolean b)
  {
    super(new Parameters());
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "POSTaggerKnown";
    setEncoding(null);
    setLabeler(new POSLabel());
    setExtractor(new POSTaggerKnown$$1());
    isClone = false;
  }

  public static TestingMetric getTestingMetric() { return null; }


  private boolean isClone;

  public void unclone() { isClone = false; }

  public POSTaggerKnown()
  {
    super("edu.illinois.cs.cogcomp.pos.lbjava.POSTaggerKnown");
    isClone = true;
  }

	public POSTaggerKnown(String modelPath, String lexiconPath) { this(new Parameters(), modelPath, lexiconPath); }

	public POSTaggerKnown(Parameters p, String modelPath, String lexiconPath) {
		super(p);
		try {
			lcFilePath = new java.net.URL("file:" + modelPath);
			lexFilePath = new java.net.URL("file:" + lexiconPath);
		}
		catch (Exception e) {
			System.err.println("ERROR: Can't create model or lexicon URL: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		if (new java.io.File(modelPath).exists()) {
			readModel(lcFilePath);
			readLexiconOnDemand(lexFilePath);
		}
		else if (IOUtilities.existsInClasspath(POSTaggerKnown.class, modelPath)) {
			readModel(IOUtilities.loadFromClasspath(POSTaggerKnown.class, modelPath));
			readLexiconOnDemand(IOUtilities.loadFromClasspath(POSTaggerKnown.class, lexiconPath));
		}
		else {
			containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
			name = "POSTaggerKnown";
			setLabeler(new POSLabel());
			setExtractor(new POSTaggerKnown$$1());
		}

		isClone = false;
	}

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete"; }

  public void learn(Object example)
  {
    if (isClone)
    {
      if (!(example instanceof Token || example instanceof Object[]))
      {
        String type = example == null ? "null" : example.getClass().getName();
        System.err.println("Classifier 'POSTaggerKnown(Token)' defined on line 164 of POSKnown.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }

      loadInstance();
      instance.learn(example);
      return;
    }

    if (example instanceof Object[])
    {
      Object[] a = (Object[]) example;
      if (a[0] instanceof int[])
      {
        super.learn((int[]) a[0], (double[]) a[1], (int[]) a[2], (double[]) a[3]);
        return;
      }
    }

    super.learn(example);
  }

  public void learn(Object[] examples)
  {
    if (isClone)
    {
      if (!(examples instanceof Token[] || examples instanceof Object[][]))
      {
        String type = examples == null ? "null" : examples.getClass().getName();
        System.err.println("Classifier 'POSTaggerKnown(Token)' defined on line 164 of POSKnown.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }

      loadInstance();
      instance.learn(examples);
      return;
    }

    super.learn(examples);
  }

  private Feature cachedFeatureValue(Object __example)
  {
    Token w = (Token) __example;
    String __cachedValue = w.partOfSpeech;

    if (__cachedValue != null)
    {
      Feature result = new DiscretePrimitiveStringFeature(containingPackage, name, "", __cachedValue, valueIndexOf(__cachedValue), (short) allowableValues().length);
      return result;
    }

    Feature __result;
    __result = valueOf(w, __baselineTarget.allowableTags(__wordForm.discreteValue(w)));
    __cachedValue = w.partOfSpeech = __result.getStringValue();
    return __result;
  }

  public FeatureVector classify(Object __example)
  {
    if (isClone)
    {
      if (!(__example instanceof Token || __example instanceof Object[]))
      {
        String type = __example == null ? "null" : __example.getClass().getName();
        System.err.println("Classifier 'POSTaggerKnown(Token)' defined on line 164 of POSKnown.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }

      loadInstance();
      return instance.classify(__example);
    }

    if (__example instanceof Object[])
    {
      Object[] a = (Object[]) __example;
      if (a[0] instanceof int[])
        return super.classify((int[]) a[0], (double[]) a[1]);
    }

    return new FeatureVector(cachedFeatureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    if (isClone)
    {
      if (!(__example instanceof Token || __example instanceof Object[]))
      {
        String type = __example == null ? "null" : __example.getClass().getName();
        System.err.println("Classifier 'POSTaggerKnown(Token)' defined on line 164 of POSKnown.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }

      loadInstance();
      return instance.featureValue(__example);
    }

    if (__example instanceof Object[])
    {
      Object[] a = (Object[]) __example;
      if (a[0] instanceof int[])
        return super.featureValue((int[]) a[0], (double[]) a[1]);
    }

    return cachedFeatureValue(__example);
  }

  public String discreteValue(Object __example)
  {
    if (isClone)
    {
      if (!(__example instanceof Token || __example instanceof Object[]))
      {
        String type = __example == null ? "null" : __example.getClass().getName();
        System.err.println("Classifier 'POSTaggerKnown(Token)' defined on line 164 of POSKnown.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }

      loadInstance();
      return instance.discreteValue(__example);
    }

    if (__example instanceof Object[])
    {
      Object[] a = (Object[]) __example;
      if (a[0] instanceof int[])
        return super.discreteValue((int[]) a[0], (double[]) a[1]);
    }

    return cachedFeatureValue(__example).getStringValue();
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (isClone)
    {
      if (!(examples instanceof Token[] || examples instanceof Object[][]))
      {
        String type = examples == null ? "null" : examples.getClass().getName();
        System.err.println("Classifier 'POSTaggerKnown(Token)' defined on line 164 of POSKnown.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }

      loadInstance();
      return instance.classify(examples);
    }

    FeatureVector[] result = super.classify(examples);
    return result;
  }

  public static void main(String[] args)
  {
    String testParserName = null;
    String testFile = null;
    Parser testParser = getTestParser();

    try
    {
      if (!args[0].equals("null"))
        testParserName = args[0];
      if (args.length > 1) testFile = args[1];

      if (testParserName == null && testParser == null)
      {
        System.err.println("The \"testFrom\" clause was not used in the learning classifier expression that");
        System.err.println("generated this classifier, so a parser and input file must be specified.\n");
        throw new Exception();
      }
    }
    catch (Exception e)
    {
      System.err.println("usage: edu.illinois.cs.cogcomp.pos.lbjava.POSTaggerKnown \\");
      System.err.println("           <parser> <input file> [<null label> [<null label> ...]]\n");
      System.err.println("     * <parser> must be the fully qualified class name of a Parser, or \"null\"");
      System.err.println("       to use the default as specified by the \"testFrom\" clause.");
      System.err.println("     * <input file> is the relative or absolute path of a file, or \"null\" to");
      System.err.println("       use the parser arguments specified by the \"testFrom\" clause.  <input");
      System.err.println("       file> can also be non-\"null\" when <parser> is \"null\" (when the parser");
      System.err.println("       specified by the \"testFrom\" clause has a single string argument");
      System.err.println("       constructor) to use an alternate file.");
      System.err.println("     * A <null label> is a label (or prediction) that should not count towards");
      System.err.println("       overall precision and recall assessments.");
      System.exit(1);
    }

    if (testParserName == null && testFile != null && !testFile.equals("null"))
      testParserName = testParser.getClass().getName();
    if (testParserName != null)
      testParser = edu.illinois.cs.cogcomp.lbjava.util.ClassUtils.getParser(testParserName, new Class[]{ String.class }, new String[]{ testFile });
    POSTaggerKnown classifier = new POSTaggerKnown();
    TestDiscrete tester = new TestDiscrete();
    for (int i = 2; i < args.length; ++i)
      tester.addNull(args[i]);
    TestDiscrete.testDiscrete(tester, classifier, classifier.getLabeler(), testParser, true, 0);
  }

  public int hashCode() { return "POSTaggerKnown".hashCode(); }
  public boolean equals(Object o) { return o instanceof POSTaggerKnown; }

  public edu.illinois.cs.cogcomp.lbjava.classify.Feature valueOf(int[] a0, double[] a1, java.util.Collection a2)
  {
    if (isClone)
    {
      loadInstance();
      return instance.valueOf(a0, a1, a2);
    }

    return super.valueOf(a0, a1, a2);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.Feature valueOf(java.lang.Object a0, java.util.Collection a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.valueOf(a0, a1);
    }

    return super.valueOf(a0, a1);
  }

  public void write(java.io.PrintStream a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.write(a0);
      return;
    }

    super.write(a0);
  }

  public void write(edu.illinois.cs.cogcomp.lbjava.util.ExceptionlessOutputStream a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.write(a0);
      return;
    }

    super.write(a0);
  }

  public void initialize(int a0, int a1)
  {
    if (isClone)
    {
      loadInstance();
      instance.initialize(a0, a1);
      return;
    }

    super.initialize(a0, a1);
  }

  public void read(edu.illinois.cs.cogcomp.lbjava.util.ExceptionlessInputStream a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.read(a0);
      return;
    }

    super.read(a0);
  }

  public void learn(int[] a0, double[] a1, int[] a2, double[] a3)
  {
    if (isClone)
    {
      loadInstance();
      instance.learn(a0, a1, a2, a3);
      return;
    }

    super.learn(a0, a1, a2, a3);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.Feature featureValue(int[] a0, double[] a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.featureValue(a0, a1);
    }

    return super.featureValue(a0, a1);
  }

  public java.lang.String discreteValue(int[] a0, double[] a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.discreteValue(a0, a1);
    }

    return super.discreteValue(a0, a1);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector classify(int[] a0, double[] a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.classify(a0, a1);
    }

    return super.classify(a0, a1);
  }

  public void forget()
  {
    if (isClone)
    {
      loadInstance();
      instance.forget();
      return;
    }

    super.forget();
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet scores(java.lang.Object a0, java.util.Collection a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.scores(a0, a1);
    }

    return super.scores(a0, a1);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet scores(int[] a0, double[] a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.scores(a0, a1);
    }

    return super.scores(a0, a1);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet scores(int[] a0, double[] a1, java.util.Collection a2)
  {
    if (isClone)
    {
      loadInstance();
      return instance.scores(a0, a1, a2);
    }

    return super.scores(a0, a1, a2);
  }

  public void setParameters(edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner.Parameters a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setParameters(a0);
      return;
    }

    super.setParameters(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.learn.Learner.Parameters getParameters()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getParameters();
    }

    return super.getParameters();
  }

  public void setLabeler(edu.illinois.cs.cogcomp.lbjava.classify.Classifier a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setLabeler(a0);
      return;
    }

    super.setLabeler(a0);
  }

  public void setExtractor(edu.illinois.cs.cogcomp.lbjava.classify.Classifier a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setExtractor(a0);
      return;
    }

    super.setExtractor(a0);
  }

  public void doneLearning()
  {
    if (isClone)
    {
      loadInstance();
      instance.doneLearning();
      return;
    }

    super.doneLearning();
  }

  public void doneWithRound()
  {
    if (isClone)
    {
      loadInstance();
      instance.doneWithRound();
      return;
    }

    super.doneWithRound();
  }

  public void setLTU(edu.illinois.cs.cogcomp.lbjava.learn.LinearThresholdUnit a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setLTU(a0);
      return;
    }

    super.setLTU(a0);
  }

  public void setNetworkLabel(int a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setNetworkLabel(a0);
      return;
    }

    super.setNetworkLabel(a0);
  }

  public void write(java.lang.String a0, java.lang.String a1)
  {
    if (isClone)
    {
      loadInstance();
      instance.write(a0, a1);
      return;
    }

    super.write(a0, a1);
  }

  public void save()
  {
    if (isClone)
    {
      loadInstance();
      instance.save();
      return;
    }

    super.save();
  }

  public void read(java.lang.String a0, java.lang.String a1)
  {
    if (isClone)
    {
      loadInstance();
      instance.read(a0, a1);
      return;
    }

    super.read(a0, a1);
  }

  public void learn(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector[] a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.learn(a0);
      return;
    }

    super.learn(a0);
  }

  public void learn(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.learn(a0);
      return;
    }

    super.learn(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.Feature featureValue(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.featureValue(a0);
    }

    return super.featureValue(a0);
  }

  public java.lang.String discreteValue(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.discreteValue(a0);
    }

    return super.discreteValue(a0);
  }

  public double realValue(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.realValue(a0);
    }

    return super.realValue(a0);
  }

  public double realValue(int[] a0, double[] a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.realValue(a0, a1);
    }

    return super.realValue(a0, a1);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector[] classify(java.lang.Object[][] a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.classify(a0);
    }

    return super.classify(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector classify(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.classify(a0);
    }

    return super.classify(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector[] classify(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector[] a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.classify(a0);
    }

    return super.classify(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet scores(java.lang.Object a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.scores(a0);
    }

    return super.scores(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet scores(edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.scores(a0);
    }

    return super.scores(a0);
  }

  public void readLexiconOnDemand(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.readLexiconOnDemand(a0);
      return;
    }

    super.readLexiconOnDemand(a0);
  }

  public void readLexiconOnDemand(java.net.URL a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.readLexiconOnDemand(a0);
      return;
    }

    super.readLexiconOnDemand(a0);
  }

  public void setParameters(edu.illinois.cs.cogcomp.lbjava.learn.Learner.Parameters a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setParameters(a0);
      return;
    }

    super.setParameters(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.Classifier getLabeler()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getLabeler();
    }

    return super.getLabeler();
  }

  public edu.illinois.cs.cogcomp.lbjava.classify.Classifier getExtractor()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getExtractor();
    }

    return super.getExtractor();
  }

  public edu.illinois.cs.cogcomp.lbjava.learn.Learner emptyClone()
  {
    if (isClone)
    {
      loadInstance();
      return instance.emptyClone();
    }

    return super.emptyClone();
  }

  public void setLexicon(edu.illinois.cs.cogcomp.lbjava.learn.Lexicon a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setLexicon(a0);
      return;
    }

    super.setLexicon(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.learn.Lexicon getLexicon()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getLexicon();
    }

    return super.getLexicon();
  }

  public edu.illinois.cs.cogcomp.lbjava.learn.Lexicon getCurrentLexicon()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getCurrentLexicon();
    }

    return super.getCurrentLexicon();
  }

  public void setLabelLexicon(edu.illinois.cs.cogcomp.lbjava.learn.Lexicon a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setLabelLexicon(a0);
      return;
    }

    super.setLabelLexicon(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.learn.Lexicon getLabelLexicon()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getLabelLexicon();
    }

    return super.getLabelLexicon();
  }

  public void setEncoding(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setEncoding(a0);
      return;
    }

    super.setEncoding(a0);
  }

  public void setModelLocation(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setModelLocation(a0);
      return;
    }

    super.setModelLocation(a0);
  }

  public void setModelLocation(java.net.URL a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setModelLocation(a0);
      return;
    }

    super.setModelLocation(a0);
  }

  public java.net.URL getModelLocation()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getModelLocation();
    }

    return super.getModelLocation();
  }

  public void setLexiconLocation(java.net.URL a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setLexiconLocation(a0);
      return;
    }

    super.setLexiconLocation(a0);
  }

  public void setLexiconLocation(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.setLexiconLocation(a0);
      return;
    }

    super.setLexiconLocation(a0);
  }

  public java.net.URL getLexiconLocation()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getLexiconLocation();
    }

    return super.getLexiconLocation();
  }

  public void countFeatures(edu.illinois.cs.cogcomp.lbjava.learn.Lexicon.CountPolicy a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.countFeatures(a0);
      return;
    }

    super.countFeatures(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.learn.Lexicon getLexiconDiscardCounts()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getLexiconDiscardCounts();
    }

    return super.getLexiconDiscardCounts();
  }

  public java.lang.Object[] getExampleArray(java.lang.Object a0, boolean a1)
  {
    if (isClone)
    {
      loadInstance();
      return instance.getExampleArray(a0, a1);
    }

    return super.getExampleArray(a0, a1);
  }

  public java.lang.Object[] getExampleArray(java.lang.Object a0)
  {
    if (isClone)
    {
      loadInstance();
      return instance.getExampleArray(a0);
    }

    return super.getExampleArray(a0);
  }

  public void setReadLexiconOnDemand()
  {
    if (isClone)
    {
      loadInstance();
      instance.setReadLexiconOnDemand();
      return;
    }

    super.setReadLexiconOnDemand();
  }

  public int getPrunedLexiconSize()
  {
    if (isClone)
    {
      loadInstance();
      return instance.getPrunedLexiconSize();
    }

    return super.getPrunedLexiconSize();
  }

  public void saveModel()
  {
    if (isClone)
    {
      loadInstance();
      instance.saveModel();
      return;
    }

    super.saveModel();
  }

  public void saveLexicon()
  {
    if (isClone)
    {
      loadInstance();
      instance.saveLexicon();
      return;
    }

    super.saveLexicon();
  }

  public void writeModel(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.writeModel(a0);
      return;
    }

    super.writeModel(a0);
  }

  public void writeLexicon(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.writeLexicon(a0);
      return;
    }

    super.writeLexicon(a0);
  }

  public void readModel(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.readModel(a0);
      return;
    }

    super.readModel(a0);
  }

  public void readModel(java.net.URL a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.readModel(a0);
      return;
    }

    super.readModel(a0);
  }

  public void readLexicon(java.net.URL a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.readLexicon(a0);
      return;
    }

    super.readLexicon(a0);
  }

  public void readLexicon(java.lang.String a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.readLexicon(a0);
      return;
    }

    super.readLexicon(a0);
  }

  public void readLabelLexicon(edu.illinois.cs.cogcomp.lbjava.util.ExceptionlessInputStream a0)
  {
    if (isClone)
    {
      loadInstance();
      instance.readLabelLexicon(a0);
      return;
    }

    super.readLabelLexicon(a0);
  }

  public edu.illinois.cs.cogcomp.lbjava.learn.Lexicon demandLexicon()
  {
    if (isClone)
    {
      loadInstance();
      return instance.demandLexicon();
    }

    return super.demandLexicon();
  }

  public static class Parameters extends SparseNetworkLearner.Parameters
  {
    public Parameters()
    {
      SparseAveragedPerceptron.Parameters p = new SparseAveragedPerceptron.Parameters();
      p.learningRate = .1;
      p.thickness = 2;
      baseLTU = new SparseAveragedPerceptron(p);
    }
  }
}

