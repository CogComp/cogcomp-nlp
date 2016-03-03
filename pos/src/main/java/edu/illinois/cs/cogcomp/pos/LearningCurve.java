package edu.illinois.cs.cogcomp.pos;

import java.util.Date;
import java.io.*;
import edu.illinois.cs.cogcomp.pos.lbjava.*;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.parse.*;


/**
  * This program does a brute force search over 3 parameters of the LBJ POS
  * tagger's training regimen, testing each combination of parameter values on
  * the test data found in {@link POSConfigurator#devData} after training the
  * learning classifiers from scratch each time.  All of the performance
  * numbers computed by this program are stored in files in the
  * <code>curves/</code> subdirectory of the working directory, which must
  * exist prior to running this program.  When this program has completed, the
  * {@link POSTagger} class will implement the best peforming classifier
  * arrived at during the search.
  *
  * <h4>Usage</h4>
  * <blockquote><code>
  *   java edu.illinois.cs.cogcomp.lbj.pos.LearningCurve \
  *            &lt;knownThicknessLB&gt; &lt;knownThicknessUB&gt; \
  *            &lt;unKnownThicknessLB&gt; &lt;unKnownThicknessUB&gt; \
  *            &lt;rounds&gt;
  * </code></blockquote>
  *
  * <h4>Input</h4>
  * The first four command line parameters are lower bounds (hence, LB) and
  * upper bounds (hence, UB) for the first two parameters in the search, which
  * are the thicknesses of the separators in each of the two learning
  * classifiers.  These are all real values.  All values in increments of 0.5
  * between and including the lower and upper bounds for each parameter are
  * part of the search space.  The <code>&lt;rounds&gt;</code> parameter is
  * the maximum number of training rounds that each learning classifier will
  * be trained for.  The complete tagger is tested after each round.
  *
  * <h4>Output</h4>
  * For each combination of the thickness parameter settings, a file is
  * created in the <code>curves/</code> directory whose name is of the form
  * <code>curve_<i>k</i>_<i>u</i></code>, where <code><i>k</i></code> is the
  * thickness setting for {@link POSTaggerKnown} and <code><i>u</i></code> is
  * the thickness setting for {@link POSTaggerUnknown}.  Each line of text in
  * this file contains a round number and the performance of the complete
  * tagger after completing that training round.
  *
  * <p> During the search, progress updates are sent to <code>STDOUT</code>.
  * Whenever a better performing classifier is found, the two learning
  * classifiers are saved.  Thus, when this program terminates,
  * {@link POSTagger} will implement the best performing classifier arrived at
  * during the search.  Finally, the parameter settings that produced the best
  * results are sent to <code>STDOUT</code> upon completion of the search.
  *
  * @author Nick Rizzolo
 **/
public class LearningCurve
{
  static final POSLabel labeler = new POSLabel();
  static final LabelVectorReturner lvr =
    new LabelVectorReturner()
    {
      public String getOutputType() { return "discrete"; }
      public String[] allowableValues() { return labeler.allowableValues(); }
      public String discreteValue(Object e)
      {
        return classify(e).firstFeature().getStringValue();
      }
    };

