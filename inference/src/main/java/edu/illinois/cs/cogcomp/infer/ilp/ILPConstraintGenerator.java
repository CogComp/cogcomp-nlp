/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

import java.util.List;

public abstract class ILPConstraintGenerator {

    /**
     * Should the constraint be added as a cutting plane constraint (if the inference is with
     * CuttingPlaneILPSolver)?
     */
    public final boolean delayedConstraint;
    public final String name;

    private IInstance input = null;

    private ITransformer<IStructure, IStructure> outputTransformer = null;

    public ILPConstraintGenerator(String name, boolean delayedConstraint) {
        this.name = name;
        this.delayedConstraint = delayedConstraint;
    }

    public abstract List<ILPConstraint> getILPConstraints(IInstance x,
            InferenceVariableLexManager variables);

    /**
     * The subset of constraints that are violated.
     *
     * @param x
     * @param y
     * @return
     */
    public abstract List<ILPConstraint> getViolatedILPConstraints(IInstance x, IStructure y,
            InferenceVariableLexManager variables);

    /**
     * The input that this constraint operates upon need not be the full input to the problem. This
     * function returns the part of the input that is relevant to this constraint. By default, it it
     * returns null. The only way in which it will not be null is if setConstraintInput is used to
     * explicitly set the input.
     *
     * @return
     */
    public IInstance getConstraintInput() {
        return input;
    }

    public void setConstraintInput(IInstance input) {
        this.input = input;
    }


    public IStructure getConstraintOutput(IStructure y) {
        if (this.outputTransformer == null)
            return y;
        else
            return this.outputTransformer.transform(y);
    }

    public void setConstraintOutputGenerator(ITransformer<IStructure, IStructure> transformer) {
        this.outputTransformer = transformer;
    }

    public boolean isDelayedConstraint() {
        return delayedConstraint;
    }

}
