package edu.illinois.cs.cogcomp.srl.nom;

import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.srl.core.*;
import edu.illinois.cs.cogcomp.srl.data.FramesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class NomSRLManager extends SRLManager {

	private final static Logger log = LoggerFactory
			.getLogger(NomSRLManager.class);

	// XXX: Don't change these arrays unless you know what you are doing. If you
	// change the order of elements, or adding or removing elements, then you
	// have to re-train the Classifier.
	private final static String[] coreArguments = { "A0", "A1", "A2", "A3",
			"A4", "A5", "A8", "A9" };

	private final static String[] modifierArguments = { "AM-ADV", "AM-CAU",
			"AM-DIR", "AM-DIS", "AM-EXT", "AM-LOC", "AM-MNR", "AM-NEG",
			"AM-PNC", "AM-PRD", "AM-TMP" };

	private static final String[] allArguments = { NULL_LABEL, "A0", "A1",
			"A2", "A3", "A4", "A5", "A8", "A9", "AM-ADV", "AM-CAU", "AM-DIR",
			"AM-DIS", "AM-EXT", "AM-LOC", "AM-MNR", "AM-NEG", "AM-PNC",
			"AM-PRD", "AM-TMP", "C-A0", "C-A1", "C-A2", "C-A3", "C-SUP",
			"R-A0", "R-A1", "R-A2", "R-A3", "R-A4", "R-A8", "R-AM-CAU",
			"R-AM-LOC", "SUP" };

	private static final String[] allSenses = { "01", "02", "03", "04", "05",
			"06", "07", "08", "09", "10", "11", "12", "13", "14" };

	public final static Set<String> coreArgumentSet = Collections
			.unmodifiableSet(new TreeSet<String>(Arrays.asList(coreArguments)));

	public static final Set<String> modifierArgumentSet = Collections
			.unmodifiableSet(new TreeSet<String>(Arrays
					.asList(modifierArguments)));

	private ArgumentCandidateGenerator candidateGenerator;
	private final AbstractPredicateDetector heuristicPredicateDetector;

	public NomSRLManager(boolean trainingMode, String defaultParser) throws Exception {
		super(trainingMode, defaultParser);
		candidateGenerator = new NomArgumentCandidateGenerator(this);

		this.heuristicPredicateDetector = new NomPredicateDetectorHeuristic(
				this);

	}

	@Override
	public SRLType getSRLType() {
		return SRLType.Nom;
	}

	@Override
	protected String[] getArgumentLabels() {
		return allArguments;
	}

	@Override
	protected String[] getSenseLabels() {
		return allSenses;
	}

	@Override
	public Set<String> getCoreArguments() {
		return coreArgumentSet;
	}

	@Override
	public Set<String> getModifierArguments() {
		return modifierArgumentSet;
	}

	@Override
	public int getNumArguments() {
		return allArguments.length;
	}

	@Override
	public int getNumSenses() {
		return allSenses.length;
	}

	@Override
	public String getArgument(int id) {
		return allArguments[id];
	}

	@Override
	public String getSense(int id) {
		return allSenses[id];
	}

	@Override
	public ArgumentCandidateGenerator getArgumentCandidateGenerator() {
		return this.candidateGenerator;
	}

	@Override
	public FramesManager getFrameManager() {
		return FramesManager.getNombankInstance();
	}

	@Override
	public String getPredictedViewName() {
		return ViewNames.SRL_NOM;
	}

	public AbstractPredicateDetector getHeuristicPredicateDetector() {
		return heuristicPredicateDetector;
	}

	@Override
	public AbstractPredicateDetector getLearnedPredicateDetector() {
		try {
			return new LearnedPredicateDetector(this);
		} catch (Exception e) {
			log.error("Unable to load the predicate detector!", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getPruneSize(Models model) {
		switch (model) {
		case Classifier:
			return 7;
		case Identifier:
			return 6;
		case Predicate:
		case Sense:
			return 4;
		}

		return 4;
	}
}
