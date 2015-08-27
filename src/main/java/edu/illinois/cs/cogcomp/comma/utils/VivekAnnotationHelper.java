package edu.illinois.cs.cogcomp.comma.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.illinois.cs.cogcomp.comma.CommaProperties;

public class VivekAnnotationHelper {
	Map<String, String> annotationIdToAnnotation = new HashMap<String, String>();
	
	public VivekAnnotationHelper(){
		instantiate();
	}
	
	public void instantiate(){
		String OriginalVivekAnnotationFileName = CommaProperties.getInstance().getOriginalVivekAnnotationFile();
		Scanner scanner;
		try {
			scanner = new Scanner(new File(OriginalVivekAnnotationFileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		String line;

		line = scanner.nextLine().trim();
		
		while (scanner.hasNext()) {
			assert line.startsWith("%%%") : line;
	
			// Next line is the sentence id (in PTB)
			String annotationId = scanner.nextLine();
			String annotation = "";
			while(scanner.hasNext() && !(line = scanner.nextLine()).startsWith("%%%")){
				annotation += line + "\n";
			}
			annotationIdToAnnotation.put(annotationId, annotation);
		}
		scanner.close();
	}
	
	public String getAnnotation(String annotationId){
		return annotationIdToAnnotation.get(annotationId);
	}
}
