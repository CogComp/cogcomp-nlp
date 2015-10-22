package edu.illinois.cs.cogcomp.nlp;

import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesJsonReader;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizerPrev;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StanfordTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.EvaluationCriteria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Evaluator {
    private ArrayList<String> rawTexts;
    private Record goldStandardRecord;

    public Evaluator() {
        OntoNotesJsonReader reader = new OntoNotesJsonReader("json_output.txt");
        rawTexts = reader.getRawTexts();
        goldStandardRecord = reader.parseIntoCuratorRecord();
    }

    /**
     * Evaluate the IllinoisTokenizer based on criteria.
     * @param criteria
     */
    public void evaluateIllinoisTokenizer(EvaluationCriteria criteria) {
        IllinoisTokenizerPrev illinoisTokenizerPrev = new IllinoisTokenizerPrev(rawTexts);
        Record illinoisRecord = illinoisTokenizerPrev.parseIntoCuratorRecord();
        evaluateTokenizer(goldStandardRecord, illinoisRecord, criteria);
    }

    /**
     * Evaluate the StanfordTokenizer based on criteria.
     * @param criteria
     */
    public void evaluateStanfordTokenizer(EvaluationCriteria criteria) {
        StanfordTokenizer stanfordTokenizer = new StanfordTokenizer(rawTexts);
        Record stanfordRecord = stanfordTokenizer.parseIntoCuratorRecord();
        evaluateTokenizer(goldStandardRecord, stanfordRecord, criteria);
    }

    private void evaluateTokenizer(Record goldRecord, Record sampleRecord, EvaluationCriteria criteria) {
        Map<String, Integer> goldHashMap = new LinkedHashMap<String, Integer>();
        Map<String, Integer> sampleHashMap = new LinkedHashMap<String, Integer>();

        for (Span eachSpan : goldRecord.getLabelViews().get("tokens").getLabels()) {
            String key = eachSpan.getSource();
            key += ","+Integer.toString(eachSpan.getStart());
            key += ","+Integer.toString(eachSpan.getEnding());
            goldHashMap.put(key, 0);
        }

        for (Span eachSpan : sampleRecord.getLabelViews().get("tokens").getLabels()) {
            String key = eachSpan.getSource();
            key += ","+Integer.toString(eachSpan.getStart());
            key += ","+Integer.toString(eachSpan.getEnding());
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
//            System.out.println("\nSample:");
//            for (String sampleKey:sampleHashMap.keySet()) {
//                if (sampleHashMap.get(sampleKey) == 1)
//                    System.out.print(sampleKey + "    ");
//            }
//            System.out.println("\n\nGold:");
//            for (String goldKey:goldHashMap.keySet()) {
//                if (goldHashMap.get(goldKey) == 0)
//                    System.out.print(goldKey + "    ");
//            }
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
