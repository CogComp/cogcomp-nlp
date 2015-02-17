package edu.illinois.cs.cogcomp.comma;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.ServiceUnavailableException;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.edison.sentences.TokenizerUtilities.SentenceViewGenerators;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.srl.data.PropbankReader;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;

/**
 * Data reader for the comma dataset of Srikumar et al.
 */
public class CommaReader implements Parser {
	private final String annotationFile;
	private List<Comma> commas;
	public List<Boolean> annotated;
	private int currentComma;
	List<TextAnnotation> taList;
	public CommaReader(String annotationFile){
		this.annotationFile = annotationFile;
		commas = new ArrayList<Comma>();
		annotated = new ArrayList<Boolean>();
		
		//File f = new File("data/CommaTA.ser");
		File f = new File("data/CommaTAGoldFinal.ser");

		if (f.exists()) {
			System.out.println("File exists");
			try {
				readSerData(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("File not found!");
			try {
				readData();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readSerData(File f) throws IOException{
		FileInputStream fileIn = new FileInputStream(f);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		try {
			commas = (List<Comma>)in.readObject();
			annotated = (List<Boolean>)in.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		in.close();
		fileIn.close();
	}

	private void readData() throws ServiceUnavailableException, AnnotationFailedException, TException, IOException{
		taList = getTAList();
		Map<String, TextAnnotation>taMap = new HashMap<String, TextAnnotation>();
		PrintWriter writer;
		writer = new PrintWriter("data/annotations", "UTF-8");
		int i=1;
		for(TextAnnotation ta : taList){
			writer.println(i++ + "\t" + ta.getId() + "\t" + ta.getText());
			taMap.put(ta.getText(), ta);
		}
		writer.close();
		
		Scanner scanner = new Scanner(new File(annotationFile));
		String line;
		
		int count = 0;
		int misses=0;
		int failures = 0, successes = 0, skipped = 0;
		while (scanner.hasNext()) {
			count++;
			
			// A list of commas positions and their labels
			List<Comma> commaList = new ArrayList<Comma>();
			List<Boolean> annotatedList = new ArrayList<Boolean>();
			line = scanner.nextLine().trim();
			assert line.startsWith("%%%"):line;

			// Next line is the sentence id (in PTB), ignore for now
			String  textId = scanner.nextLine();
			//int textIdx = Integer.parseInt(textId.substring(textId.indexOf('.')+1));

			String rawText = scanner.nextLine().trim();
			
			boolean skip=false;
			TextAnnotation goldTA=null, TA=null;
			boolean annotationFailed = false;
			
			if(taMap.containsKey(rawText)){
				goldTA = taMap.get(rawText);
				TA = new TextAnnotation(goldTA.getCorpusId(), goldTA.getId(), Arrays.asList(goldTA.getTokenizedText()));
				annotationFailed = addAnnotations(TA);
				if(annotationFailed)
					failures++;
				else
					successes++;
			}
			else{
				skip = true;
				skipped++;
			}
			
			
			line = scanner.nextLine().trim();
			assert line.length() == 0:line;

			line = scanner.nextLine().trim();
			assert line.equals("ANNOTATION:"):line;

			line = scanner.nextLine().trim();

			Map<Integer, Set<Integer>> labeledCommas = getLabeledCommas(line);

			line = scanner.nextLine().trim();
			assert line.length() == 0:line;

			line = scanner.nextLine().trim();
			assert line.equals("COMMAS: " + labeledCommas.size() + " Total") : line + "\nVS\n" + "COMMAS: " + labeledCommas.size() + " Total\n" + "rawText = " + rawText;

			for (int commaId : Sorters.sortSet(labeledCommas.keySet())) {
				line = scanner.nextLine().trim();
				assert line.startsWith(commaId + "."):line;

				String commaLabel = line.split("\\]")[0].split("\\[")[1].trim();
				for (int commaIndex : labeledCommas.get(commaId)){
					if (!skip){
						commaList.add(new Comma(commaIndex, commaLabel, rawText, TA, goldTA));
						annotatedList.add(annotationFailed);
					}
				}
				String tmp = line.substring(line.indexOf(":") + 1,
						line.indexOf(" relation")).trim();
				int numRelations = Integer.parseInt(tmp);

				// Skip the relations and the comment
				line = scanner.nextLine();
				assert line.startsWith("(Comments:"):line;
				for (int relationId = 0; relationId < numRelations; relationId++)
					scanner.nextLine();

				// Skip the empty line after a comma group
				line = scanner.nextLine().trim();
				assert line.length() == 0:line;
			}
			
			commas.addAll(commaList);
			annotated.addAll(annotatedList);
			if(skip)
				System.out.println(count + " SKIPPED(" + skipped + ")");
			else if(annotationFailed)
				System.out.println(count + " ANNOTATION FAILED("+ failures+ ")");
			else
				System.out.println(count + " SUCCESFUL(" + successes + ")");
		}
		writeSerData();
		scanner.close();
	}
	
	public void writeSerData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream("data/CommaTAGoldFinal.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(commas);
		out.writeObject(annotated);
		out.close();
		fileOut.close();
	}
	
	public static boolean addAnnotations(TextAnnotation ta){
		String curatorHost = "trollope.cs.illinois.edu";
		int curatorPort = 9010;
		boolean respoectTokenization = true;
		CuratorClient client = new CuratorClient(curatorHost, curatorPort, respoectTokenization);
		
		// Should the curator's cache be forcibly updated?
		boolean forceUpdate = false;
		boolean annotationFailed = false;
		
		try{
			client.addNamedEntityView(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		try{
			client.addChunkView(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		try{
			client.addStanfordParse(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		try{
			client.addPOSView(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		try{
			client.addBerkeleyParse(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		try{
			client.addCharniakParse(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		try{
			client.addSRLNomView(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		try{
			client.addSRLVerbView(ta, forceUpdate);
		}catch(Exception e){
			annotationFailed=true;
			e.printStackTrace();
		}
		
		return annotationFailed;
	}

	public TextAnnotation getMatchingTA(String rawText){
		TextAnnotation best=null;
		int bestScore=0;
		
		String[] rawTokens = rawText.split("\\s+");
		System.out.println();
		for (TextAnnotation ta : taList) {
			int score = 0;
			String text = ta.getText();
			for (String token : rawTokens) {
				if (text.contains(token))
					score++;

				if (score > bestScore) {
					best = ta;
					bestScore = score;
				}
			}
		}
		return best;
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
		PrintWriter writer;
		try {
			writer = new PrintWriter("t3.txt", "UTF-8");
			ptbr = new PennTreebankReader("data/pennTreeBank/treebank-3/parsed/mrg/wsj", sections);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		for (TextAnnotation ta : ptbr) {
			if(ta!=null){
				taList.add(ta);
				writer.println(ta);
			}
		}
		writer.close();
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
	
	public Comma getMatchingComma(String s){
		for(Comma c : commas){
			if(c.goldTA.getText().contains(s))
				return c;
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt");
		Comma sentence;
		//String s = "In one feature , called `` In the Dumpster";
		String s[] = {
				"In one feature , called `` In the Dumpster",
				""
		};
		Comma c = reader.getMatchingComma(s);
		if(c == null)
			System.out.println("FML!!!!!");
		else{
			System.out.println(c.goldTA.getView(ViewNames.PARSE_GOLD));
			System.out.println(c.goldTA.getView(ViewNames.POS));
			System.out.println(c.goldTA.getView(ViewNames.NER));
			System.out.println(c.goldTA.getView(ViewNames.SRL_VERB));
			System.out.println(c.goldTA.getView(ViewNames.SHALLOW_PARSE));
		}
	}
}