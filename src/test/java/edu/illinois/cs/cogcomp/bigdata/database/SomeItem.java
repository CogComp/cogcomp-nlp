package edu.illinois.cs.cogcomp.bigdata.database;

public class SomeItem {
	public int task;
	public int dataset;
	public int input;
	public int numVars;
	public int constraintsId;
	public int solutionId;

	public SomeItem(int task, int dataset, int input, int numVars,
			int constraintsId, int solutionId) {
		this.task = task;
		this.dataset = dataset;
		this.input = input;
		this.numVars = numVars;
		this.constraintsId = constraintsId;
		this.solutionId = solutionId;
	}
}