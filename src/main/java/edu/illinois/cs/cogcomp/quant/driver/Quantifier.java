package edu.illinois.cs.cogcomp.quant.driver;
import java.io.*;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
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
		// Punctuation
		wordSplitPat[3] = Pattern.compile("(\\S)([\\:\\;\\!\\?])");
		wordSplitPat[4] = Pattern.compile("([\\:\\;\\!\\?])(\\S)");
		// Separate commas from words, but not from within numbers
		wordSplitPat[15] = Pattern.compile("(\\S),(\\s|$)");
		wordSplitPat[16] = Pattern.compile("(^|\\s),(\\S)");
		wordSplitPat[17] = Pattern.compile("(\\D),(\\S)");
		wordSplitPat[18] = Pattern.compile("(\\S),(\\D)");
		// Smoosh times together
		wordSplitPat[19] = Pattern.compile("(\\d\\d?)\\s*:\\s*(\\d\\d)(\\W|$)");
		// Keep thing that look like abbrev, honorific together
		wordSplitPat[20] = Pattern.compile("(^|\\s)([A-Z][a-z]*)\\s*\\.");
		// Weird things with dates
		wordSplitPat[21] = Pattern.compile("(\\d),(\\d{4,})(\\W)");
		// Separate words from closing punctuation
		wordSplitPat[22] = Pattern.compile("(\\w)(\\.)(\\W*)$");
        // 1990 s -> 1990s
		wordSplitPat[23] = Pattern.compile("(\\d\\d\\d\\d|\\d\\d)\\s*s(\\s+|$)");
		//' 90 -> '90
		wordSplitPat[24] = Pattern.compile("'\\s*(\\d\\d)($|\\W)");
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
					+" "+matcher.group(2));
		}
		
		matcher = wordSplitPat[4].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+" "+matcher.group(2));
		}
		
		// Separate commas from words, but not from within numbers
		matcher = wordSplitPat[15].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+" ,"+matcher.group(2));
		}
		matcher = wordSplitPat[16].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+", "+matcher.group(2));
		}
		matcher = wordSplitPat[17].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+" , "+matcher.group(2));
		}
		matcher = wordSplitPat[18].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+" , "+matcher.group(2));
		}
		
		// Smoosh times together
		matcher = wordSplitPat[19].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+":"+matcher.group(2)+matcher.group(3));
		}
		
		// Keep thing that look like abbrev, honorific together
		matcher = wordSplitPat[20].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)+
							matcher.group(2)+".");
		}
		
		// Weird things with dates
		matcher = wordSplitPat[21].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+" , "+matcher.group(2)+matcher.group(3));
		}
		
		// Separate words from closing punctuation
		matcher = wordSplitPat[22].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)
					+" "+matcher.group(2)+" "+matcher.group(3));
		}
		
        // 1990 s -> 1990s
		matcher = wordSplitPat[23].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), matcher.group(1)+"s ");
		}
		
		//' 90 -> '90
		matcher = wordSplitPat[24].matcher(sentence);
		while( matcher.find()){
			sentence = sentence.replace(matcher.group(), "'"+
								matcher.group(1)+matcher.group(2));
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
		}
		
		Chunker chunker = new Chunker();
	    Parser parser = new PlainToTokenParser(new WordSplitter(
	    		new SentenceSplitter(sentences)));
	    String previous = "";
	    String chunk="";
	    boolean inChunk = false;
	    String prediction="", prevWord="";
	    int startPos=0, endPos=0, tokenPos=0;
	    
	    for (Word w = (Word) parser.next(); w != null; w = (Word) parser.next()) {
//	    	if( tokenPos>0 ){
//	    		if( !taCurator.getToken(tokenPos).contains(w.form) && 
//	    				!taCurator.getToken(tokenPos).contains(prevWord))
//	    			tokenPos++;
//	    	}
	    	prediction = chunker.discreteValue(w);
	    	if (prediction.startsWith("B-")|| prediction.startsWith("I-")
	    							&& !previous.endsWith(prediction.substring(2))){
	    		if( !inChunk ){
	    			inChunk = true;
	    			startPos = taCurator.getTokenCharacterOffset(tokenPos).getFirst();
	    		}	
	    	}
	    	if( inChunk ){
	    		chunk += w.form+" ";
	    	}
    		if (!prediction.equals("O")
    					&& (w.next == null
    					|| chunker.discreteValue(w.next).equals("O")
    					|| chunker.discreteValue(w.next).startsWith("B-")
    					|| !chunker.discreteValue(w.next).endsWith(
    							prediction.substring(2)))){
    			
    			endPos = taCurator.getTokenCharacterOffset(tokenPos).getSecond();
    			QuantSpan span = new QuantSpan(null, startPos, endPos);
    			if(standardized) {
    				span.object = normalizer.parse(chunk, 
    						chunker.discreteValue(w).substring(2));
    			}
    			quantSpans.add(span);
    			inChunk = false;
    			chunk = "";
    		}
    		previous = prediction;
    		prevWord = w.form;
    		if(taCurator.getToken(tokenPos).trim().endsWith(w.form.trim())){
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
			if(quantSpans.get(quantIndex).start == ta.getTokenCharacterOffset(i).getFirst()) {
				ans += " [ ";
			}
			ans += ta.getToken(i) + " ";
			if(quantSpans.get(quantIndex).end == ta.getTokenCharacterOffset(i).getSecond()) {
				ans += " ] " + quantSpans.get(quantIndex) + " ";
				if(quantIndex < quantSpans.size()-1) quantIndex++;
			}
		}
	    return ans;
	}
	

	
	public static void main(String args[])throws Throwable {
		String inputFile=null;
		String standardized=null;
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
		while( (str = br.readLine())!=null )
			txt+=str+" ";
		
		if( standardized.equals("Y") || standardized.equals("y") )
			System.out.println(quantifier.getAnnotatedString(txt,true));
		else
			System.out.println(quantifier.getAnnotatedString(txt,false));
	}
	
}
