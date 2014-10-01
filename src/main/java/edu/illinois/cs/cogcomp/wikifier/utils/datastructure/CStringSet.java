package edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;

public class CStringSet extends TCustomHashSet<Constituent>{

    private static final HashingStrategy<Constituent> surfaceHashingStrategy = new HashingStrategy<Constituent>() {

        /**
         *
         */
        private static final long serialVersionUID = -804476820385903729L;

        @Override
        public int computeHashCode(Constituent c) {
            return c.getSurfaceString().hashCode();
        }

        @Override
        public boolean equals(Constituent arg0, Constituent arg1) {
            return arg0.getSurfaceString().equals(arg1.getSurfaceString());
        }

    };

    public CStringSet(){
        super(surfaceHashingStrategy);
    }

}
