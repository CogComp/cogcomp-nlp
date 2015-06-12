package edu.illinois.cs.cogcomp.comma;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.comma.utils.Prac;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class RelabelOtherHelper {
	public static void main(String[] args){
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt", "data/CommaFullView.ser", CommaReader.Ordering.ORIGINAL_SENTENCE);
		List<Comma> commas = reader.getCommas();
		Map<String, Comma> idToComma = new HashMap<String, Comma>();
		for(Comma comma: commas)
			idToComma.put(comma.getCommaID(), comma);
		
		Scanner scanner = new Scanner(System.in);
		while(true){
			String id = scanner.nextLine();
			Comma comma = idToComma.get(id);
			System.out.println(Prac.pennString(((TreeView)comma.getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0))+"\n\n\n");
		}
	}
	
	public static void duplicateFinder(){
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt", "data/CommaFullView.ser", CommaReader.Ordering.ORIGINAL_SENTENCE);
		List<Comma> commas = reader.getCommas();
		Map<String, Comma> commaIDs = new HashMap<String, Comma>();
		for(Comma comma: commas){
			String id = comma.getCommaID();
			if(commaIDs.containsKey(id)){
				System.out.println(commaIDs.get(id).getAnnotatedText());
				System.out.println(comma.getAnnotatedText());
			}
			else {
				commaIDs.put(id, comma);
			}
		}
		System.out.println("shaha");
		System.out.println(commaIDs.size());
		System.out.println(commas.size());
	}
	
	public static void createAnnotationFile(){
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt", "data/CommaFullView.ser", CommaReader.Ordering.ORIGINAL_SENTENCE);
		List<Comma> commas = reader.getCommas();
		String otherTextFileData = "";
		for(Comma comma: commas){
			if(comma.getRole().equals("Other")){
				System.out.println(comma.getRole() + "\t" + comma.getBayraktarLabel());
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
}
