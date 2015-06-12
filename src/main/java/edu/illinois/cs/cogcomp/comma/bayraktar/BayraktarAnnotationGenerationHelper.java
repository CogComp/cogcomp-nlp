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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.collections.map.MultiValueMap;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaLabeler;
import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.utils.Prac;
import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;


public class BayraktarAnnotationGenerationHelper {
	
	static CommaProperties properties = CommaProperties.getInstance();
	private static String treebankHome = properties.getPTBHDir();
	
	/**
	 * return a list of list of TextAnnotation read from specified sections of PTB 
	 * @param sections the section of PTB that are needed
	 * @return list of TextAnnotation read from sections
	 */
	public static List<TextAnnotation> getPTBTAList(String[] sections) {
		List<TextAnnotation> taList = new ArrayList<TextAnnotation>();
        Iterator<TextAnnotation> ptbReader;

        try {
            ptbReader = new PennTreebankReader(treebankHome, sections);
        	//ptbReader = new PennTreebankReader(treebankHome);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        while (ptbReader.hasNext()) {
            TextAnnotation ta = ptbReader.next();
            taList.add(ta);
        }
        return taList;
	}
	
    public static List<TextAnnotation> getPTBTAList() {
    	String[] sections = { "00", "01", "02" , "03", "04", "05", "06", "07", "08", "09", "10"};//, "11", "12" , "13", "14", "15", "16", "17", "18", "19", "20"};
    	return getPTBTAList(sections);
    }
    
    /**
     * 
     * @return a MultiValueMap mapping bayraktar-patterns to Commas taht produce the bayraktar-pattern
     */
    public static MultiValueMap getBayraktarPatternToCommas(){
    	List<TextAnnotation> tas = getPTBTAList();
    	List<Comma> commas = new ArrayList<Comma>();
    	for(TextAnnotation ta : tas){
    		List<Comma> commasInTa = CommaLabeler.getCommas(ta); 
    		commas.addAll(commasInTa);
    	}
    	MultiValueMap bayraktarPatternToTA = new MultiValueMap();
    	for(Comma comma : commas){
    		String bayraktarPattern = comma.getBayraktarPattern();
    		bayraktarPatternToTA.put(bayraktarPattern, comma);
    	}
    	return bayraktarPatternToTA;
    }

    
    static Scanner scanner = new Scanner(System.in);
    /**
     * Present bayraktarPattern and example commas to user on console. The user can then choose from the provided labels to decide on an annotation.
     * @param bayraktarPattern the pattern that need to be annotated
     * @param exampleCommas examples of commas whose bayraktar-pattern is bayraktarPattern
     * @return true if successfully annotated else false
     */
    public static boolean present(String bayraktarPattern, List<Comma> exampleCommas){
    	String data = exampleCommas.size() + "\t" + bayraktarPattern + "\n"
    			+ exampleCommas.get(0).getAnnotatedText() + "\n"
    			+ exampleCommas.get(1).getAnnotatedText() + "\n"
    			+ exampleCommas.get(2).getAnnotatedText() + "\n"
    			+ Prac.pennString(((TreeView)exampleCommas.get(0).getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0));
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
    			System.out.println(exampleCommas.get(currSentence++).getAnnotatedText());
    			break;
    		case 1:
    			System.out.println(Prac.pennString(((TreeView)exampleCommas.get(currParse++).getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0)));
    			break;
    		case 69:
    			return false;
    		default:
    			int idx = cmd - 2;
    			
				try {
	    			PrintWriter patternFile = new PrintWriter(new BufferedWriter(new FileWriter("data/Us+Bayraktar-SyntaxToLabel/" + labels[idx], true)));
					patternFile.println("\n" + bayraktarPattern);
					patternFile.close();
					PrintWriter infoFile = new PrintWriter(new BufferedWriter(new FileWriter("data/Us+Bayraktar-SyntaxToLabel/" + labels[idx] + "-New", true)));
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
    
    public static void main(String[] args){
    	MultiValueMap syntaxPatternToTA = getBayraktarPatternToCommas();
    	@SuppressWarnings("unchecked")
		List<Entry<String, ArrayList<Comma>>> sortedPatterns = new ArrayList<Entry<String,ArrayList<Comma>>>(syntaxPatternToTA.entrySet());
    	Collections.sort(sortedPatterns, new Comparator<Entry<String, ArrayList<Comma>>>(){
			@Override
			public int compare(Entry<String, ArrayList<Comma>> o1,
					Entry<String, ArrayList<Comma>> o2) {
				return o2.getValue().size() - o1.getValue().size();
			}
    	});
    	
    	
    	int totalCount = syntaxPatternToTA.totalSize();
    	int annotatedPatternFrequency = 0;
    	int annotatedFrequency = 0;
    	for(Entry<String, ArrayList<Comma>> entry : sortedPatterns){
    		int patternFrequency = entry.getValue().size();
    		
    		boolean annottaionFound = BayraktarPatternLabeler.isBayraktarLabelAvailable(entry.getKey()); 
    		if(true == annottaionFound || true==present(entry.getKey(), entry.getValue())){
    			annotatedPatternFrequency += patternFrequency;
    			annotatedFrequency++;	
    		}
    		//System.out.println(annotatedFrequency + "\t" + annotatedPatternFrequency/(double)totalCount);
    	}
    	
    	
    	System.out.println("total patterns annotated = " + annotatedFrequency);
    	System.out.println("total occurances annotated = " + annotatedPatternFrequency/(double)totalCount);
	}
}
