package edu.illinois.cs.cogcomp.infer.ilp;

public interface ILPSolver {

	void setMaximize(boolean arg0);

	int addBooleanVariable(double arg0);
	
	int addRealVariable(double arg0);

        int addIntegerVariable(double arg0);

	int[] addDiscreteVariable(double[] arg0);

	void addEqualityConstraint(int[] arg0, double[] arg1, double arg2);

	void addGreaterThanConstraint(int[] arg0, double[] arg1, double arg2);

	void addLessThanConstraint(int[] arg0, double[] arg1, double arg2);

	boolean solve() throws Exception;

	boolean isSolved();

	boolean getBooleanValue(int arg0);

        int getIntegerValue(int arg0);
	
	double getRealValue(int arg0);

	double objectiveValue();

	void reset();

	void write(java.lang.StringBuffer arg0);
}
