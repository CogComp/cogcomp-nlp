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
package edu.illinois.cs.cogcomp.chunker.utils;


/**
  * Constant values used by the LBJ source file.
  *
  * @author Nick Rizzolo
 **/
public class Constants {
  /** The path to a file containing training data. */
  public static final String trainingData =
          "/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/train.txt";
  public static final String testData =
          "/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/test.txt";

    public static final String modelName =
            "illinois-chunker";
    public static final String defaultModelDir =
            "models/edu/illinois/cs/cogcomp/chunker/main/lbjava/";
}

