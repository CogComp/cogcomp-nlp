package edu.illinois.cs.cogcomp.ner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.GoldAnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;

/** 
 * This class tests the provided data, a directory passed as an argument, against the Stanford
 * NER 4 class model. The input data is expected to be in CoNLL column format, and include the 
 * 4 classes, PERSON, LOCATION, ORGANIZATION and MISC. It will print two tabular report at the
 * end using our own TestDiscrete class. The first report will present token level accuracy, the 
 * second will present phrase level accuracy.
 */
public class StanfordNERTest {

    /**
    * @param args
    * @throws Exception
    */
    public static void main(String[] args) throws Exception {
        
        // use the 4 class classifier.
        String serializedClassifier = "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz";
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
        File[] files = new File(args[0]).listFiles();
        List<List<CoreLabel>> result = new ArrayList<List<CoreLabel>>();
        if (files == null || files.length == 0) {
            System.err.println("Either the directory did not exist, or there were no files within.");
            System.exit(-1);
        }
        
        // apply stanford to each file in the directory. This will result in a data structure that stores
        // both the gold standard label, AND the prediction, which is handy.
        for (File file : files) {
            String fileContents = IOUtils.slurpFile(file.getAbsolutePath());
            CoNLLColumnReaderAndWriter t = new CoNLLColumnReaderAndWriter();
            SeqClassifierFlags flags = new SeqClassifierFlags();
            flags.deleteBlankLines = true;
            t.init(flags);
            List<List<CoreLabel>> out = classifier.classifyRaw(fileContents, t);
            
            // translate LOCATION labels to B-LOC, I-LOC and so on. Same for ORGANIZATION , PEOPLE and MISC
            for (List<CoreLabel> sentence : out) {
                CoreLabel previousWord = null;
                for (CoreLabel word : sentence) {
                    String currentAnnotation = word.get(CoreAnnotations.AnswerAnnotation.class);
                    if (!word.get(CoreAnnotations.AnswerAnnotation.class).equals("O")) {
                        String prevAnnotation = previousWord == null ? "" : previousWord.get(CoreAnnotations.AnswerAnnotation.class);
                        if (currentAnnotation.equals("LOCATION")) {
                            if (prevAnnotation.contains("LOC")) {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "I-LOC");
                            } else {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "B-LOC");
                            }
                        } else if (currentAnnotation.equals("PERSON")) {
                            if (prevAnnotation.contains("PER")) {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "I-PER");
                            } else {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "B-PER");
                            }
                        } else if (currentAnnotation.equals("ORGANIZATION")) {
                            if (prevAnnotation.contains("ORG")) {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "I-ORG");
                            } else {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "B-ORG");
                            }
                        } else if (currentAnnotation.equals("MISC")) {
                            if (prevAnnotation.contains("MISC")) {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "I-MISC");
                            } else {
                                word.set(CoreAnnotations.AnswerAnnotation.class, "B-MISC");
                            }
                        } else 
                            System.err.println("WHAT KIND OF ANNOTATION IS "+currentAnnotation);
                    }
                    previousWord = word;
                    //System.out.print(word.word() + '\t' + word.get(CoreAnnotations.AnswerAnnotation.class) + '\t' + word.get(CoreAnnotations.GoldAnswerAnnotation.class) +'\n');
                }
            }
            result.addAll(out);
        }
        
        // produce token level accuracy.
        System.out.println("Token level accuracy:");
        TestDiscrete td = TestDiscrete.testDiscrete(new AnswerClassifier(), new GoldClassifier(), new StanfordParser(result, false));
        td.addNull("O");
        td.printPerformance(System.out);
        
        // produce phrase level accuracy.
        System.out.println("\nPhrase level accuracy:");
        td = TestDiscrete.testDiscrete(new AnswerClassifier(), new GoldClassifier(), new StanfordParser(result, true));
        td.addNull("O");
        td.printPerformance(System.out);
    }
}

/**
 * Produce the gold standard discrete value.
 * @author redman
 */
class GoldClassifier extends Classifier {
    @Override
    public FeatureVector classify(Object o) {
        return null;
    }
    
    /**
     * Strip off the boundry indicator.
     * @param label the label to prune
     * @return the stripped label.
     */
    private String prune(String label) {
        if (label.equals("O"))
            return label;
        else
            return label.substring(2);
    }
    /**
     * return the gold label at this token.
     * @param o The object to classify.
     * @return The value of the feature produced for the input object.
     **/
    public String discreteValue(Object o) {
        Class<GoldAnswerAnnotation> cls = CoreAnnotations.GoldAnswerAnnotation.class;
        if (o instanceof CoreLabel) {
            // single token.
            CoreLabel cl = (CoreLabel) o;
            return cl.get(cls);
        } else {
            // compile the phrase hit
            ArrayList<CoreLabel> hits = (ArrayList)o;
            String firstLabel = hits.get(0).get(cls);
            if (hits.size() == 1)
                return prune(firstLabel);
            
            if (!firstLabel.startsWith("B-"))
                System.err.println("There was a non start tag starting a phrase match in gold!");
            String continueLabel = "I-"+firstLabel.substring(2);
            
            // just make sure the programming logic is correct, we should never see mixed tokens.
            for (int i = 1; i < hits.size() ; i++) {
                CoreLabel cl = hits.get(i);
                if (!cl.get(cls).equals(continueLabel))
                    System.err.println("There was a non continue tag in a phrase match in gold!");
            }
            return prune(firstLabel);
        }
    }
}

