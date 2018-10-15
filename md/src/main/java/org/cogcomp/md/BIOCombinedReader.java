/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import net.didion.jwnl.JWNLException;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.*;
import java.util.*;
import java.lang.*;

/**
 * @ParserType: Constituent
 * This is a reader that reads multiple corpus at the same time.
 * Current version supports reading ACE and ERE together.
 * The reader reads TextAnnotation list from file.
 * Must use the function "generateNewSplit" to splits before using.
 */
public class BIOCombinedReader extends BIOReader {
    List<Constituent> constituents;
    List<TextAnnotation> currentTas;
    int cons_idx;
    int ta_idx;
    String _type;
    String _mode;
    Boolean _taOnly;
    /**
     * @param fold The fold index (0-4) you want to access
     * @param mode Indicates the corpus and train/eval e.g. "ERE-TRAIN"
     *              mode "ALL-TRAIN/EVAL" indicates hybrid corpus.
     * @param type Indicates the type (NAM/NOM/PRO/ALL) kept
     * @throws DatastoreException 
     * @throws JWNLException 
     * @throws IOException 
     * @throws InvalidEndpointException 
     * @throws InvalidPortException 
     */
    public BIOCombinedReader(int fold, String mode, String type) throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        _mode = mode;
        _type = type;
        _taOnly = false;
        currentTas = readTasByFold(fold, mode);
        constituents = getTokensFromTAs();
        cons_idx = 0;
        ta_idx = 0;
        id = "Hybrid_" + fold;
    }

    public BIOCombinedReader(int fold, String mode, String type, Boolean taOnly) throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException{
        _mode = mode;
        _type = type;
        _taOnly = taOnly;
        currentTas = readTasByFold(fold, mode);
        if (!taOnly) {
            constituents = getTokensFromTAs();
            cons_idx = 0;
        }
        ta_idx = 0;
        id = "Hybrid_" + fold;
    }
    public List<TextAnnotation> readTasByFold(int fold, String mode){
        List<String> corpus = new ArrayList<>();
        if (mode.contains("ACE")){
            corpus.add("ACE");
        }
        if (mode.contains("ERE")){
            corpus.add("ERE");
        }
        if (mode.contains("ALL")){
            corpus.add("ACE");
            corpus.add("ERE");
        }
        List<TextAnnotation> tas = getTAs(corpus);
        HashMap<String, TextAnnotation> taMap = new HashMap<>();
        for (TextAnnotation ta : tas){
            taMap.put(ta.getId(), ta);
        }
        List<TextAnnotation> ret = new ArrayList<>();
        String file_name = "";
        if (mode.contains("ACE")) {
            if (mode.contains("TRAIN")) {
                file_name = "data/split/ace_train_fold_" + fold;
            } else if (mode.contains("EVAL")) {
                file_name = "data/split/ace_eval_fold_" + fold;
            } else {
                return ret;
            }
        }
        else if (mode.contains("ERE")){
            if (mode.contains("TRAIN")) {
                file_name = "data/split/ere_train_fold_" + fold;
            } else if (mode.contains("EVAL")) {
                file_name = "data/split/ere_eval_fold_" + fold;
            } else {
                return ret;
            }
        }
        else if (mode.contains("ALL")){
            if (mode.contains("TRAIN")) {
                file_name = "data/split/train_fold_" + fold;
            } else if (mode.contains("EVAL")) {
                file_name = "data/split/eval_fold_" + fold;
            } else {
                return ret;
            }
        }
        POSAnnotator posAnnotator = new POSAnnotator();
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String line;
            while ((line = br.readLine()) != null) {
                TextAnnotation ta = taMap.get(line);
                try {
                    ta.addView(posAnnotator);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                ret.add(ta);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
    private List<Constituent> getTokensFromTAs() throws InvalidPortException, InvalidEndpointException, IOException, JWNLException, DatastoreException {
        List<Constituent> ret = new ArrayList<>();
        WordNetManager wordNet = null;
        Gazetteers gazetteers = null;
        BrownClusters brownClusters = null;
        Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
        File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
        gazetteers = GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", true, Language.English);
        Vector<String> bcs = new Vector<>();
        bcs.add("brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
        bcs.add("brown-clusters/brownBllipClusters");
        bcs.add("brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt");
        Vector<Integer> bcst = new Vector<>();
        bcst.add(5);
        bcst.add(5);
        bcst.add(5);
        Vector<Boolean> bcsl = new Vector<>();
        bcsl.add(false);
        bcsl.add(false);
        bcsl.add(false);
        brownClusters = BrownClusters.get(bcs, bcst, bcsl);
        WordNetManager.loadConfigAsClasspathResource(true);
        wordNet = WordNetManager.getInstance();
        for (TextAnnotation ta : currentTas){
            View tokenView = ta.getView(ViewNames.TOKENS);
            String mentionViewName = "";
            if (ta.getId().startsWith("bn") || ta.getId().startsWith("nw")){
                mentionViewName = ViewNames.MENTION_ACE;
            }
            else{
                mentionViewName = ViewNames.MENTION_ERE;
            }
            View mentionView = ta.getView(mentionViewName);
            View bioView = new SpanLabelView("BIO", BIOReader.class.getCanonicalName(), ta, 1.0f);
            String[] token2tags = new String[tokenView.getConstituents().size()];
            for (int i = 0; i < token2tags.length; i++){
                token2tags[i] = "O";
            }
            for (Constituent c : mentionView.getConstituents()){
                if (!_type.equals("ALL")) {
                    String excludeType = _type;
                    if (!c.getAttribute("EntityMentionType").equals(excludeType)) {
                        continue;
                    }
                }
                Constituent cHead = ACEReader.getEntityHeadForConstituent(c, ta, "HEAD");
                if (!c.hasAttribute("EntityType")) {
                    c.addAttribute("EntityType", c.getLabel());
                }
                if (cHead == null){
                    continue;
                }
                if (c.getAttribute("EntityType").equals("VEH") || c.getAttribute("EntityType").equals("WEA")){
                    //continue;
                }
                c.addAttribute("EntityType", "MENTION");

                /**
                 * @Note that unlike BIOReader, the tagging schema is set to "BIOLU" here
                 */
                if (cHead.getStartSpan()+1 == cHead.getEndSpan()) {
                    token2tags[cHead.getStartSpan()] = "U-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                }
                else {
                    token2tags[cHead.getStartSpan()] = "B-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                    for (int i = cHead.getStartSpan() + 1; i < cHead.getEndSpan() - 1; i++) {
                        token2tags[i] = "I-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                    }
                    token2tags[cHead.getEndSpan() - 1] = "L-" + c.getAttribute("EntityType") + "," + c.getAttribute("EntityMentionType");
                }
            }
            for (int i = 0; i < token2tags.length; i++){
                Constituent curToken = tokenView.getConstituentsCoveringToken(i).get(0);
                Constituent newToken = curToken.cloneForNewView("BIO");
                if (token2tags[i].equals("O")) {
                    newToken.addAttribute("BIO", token2tags[i]);
                }
                else{
                    String[] group = token2tags[i].split(",");
                    String tag = group[0];
                    String eml = group[1];
                    newToken.addAttribute("BIO", tag);
                    newToken.addAttribute("EntityMentionType", eml);
                }
                newToken.addAttribute("GAZ", ((FlatGazetteers)gazetteers).annotateConstituent(newToken, false));
                newToken.addAttribute("BC", brownClusters.getPrefixesCombined(newToken.toString()));
                if (!newToken.toString().contains("http")) {
                    newToken.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, newToken));
                    newToken.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, newToken));
                }
                else {
                    newToken.addAttribute("WORDNETTAG", ",");
                    newToken.addAttribute("WORDNETHYM", ",");
                }
                if (_mode.contains("TRAIN")){
                    newToken.addAttribute("isTraining", "true");
                }
                else{
                    newToken.addAttribute("isTraining", "false");
                }
                bioView.addConstituent(newToken);
            }
            ta.addView("BIO", bioView);
            for (Constituent c : bioView){
                ret.add(c);
            }
        }
        return ret;
    }

    /**
     *
     * @param corpus A list of corpus that is in the splitting pool.
     * @param prefix The prefix of the split record file for output.
     *
     * @Note To make the Reader works without any modification,
     *        The prefix of ACE corpus should be "ace_"
     *        The prefix of ERE corpus should be "ere_"
     *        The prefix of combined corpus should be ""
     */
    public static void generateNewSplit(List<String> corpus, String prefix){
        List<TextAnnotation> tas = getTAs(corpus);
        long seed = System.nanoTime();
        Collections.shuffle(tas, new Random(seed));
        int size = tas.size();
        for (int i = 0; i < 5; i++){
            int start = i * (size / 5);
            int end = start + size / 5;
            if (i == 4){
                end = size;
            }
            List<TextAnnotation> eval = new ArrayList<>(tas.subList(start, end));
            List<TextAnnotation> train = new ArrayList<>(tas.subList(0, start));
            train.addAll(new ArrayList<>(tas.subList(end, size)));
            System.out.println("Partitioning fold " + i + " train_size: " + train.size() + " eval_size:" + eval.size());
            String train_file_name = "data/split/" + prefix + "train_fold_" + i;
            String eval_file_name = "data/split/" + prefix + "eval_fold_" + i;
            BufferedWriter bw = null;
            FileWriter fw = null;
            try {
                fw = new FileWriter(train_file_name);
                bw = new BufferedWriter(fw);
                for (TextAnnotation ta : train){
                    bw.write(ta.getId() + "\n");
                }
                bw.close();
                fw.close();
                fw = new FileWriter(eval_file_name);
                bw = new BufferedWriter(fw);
                for (TextAnnotation ta : eval){
                    bw.write(ta.getId() + "\n");
                }
                bw.close();
                fw.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("Splitting fold " + i + " done");
        }
    }
    public static List<TextAnnotation> getTAs(List<String> scope){
        List<TextAnnotation> tas = new ArrayList<>();
        if (scope.contains("ACE")) {
            ACEReader aceReader = null;
            try {
                aceReader = new ACEReader("data/all", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (TextAnnotation ta : aceReader) {
                tas.add(ta);
            }
        }
        if (scope.contains("ERE")) {
            EREMentionRelationReader ereMentionRelationReader = null;
            try {
                ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);

            } catch (Exception e) {
                e.printStackTrace();
            }
            for (XmlTextAnnotation xta : ereMentionRelationReader) {
                tas.add(xta.getTextAnnotation());
            }
        }
        return tas;
    }
    public Object next(){
        if (_taOnly){
            if (ta_idx == currentTas.size()){
                return null;
            }
            else {
                ta_idx ++;
                return currentTas.get(ta_idx - 1);
            }
        }
        else {
            if (cons_idx == constituents.size()) {
                return null;
            } else {
                cons_idx++;
                return constituents.get(cons_idx - 1);
            }
        }
    }
    public void reset(){
        if (_taOnly){
            ta_idx = 0;
        }
        else {
            cons_idx = 0;
        }
    }
    public void close(){

    }

}