  public static void main(String[] args)
  {
    double[] knownBounds = new double[2];
    double[] unknownBounds = new double[2];
    int rounds = 0;

    try
    {
      knownBounds[0] = Double.parseDouble(args[0]);
      knownBounds[1] = Double.parseDouble(args[1]);
      unknownBounds[0] = Double.parseDouble(args[2]);
      unknownBounds[1] = Double.parseDouble(args[3]);
      rounds = Integer.parseInt(args[4]);
      if (knownBounds[0] < 0 || knownBounds[1] < knownBounds[0]
          || unknownBounds[0] < 0 || unknownBounds[1] < unknownBounds[0]
          || rounds < 1 || args.length > 5)
        throw new Exception();
    }
    catch (Exception e)
    {
      System.err.println(
  "usage: java edu.illinois.cs.cogcomp.lbj.pos.LearningCurve <knownThicknessLB> \\\n"
+ "                              <knownThicknessUB> <unKnownThicknessLB> \\\n"
+ "                              <unKnownThicknessUB> <rounds>");
      System.exit(1);
    }

    POSTagger tagger = new POSTagger();
    POSTaggerKnown knownTagger = new POSTaggerKnown();
    POSTaggerUnknown unknownTagger = new POSTaggerUnknown();
    knownTagger.setLabeler(lvr);
    unknownTagger.setLabeler(lvr);
    Parser knownTrainParser =
      new FeatureVectorParser("src/main/java/edu/illinois/cs/cogcomp/lbj/pos/POSTaggerKnown.ex",
                              "src/main/java/edu/illinois/cs/cogcomp/lbj/pos/POSTaggerKnown.lex");
    Parser unknownTrainParser =
      new FeatureVectorParser("src/main/java/edu/illinois/cs/cogcomp/lbj/pos/POSTaggerUnknown.ex",
                              "src/main/java/edu/illinois/cs/cogcomp/lbj/pos/POSTaggerUnknown.lex");
    Parser testParser = new POSBracketToToken(new POSConfigurator().getDefaultConfig().getString("devData"));
    double bestF1 = 0;
    int bestRounds = 0;
    double bestKnownThickness = 0;
    double bestUnknownThickness = 0;

    for (double k = knownBounds[0]; k <= knownBounds[1]; k += .5)
      for (double u = unknownBounds[0]; u <= unknownBounds[1]; u += .5)
      {
        double bestCurrentF1 = 0;
        int bestCurrentRound = 0;
        PrintStream out = null;

        try
        {
          out =
            new PrintStream(
                new FileOutputStream("curves/curve_" + k + "_" + u));
        }
        catch (Exception e)
        {
          System.err.println(
              "Can't open curves/curve_" + k + "_" + u + " for output: " + e);
          System.exit(1);
        }

        System.out.println("Training k = " + k + ", u = " + u + " started at "
                           + new Date());

        knownTagger.forget();
        unknownTagger.forget();
        knownTagger.setLTU(new SparseAveragedPerceptron(.1, 0, k));
        unknownTagger.setLTU(new SparseAveragedPerceptron(.1, 0, u));

        for (int i = 0; i < rounds; ++i)
        {
          POSTaggerKnown.isTraining = true;
          for (Object ex = knownTrainParser.next(); ex != null;
               ex = knownTrainParser.next())
            knownTagger.learn(ex);
          POSTaggerKnown.isTraining = false;

          POSTaggerUnknown.isTraining = true;
          for (Object ex = unknownTrainParser.next(); ex != null;
               ex = unknownTrainParser.next())
            unknownTagger.learn(ex);
          POSTaggerUnknown.isTraining = false;

          TestDiscrete results = new TestDiscrete();
          for (Object ex = testParser.next(); ex != null;
               ex = testParser.next())
            results.reportPrediction(tagger.discreteValue(ex),
                                     labeler.discreteValue(ex));

          double f1 = results.getOverallStats()[2];
          out.println((i + 1) + "  " + f1);

          if (f1 > bestCurrentF1)
          {
            bestCurrentF1 = f1;
            bestCurrentRound = i + 1;

            if (f1 > bestF1)
            {
              bestF1 = f1;
              bestRounds = i + 1;
              bestKnownThickness = k;
              bestUnknownThickness = u;
              knownTagger.save();
              unknownTagger.save();
            }
          }

          if ((i + 1) % 5 == 0)
            System.out.println(
                (i + 1) + " rounds.  Best so far: (" + bestCurrentRound + ") "
                + bestCurrentF1 + " at " + new Date());

          knownTrainParser.reset();
          unknownTrainParser.reset();
          testParser.reset();
        }

        out.close();
      }

    System.out.println("\nBest overall:");
    System.out.println(
        "  Thickness for POSTaggerKnown: " + bestKnownThickness);
    System.out.println(
        "  Thickness for POSTaggerUnknown: " + bestUnknownThickness);
    System.out.println("  Rounds: " + bestRounds);
    System.out.println("  Performance: " + bestF1);
  }
}

