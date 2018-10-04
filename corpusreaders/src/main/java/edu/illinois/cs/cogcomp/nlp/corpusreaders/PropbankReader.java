/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;

import java.util.ArrayList;
import java.util.List;

import static edu.illinois.cs.cogcomp.nlp.corpusreaders.AbstractSRLAnnotationReader.Fields;

/**
 * @author Vivek Srikumar
 */
public class PropbankReader extends AbstractSRLAnnotationReader {

    public final static String FormIdentifier = "Form";
    public final static String TenseIdentifier = "Tense";
    public final static String AspectIdentifier = "Aspect";
    public final static String PersonIdentifier = "Person";
    public final static String VoiceIdentifier = "Voice";
    public static final String Tagger = "Tagger";

    public enum Forms {
        Infinitive, Gerund, Participle, Finite, None;

        static Forms getForm(char c) {
            if (c == 'v')
                return Finite;
            else if (c == 'i')
                return Infinitive;
            else if (c == 'g')
                return Gerund;
            else if (c == 'p')
                return Participle;
            else
                return None;
        }
    }

    public enum Tenses {
        Future, Past, Present, None;

        static Tenses getTense(char c) {
            if (c == 'f')
                return Future;
            else if (c == 'p')
                return Past;
            else if (c == 'n')
                return Present;
            else
                return None;
        }
    }

    public enum Aspects {
        Perfect, Progressive, Both, None;

        static Aspects getAspect(char c) {
            switch (c) {
                case 'p':
                    return Perfect;
                case 'o':
                    return Progressive;
                case 'b':
                    return Both;
                default:
                    return None;
            }
        }
    }

    public enum Person {
        Third, None;

        static Person getPerson(char c) {
            if (c == '3')
                return Third;
            else
                return None;
        }
    }

    public enum Voices {
        Active, Passive, None;

        static Voices getVoice(char c) {
            switch (c) {
                case 'a':
                    return Active;
                case 'p':
                    return Passive;
                default:
                    return None;
            }
        }
    }

    public PropbankReader(String treebankHome, String propbankHome, String srlViewName,
            boolean mergeContiguousCArgs) throws Exception {
        super(treebankHome, propbankHome, srlViewName, mergeContiguousCArgs);
    }

    public PropbankReader(String treebankHome, String propbankHome, String[] sections,
            String srlViewName, boolean mergeContiguousCArgs) throws Exception {
        super(treebankHome, propbankHome, sections, srlViewName, mergeContiguousCArgs);
    }

    public PropbankReader(Iterable<TextAnnotation> list, String treebankHome, String propbankHome,
            String[] sections, String srlViewName, boolean mergeContiguousCArgs) throws Exception {
        super(list, treebankHome, propbankHome, sections, srlViewName, mergeContiguousCArgs);
    }

    @Override
    protected Fields readFields(String line) {
        return new PropbankFields(line);
    }

    @Override
    protected String getDataFile(String dataHome) {
        return dataHome + "/prop.txt";
    }

}


class PropbankFields extends Fields {

    private final String tagger;
    private final String roleSet;
    private final String inflection;
    private final List<GoldLabel> propLabels;

    public PropbankFields(String line) {
        super(line);

        String[] fields = line.split("\\s");

        wsjFileName = fields[0];
        sentence = Integer.parseInt(fields[1]);
        predicateTerminal = Integer.parseInt(fields[2]);
        tagger = fields[3];

        roleSet = fields[4];
        inflection = fields[5];

        propLabels = new ArrayList<>();

        for (int i = 6; i < fields.length; i++) {
            propLabels.add(new GoldLabel(fields[i]));
        }

        section = wsjFileName.split("/")[1];

        identifier = wsjFileName + ":" + sentence;
        lemma = roleSet.substring(0, roleSet.indexOf('.'));
        sense = roleSet.substring(roleSet.indexOf(".") + 1);

    }

    public Constituent createPredicate(TextAnnotation ta, String viewName,
            List<Tree<Pair<String, IntPair>>> yield) {

        Tree<Pair<String, IntPair>> l = yield.get(predicateTerminal);
        int start = l.getLabel().getSecond().getFirst();
        Constituent predicate = new Constituent("Predicate", viewName, ta, start, start + 1);

        predicate.addAttribute(PropbankReader.LemmaIdentifier, lemma);
        predicate.addAttribute(PropbankReader.SenseIdentifier, sense);

        predicate.addAttribute(PropbankReader.FormIdentifier,
                PropbankReader.Forms.getForm(inflection.charAt(0)).name());

        predicate.addAttribute(PropbankReader.TenseIdentifier,
                PropbankReader.Tenses.getTense(inflection.charAt(1)).name());
        predicate.addAttribute(PropbankReader.AspectIdentifier,
                PropbankReader.Aspects.getAspect(inflection.charAt(2)).name());

        predicate.addAttribute(PropbankReader.PersonIdentifier,
                PropbankReader.Person.getPerson(inflection.charAt(3)).name());

        predicate.addAttribute(PropbankReader.VoiceIdentifier,
                PropbankReader.Voices.getVoice(inflection.charAt(4)).name());

        predicate.addAttribute(PropbankReader.Tagger, tagger);

        return predicate;

    }

    @Override
    public List<? extends GoldLabel> getGoldLabels() {
        return propLabels;
    }

}
