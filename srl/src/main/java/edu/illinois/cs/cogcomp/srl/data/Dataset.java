/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.data;

public enum Dataset {
    PTBAll, PTBTrain, PTBDev, PTBTest, PTB0204, PTBTrainDev;

    public static String[] stringValues() {
        String[] values = new String[Dataset.values().length];
        Dataset[] datasets = Dataset.values();
        for (int i = 0; i < datasets.length; i++) {
            Dataset d = datasets[i];
            values[i] = d.name();
        }
        return values;
    }
}
