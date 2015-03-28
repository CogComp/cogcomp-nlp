package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.edison.data.corpora.PennTreebankReader;
import edu.illinois.cs.cogcomp.edison.data.srl.NombankReader;
import edu.illinois.cs.cogcomp.edison.data.srl.PropbankReader;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.io.*;
import java.util.*;

import org.apache.commons.collections.map.HashedMap;
import org.joda.time.field.DividedDateTimeField;

/**
 * Data reader for the comma dataset of Srikumar et al.
 */
public class CommaReader implements Parser {
    private Annotator annotator;
    private final String annotationFile;
    private List<Comma> commas;
    private int currentComma;
    private static String treebankHome, propbankHome, nombankHome;

    public CommaReader(String annotationFile) {
        this.annotationFile = annotationFile;
        this.commas = new ArrayList<Comma>();

        CommaProperties properties = CommaProperties.getInstance();
        treebankHome = properties.getPTBHDir();
        propbankHome = properties.getPropbankDir();
        nombankHome = properties.getNombankDir();

        try {
            this.annotator = new Annotator(true, true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //File f = new File("data/CommaTA.ser");
        File f = new File("data/CommaTAGoldFinal.ser");

        if (f.exists()) {
            System.out.println("File exists");
            try {
                readSerData(f);
            } catch (IOException e) {
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
    private void readSerData(File f) throws IOException {
        FileInputStream fileIn = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        try {
            commas = (List<Comma>)in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        in.close();
        fileIn.close();
    }

    private void readData() throws IOException {
        Map<String, TextAnnotation> taMap = getTAMap();
        Scanner scanner = new Scanner(new File(annotationFile));
        String line;

        int count = 0;
        int failures = 0, skipped = 0;
        while (scanner.hasNext()) {
            count++;

            // A list of commas positions and their labels
            List<Comma> commaList = new ArrayList<Comma>();
            line = scanner.nextLine().trim();
            assert line.startsWith("%%%"):line;

            // Next line is the sentence id (in PTB)
            String textId = scanner.nextLine();

            String rawText = scanner.nextLine().trim();

            boolean skip=false;
            TextAnnotation goldTA = null, TA = null;

            if(taMap.containsKey(textId)){
                goldTA = taMap.get(textId);
                try {
                    TA = annotator.preProcess(rawText);
                } catch (Exception e) {
                    failures++;
                }
            }
            else {
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
            assert line.equals("COMMAS: " + labeledCommas.size() + " Total") : line + "\nVS\n" + "COMMAS: " +
                    labeledCommas.size() + " Total\n" + "rawText = " + rawText;

            Sentence sentence  = new Sentence();
            for (int commaId : Sorters.sortSet(labeledCommas.keySet())) {
                line = scanner.nextLine().trim();
                assert line.startsWith(commaId + "."):line;

                String commaLabel = line.split("\\]")[0].split("\\[")[1].trim();
                for (int commaIndex : labeledCommas.get(commaId)){
                    if (!skip) {
                    	Comma c = new Comma(commaIndex, commaLabel, rawText, TA, goldTA, sentence);
                    	sentence.addComma(c);;
                        commaList.add(c);
                    }
                }
                String tmp = line.substring(line.indexOf(":") + 1, line.indexOf(" relation")).trim();
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
            System.out.print(count);
            if (skipped > 0)
                System.out.print(" SKIPPED(" + skipped + ")");
            if (failures > 0)
                System.out.print(" ANNOTATION FAILED(" + failures + ")");
        }
        writeSerCommas(commas, "data/CommaTAGoldFinal.ser");
        scanner.close();
    }

    public static void writeSerCommas(List<Comma> commas, String filename) throws IOException{
        FileOutputStream fileOut = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(commas);
        out.close();
        fileOut.close();
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
    public void close() {}

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

    /**
     * Returns the map of gold-standard annotations (SRLs, parses) for the comma data (found in section 00 of PTB).
     *
     * @return A map of 'gold' {@link TextAnnotation} indexed by their IDs
     */
    public Map<String, TextAnnotation> getTAMap() {
        Map<String, TextAnnotation> taMap = new HashMap<String, TextAnnotation>();
        Iterator<TextAnnotation> ptbReader, propbankReader, nombankReader;

        String[] sections = { "00" };
        String goldVerbView = ViewNames.SRL_VERB + "_GOLD";
        String goldNomView = ViewNames.SRL_NOM + "_GOLD";

        try {
            ptbReader = new PennTreebankReader(treebankHome, sections);
            propbankReader = new PropbankReader(treebankHome, propbankHome, sections, goldVerbView, true);
            nombankReader = new NombankReader(treebankHome, nombankHome, sections, goldNomView, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Add the gold parses for each sentence in the corpus
        while (ptbReader.hasNext()) {
            TextAnnotation ta = ptbReader.next();
            taMap.put(ta.getId(), ta);
        }
        // Add the new SRL_VERB view (if it exists)
        while (propbankReader.hasNext()) {
            TextAnnotation verbTA = propbankReader.next();
            if (!verbTA.hasView(goldVerbView)) continue;

            TextAnnotation ta = taMap.get(verbTA.getId());
            ta.addView(ViewNames.SRL_VERB, verbTA.getView(goldVerbView));
            taMap.put(ta.getId(), ta);
        }
        // Add the new SRL_NOM view (if it exists)
        while (nombankReader.hasNext()) {
            TextAnnotation nomTA = nombankReader.next();
            if (!nomTA.hasView(goldNomView)) continue;

            TextAnnotation ta = taMap.get(nomTA.getId());
            ta.addView(ViewNames.SRL_NOM, nomTA.getView(goldNomView));
            taMap.put(ta.getId(), ta);
        }
        return taMap;
    }

    public static void main(String[] args) throws IOException {
        CommaReader cr = new CommaReader("data/comma_resolution_data.txt");
        Comma c;
        Set<Sentence> sentenceSet = new HashSet<Sentence>();
        while((c=(Comma)cr.next()) != null)
        {
        	Sentence s = c.getSentence();
        	sentenceSet.add(s);
        }
        divideTDT(sentenceSet);
    }
    
    public static void divideTDT(Collection<Sentence> sentences) throws IOException{
    	List<Sentence> sentenceList = new ArrayList<Sentence>(sentences);
    	double train = 0.7;
    	double dev = 0.1;
    	double test = 0.2;
    	int numSentences = sentenceList.size();
    	Collection<Sentence> trainSentences = sentenceList.subList(0, (int) (numSentences * train));
    	Collection<Sentence> devSentences = sentenceList.subList((int) (numSentences * train), (int) (numSentences * (train+dev)));
    	Collection<Sentence> testSentences = sentenceList.subList((int) (numSentences * (train+dev)), numSentences);
    	
    	List<Comma> trainCommas = new ArrayList<Comma>();
    	List<Comma> devCommas = new ArrayList<Comma>();
    	List<Comma> testCommas = new ArrayList<Comma>();
    	
    	for(Sentence s: trainSentences)
    		trainCommas.addAll(s.getCommas());
    	
    	for(Sentence s: devSentences)
    		devCommas.addAll(s.getCommas());

    	for(Sentence s: testSentences)
   			testCommas.addAll(s.getCommas());

    	System.out.println("hello");
    	writeSerCommas(trainCommas, "data/train_commas.ser");
    	writeSerCommas(trainCommas, "data/dev_commas.ser");
    	writeSerCommas(trainCommas, "data/test_commas.ser");
    }
    
}