/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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