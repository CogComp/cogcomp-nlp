package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;

public abstract class Evaluator {
    public abstract void setViews(View goldView, View predictedView);
    public abstract void evaluate(ClassificationTester senseTester);
}