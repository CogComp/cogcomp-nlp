package edu.illinois.cs.cogcomp.nlp;

import edu.illinois.cs.cogcomp.nlp.utility.EvaluationCriteria;

public class Main {

    public static void main(String[] args) {
//        SentenceSplitter p = new SentenceSplitter("/Users/yjiang/Developer/test.txt");
//        Sentence[] sent = p.splitAll();
//        for(int i = 0; i < sent.length; i++) {
//            System.out.println(sent[i].toString());
//        }

//        OntoNotesParser parser = new OntoNotesParser("wsj_0089.onf");
//        parser.writeToFileInJson("json_output.txt");

//        OntoNotesJsonReader reader = new OntoNotesJsonReader("json_output.txt");
//        reader.parseIntoCuratorRecord();

        Evaluator evaluator = new Evaluator();
        System.out.println("Illinois");
        evaluator.evaluateIllinoisTokenizer(EvaluationCriteria.ON_SAMPLE_AGAINST_GOLD_STANDARD);
        evaluator.evaluateIllinoisTokenizer(EvaluationCriteria.ON_GOLD_STANDARD_AGAINST_SAMPLE);
        System.out.println("Stanford");
        evaluator.evaluateStanfordTokenizer(EvaluationCriteria.ON_SAMPLE_AGAINST_GOLD_STANDARD);
        evaluator.evaluateStanfordTokenizer(EvaluationCriteria.ON_GOLD_STANDARD_AGAINST_SAMPLE);

//        OntoNotesJsonReader reader = new OntoNotesJsonReader("json_output.txt");
//        ArrayList<String> rawTexts = reader.getRawTexts();
//        StanfordTokenizer stanfordTokenizer = new StanfordTokenizer(rawTexts);
    }
}
