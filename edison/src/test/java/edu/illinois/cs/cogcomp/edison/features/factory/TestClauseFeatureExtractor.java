package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.PseudoParse;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestTAResource;
import junit.framework.TestCase;

import java.util.List;
import java.util.Set;

/**
 * Test class
 * NB: If needed, please re-create the {@code test.ta} and {@code feature.collection.text} files
 * using {@link CreateTestTAResource} and
 * {@link CreateTestFeaturesResource}
 *
 * @author Vivek Srikumar
 */
public class TestClauseFeatureExtractor extends TestCase {
	private static List<TextAnnotation> tas;

	static {
		try {
			tas = IOUtils.readObjectAsResource(TestClauseFeatureExtractor.class, "test.ta");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final void testClauseView() throws Exception {
		testFex(ClauseFeatureExtractor.CHARNIAK);
	}

	private void testFex(FeatureExtractor fex) throws Exception {

		for (TextAnnotation ta : tas) {
			if (!ta.hasView(ViewNames.PARSE_CHARNIAK)) continue;

			ta.addView(ClauseViewGenerator.CHARNIAK);
			ta.addView(PseudoParse.CHARNIAK);

			System.out.println(ta.getView(ViewNames.PSEUDO_PARSE_CHARNIAK));

			PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

			for (Constituent predicate : pav.getPredicates()) {
				Constituent p = predicate.cloneForNewView("dummy");

				for (Relation arg : pav.getArguments(predicate)) {
					Constituent c = arg.getTarget().cloneForNewView("dummy");

					new Relation("", p, c, 1.0);

					Set<Feature> features = fex.getFeatures(c);

					System.out.println(c + "\t" + features);
				}
			}
		}

	}
}
