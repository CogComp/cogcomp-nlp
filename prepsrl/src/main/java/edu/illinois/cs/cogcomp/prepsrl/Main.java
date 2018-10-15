/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.lbjava.nlp.DataReader;
import edu.illinois.cs.cogcomp.prepsrl.data.PrepSRLDataReader;
import edu.illinois.cs.cogcomp.prepsrl.inference.ConstrainedPrepSRLClassifier;

import java.io.File;

public class Main {
    private ResourceManager rm = PrepSRLConfigurator.defaults();
    private String dataDir = rm.getString(PrepSRLConfigurator.PREP_DATA_DIR);
    private String modelsDir = rm.getString(PrepSRLConfigurator.MODELS_DIR);
    private String modelName = modelsDir + File.separator + PrepSRLClassifier.CLASS_NAME;

    public void train() {
        if (!IOUtils.exists(modelsDir))
            IOUtils.mkdir(modelsDir);
        Learner classifier = new PrepSRLClassifier(modelName + ".lc", modelName + ".lex");
        Parser trainDataReader = new PrepSRLDataReader(dataDir, "train");
        BatchTrainer trainer = new BatchTrainer(classifier, trainDataReader, 1000);
        trainer.train(20);
        classifier.save();
        trainDataReader.close();
    }

    public void test() {
        ConstrainedPrepSRLClassifier classifier = new ConstrainedPrepSRLClassifier();
        Parser testDataReader = new PrepSRLDataReader(dataDir, "test");
        TestDiscrete tester = new TestDiscrete();
        TestDiscrete.testDiscrete(tester, classifier, new PrepSRLClassifier.Label(),
                testDataReader, true, 100);
        testDataReader.close();
    }

    public static void main(String[] args) {
        Main trainer = new Main();
        trainer.train();
        trainer.test();
    }
}
