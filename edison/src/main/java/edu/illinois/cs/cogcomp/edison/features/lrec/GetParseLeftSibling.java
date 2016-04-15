package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;

import java.util.*;

public class GetParseLeftSibling extends FeatureInputTransformer {
	private final String parseViewName;
	
	public GetParseLeftSibling(String parseViewName){
		this.parseViewName = parseViewName;
	}
	
	@Override
	public List<Constituent> transform(Constituent input) {
		TextAnnotation ta = input.getTextAnnotation();

		TreeView parse = (TreeView) ta.getView(parseViewName);

		List<Constituent> siblings = new ArrayList<>();
		try {
			Constituent phrase = parse.getParsePhrase(input);
			List<Relation> in = phrase.getIncomingRelations();

			if (in.size() > 0) {
				Constituent prev = null;
				Relation relation = in.get(0);
				List<Relation> outgoingRelations = relation.getSource()
						.getOutgoingRelations();

                for (Relation r : outgoingRelations) {
                    if (r.getTarget() == phrase) {
                        break;
                    }
                    prev = r.getTarget();
                }

				if (prev != null)
					siblings.add(prev);
			}

		} catch (EdisonException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return siblings;
	}
	
	@Override
	public String name() {
		return "#lsis";
	}

}
