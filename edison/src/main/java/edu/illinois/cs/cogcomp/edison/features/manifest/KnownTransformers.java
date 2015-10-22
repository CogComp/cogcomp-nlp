package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;

import java.util.HashMap;
import java.util.Map;

class KnownTransformers {
	static Map<String, FeatureInputTransformer> transformers = new HashMap<>();

	static {
		transformers.put("constituent-parent", FeatureInputTransformer.constituentParent);

		transformers.put("constituent-child", FeatureInputTransformer.constituentChild);

		transformers.put("charniak-head", FeatureInputTransformer.charniakHead);
		transformers.put("stanford-head", FeatureInputTransformer.stanfordDependencyHead);
		transformers.put("easy-first-head", FeatureInputTransformer.easyFirstDependencyHead);

		transformers.put("stanford-modifiers", FeatureInputTransformer.stanfordDependencyModifiers);
		transformers.put("easy-first-modifiers", FeatureInputTransformer.easyFirstDependencyModifiers);

		transformers.put("charniak-governor", FeatureInputTransformer.charniakGovernor);
		transformers.put("stanford-governor", FeatureInputTransformer.stanfordDependencyGovernor);
		transformers.put("easy-first-governor", FeatureInputTransformer.easyFirstDependencyGovernor);

		transformers.put("charniak-object", FeatureInputTransformer.charniakObject);
		transformers.put("stanford-object", FeatureInputTransformer.stanfordDependencyObject);
		transformers.put("easy-first-object", FeatureInputTransformer.easyFirstDependencyObject);

		transformers.put("easy-first-neighboring-pp", FeatureInputTransformer.easyFirstNeighboringPP);

		transformers
				.put("easy-first-subject-of-dominating-verb", FeatureInputTransformer.easyFirstSubjectOfDominatingVerb);

		transformers.put("previous-word", FeatureInputTransformer.previousWord);
		transformers.put("next-word", FeatureInputTransformer.nextWord);

		transformers.put("first-word", FeatureInputTransformer.firstWord);
		transformers.put("last-word", FeatureInputTransformer.lastWord);

		transformers.put("each-word", FeatureInputTransformer.eachWordInConstituent);

	}

	public static void registerTransformer(String name, FeatureInputTransformer transformer) {
		transformers.put(name, transformer);
	}

}
