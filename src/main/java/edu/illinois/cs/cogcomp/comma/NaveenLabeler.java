package edu.illinois.cs.cogcomp.comma;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NaveenLabeler {
	private static Map<String, String> otherCommaIdToNaveenLabel = new HashMap<String, String>();
	
	static {
		Scanner scanner;
		try {
			scanner = new Scanner(new File("data/otherFile.txt"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		String id = scanner.nextLine();
		String line;
		while(id!=null){
			line = scanner.nextLine();//sentenceText
			assert line.length()>0;
			String naveenLabel = scanner.nextLine();
			naveenLabel = naveenLabel.split("-")[0];
			otherCommaIdToNaveenLabel.put(id, naveenLabel);
			while(!scanner.nextLine().isEmpty());
			while((id=scanner.nextLine()).isEmpty());
		}
		scanner.close();
	}
	
	public static String getNaveenLabel(Comma c){
		String id = c.getCommaID();
		return otherCommaIdToNaveenLabel.get(id);
	}
}
