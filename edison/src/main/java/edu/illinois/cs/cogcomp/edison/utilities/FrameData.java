/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import java.util.*;

/**
 * This file contains the datastore required to store the frame information.
 * The information are populated by FrameManager class.
 *
 * @author Vivek Srikumar
 */
public class FrameData {
    private String lemma;

    public static class SenseFrameData {
        Map<String, ArgumentData> argDescription = new HashMap<>();
        String verbClass = "UNKNOWN";
        String senseName;
        List<Example> examples = new ArrayList<>();
    }

    public static class ArgumentData {
        String description;
        Set<String> vnTheta = new HashSet<>();
    }

    private static class Example {
        String text;
        String name;
        Map<String, String> argDescriptions = new HashMap<>();
        Map<String, String> argExamples = new HashMap<>();
    }

    private Map<String, SenseFrameData> senseFrameData;

    public FrameData(String lemma) {
        this.lemma = lemma;
        senseFrameData = new HashMap<>();
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

    public void addSense(String sense, String senseName, String verbClass) {
        this.senseFrameData.put(sense, new SenseFrameData());
        this.senseFrameData.get(sense).verbClass = verbClass;
        this.senseFrameData.get(sense).senseName = senseName;
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

    public SenseFrameData getArgInfoForSense(String sense) {
        assert this.senseFrameData.containsKey(sense) : sense
                + " missing for predicate lemma " + this.lemma;
        return this.senseFrameData.get(sense);
    }

    public void addArgumentDescription(String sense, String arg,
                                       String description) {
        assert this.senseFrameData.containsKey(sense);
        assert this.senseFrameData.get(sense).argDescription.containsKey(arg);

        senseFrameData.get(sense).argDescription.get(arg).description = description;
    }

    public String getArgumentDescription(String sense, String arg) {
        assert this.senseFrameData.containsKey(sense);
        assert this.senseFrameData.get(sense).argDescription.containsKey(arg);

        return senseFrameData.get(sense).argDescription.get(arg).description;
    }

    public void addArgumentVNTheta(String sense, String arg, String vnTheta) {
        assert this.senseFrameData.containsKey(sense);
        assert this.senseFrameData.get(sense).argDescription.containsKey(arg);

        senseFrameData.get(sense).argDescription.get(arg).vnTheta.add(vnTheta);
    }

    public Set<String> getLegalArguments() {
        Set<String> l = new HashSet<>();
        for (String s : this.getSenses())
            l.addAll(this.getArgsForSense(s));
        return l;
    }

    public String getSenseName(String sense) {
        assert this.senseFrameData.containsKey(sense) : sense
                + " missing for predicate lemma " + this.lemma;
        return this.senseFrameData.get(sense).senseName;
    }
}