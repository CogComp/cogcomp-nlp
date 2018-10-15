/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl.data;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.utilities.XMLUtils;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.lbjava.nlp.DataReader;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Data reader for the Semeval dataset. Creates the {@link ViewNames#SRL_PREP} view using the
 * preposition roles instead of the original senses (uses the {@code sense2role.csv} map).
 *
 * @author Vivek Srikumar
 * @author Christos Christodoulopoulos
 */
public class PrepSRLDataReader extends DataReader {
    private static Preprocessor preprocessor;
    private static Logger log = LoggerFactory.getLogger(PrepSRLDataReader.class);

    private static final String semevalTrainDataDirectory = "train/xml";
    private static final String semevalTestDataDirectory = "test/xml";
    private static final String semevalKeyDirectory = "Answers";

    private int currentNodeId;
    private Hashtable<String, String> keys;

    private static Map<String, String> senseToRole;
    private static Map<String, Set<String>> prepositionToValidRoles;

    private static final Set<String> prepositions = new HashSet<>(Arrays.asList("about", "above",
            "across", "after", "against", "along", "among", "around", "as", "at", "before",
            "behind", "beneath", "beside", "between", "by", "down", "during", "for", "from", "in",
            "inside", "into", "like", "of", "off", "on", "onto", "over", "round", "through", "to",
            "towards", "with"));

    private static final Set<String> mwPrepositionsList = new HashSet<>(Arrays.asList(
            "according to", "ahead of", "as of", "as per", "as regards", "aside from", "at least",
            "because of", "close to", "due to", "except for", "far from", "in to", "inside of",
            "instead of", "near to", "next to", "on to ", "out from", "out of", "outside of",
            "owing to", "prior to", "pursuant to", "regardless of", "subsequent to", "thanks to",
            "that of", "as far as", "as well as", "by means of", "in accordance with",
            "in addition to", "in case of", "in front of", "in lieu of", "in place of",
            "in point of", "in spite of", "on account of", "on behalf of", "with regard to",
            "with respect to"));

    public PrepSRLDataReader(String dataDir, String corpusName) {
        super(dataDir, corpusName, ViewNames.SRL_PREP);
    }

    @Override
    public void preprocess(TextAnnotation ta) throws AnnotatorException {
        getPreprocessor().annotate(ta);
    }

    private Preprocessor getPreprocessor() {
        if (preprocessor == null)
            preprocessor = new Preprocessor(PrepSRLConfigurator.defaults());
        return preprocessor;
    }

    @Override
    public List<TextAnnotation> readData() {
        lazyReadMaps();

        List<TextAnnotation> textAnnotations = new ArrayList<>();
        String dataDir = file + File.separator;
        dataDir +=
                (corpusName.equals("train") ? semevalTrainDataDirectory : semevalTestDataDirectory);
        for (String currentFile : getFiles(dataDir)) {
            NodeList instanceNodeList;
            try {
                // read the xml
                Document dom = XMLUtils.getXMLDOM(dataDir + File.separator + currentFile + ".xml");
                Element docElem = dom.getDocumentElement();
                instanceNodeList = docElem.getElementsByTagName("instance");
            } catch (Exception ex) {
                System.err.println("Unable to get the DOM" + ex);
                return null;
            }

            // read the key file
            if (corpusName.equals("test")) {
                String keyFileName;

                int start = currentFile.indexOf('-') + 1;
                int end = currentFile.indexOf('.');
                keyFileName = currentFile.substring(start, end);

                try {
                    keys = new Hashtable<>();
                    LineIO.read(file + File.separator + semevalKeyDirectory + File.separator
                            + keyFileName + ".key", new ITransformer<String, Void>() {

                        public Void transform(String input) {
                            String[] parts = input.split(" ");
                            keys.put(parts[1], parts[2]);
                            return null;
                        }
                    });
                } catch (FileNotFoundException e) {
                    System.err.println("File " + semevalKeyDirectory + File.separator + keyFileName
                            + ".key not found" + e);
                    return null;
                }
            }
            while (currentNodeId < instanceNodeList.getLength()) {
                TextAnnotation ta =
                        makeNewTextAnnotation((Element) instanceNodeList.item(currentNodeId));
                if (ta == null) {
                    logger.error("{} returned null.", instanceNodeList.item(currentNodeId));
                    currentNodeId++;
                    continue;
                }
                textAnnotations.add(ta);
                currentNodeId++;
            }
        }
        return consolidate(textAnnotations);
    }

    /**
     * Consolidate {@link TextAnnotation}s that have the same text but separate gold views. This is
     * required because of the nature of the Semeval annotations (one annotation per example).
     *
     * @param tas The list of {@link TextAnnotation}s with the Semeval annotations
     * @return The consolidated list of {@link TextAnnotation}s
     */
    private List<TextAnnotation> consolidate(List<TextAnnotation> tas) {
        List<TextAnnotation> consolidatedTAs = new ArrayList<>();
        Map<Integer, List<TextAnnotation>> taMap = new HashMap<>();
        for (TextAnnotation ta : tas) {
            int key = ta.getText().hashCode();
            List<TextAnnotation> annotations = taMap.getOrDefault(key, new ArrayList<>());
            annotations.add(ta);
            taMap.put(key, annotations);
        }
        for (int key : taMap.keySet()) {
            List<TextAnnotation> annotations = taMap.get(key);
            TextAnnotation ta1 = annotations.get(0);
            View view1 = ta1.getView(viewName);
            for (int i = 1; i < annotations.size(); i++) {
                TextAnnotation taI = annotations.get(i);
                View viewI = taI.getView(viewName);
                for (Constituent c : viewI.getConstituents())
                    view1.addConstituent(c);
            }
            consolidatedTAs.add(ta1);
        }
        return consolidatedTAs;
    }

    private static void lazyReadMaps() {
        // Read the sense2role conversion
        senseToRole = new HashMap<>();
        prepositionToValidRoles = new HashMap<>();
        boolean firstLine = true;
        try {
            for (String line : LineIO.readFromClasspath("sense2role.csv")) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.matches("^,+$"))
                    continue;
                String[] parts = line.split(",");

                String preposition = parts[0].trim();
                String sense = preposition + ":" + parts[1].trim();
                String role = parts[3].trim();

                if (preposition.length() == 0)
                    continue;

                senseToRole.put(sense, role);

                if (!prepositionToValidRoles.containsKey(preposition))
                    prepositionToValidRoles.put(preposition, new HashSet<>());
                prepositionToValidRoles.get(preposition).add(role);
            }
        } catch (Exception ex) {
            System.err.println("Error reading the sense2role file" + ex);
        }
    }

    @Override
    public List<Constituent> candidateGenerator(TextAnnotation ta) {
        List<Constituent> candidates = new ArrayList<>();
        // The Semeval data is annotated with only one preposition per example sentence.
        // Thus, every other preposition in the same sentence will not have a gold annotation,
        // and will be mistakenly considered as a CANDIDATE. To fix this, only add prepositions
        // from the gold annotations.
        return getFinalCandidates(ta.getView(viewName), candidates);
    }

    public static boolean isPrep(TextAnnotation ta, int tokenId) {
        String pos = WordHelpers.getPOS(ta, tokenId);
        String word = WordHelpers.getWord(ta, tokenId);
        String lowerCase = word.toLowerCase().trim();

        boolean validPreposition = prepositions.contains(lowerCase);

        boolean isPrepositionPOS = POSUtils.isPOSPreposition(pos);

        // we need to consider the case of "to + verb"
        boolean isToVP = false;
        if (tokenId < ta.size() - 1) {
            if (lowerCase.equals("to") && POSUtils.isPOSVerb(WordHelpers.getPOS(ta, tokenId + 1)))
                isToVP = true;
        }

        return validPreposition && (isPrepositionPOS && !isToVP);
    }

    public static Constituent isBigramPrep(TextAnnotation ta, int tokenId, String viewName) {
        String word = WordHelpers.getWord(ta, tokenId);
        if (tokenId < ta.size() - 1) {
            String nextWord = WordHelpers.getWord(ta, tokenId + 1);
            if (mwPrepositionsList.contains(word + " " + nextWord))
                return new Constituent("", viewName, ta, tokenId, tokenId + 2);
        }
        return null;
    }

    public static Constituent isTrigramPrep(TextAnnotation ta, int tokenId, String viewName) {
        String word = WordHelpers.getWord(ta, tokenId);
        if (tokenId < ta.size() - 2) {
            String nextWord = WordHelpers.getWord(ta, tokenId + 1);
            String nextNextWord = WordHelpers.getWord(ta, tokenId + 2);
            if (mwPrepositionsList.contains(word + " " + nextWord + " " + nextNextWord))
                return new Constituent("", viewName, ta, tokenId, tokenId + 3);
        }
        return null;
    }

    public static Set<String> getLegalRoles(Constituent predicate) {
        if (prepositionToValidRoles == null)
            lazyReadMaps();
        Set<String> strings = new HashSet<>();
        strings.add(DataReader.CANDIDATE);
        if (prepositionToValidRoles.containsKey(predicate.getSurfaceForm().toLowerCase()))
            strings.addAll(prepositionToValidRoles.get(predicate.getSurfaceForm().toLowerCase()));
        return strings;
    }

    private TextAnnotation makeNewTextAnnotation(Element item) {
        String id = item.getAttribute("id");

        NodeList nl = item.getElementsByTagName("context");
        NodeList children = nl.item(0).getChildNodes();

        String rawSentenceString = nl.item(0).getTextContent().replaceAll("[\\t\\n]", "").trim();

        String preposition = "";
        int prepositionPosition = -1;

        for (int i = 0; i < children.getLength(); i++) {
            Node currentNode = children.item(i);

            if (currentNode.getNodeName().equals("head")) {
                preposition = currentNode.getTextContent().toLowerCase();
                int previousLength = 0;
                if (i > 0)
                    previousLength = tokenize(children.item(i - 1).getTextContent()).size();
                prepositionPosition = previousLength;
            }
        }
        String label;
        if (corpusName.equals("test")) {
            if (keys.containsKey(id))
                label = keys.get(id);
            else
                return null;
        } else {
            label =
                    ((Element) (item.getElementsByTagName("answer").item(0)))
                            .getAttribute("senseid");
        }

        // Take only the first label for the 500 or so instances which are given multiple labels.
        if (label.contains(" "))
            label = label.substring(0, label.indexOf(" ")).trim();

        if (label.length() == 0) {
            log.info("No label for id {}, ignoring sentence", id);
            return null;
        }

        rawSentenceString = rawSentenceString.replaceAll("`", "``");
        rawSentenceString = rawSentenceString.replaceAll("\"", "''");
        // XXX Assume text is pre-tokenized
        String[] tokens = rawSentenceString.split("\\s+");

        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(
                        "Semeval2007Prepositions", id, Collections.singletonList(tokens));

        if (!ta.getTokens()[prepositionPosition].toLowerCase().equals(preposition)) {
            assert false;
        }

        TokenLabelView prepositionLabelView = new TokenLabelView(viewName, ta);
        String role = senseToRole.get(preposition + ":" + label);
        prepositionLabelView.addTokenLabel(prepositionPosition, role, 1.0);

        ta.addView(viewName, prepositionLabelView);

        return ta;
    }

    private static List<String> tokenize(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);

        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens())
            tokens.add(tokenizer.nextToken());

        return tokens;
    }

    private List<String> getFiles(String dataDir) {
		List<String> files = new ArrayList<>();
		FilenameFilter xmlFilter = (dir, name) -> (name.startsWith("pp-") && name.endsWith(".xml") && IOUtils
                .isFile(dir.getAbsolutePath() + File.separator + name));

		String[] xmlFiles = (new File(dataDir)).list(xmlFilter);

		assert xmlFiles != null;
		for (String fileName : xmlFiles) {
			String rawFileName = IOUtils.stripFileExtension(fileName);
			files.add(rawFileName);
		}
		return files;
	}
}
