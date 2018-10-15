/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IOHelper {
    /**
     * A method that outputs ACEMentionReaders to files
     * In order to save time on experiments
     * @param input A initialized ACEMentionReader
     * @param outputFile The desired output file path
     */
    public static void serializeDataOut(ACEMentionReader input, String outputFile){
        String fileName= outputFile;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(input);
            oos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Reads ACEMentionReader class from file
     * @param inputFile The input file path
     * @return An ACEMentionReader class generated from the file
     */
    public static ACEMentionReader serializeDataIn(String inputFile){
        String fileName= inputFile;
        ACEMentionReader ret = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            ret = (ACEMentionReader) ois.readObject();
            ois.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * This method produces pre-process ACEMentionReaders and save to files
     */
    public static void produceReaders(){
        for (int i = 0; i < 5; i++){
            ACEMentionReader curTrain = new ACEMentionReader("data/partition_with_dev/train/" + i, "relations_mono");
            serializeDataOut(curTrain, "relation-extraction/preprocess/reader/train_fold_" + i);
            ACEMentionReader curTest = new ACEMentionReader("data/partition_with_dev/eval/" + i, "relations_mono");
            serializeDataOut(curTest, "relation-extraction/preprocess/reader/test_fold_" + i);
        }
        ACEMentionReader full = new ACEMentionReader("data/all", "relations_mono");
        serializeDataOut(full, "relation-extraction/preprocess/reader/all");
    }

    /**
     * This method is designed to help read ACEMentionReaders from files
     * @param fold The current fold index (0-4)
     * @param mode "TRAIN" or "TEST"
     * @return An ACEMentionReader class
     */
    public static ACEMentionReader readFiveFold(int fold, String mode){
        if (mode.equals("TRAIN")) {
            return serializeDataIn("relation-extraction/preprocess/reader/train_fold_" + fold);
        }
        else {
            return serializeDataIn("relation-extraction/preprocess/reader/test_fold_" + fold);
        }
    }

    /**
     * Helper function for print essential information of a relation
     * @param r Input Relation
     */
    public static void printRelation (Relation r){
        TextAnnotation ta = r.getSource().getTextAnnotation();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent source_head = RelationFeatureExtractor.getEntityHeadForConstituent(source, ta, "");
        Constituent target_head = RelationFeatureExtractor.getEntityHeadForConstituent(target, ta, "");
        System.out.println(ta.getSentenceFromToken(source.getStartSpan()));
        System.out.println(r.getRelationName());
        System.out.println(r.getAttribute("RelationType") + ":" + r.getAttribute("RelationSubtype"));
        System.out.println(source.toString() + " || " + target.toString());
        System.out.println(source_head.toString() + " || " + target_head.toString());
    }

    public static void main (String[] args){
        produceReaders();
    }
}
