package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.lbj.*;
import edu.illinois.cs.cogcomp.quant.standardize.Normalizer;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.*;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class Quantifier extends Annotator {
	
	public Normalizer normalizer;
	public static Pattern wordSplitPat[];
	public static TextAnnotationBuilder taBuilder;
	private String dataDir = "data";
    private String modelsDir = "models";
    private String modelName = modelsDir + File.separator + new QuantitiesClassifier();
	
	static {
        IllinoisTokenizer tokenizer = new IllinoisTokenizer();
        taBuilder = new TokenizerTextAnnotationBuilder( tokenizer );
	}
	
	public Quantifier() {
        super(ViewNames.QUANTITIES, new String[0]);
		normalizer = new Normalizer();
		wordSplitPat = new Pattern[25];
		// Dashes
		wordSplitPat[0] = Pattern.compile("-(\\D)"); 
		wordSplitPat[1] = Pattern.compile("(\\S)-");
		wordSplitPat[2] = Pattern.compile("(\\d)-(\\d|\\.\\d)");
		// Remove commas from within numbers
		wordSplitPat[3] = Pattern.compile("(\\d),(\\d)");
		// Remove dollar signs
		wordSplitPat[4] = Pattern.compile("\\$(\\d)");
		wordSplitPat[5] = Pattern.compile("(\\d)\\$");
		// Percentages
		wordSplitPat[6] = Pattern.compile("(\\d)%");
	}
	
	public static String wordsplitSentence( String sentence ){
		Matcher matcher;
		matcher = wordSplitPat[0].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), "- "+matcher.group(1));
		}
		matcher = wordSplitPat[1].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)+" -");
		}
		matcher = wordSplitPat[2].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+" - "+matcher.group(2));
		}
		matcher = wordSplitPat[3].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+matcher.group(2));
		}
		matcher = wordSplitPat[4].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), "$ "+matcher.group(1));
		}
		matcher = wordSplitPat[5].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)+" $");
		}
		matcher = wordSplitPat[6].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)+" %");
		}
		return sentence;
	}
	
	public List<QuantSpan> getSpans(String text, boolean standardized) throws AnnotatorException {
		TextAnnotation taCurator = taBuilder.createTextAnnotation(text);
		TextAnnotation taQuant = taBuilder.createTextAnnotation(wordsplitSentence(text).trim());
		List<QuantSpan> quantSpans = new ArrayList<QuantSpan>();
		String sentences[] = new String[taQuant.getNumberOfSentences()];
		for(int i=0; i<taQuant.getNumberOfSentences(); ++i) {
			sentences[i] = taQuant.getSentence(i).getText();
		}
		QuantitiesClassifier chunker = new QuantitiesClassifier(modelName + ".lc", modelName + ".lex");
	    Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(sentences)));
	    String previous = "";
	    String chunk="";
	    boolean inChunk = false;
	    String prediction="";
	    int startPos=0, endPos=0, tokenPos=0;
	    
	    for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) {
		    	prediction = chunker.discreteValue(w);
		    	if (prediction.startsWith("B-")|| prediction.startsWith("I-")
		    							&& !previous.endsWith(prediction.substring(2))){
		    		if( !inChunk && tokenPos < taCurator.size()){
		    			inChunk = true;
		    			startPos = taCurator.getTokenCharacterOffset(tokenPos).getFirst();
		    		}	
		    	}
		    	if( inChunk ){
		    		chunk += w.form+" ";
		    	}
	    		if (!prediction.equals("O")
	    					&& tokenPos < taCurator.size()
	    					&& (w.next == null
	    					|| chunker.discreteValue(w.next).equals("O")
	    					|| chunker.discreteValue(w.next).startsWith("B-")
	    					|| !chunker.discreteValue(w.next).endsWith(
	    							prediction.substring(2)))){
	    			
	    			endPos = taCurator.getTokenCharacterOffset(tokenPos).getSecond();
	    			QuantSpan span = new QuantSpan(null, startPos, endPos);
	    			try { 
		    			if(standardized) {
		    				span.object = normalizer.parse(chunk, 
		    						chunker.discreteValue(w).substring(2));
		    			}
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
	    			quantSpans.add(span);
	    			inChunk = false;
	    			chunk = "";
	    		}
	    		previous = prediction;
	    		if(tokenPos < taCurator.size() && taCurator.getToken(tokenPos).trim().
	    				endsWith(w.form.trim())){
	    			tokenPos++;
	    		}
	    }
		return quantSpans;
	}
	
	public String getAnnotatedString(String text, boolean standardized) throws Exception {
		String ans = "";
		TextAnnotation ta = taBuilder.createTextAnnotation(text);
		List<QuantSpan> quantSpans = getSpans(text, standardized);
		int quantIndex = 0;
		for(int i=0; i<ta.size(); ++i) {
			if(quantSpans.get(quantIndex).start == 
					ta.getTokenCharacterOffset(i).getFirst()) {
				ans += " [ ";
			}
			ans += ta.getToken(i) + " ";
			if(quantSpans.get(quantIndex).end == 
					ta.getTokenCharacterOffset(i).getSecond()) {
				ans += " ] " + quantSpans.get(quantIndex) + " ";
				if(quantIndex < quantSpans.size()-1) quantIndex++;
			}
		}
	    return ans;
	}
	
	public static void main(String args[]) throws Throwable {
		Quantifier quantifier = new Quantifier();
		quantifier.trainOnAll();
		quantifier.test();
	}

	@Override
	public void addView(TextAnnotation ta) throws AnnotatorException {
		assert (ta.hasView(ViewNames.SENTENCE));
		SpanLabelView quantifierView = new SpanLabelView(
				ViewNames.QUANTITIES, "illinois-quantifier", ta, 1d);
		List<QuantSpan> quantSpans = getSpans(ta.getTokenizedText(), true);
		for (QuantSpan span : quantSpans) {
			int startToken = ta.getTokenIdFromCharacterOffset(span.start);
			int endToken = ta.getTokenIdFromCharacterOffset(span.end);
			quantifierView.addSpanLabel(startToken, endToken, span.object.toString(), 1d);
		}
		ta.addView(ViewNames.QUANTITIES, quantifierView);
	}

	public void trainOnAll() {
        QuantitiesClassifier classifier = new QuantitiesClassifier(modelName + ".lc", modelName + ".lex");
        QuantitiesDataReader trainReader = new QuantitiesDataReader(dataDir + "/allData.txt", "train");
        BatchTrainer trainer = new BatchTrainer(classifier, trainReader);
        trainer.train(45);
        classifier.save();
    }
	
    public void train() {
        QuantitiesClassifier classifier = new QuantitiesClassifier(modelName + ".lc", modelName + ".lex");
        QuantitiesDataReader trainReader = new QuantitiesDataReader(dataDir + "/train.txt", "train");
        BatchTrainer trainer = new BatchTrainer(classifier, trainReader);
        trainer.train(45);
        classifier.save();
    }

    public void test() {
    		QuantitiesClassifier classifier = new QuantitiesClassifier(modelName + ".lc", modelName + ".lex");
        QuantitiesDataReader testReader = new QuantitiesDataReader(dataDir + "/test.txt", "test");
        TestDiscrete tester = new TestDiscrete();
        tester.addNull("O");
        TestDiscrete.testDiscrete(tester, classifier, new QuantitiesLabel(), testReader, true, 1000);
    }
}
