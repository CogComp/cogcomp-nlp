package edu.illinois.cs.cogcomp.srl.inference;

import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.inference.constraints.*;

public enum SRLConstraints {

	noOverlappingArguments {
		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new NoOverlapConstraint(manager);
		}
	},
	noDuplicateCore {
		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new NoDuplicateCoreConstraint(manager);
		}
	},
	CArgumentConstraint {
		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new CArgConstraints(manager);
		}
	},
	RArgumentConstraint {
		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new RArgConstraints(manager);
		}
	},
	predicateSense {
		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new PredicateSenseConstraints(manager);
		}
	},
	beVerbConstraint {
		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new BeVerbConstraints(manager);
		}
	},
	supportVerbConstraint {

		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new SupportVerbConstraint(manager);
		}
	},
	noCrossArgumentExclusiveOverlap {

		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new CrossArgumentExclusiveOverlap(manager);
		}

	},
	crossArgumentRetainedModifiers {

		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new CrossArgumentRetainedModifiers(manager);
		}
	},
	atLeastOneCoreArgument {

		@Override
		public SRLILPConstraintGenerator getGenerator(SRLManager manager) {
			return new AtLeastOneCoreArgument(manager);
		}

	}

	;

	public abstract SRLILPConstraintGenerator getGenerator(SRLManager manager);
}
