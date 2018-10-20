/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

public abstract class Evaluator {
    protected void cleanAttributes(View gold, View prediction) {
        // get rid of attributes
        gold.removeAttributes();
        prediction.removeAttributes();
    }

    public abstract void evaluate(ClassificationTester senseTester, View goldView,
            View predictedView);
}
