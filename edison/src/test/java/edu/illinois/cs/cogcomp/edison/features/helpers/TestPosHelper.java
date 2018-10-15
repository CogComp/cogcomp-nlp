/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.IOException;

/**
 * Created by mssammon on 3/20/16.
 */
public class TestPosHelper {
    public static final String CONFIG = "src/test/resources/lrec-config.txt";
    public static final String POS_CORPUS = "posCorpus";

    public static ResourceManager rm;
    public static String corpus;


    static {
        try {
            rm = new ResourceManager(CONFIG);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        corpus = rm.getString(POS_CORPUS);
    }
}
