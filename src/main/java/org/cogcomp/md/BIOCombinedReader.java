package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ColumnFormatReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.lang.*;

/**
 * Created by xuany on 7/30/2017.
 */
public class BIOCombinedReader implements Parser {
    List<Constituent> constituents;
    int cons_idx;
    public BIOCombinedReader(int fold, String mode){
        constituents = readTasByFold(fold, mode);
        cons_idx = 0;
    }
    public List<Constituent> readTasByFold(int fold, String mode){
        List<TextAnnotation> tas = getTAs();
        HashMap<String, TextAnnotation> taMap = new HashMap<>();
        for (TextAnnotation ta : tas){
            taMap.put(ta.getId(), ta);
        }
        List<Constituent> ret = new ArrayList<>();
        String file_name = "";
        if (mode.equals("TRAIN")) {
            file_name = "data/split/train_fold_" + fold;
        }
        else if (mode.equals("EVAL")){
            file_name = "data/split/eval_fold_" + fold;
        }
        else {
            return ret;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String line;
            while ((line = br.readLine()) != null) {
                TextAnnotation ta = taMap.get(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }
    public static void generateNewSplit(){
        List<TextAnnotation> tas = getTAs();
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
            String train_file_name = "data/split/train_fold_" + i;
            String eval_file_name = "data/split/eval_fold_" + i;
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
    public static List<TextAnnotation> getTAs(){
        List<TextAnnotation> tas = new ArrayList<>();
        ACEReader aceReader = null;
        try{
            aceReader = new ACEReader("data/all", false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        for (TextAnnotation ta : aceReader) {
            tas.add(ta);
        }
        EREMentionRelationReader ereMentionRelationReader = null;
        try {
            ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);

        }
        catch (Exception e){
            e.printStackTrace();
        }
        for (XmlTextAnnotation xta : ereMentionRelationReader){
            tas.add(xta.getTextAnnotation());
        }
        return tas;
    }
    public Object next(){
        return null;
    }
    public void reset(){

    }
    public void close(){

    }

}
