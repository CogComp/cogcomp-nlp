/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

public class ILPSolverFactory {

    private int beamSize = -1;
    public final SolverType type;

    public enum SolverType {
        Beam, Gurobi, OJAlgo, JLISCuttingPlaneGurobi
    }

    /**
     * Create ILP solvers that using one of XpresssMP, Gurobi, or Cutting plane solvers
     *
     * @param type
     */
    public ILPSolverFactory(SolverType type) {
        this.type = type;
        if (type == SolverType.Beam)
            throw new RuntimeException("Unknown beam size. Use other constructor");
    }

    /**
     * Create ILP solvers that perform beam search with a specified beam size
     *
     * @param size
     */
    public ILPSolverFactory(int size) {
        this.type = SolverType.Beam;
        this.beamSize = size;
    }

    public ILPSolver getSolver() {
        switch (type) {
            case Beam:
                return new BeamSearch(beamSize);
            case Gurobi:
                return new GurobiHook();
            case OJAlgo:
                return new OJalgoHook();
            case JLISCuttingPlaneGurobi:
                return new JLISCuttingPlaneILPSolverGurobi(new GurobiHook());
        }

        throw new RuntimeException();
    }
}
