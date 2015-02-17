package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class CommaHelper {
	public static final boolean GOLD = true;
	public static final boolean NER = true;
	public static final boolean SRL = true;
	public static Relation getSRL(Comma c){
    	PredicateArgumentView pav = (PredicateArgumentView)c.TA.getView(ViewNames.SRL_VERB);
		List<Relation> rels = new ArrayList<Relation>();
		for(Constituent pred : pav.getPredicates()){
			rels.addAll(pav.getArguments(pred));
		}
		for(Relation rel : rels){
			if(rel.getTarget().getEndSpan()>c.commaPosition && rel.getTarget().getStartSpan()<=c.commaPosition)
				return rel;
		}
		return null;
    }
}
