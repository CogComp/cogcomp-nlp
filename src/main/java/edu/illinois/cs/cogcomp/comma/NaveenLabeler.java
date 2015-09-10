package edu.illinois.cs.cogcomp.comma;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.comma.utils.PrettyPrint;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;

public class NaveenLabeler {
	private static Map<String, String> otherCommaIdToNaveenLabel = new HashMap<>();
	
	static {
		CommaProperties commaProperties = CommaProperties.getInstance();
		Scanner scanner;
		try {
			scanner = new Scanner(new File(commaProperties.getOtherRelabeledFile()));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		String id = scanner.nextLine();
		String line;
		while(scanner.hasNextLine()){
			line = scanner.nextLine();//sentenceText
			assert line.length()>0;
			String naveenLabel = scanner.nextLine();
			naveenLabel = naveenLabel.split("-")[0].split("\\+")[0];
			/*if(naveenLabel.contains("Said"))
				naveenLabel = "Said";
			else
				naveenLabel = naveenLabel.split("-")[0].split("\\+")[0];*/
			otherCommaIdToNaveenLabel.put(id, naveenLabel);
			while(!scanner.nextLine().isEmpty());//removes following lines which contain comments on the label
			while(scanner.hasNextLine() && (id=scanner.nextLine()).isEmpty());//skips over empty lines and stops when it reaches a non-empty line. This line is the id of the next comma
		}
		scanner.close();
	}
	
	public static String getNaveenLabel(Comma c){
		String id = c.getCommaID();
		return otherCommaIdToNaveenLabel.get(id);
	}
	
	public static String getNaveenLabel(String id){
		return otherCommaIdToNaveenLabel.get(id);
	}
	
	/**
	 * creates a file with all the commas labeled as Other by Srikumar et al
	 * used for manually relabeling the commas
	 */
	public static void createFileOfOtherCommas(){
		VivekAnnotationCommaParser reader = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		List<Comma> commas = reader.getCommas();
		String otherTextFileData = "";
		for(Comma comma: commas){
			if(comma.getVivekRole().equals("Other")){
				otherTextFileData+=comma.getCommaID()+"\n";
				otherTextFileData+=comma.getBayraktarAnnotatedText() + "\n\n\n\n";
			}
		}
		File otherFile = new File("data/change.txt");
		try {
			FileUtils.writeStringToFile(otherFile, otherTextFileData);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * input: comma-id of a comma
	 * output: penntreebank based parse tree annotation of the sentence of the comma whose comma-id was provided
	 */
	public static void annotationHelper(){
		VivekAnnotationCommaParser reader = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		List<Comma> commas = reader.getCommas();
		Map<String, Comma> idToComma = new HashMap<>();
		for(Comma comma: commas)
			idToComma.put(comma.getCommaID(), comma);
		
		Scanner scanner = new Scanner(System.in);
		while(true){
			String id = scanner.nextLine();
			Comma comma = idToComma.get(id);
			System.out.println(PrettyPrint.pennString(((TreeView)comma.getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0))+"\n\n\n");
		}
	}
	
	public static void main(String[] args){
		for(Map.Entry<String, String> idLabel : otherCommaIdToNaveenLabel.entrySet()){
			if(idLabel.getValue().equals("Quotation")){
				System.out.println(idLabel.getKey());
			}
		}
	}
}
