package edu.illinois.cs.cogcomp.verbsense.learn;

public class LearnerParameters {

	public static enum LearningMode {
		SSVM, CV, Perceptron
	}

	private double cStruct;
	private LearningMode learningMode;
	private int numRounds;
	private double learningRate;

	public static LearnerParameters getCVParams() {
		LearnerParameters p = new LearnerParameters();
		p.learningMode = LearningMode.CV;
		return p;
	}

	public static LearnerParameters getPerceptronParams(int numRounds, double learningRate) {
		LearnerParameters p = new LearnerParameters();
		p.learningMode = LearningMode.Perceptron;
		p.numRounds = numRounds;
		p.learningRate = learningRate;
		return p;
	}

	public static LearnerParameters getSSVMParams(double cStruct) {
		LearnerParameters p = new LearnerParameters();
		p.learningMode = LearningMode.SSVM;
		p.cStruct = cStruct;
		return p;
	}

	private LearnerParameters() {
	}

	public String getLearnerParametersIdentifier() {
		String output = "mode=" + learningMode;

		switch (learningMode) {
		case SSVM:
			output += (".cStruct=" + cStruct);
			break;
		case CV:
			output = "cv";
			break;

		case Perceptron:
			output += ".numRounds=" + this.numRounds + ".learningRate="
					+ this.learningRate;
		}

		return output;
	}

	public int getNumRounds() {
		assert this.learningMode == LearningMode.Perceptron;
		return this.numRounds;
	}

	public double getcStruct() {
		return cStruct;
	}

	public LearningMode getLearningAlgorithm() {
		return learningMode;
	}

	public double getLearningRate() {
		return learningRate;
	}

	@Override
	public String toString() {
		return getLearnerParametersIdentifier();
	}
}
