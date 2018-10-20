/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * A class to generate random labels...
 */
public class RandomLabelGenerator {
    private Logger logger = LoggerFactory.getLogger(RandomLabelGenerator.class);
    private String[] labelTypes = null; // will be initialized to something like:
                                        // {"O","PER","ORG","LOC","MISC"};
    private String[] labelNames = null; // will be initialized to something like:
                                        // {"O","B-PER","I-PER","B-LOC","I-LOC","B-ORG","I-ORG","B-MISC","I-MISC"};
    private double noiseLevel;// this is the noise that we put into label aggregation feature for
                              // previous predictions and for level2; set this value to 0 to
                              // eliminate any noise
    private static final int randomizationSeed = 70;
    private Random rand = null;

    public RandomLabelGenerator(String[] _labelTypes,
            TextChunkRepresentationManager.EncodingScheme encodingScheme, double noiseLevel) {

        this.noiseLevel = noiseLevel;

        if (_labelTypes.length == 1 && noiseLevel > 0.) {
            logger.warn("ERROR: only one label has been specified and noise level is non-zero. "
                    + "setting noise level to zero.");
            this.noiseLevel = 0;
        }

        rand = new Random(randomizationSeed);
        labelTypes = new String[_labelTypes.length + 1];
        labelTypes[0] = "O";
        System.arraycopy(_labelTypes, 0, labelTypes, 1, _labelTypes.length);
        // now dealing with label names
        if (encodingScheme == TextChunkRepresentationManager.EncodingScheme.BIO
                || encodingScheme == TextChunkRepresentationManager.EncodingScheme.IOB1) {
            labelNames = new String[_labelTypes.length * 2 + 1];
            labelNames[0] = "O";
            for (int i = 0; i < _labelTypes.length; i++) {
                labelNames[2 * i + 1] = "B-" + _labelTypes[i];
                labelNames[2 * i + 2] = "I-" + _labelTypes[i];
            }
        }
        if (encodingScheme == TextChunkRepresentationManager.EncodingScheme.IOE1
                || encodingScheme == TextChunkRepresentationManager.EncodingScheme.IOE2) {
            labelNames = new String[_labelTypes.length * 2 + 1];
            labelNames[0] = "O";
            for (int i = 0; i < _labelTypes.length; i++) {
                labelNames[2 * i + 1] = "E-" + _labelTypes[i];
                labelNames[2 * i + 2] = "I-" + _labelTypes[i];
            }
        }
        if (encodingScheme == TextChunkRepresentationManager.EncodingScheme.BILOU) {
            labelNames = new String[_labelTypes.length * 4 + 1];
            labelNames[0] = "O";
            for (int i = 0; i < _labelTypes.length; i++) {
                labelNames[4 * i + 1] = "B-" + _labelTypes[i];
                labelNames[4 * i + 2] = "I-" + _labelTypes[i];
                labelNames[4 * i + 3] = "L-" + _labelTypes[i];
                labelNames[4 * i + 4] = "U-" + _labelTypes[i];
            }
        }
    }

    public double nextDouble() {
        return rand.nextDouble();
    }

    public boolean useNoise() {
        return rand.nextDouble() < noiseLevel;
    }

    public String randomLabel() {
        // logger.info(rand);
        // logger.info(labelNames);
        int pos = (int) (rand.nextDouble() * labelNames.length);
        if (pos >= labelNames.length)
            pos = labelNames.length - 1;
        return labelNames[pos];
    }

    public String randomType() {
        int pos = (int) (rand.nextDouble() * labelTypes.length);
        if (pos >= labelTypes.length)
            pos = labelTypes.length - 1;
        return labelTypes[pos];
    }
}
