/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.chunker.main;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * Created by qning2 on 10/23/16.
 * This code needs access to cogcomp servers for benchmark datasets
 */
public class ChunkerBenchmark {
    public static void main(String[] args){
        ResourceManager rm = new ChunkerConfigurator().getDefaultConfig();
        String testFileName = rm.getString("testGoldPOSData");
        String testNoPOSFileName = rm.getString("testNoPOSData");

        System.out.println("\nWith Gold POS");
        ChunkTester.chunkTester(testFileName);

        System.out.println("\nWith NO POS");
        ChunkTester.chunkTester(testNoPOSFileName);
    }
}
