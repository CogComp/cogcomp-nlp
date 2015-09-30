package edu.illinois.cs.cogcomp.srl.core;

import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The argument identifier, that uses the scores from the learned model and
 * scales it for high recall. The scaling function has two parameters (A,B) and
 * the scaled score is A * classifier-score + B. The scaled decision is true if
 * this is positive.
 *
 * @author Vivek Srikumar
 *
 */
@SuppressWarnings("serial")
public class ArgumentIdentifier extends Predicate<SRLMulticlassInstance> {

	private final static Logger log = LoggerFactory.getLogger(ArgumentIdentifier.class);

	private final double A;
	private final double B;

	private final SRLManager manager;

	public ArgumentIdentifier(double A, double B, SRLManager manager) {
		this.A = A;
		this.B = B;
		this.manager = manager;

	}

	public boolean getIdentifierScaledDecision(SRLMulticlassInstance x) {
		try {
			return getIdentifierScaledScore(x) >= 0;
		} catch (Exception e) {
			log.error("Unable to get identifier decision", e);
			throw new RuntimeException(e);
		}
	}

	public double getIdentifierScaledScore(SRLMulticlassInstance x)
			throws Exception {
		return scaleIdentifierScore(getIdentifierRawScore(x));
	}

	public double scaleIdentifierScore(double identifierRawScore) {
		double score = A * identifierRawScore + B;
		log.debug("Scaled score = {}, raw score = {}", score, identifierRawScore);
		return score;
	}

	public double getIdentifierRawScore(SRLMulticlassInstance x) throws Exception {
		log.debug("Classifying {}", x);
		WeightVector w = manager.getModelInfo(Models.Identifier).getWeights();

		SRLMulticlassLabel y1 = new SRLMulticlassLabel(x, 1, Models.Identifier, manager);
		SRLMulticlassLabel y0 = new SRLMulticlassLabel(x, 0, Models.Identifier, manager);
		double score1= w.dotProduct(x.getCachedFeatureVector(Models.Identifier),1 * manager.getModelInfo(Models.Identifier).getLexicon().size());
		double score2= w.dotProduct(x.getCachedFeatureVector(Models.Identifier));
		return (double) (score1 - score2);
	}

	@Override
	public Boolean transform(SRLMulticlassInstance input) {
		return this.getIdentifierScaledDecision(input);
	}
}
