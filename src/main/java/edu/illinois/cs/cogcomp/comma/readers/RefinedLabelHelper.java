package edu.illinois.cs.cogcomp.comma.readers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.utils.PrettyPrintParseTree;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;

/**
 * Used to generate a file of all the commas labeled as Other so that they can be reannotated
 * Once reannotated that same file is used as a data source for providing commas with the refined labels
 * @author navari
 *
 */
public class RefinedLabelHelper {
	private static Map<String, String> otherCommaIdToRefinedLabel = new HashMap<>();
	
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
			String refinedLabel = scanner.nextLine();
			refinedLabel = refinedLabel.split("-")[0].split("\\+")[0];
			otherCommaIdToRefinedLabel.put(id, refinedLabel);
			while(!scanner.nextLine().isEmpty());//removes following lines which contain comments on the label
			while(scanner.hasNextLine() && (id=scanner.nextLine()).isEmpty());//skips over empty lines and stops when it reaches a non-empty line. This line is the id of the next comma
		}
		scanner.close();
	}
	
	/**
	 * @param c the commas whose refined label is needed
	 * @return the refined label if it is available, else null
	 */
	public static String getRefinedLabel(Comma c){
		String id = c.getCommaID();
		return otherCommaIdToRefinedLabel.get(id);
	}

	/** 
	 * 
	 * @param id the comma label id with which the label is associated 
	 * @return the refine label if it is available, else null
	 */ 
	public static String getRefinedLabel(String id){
		return otherCommaIdToRefinedLabel.get(id);
	}
	
	/**
	 * creates a file with all the commas labeled as Other by Srikumar et al
	 * used for manually relabeling the commas
	 */
	public static void createFileOfOtherCommas(){
		CommaProperties properties = CommaProperties.getInstance();
		assert !properties.useNewLabelSet();
		SrikumarAnnotationReader reader = new SrikumarAnnotationReader(properties.getOriginalSrikumarAnnotationFile());
		List<Comma> commas = reader.getCommas();
		String otherTextFileData = "";
		for(Comma comma: commas){
			if(comma.getLabel().equals("Other")){
				otherTextFileData+=comma.getCommaID()+"\n";
				otherTextFileData+=comma.getBayraktarAnnotatedText() + "\n\n\n\n";
			}
		}
		File otherFile = new File("data/refineOtherLabels.txt");
		try {
			FileUtils.writeStringToFile(otherFile, otherTextFileData);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
