package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class AttributeFeature implements FeatureExtractor {

	private String attributeName;

	public AttributeFeature(String attributeName) {
		this.attributeName = attributeName;

	}

	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {

		Set<Feature> set = new LinkedHashSet<>();
		if (c.hasAttribute(attributeName)) {
			set.add(DiscreteFeature.create(c.getAttribute(attributeName)));
		}
		return set;
	}

	@Override
	public String getName() {
		return "#" + attributeName;
	}

}
