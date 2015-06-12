package edu.illinois.cs.cogcomp.comma;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.comma.CommaReader.Ordering;
import edu.illinois.cs.cogcomp.comma.utils.Prac;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;


public class ErrorAnalysis{
    private final String annotationFile;
    
    Map<String, String> idToInstanceInfoMap;

	public ErrorAnalysis(String annotationFile, Parser commaParser) {
		this.annotationFile = annotationFile;
		idToInstanceInfoMap = new HashMap<String, String>();
		try {
			readData(commaParser);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private void readData(Parser commaParser) throws Exception {
		Map<String, TextAnnotation> idTAMap = new HashMap<String, TextAnnotation>();
		Map<String, TextAnnotation> idGoldTAMap = new HashMap<String, TextAnnotation>();
		for(Comma c = (Comma) commaParser.next(); c!=null; c = (Comma) commaParser.next()){
			TextAnnotation goldTA = c.getTextAnnotation(true);
			TextAnnotation TA = c.getTextAnnotation(false);
			idTAMap.put(goldTA.getId(), TA);
			idGoldTAMap.put(goldTA.getId(), goldTA);
		}
		
		Scanner scanner = new Scanner(new File(annotationFile));
		String line;

		line = scanner.nextLine().trim();
		int goldNull=0, TAnull = 0;
		while (scanner.hasNext()) {
			String source = "";
			assert line.startsWith("%%%") : line;

			// Next line is the sentence id (in PTB)
			String textId = scanner.nextLine();
			source += "\n" + textId;
			
			
			while(scanner.hasNext() && !(line = scanner.nextLine()).startsWith("%%%")){
				source += "\n" + line;
			}
			TextAnnotation TA = idTAMap.get(textId);
			TextAnnotation goldTA = idGoldTAMap.get(textId);
			if(goldTA==null)
				System.out.println("Gold null " + ++goldNull);
			if(TA==null)
				System.out.println("Auto null " + ++TAnull);
			
			StringBuilder info = new StringBuilder();
			info.append(source);
			if(goldTA!=null){
				info.append("\n\nPARSE_GOLD\n");
				TreeView tv = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
				info.append(Prac.pennString(tv.getTree(0)));
			}
			if(TA!=null){
				info.append("\n\nPARSE_STANFORD\n");
				TreeView tv1 = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
				info.append(Prac.pennString(tv1.getTree(0)));
				info.append("\n\nPARSE_CHARNIAK\n");
				TreeView tv2 = (TreeView) TA.getView(ViewNames.PARSE_CHARNIAK);
				info.append(Prac.pennString(tv2.getTree(0)));
				info.append("\n\nNER\n");
				info.append(TA.getView(ViewNames.NER));
				info.append("\n\nSHALLOW_PARSE\n");
				info.append(TA.getView(ViewNames.SHALLOW_PARSE));
				info.append("\n\nPOS\n");
				info.append(TA.getView(ViewNames.POS));
				info.append("\n\nSRL_VERB\n");
				info.append(TA.getView(ViewNames.SRL_VERB));
				info.append("\n\nSRL_NORM\n");
				info.append(TA.getView(ViewNames.SRL_NOM));
				info.append("\n\nSRL_PREP\n");
				info.append(TA.getView(ViewNames.SRL_PREP));
			}
			if(!idToInstanceInfoMap.containsKey(textId))
				idToInstanceInfoMap.put(textId, info.toString());
		}

		scanner.close();
	}

    public String getInstanceInfo(String textId){
    	return idToInstanceInfoMap.get(textId);
    }
    
    public static void logPredictionError(String filename, String sentenceText, String prediction, String gold, String info, FeatureVector fv) throws FileNotFoundException{
    	File file = new File(filename);
    	if(file.exists())
    		file = new File(filename+".2");
    	file.getParentFile().mkdirs();
    	PrintWriter writer = new PrintWriter(file);
    	writer.println("Gold: " + gold);
    	writer.println("Prediction: " + prediction);
    	writer.println("\n" + sentenceText);
    	writer.println("\n\n----------------------------------------FEATURES--------------------------------------------");
    	for(int i = 0; i < fv.featuresSize(); i++)
    		writer.println(fv.getFeature(i).toStringNoPackage() + "\n");
    	writer.println("\n\n---------------------------------------ANNOTATIONS------------------------------------------");
		writer.println(info);
		writer.close();
    }
    
    public static void main(String[] args) throws IOException {
    	Parser commaParser = new CommaReader("data/comma_resolution_data.txt", "data/CommaTAGoldFinal.ser", Ordering.ORIGINAL_SENTENCE);
    	ErrorAnalysis ea = new ErrorAnalysis("data/comma_resolution_data.txt", commaParser);
        
        Set<String> textIds = new HashSet<String>();
        for(Comma c = (Comma) commaParser.next(); c!=null; c = (Comma) commaParser.next()){
			TextAnnotation goldTA = c.getTextAnnotation(true);
			textIds.add(goldTA.getId());
        }
        
        for(String textID : textIds){
			PrintWriter writer = new PrintWriter("data/full_annotation/" + (textID).replaceAll("\\W+", "_"));
			writer.println(ea.getInstanceInfo(textID));
			writer.close();
        }
    }
    
    
}