package main.java.edu.illinois.cs.cogcomp.wikifier.utils.datastructure;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ConstituentMap<V> extends TCustomHashMap<Constituent, V>{

    private static final HashingStrategy<Constituent> spanHashStrategy = new HashingStrategy<Constituent>() {

        /**
         *
         */
        private static final long serialVersionUID = -804476820385903729L;

        @Override
        public int computeHashCode(Constituent arg0) {

            return new HashCodeBuilder()
                        .append(arg0.getStartSpan())
                        .append(arg0.getEndSpan())
                        .toHashCode();
        }

        @Override
        public boolean equals(Constituent arg0, Constituent arg1) {
            return arg0.getStartSpan()==arg1.getStartSpan() && arg0.getEndSpan() == arg1.getEndSpan();
        }

    };

    public ConstituentMap(){
        super(spanHashStrategy);
    }

    public static class ConstituentSet extends TCustomHashSet<Constituent>{
        public ConstituentSet(){
            super(spanHashStrategy);
        }
    }

}
