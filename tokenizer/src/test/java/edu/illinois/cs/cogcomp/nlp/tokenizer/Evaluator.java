package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesJsonReader;
import edu.illinois.cs.cogcomp.nlp.utility.CcgTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.nlp.utility.EvaluationCriteria;
import edu.illinois.cs.cogcomp.nlp.utility.Utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Evaluator {
    private ArrayList<String> rawTexts;
    private TextAnnotation goldStandardTA;

    public Evaluator() {
        OntoNotesJsonReader reader = new OntoNotesJsonReader("tokenizer/src/test/resources/json_output.txt");
        rawTexts = reader.getRawTexts();
        goldStandardTA = Utility.parseIntoTextAnnotation(reader.getSentences());
    }

	public static void main(String[] args) {
		Evaluator evaluator = new Evaluator();
		System.out.println("Illinois");
		evaluator.evaluateIllinoisTokenizer(EvaluationCriteria.ON_SAMPLE_AGAINST_GOLD_STANDARD);
		evaluator.evaluateIllinoisTokenizer(EvaluationCriteria.ON_GOLD_STANDARD_AGAINST_SAMPLE);
		System.out.println("Stanford");
		evaluator.evaluateStanfordTokenizer(EvaluationCriteria.ON_SAMPLE_AGAINST_GOLD_STANDARD);
		evaluator.evaluateStanfordTokenizer(EvaluationCriteria.ON_GOLD_STANDARD_AGAINST_SAMPLE);
	}

    /**
     * Evaluate the IllinoisTokenizer based on criteria.
     */
    public void evaluateIllinoisTokenizer(EvaluationCriteria criteria) {
		CcgTextAnnotationBuilder illinoisTokenizer = new CcgTextAnnotationBuilder(new IllinoisTokenizer());
		// This is not the best method to pass the raw sentences, since the sentence splitter
		// might make different choices, but for now it works.
		String rawString = "";
		for (String text : rawTexts) rawString += text + System.lineSeparator();
        TextAnnotation illinoisTA = illinoisTokenizer.createTextAnnotation(rawString.trim());
        evaluateTokenizer(goldStandardTA, illinoisTA, criteria);
    }

    /**
     * Evaluate the StanfordTokenizer based on criteria.
     */
    public void evaluateStanfordTokenizer(EvaluationCriteria criteria) {
        StanfordTokenizer stanfordTokenizer = new StanfordTokenizer(rawTexts);
        TextAnnotation stanfordTA = Utility.parseIntoTextAnnotation(stanfordTokenizer.getSentences());
		evaluateTokenizer(goldStandardTA, stanfordTA, criteria);
    }

    private void evaluateTokenizer(TextAnnotation goldRecord, TextAnnotation sampleRecord, EvaluationCriteria criteria) {
        Map<String, Integer> goldHashMap = new LinkedHashMap<>();
        Map<String, Integer> sampleHashMap = new LinkedHashMap<>();

        for (Constituent eachSpan : goldRecord.getView(ViewNames.TOKENS).getConstituents()) {
            String key = eachSpan.getSurfaceForm();
            key += ","+Integer.toString(eachSpan.getStartSpan());
            key += ","+Integer.toString(eachSpan.getEndSpan());
            goldHashMap.put(key, 0);
        }

        for (Constituent eachSpan : sampleRecord.getView(ViewNames.TOKENS).getConstituents()) {
            String key = eachSpan.getSurfaceForm();
            key += ","+Integer.toString(eachSpan.getStartSpan());
            key += ","+Integer.toString(eachSpan.getEndSpan());
            sampleHashMap.put(key, 0);
        }

        int count = 0;
        if (criteria == EvaluationCriteria.ON_SAMPLE_AGAINST_GOLD_STANDARD) {
            for (String sampleKey : sampleHashMap.keySet()) {
                if (goldHashMap.containsKey(sampleKey)) {
                    count++;
                    goldHashMap.put(sampleKey, 1);
                }
                else {
                    sampleHashMap.put(sampleKey, 1);
                }
            }
            System.out.println("Sample against Gold: " + (float) count / sampleHashMap.size());
        }
        else {
            for (String goldKey : goldHashMap.keySet()) {
                if (sampleHashMap.containsKey(goldKey)) {
                    count++;
                    sampleHashMap.put(goldKey, 1);
                }
            }
            System.out.println("Gold against Sample: " + (float) count / goldHashMap.size());
        }
    }
}
