package edu.illinois.cs.cogcomp.comma;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Queries;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Data reader for the comma dataset of Srikumar et al.
 */
public class CommaReader implements Parser {
	private Scanner scanner;
	private final String annotationFile;
	private List<Comma> commas;
	private int currentComma;

	public CommaReader(String annotationFile){
		this.annotationFile = annotationFile;
		commas = new ArrayList<Comma>();
		readData();
	}

	private void readData() {
		
		List<TextAnnotation> taList = getTAList();
		Map<String, TextAnnotation>taMap = new HashMap<String, TextAnnotation>();
		PrintWriter writer;
		try {
			writer = new PrintWriter("data/annotations", "UTF-8");
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		int i=1;
		for(TextAnnotation ta : taList){
			writer.println(i++ + "\t" + ta.getId() + "\t" + ta.getText());
			taMap.put(ta.getText(), ta);
		}
		writer.close();
		
		try {
			scanner = new Scanner(new File(annotationFile));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		String line;
		int misses = 1;
		while (scanner.hasNext()) {
			// A list of commas positions and their labels
			List<Comma> commaList = new ArrayList<Comma>();

			line = scanner.nextLine().trim();
			assert line.startsWith("%%%");

			// Next line is the sentence id (in PTB), ignore for now
			String  textId = scanner.nextLine();
			/*textId = textId.split("\\.")[1];
			int textNum = Integer.parseInt(textId);*/

			String rawText = scanner.nextLine().trim();
			
			boolean skip=false;
			if(!taMap.containsKey(rawText)){
				skip = true;
				//System.out.println(misses + "\t" + rawText);
				misses++;
			}
			line = scanner.nextLine().trim();
			assert line.length() == 0;

			line = scanner.nextLine().trim();
			assert line.equals("ANNOTATION:");

			line = scanner.nextLine().trim();

			Map<Integer, Set<Integer>> labeledCommas = getLabeledCommas(line);

			line = scanner.nextLine().trim();
			assert line.length() == 0;

			line = scanner.nextLine().trim();
			assert line.equals("COMMAS: " + labeledCommas.size() + " Total") : rawText
					+ "\n" + labeledCommas.size() + "\n" + line;

			for (int commaId : Sorters.sortSet(labeledCommas.keySet())) {
				line = scanner.nextLine().trim();
				assert line.startsWith(commaId + ".");

				String relationName = line.split("\\]")[0].split("\\[")[1]
						.trim();

				for (int commaIndex : labeledCommas.get(commaId))
					if(!skip)
						commaList.add(new Comma(commaIndex, relationName, rawText, taMap.get(rawText)));

				String tmp = line.substring(line.indexOf(":") + 1,
						line.indexOf(" relation")).trim();
				int numRelations = Integer.parseInt(tmp);

				// Skip the rest of the lines
				for (int relationId = 0; relationId < numRelations + 1; relationId++)
					scanner.nextLine();

				// Skip the final (empty) line
				line = scanner.nextLine().trim();

				assert line.length() == 0;
			}
			commas.addAll(commaList);
		}
	}

	@Override
	public Object next() {
		if (commas.size() > currentComma)
			return commas.get(currentComma++);
		return null;
	}

	@Override
	public void reset() {
		currentComma = 0;
	}

	@Override
	public void close() {
		scanner.close();
	}

	private Map<Integer, Set<Integer>> getLabeledCommas(String annotation) {
		Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
		String[] parts = annotation.split("\\s+");

		for (int tokenId = 0; tokenId < parts.length; tokenId++) {
			String token = parts[tokenId];
			if (token.startsWith("[")) {
				String id = token.split("\\]")[0].replaceAll("\\[", "");
				for (String cId : id.split(",")) {
					int commaId = Integer.parseInt(cId);
					if (!map.containsKey(commaId))
						map.put(commaId, new HashSet<Integer>());
					map.get(commaId).add(tokenId);
				}
			}
		}
		return map;
	}

	public List<TextAnnotation> getTAList(){
		String[] sections = { "00" };
		List<TextAnnotation> taList = new ArrayList<TextAnnotation>();
		PennTreebankReader ptbr;
		try {
			ptbr = new PennTreebankReader("data/pennTreeBank", sections);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (TextAnnotation ta : ptbr) {
			taList.add(ta);
		}

		Collections.sort(taList, new Comparator<TextAnnotation>() {

			@Override
			public int compare(TextAnnotation ta1, TextAnnotation ta2) {
				String[] idx1 = ta1.getId().split("\\:");
				String[] idx2 = ta2.getId().split("\\:");
				if(idx1[0].compareTo(idx2[0]) != 0)
					return idx1[0].compareTo(idx2[0]);
				return Integer.parseInt(idx1[1])-Integer.parseInt(idx2[1]);
			}
		});

		return taList;
	}
	
	public static void main(String[] args) throws Exception {
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt");
		Comma c;
		reader.next();
		reader.next();
		reader.next();
		reader.next();
		reader.next();
		reader.next();
		reader.next();
		reader.next();
		reader.next();
		reader.next();
		while ((c = (Comma) reader.next()) != null){
				TreeView parseView= (TreeView) c.ta.getView(ViewNames.PARSE_GOLD);
				System.out.println(parseView);
				List<Constituent> commaConstituents = parseView.getConstituentsCoveringToken(c.commaPosition);
				System.out.println(commaConstituents.size());
				for(Constituent con: commaConstituents)
					System.out.println(con.getLabel() + "\t" + con);
				Constituent comma = null;
				for(Constituent con: commaConstituents)
					if(TreeView.isLeaf(con)){
						comma = con;
						System.out.println("SUCESS");
					}
				for(Constituent sibiling : parseView.where(Queries.isSiblingOf(comma)))
					System.out.println(sibiling);
				/*Constituent leftSib = parseView.where(Queries.isSiblingOf(comma)).where(Queries.adjacentToBefore(comma)).iterator().next();
				System.out.println(leftSib);*/
				/*
				List<String> labels = parseView.getLabelsCoveringToken(c.commaPosition);
				System.out.println(parseView);
				for(String label: labels)
					System.out.println(label);*/
				break;
		}
	}
}
