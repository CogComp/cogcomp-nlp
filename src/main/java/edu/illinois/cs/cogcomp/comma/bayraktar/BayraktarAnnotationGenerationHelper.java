package edu.illinois.cs.cogcomp.comma.bayraktar;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.collections.map.MultiValueMap;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaLabeler;
import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.utils.PrettyPrint;
import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;


public class BayraktarAnnotationGenerationHelper {
	
    /**
     * 
     * @return a MultiValueMap mapping bayraktar-patterns to Commas taht produce the bayraktar-pattern
     */
    public static MultiValueMap getBayraktarPatternToPTBCommas(){
    	String treebankHome = CommaProperties.getInstance().getPTBHDir();
    	String[] sections = { "00", "01", "02" , "03", "04", "05", "06", "07", "08", "09", "10"};//, "11", "12" , "13", "14", "15", "16", "17", "18", "19", "20"};
    	Iterator<TextAnnotation> ptbReader;
    	try {
            ptbReader = new PennTreebankReader(treebankHome, sections);
        	//ptbReader = new PennTreebankReader(treebankHome);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    	List<Comma> commas = new ArrayList<Comma>();
    	while(ptbReader.hasNext()){
    		TextAnnotation ta = ptbReader.next();
    		List<Comma> commasInTa = CommaLabeler.getCommas(ta); 
    		commas.addAll(commasInTa);
    	}
    	MultiValueMap bayraktarPatternToCommas = new MultiValueMap();
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
    public boolean present(String bayraktarPattern, List<Comma> exampleCommas){
    	String data = exampleCommas.size() + "\t" + bayraktarPattern + "\n"
    			+ exampleCommas.get(0).getVivekAnnotatedText() + "\n"
    			+ exampleCommas.get(1).getVivekAnnotatedText() + "\n"
    			+ exampleCommas.get(2).getVivekAnnotatedText() + "\n"
    			+ PrettyPrint.pennString(((TreeView)exampleCommas.get(0).getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0));
    	System.out.println(data);

    	int currParse = 1;
    	int currSentence = 3;
    	
    	
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
    			System.out.println(exampleCommas.get(currSentence++).getVivekAnnotatedText());
    			break;
    		case 1:
    			System.out.println(PrettyPrint.pennString(((TreeView)exampleCommas.get(currParse++).getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0)));
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
    	MultiValueMap syntaxPatternToCommas = getBayraktarPatternToPTBCommas();
    	@SuppressWarnings("unchecked")
		List<Entry<String, ArrayList<Comma>>> sortedPatterns = new ArrayList<Entry<String,ArrayList<Comma>>>(syntaxPatternToCommas.entrySet());
    	
    	Collections.sort(sortedPatterns, new Comparator<Entry<String, ArrayList<Comma>>>(){
			@Override
			public int compare(Entry<String, ArrayList<Comma>> o1,
					Entry<String, ArrayList<Comma>> o2) {
				return o2.getValue().size() - o1.getValue().size();
			}
    	});
    	
    	int totalCount = syntaxPatternToCommas.totalSize();
    	int annotatedPatternFrequency = 0;
    	int annotationCount = 0;
    	scanner = new Scanner(System.in);
    	for(Entry<String, ArrayList<Comma>> entry : sortedPatterns){
    		int patternFrequency = entry.getValue().size();
    		
    		boolean annottaionFound = BayraktarPatternLabeler.isLabelAvailable(entry.getKey()); 
    		if(true == annottaionFound || true==present(entry.getKey(), entry.getValue())){
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
    	MultiValueMap syntaxPatternToCommas = getBayraktarPatternToPTBCommas();
    	@SuppressWarnings("unchecked")
		List<Entry<String, ArrayList<Comma>>> patternToCommas = new ArrayList<Entry<String,ArrayList<Comma>>>(syntaxPatternToCommas.entrySet());
    	int annotatedPatternsCounter = 0;
    	int commaCoverageCounter=0;
    	int totalCommas = 0;
    	for(Entry<String, ArrayList<Comma>> entry : patternToCommas){
    		String pattern = entry.getKey();
    		if(BayraktarPatternLabeler.isLabelAvailable(pattern)){
    			commaCoverageCounter+=entry.getValue().size();
    			annotatedPatternsCounter++;
    		}
    		totalCommas+=entry.getValue().size();
    	}
    	System.out.println("# patterns in PTB = " + patternToCommas.size());
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
