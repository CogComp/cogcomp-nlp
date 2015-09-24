package edu.illinois.cs.cogcomp.comma.bayraktar;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.comma.readers.TextAnnotationReaderToSentenceReader;
import edu.illinois.cs.cogcomp.comma.utils.PrettyPrintParseTree;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankReader;

/**
 * Provides cli interface to annotate bayraktar patterns
 * at each iteration the most frequent un-annotated bayraktar pattern along with example sentences and parses are presented and the user must select a label for the pattern
 * @author navari
 *
 */
public class BayraktarAnnotationGenerationHelper {
	
    /**
     * 
     * @return a MultiMap mapping bayraktar-patterns to Commas from that have that bayraktar-pattern
     */
    public static Multimap<String, Comma> getBayraktarPatternToPTBCommas(){
    	String treebankHome = CommaProperties.getInstance().getPTBHDir();
    	String[] sections = { "00", "01", "02" , "03", "04", "05", "06", "07", "08", "09", "10"};//, "11", "12" , "13", "14", "15", "16", "17", "18", "19", "20"};
    	PennTreebankReader ptbReader;
		try {
			ptbReader = new PennTreebankReader(treebankHome, sections);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	TextAnnotationReaderToSentenceReader sentenceReader = new TextAnnotationReaderToSentenceReader(ptbReader);
    	
    	List<Comma> commas = new ArrayList<>();
    	while(sentenceReader.hasNext()){
    		Sentence sentence = sentenceReader.next();
    		commas.addAll(sentence.getCommas());
    	}
    	Multimap<String, Comma> bayraktarPatternToCommas = HashMultimap.create();
    	for(Comma comma : commas){
    		String bayraktarPattern = comma.getBayraktarPattern();
    		bayraktarPatternToCommas.put(bayraktarPattern, comma);
    	}
    	return bayraktarPatternToCommas;
    }

    
    Scanner scanner;
    String bayraktarAnnotationsDir =  CommaProperties.getInstance().getBayraktarAnnotationsDir();
    /**
     * Present bayraktarPattern and example commas to user on console. The user can then choose from the provided labels to decide on an annotation.
     * @param bayraktarPattern the pattern that need to be annotated
     * @param exampleCommas examples of commas whose bayraktar-pattern is bayraktarPattern
     * @return true if successfully annotated else false
     */
    public boolean present(String bayraktarPattern, Collection<Comma> exampleCommas){
    	Iterator<Comma> exammpleCommaTextIt = exampleCommas.iterator();
    	Iterator<Comma> exammpleCommaParseIt = exampleCommas.iterator();
    	String data = exampleCommas.size() + "\t" + bayraktarPattern + "\n"
    			+ exammpleCommaTextIt.next().getVivekAnnotatedText() + "\n"
    			+ exammpleCommaTextIt.next().getVivekAnnotatedText() + "\n"
    			+ exammpleCommaTextIt.next().getVivekAnnotatedText() + "\n"
    			+ PrettyPrintParseTree.pennString(((TreeView)exammpleCommaTextIt.next().getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0));
    	System.out.println(data);

    	String[] labels = {"Attribute", "Complementary", "Interrupter", "Introductory", "List", "Quotation", "Substitute", "Locative"};
    	System.out.println(0 + " new sentence");
    	System.out.println(1 + " new parse");
    	for(int i=0; i<labels.length;i++)
    		System.out.println((i+2) + " " + labels[i]);
    	System.out.println("Enter cmd: ");
    	
    	int cmd = 0;
    	while(cmd==0 || cmd==1){
    		cmd = scanner.nextInt();
    		System.out.println("COMMAND IS ---------------------------- " + cmd);
    		switch (cmd) {
    		case 0:
    			System.out.println(exammpleCommaTextIt.next().getVivekAnnotatedText());
    			break;
    		case 1:
    			System.out.println(PrettyPrintParseTree.pennString(((TreeView)exammpleCommaParseIt.next().getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0)));
    			break;
    		case 69:
    			return false;
    		default:
    			int idx = cmd - 2;
    			
				try {
	    			PrintWriter patternFile = new PrintWriter(new BufferedWriter(new FileWriter(bayraktarAnnotationsDir + labels[idx], true)));
					patternFile.println("\n" + bayraktarPattern);
					patternFile.close();
					PrintWriter infoFile = new PrintWriter(new BufferedWriter(new FileWriter(bayraktarAnnotationsDir + labels[idx] + "-New", true)));
	    		    infoFile.println("\n" + data+"\n\n");
	    		    infoFile.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
    			
    			return true;
    		}
		}
		return false;
    }
    
    /**
     * starts the process of manual annotation.
     *  Gets all the bayraktar patterns in the first 10 sections of PTB and presents them in the order of decreasing frequency.
     *  THe annotator is requested to assign a label to each pattern thus presented. 
     */
    public void startManualAnnotation(){
    	Multimap<String, Comma> syntaxPatternToCommas = getBayraktarPatternToPTBCommas();
    	@SuppressWarnings("unchecked")
		List<Pair<String, Collection<Comma>>> patternToCommasList = new ArrayList<>();
    	for(String pattern : syntaxPatternToCommas.keySet()){
    		Collection<Comma> commasWithPattern = syntaxPatternToCommas.get(pattern);
    		Pair<String, Collection<Comma>> patternCommmasPair= new Pair<>(pattern, commasWithPattern);
    		patternToCommasList.add(patternCommmasPair);
    	}
    		
    	
    	Collections.sort(patternToCommasList, new Comparator<Pair<String, Collection<Comma>>>(){

			@Override
			public int compare(Pair<String, Collection<Comma>> pair1,
					Pair<String, Collection<Comma>> pair2) {
				return pair2.getSecond().size() - pair1.getSecond().size();
			}
    	});
    	
    	int totalCount = syntaxPatternToCommas.size();
    	int annotatedPatternFrequency = 0;
    	int annotationCount = 0;
    	scanner = new Scanner(System.in);
    	for(Pair<String, Collection<Comma>> pair : patternToCommasList){
    		int patternFrequency = pair.getSecond().size();
    		
    		boolean annottaionFound = BayraktarPatternLabeler.isLabelAvailable(pair.getFirst()); 
    		if(true == annottaionFound || true==present(pair.getFirst(), pair.getSecond())){
    			annotatedPatternFrequency += patternFrequency;
    			annotationCount++;	
    		}
    		System.out.println(annotationCount + "\t" + annotatedPatternFrequency/(double)totalCount);
    	}
    	scanner.close();
    	
    	System.out.println("total patterns annotated = " + annotationCount);
    	System.out.println("% of occurrences annotated = " + annotatedPatternFrequency/(double)totalCount);
    }
    
    public static void printBayraktarAnnotationStatistics(){
    	Multimap<String, Comma> syntaxPatternToCommas = getBayraktarPatternToPTBCommas();
    	@SuppressWarnings("unchecked")
    	int annotatedPatternsCounter = 0;
    	int commaCoverageCounter=0;
    	int totalCommas = 0;
    	for(String pattern : syntaxPatternToCommas.keySet()){
    		if(BayraktarPatternLabeler.isLabelAvailable(pattern)){
    			commaCoverageCounter+=syntaxPatternToCommas.get(pattern).size();
    			annotatedPatternsCounter++;
    		}
    		totalCommas+=syntaxPatternToCommas.get(pattern).size();
    	}
    	System.out.println("# patterns in PTB = " + syntaxPatternToCommas.size());
    	System.out.println("# patterns that have been annoted = " + BayraktarPatternLabeler.deleteGetTotalPatternsAnnotated());
    	System.out.println("# patterns in PTB that have annotations = " + annotatedPatternsCounter);
    	System.out.println("# commas in PTB = " + totalCommas);
    	System.out.println("% of PTB commas that have annotaitons = " + commaCoverageCounter/(double)totalCommas);
    }
    
    
    public static void main(String[] args){
    	BayraktarAnnotationGenerationHelper annotationHelper = new BayraktarAnnotationGenerationHelper();
    	annotationHelper.startManualAnnotation();
	}
}
