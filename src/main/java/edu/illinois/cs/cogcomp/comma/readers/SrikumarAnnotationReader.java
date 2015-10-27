package edu.illinois.cs.cogcomp.comma.readers;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.comma.annotators.PreProcessor;
import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.Sentence;
import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.NombankReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PropbankReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Sentence reader for the comma annotation dataset of Srikumar et al.
 */
public class SrikumarAnnotationReader implements IResetableIterator<Sentence>{
	private PreProcessor preProcessor;
    private final String annotationFile;
    private List<Comma> commas;
    private List<Sentence> sentences;
    int sentenceIdx;
    private static String treebankHome, propbankHome, nombankHome;
     
    
    
    public SrikumarAnnotationReader(String annotationFile) {
        this.annotationFile = annotationFile;
        CommaProperties properties = CommaProperties.getInstance();
        treebankHome = properties.getPTBHDir();
        propbankHome = properties.getPropbankDir();
        nombankHome = properties.getNombankDir();
        
        try {
        	this.preProcessor = new PreProcessor();
        	readData();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        reset();
    }

    private void readData() throws FileNotFoundException {
    	sentences = new ArrayList<>();
    	
        Map<String, TextAnnotation> taMap = getTAMap();
        Scanner scanner;
		scanner = new Scanner(new File(annotationFile));
        String line;

        int count = 0;
        int failures = 0, skipped = 0;
        while (scanner.hasNext()) {
            count++;

            // A list of commas positions and their labels
            line = scanner.nextLine().trim();
            assert line.startsWith("%%%"):line;

            // Next line is the sentence id (in PTB)
            String textId = scanner.nextLine();

            String[] tokenizedText = scanner.nextLine().trim().split("\\s+");

            boolean skip=false;//should we skip this sentence due to some error?
            TextAnnotation goldTa = null, ta = null;

            if(taMap.containsKey(textId)){
                goldTa = taMap.get(textId);
                try {
                    ta = preProcessor.preProcess(Collections.singletonList(tokenizedText));
                } catch (AnnotatorException e) {
                    e.printStackTrace();
                	skip = true;
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

            Map<Integer, List<Integer>> commaIndexToLabelKeys = getCommaIndexToLabelKeys(line);
            
            line = scanner.nextLine().trim();
            assert line.length() == 0:line;

            line = scanner.nextLine().trim();
            assert line.equals("COMMAS: " + commaIndexToLabelKeys.size() + " Total") : line + "\nVS\n" + "COMMAS: " +
            	commaIndexToLabelKeys.size() + " Total\n" + "rawText = " + StringUtils.join(" ", tokenizedText);

            Set<Integer> labelKeySet = new HashSet<>();
            for(List<Integer> labelKeys: commaIndexToLabelKeys.values())
            	labelKeySet.addAll(labelKeys);
            Map<Integer, String> labelKeyToLabel = new HashMap<>();
            for (int labelKey : Sorters.sortSet(labelKeySet)) {
                line = scanner.nextLine().trim();
                assert line.startsWith(labelKey + "."):line;

                String label = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                switch(label){
                	case "Entity substitute":
                		label = "Substitute";
                		break;
                	case "Entity attribute":
                		label = "Attribute";
                }
                labelKeyToLabel.put(labelKey, label);
                String tmp = line.substring(line.indexOf(":") + 1, line.indexOf("relation")).trim();
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
			if (!skip) {
				List<List<String>> commaLabels = new ArrayList<>();
				List<List<String>> refinedCommaLabels = new ArrayList<>();
				for (int tokenIdx = 0; tokenIdx < tokenizedText.length; tokenIdx++) {
					if (tokenizedText[tokenIdx].equals(",")) {
						List<String> commaLabelsForIdx = new ArrayList<>();
						List<String> refinedLabelsForIdx = new ArrayList<>();
						if (commaIndexToLabelKeys.containsKey(tokenIdx)) {
							for (int labelKey : commaIndexToLabelKeys
									.get(tokenIdx)) {
								String commaLabel = labelKeyToLabel
										.get(labelKey);
								String refinedLabel = commaLabel;
								if (commaLabel.equals("Other"))
									refinedLabel = RefinedLabelHelper
											.getRefinedLabel(tokenIdx + " "
													+ goldTa.getId());
								commaLabelsForIdx.add(commaLabel);
								refinedLabelsForIdx.add(refinedLabel);
							}
							commaLabels.add(commaLabelsForIdx);
							refinedCommaLabels.add(refinedLabelsForIdx);
						} else {
							commaLabels.add(null);
							refinedCommaLabels.add(null);
						}
					}
				}

				try {
					Sentence sentence = new Sentence(ta, goldTa, commaLabels,
							refinedCommaLabels);
					sentences.add(sentence);
				} catch (Exception e) {
					e.printStackTrace();
					failures++;
				}
			}
            
            System.out.print(count);
            if (skipped > 0)
                System.out.print(" SKIPPED(" + skipped + ")");
            if (failures > 0)
                System.out.print(" ANNOTATION FAILED(" + failures + ")");
            
        }
        scanner.close();
    }

    private Map<Integer, List<Integer>> getCommaIndexToLabelKeys(String annotation) {
        String[] parts = annotation.split("\\s+");
        Map<Integer, List<Integer>> map = new HashMap<>();

        for (int tokenIdx = 0; tokenIdx < parts.length; tokenIdx++) {
            String token = parts[tokenIdx];
            if (token.startsWith("[")) {
            	List<Integer> labelKeys = new ArrayList<>();
                String labelKeysString = token.substring(token.indexOf('[')+1, token.indexOf(']'));
                for (String labelKeyString: labelKeysString.split(",")) {
                	labelKeys.add(Integer.parseInt(labelKeyString));
                }
                map.put(tokenIdx, labelKeys);
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
        Map<String, TextAnnotation> taMap = new HashMap<>();
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
    
    public List<Sentence> getSentences(){
    	return sentences;
    }
    
    public List<Comma> getCommas(){
    	if(commas==null){
    		commas = new ArrayList<>();
    		for(Sentence s : sentences)
    			commas.addAll(s.getCommas());
    	}
    	return commas;
    }

	@Override
	public boolean hasNext() {
		return sentenceIdx >= sentences.size();
	}

	@Override
	public Sentence next() {
		return sentences.get(sentenceIdx);
	}

	@Override
	public void remove() {
		sentences.remove(sentenceIdx-1);
		sentenceIdx--;
	}

	@Override
	public void reset() {
		sentenceIdx = 0;
	}
}