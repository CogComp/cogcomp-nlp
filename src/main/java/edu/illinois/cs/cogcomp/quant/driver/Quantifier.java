package edu.illinois.cs.cogcomp.quant.driver;
import java.io.*;

import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.lbj.*;
import edu.illinois.cs.cogcomp.quant.standardize.Normalizer;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.*;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

public class Quantifier {
	
	public Normalizer normalizer;
	public static Pattern wordSplitPat[];
	
	public Quantifier() {
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
	
	public List<QuantSpan> getSpans(String text, boolean standardized) {
		TextAnnotation taCurator = new TextAnnotation("", "", text);
		TextAnnotation taQuant = new TextAnnotation("", "", wordsplitSentence(text).trim());
		List<QuantSpan> quantSpans = new ArrayList<QuantSpan>();
		String sentences[] = new String[taQuant.getNumberOfSentences()];
		for(int i=0; i<taQuant.getNumberOfSentences(); ++i) {
			sentences[i] = taQuant.getSentence(i).getText();
//			System.out.println("Sentence : "+taQuant.getSentence(i));
		}
		Chunker chunker = new Chunker();
	    Parser parser = new PlainToTokenParser(new WordSplitter(
	    		new SentenceSplitter(sentences)));
	    String previous = "";
	    String chunk="";
	    boolean inChunk = false;
	    String prediction="";
	    int startPos=0, endPos=0, tokenPos=0;
	    
	    for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) {
		    	prediction = chunker.discreteValue(w);
//		    	System.out.println("Word : "+w.form+" Label : "+prediction);
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
	
	public String getAnnotatedString(String text, boolean standardized){
		String ans = "";
		TextAnnotation ta = new TextAnnotation("", "", text);
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

	public void addQuantifierView(TextAnnotation ta) {
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
	
	public static void main(String args[])throws Throwable {
		String inputFile;
		String standardized;
		Quantifier quantifier = new Quantifier();
		if (args.length < 2){
			System.err.println(
					"usage: java driver.Quantifier <input file> <standardized(Y/N)>");
			System.exit(1);
		}
		
		inputFile = args[0];
		standardized = args[1];
		
		BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
		String txt = "", str;
		while( (str = br.readLine())!=null ) {
			txt+=str+" ";
		}
		br.close();
		
		if( standardized.equals("Y") || standardized.equals("y") ) {
			System.out.println(quantifier.getAnnotatedString(txt,true));
		} else {
			System.out.println(quantifier.getAnnotatedString(txt,false));
		}
	}
	
}
