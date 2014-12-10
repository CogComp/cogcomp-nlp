package edu.illinois.cs.cogcomp.infer.ilp;

public class ILPSolverFactory {

	private int beamSize = -1;
	public final SolverType type;

	public static enum SolverType {
		XpressMP,

		Beam,

		Gurobi,

		CuttingPlaneGurobi,

		CuttingPlaneXpressMP,

		JLISCuttingPlaneGurobi
	}

	/**
	 * Create ILP solvers that using one of XpresssMP, Gurobi, or Cutting plane
	 * solvers
	 * 
	 * @param type
	 */
	public ILPSolverFactory(SolverType type) {
		this.type = type;
		if (type == SolverType.Beam)
			throw new RuntimeException(
					"Unknown beam size. Use other constructor");
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
		case CuttingPlaneGurobi:
			return new CuttingPlaneILPHook(new GurobiHook());
		case JLISCuttingPlaneGurobi:
			return new JLISCuttingPlaneILPSolverGurobi(new GurobiHook());
		}

		throw new RuntimeException();
	}
}