/**
 * produce the prediction
 * @author redman
 */
class AnswerClassifier extends Classifier {
    /** default */
    private static final long serialVersionUID = 1L;
    
    @Override
    public FeatureVector classify(Object o) {
        return null;
    }
    /**
     * return the gold label at this token.
     * @param o The object to classify.
     * @return The value of the feature produced for the input object.
     **/
    public String discreteValue(Object o) {
        Class<AnswerAnnotation> cls = CoreAnnotations.AnswerAnnotation.class;
        if (o instanceof CoreLabel) {
            // single token.
            CoreLabel cl = (CoreLabel) o;
            return cl.get(cls);
        } else {
            // compile the phrase hit
            ArrayList<CoreLabel> hits = (ArrayList)o;
            return constructLabel(hits);
        }
    }
    
    /**
     * Strip off the boundry indicator.
     * @param label the label to prune
     * @return the stripped label.
     */
    private String prune(String label) {
        if (label.equals("O"))
            return label;
        else
            return label.substring(2);
    }
    
    /**
     * Determine what to return. If the input value is "O", and the prediction is
     * not, return the prediction (less the "B-" or "I-"). If the input is a hit,
     * and the prediction is a different ,
     * @param hits the phrase match including all tokens.
     * @return the list of labels.
     */
    private String constructLabel(ArrayList<CoreLabel> hits) {
        final Class<AnswerAnnotation> cls = CoreAnnotations.AnswerAnnotation.class;
        final Class<GoldAnswerAnnotation> gcls = CoreAnnotations.GoldAnswerAnnotation.class;
        String firstLabel = hits.get(0).get(cls);
        String goldLabel = hits.get(0).get(gcls);
        
        // just make sure the programming logic is correct, we should never see mixed tokens.
        for (int i = 0; i < hits.size() ; i++) {
            CoreLabel cl = hits.get(i);
            if (!cl.get(cls).equals(cl.get(gcls))) {
                
                // didn't match, decide what to return, make sure it doesn't match.
                if (goldLabel.equals("O"))
                    return prune(firstLabel);
                else {
                    String pruneGoldLabel = prune(goldLabel);
                    String pruneAnswerLabel = prune(firstLabel);
                    if (pruneGoldLabel.equals(pruneAnswerLabel)) {
                        // well, we can return a correct value here because we are not correct
                        return "O";
                    } else {
                        return pruneAnswerLabel;
                    }
                }
            }
        }
        return prune(firstLabel);
    }
}

/**
 * produce the core label set, always only one for token level, but potentially
 * more if phrase level, and the label spans multiple tokens.
 * @author redman
 */
class StanfordParser implements Parser {
    
    /** the labels. */
    private List<List<CoreLabel>> out = null;
    
    /** the index of the current word. */
    private int sentenceIndx = 0;
    
    /** the index of the word within that sentence. */
    private int wordIndex = 0;
    
    /** true if phrase level parsing. */
    private boolean phrase = false;
    /**
     * labels and phrase parsing provided inputs.
     * @param out the data.
     * @param phrase if true, parse phrases
     */
    StanfordParser(List<List<CoreLabel>> out, boolean phrase) {
        this.out = out;
        this.phrase = phrase;
    }
    
    /**
     * Return a core label for token level, an array of core labels for 
     * phrase level.
     */
    @Override
    public Object next() {
        if (phrase) {
            
            // we are looking for phrases, so if we see a "B-", we return all tokens in that phrase
            Class<GoldAnswerAnnotation> goldAnsCls = CoreAnnotations.GoldAnswerAnnotation.class;

            // compile the phrase or a single term.
            ArrayList<CoreLabel> labels = new ArrayList<CoreLabel>();
            while (sentenceIndx < out.size()) {
                if (wordIndex < out.get(sentenceIndx).size()) {
                    
                    // we have found the first token, see if label spans more tokens
                    CoreLabel cl = out.get(sentenceIndx).get(wordIndex++);
                    labels.add(cl);
                    if (((String)cl.get(goldAnsCls)).equals("O")) {
                        
                        // no annotation on this token.
                        return labels;
                    } else {
                        String continueLabel = "I-"+cl.get(goldAnsCls).substring(2);

                        // look for more tokens in this phrase
                        while (wordIndex < out.get(sentenceIndx).size()) {
                            cl = out.get(sentenceIndx).get(wordIndex);
                            String goldLabel = (String) cl.get(goldAnsCls);
                            if (!goldLabel.equals(continueLabel)) 
                                return labels;
                            else {
                                labels.add(cl);
                                wordIndex++;
                            }
                        }
                        return labels; // we are at the end of a sentence.
                    }
                } else {
                    sentenceIndx++;
                    wordIndex = 0;
                }
            }
        } else {
            while (sentenceIndx < out.size()) {
                if (wordIndex < out.get(sentenceIndx).size())
                    return out.get(sentenceIndx).get(wordIndex++);
                else {
                    sentenceIndx++;
                    wordIndex = 0;
                }
            }
        }
        return null;
    }

    @Override
    public void reset() {
        sentenceIndx = 0;
        wordIndex = 0;
    }
    @Override
    public void close() {        
    }
}
