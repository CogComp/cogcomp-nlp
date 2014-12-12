package edu.illinois.cs.cogcomp.srl.data;

import java.util.*;

/**
 * @author Vivek Srikumar
 *
 */
public class FrameData {
	private String lemma;

	private static class SenseFrameData {
		Map<String, ArgumentData> argDescription = new HashMap<String, ArgumentData>();
		String verbClass = "UNKNOWN";
		List<Example> examples = new ArrayList<Example>();
	}

	private static class ArgumentData {
		String description;
		Set<String> vnTheta = new HashSet<String>();
	}

	private static class Example {
		String text;
		String name;
		Map<String, String> argDescriptions = new HashMap<String, String>();
		Map<String, String> argExamples = new HashMap<String, String>();
	}

	private Map<String, SenseFrameData> senseFrameData;

	public FrameData(String lemma) {
		this.lemma = lemma;
		senseFrameData = new HashMap<String, SenseFrameData>();
	}

	public void addExample(String sense, String name, String text,
						   Map<String, String> argDescriptions, Map<String, String> argExamples) {
		Example ex = new Example();
		ex.name = name;
		ex.text = text;
		ex.argDescriptions = argDescriptions;
		ex.argExamples = argExamples;
		this.senseFrameData.get(sense).examples.add(ex);
	}

	public String getLemma() {
		return lemma;
	}

	public void addSense(String sense, String verbClass) {
		this.senseFrameData.put(sense, new SenseFrameData());
		this.senseFrameData.get(sense).verbClass = verbClass;
	}

	public Set<String> getSenses() {
		return this.senseFrameData.keySet();
	}

	public void addArgument(String sense, String arg) {
		assert this.senseFrameData.containsKey(sense);

		senseFrameData.get(sense).argDescription.put(arg, new ArgumentData());
	}

	public Set<String> getArgsForSense(String sense) {

		assert this.senseFrameData.containsKey(sense) : sense
				+ " missing for predicate lemma " + this.lemma;
		return this.senseFrameData.get(sense).argDescription.keySet();
	}

	public void addArgumentDescription(String sense, String arg,
									   String description) {
		assert this.senseFrameData.containsKey(sense);
		assert this.senseFrameData.get(sense).argDescription.containsKey(arg);

		senseFrameData.get(sense).argDescription.get(arg).description = description;
	}

	public void addArgumentVNTheta(String sense, String arg, String vnTheta) {
		assert this.senseFrameData.containsKey(sense);
		assert this.senseFrameData.get(sense).argDescription.containsKey(arg);

		senseFrameData.get(sense).argDescription.get(arg).vnTheta.add(vnTheta);

	}

	public Set<String> getLegalArguments() {
		Set<String> l = new HashSet<String>();
		for (String s : this.getSenses())
			l.addAll(this.getArgsForSense(s));
		return l;
	}
}
