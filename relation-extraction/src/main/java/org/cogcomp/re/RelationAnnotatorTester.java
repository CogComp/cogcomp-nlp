package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;

/*
 * Class org.cogcomp.re.RelationAnnotatorTester
 * The only purpose of this file is to test the correctness of
 * Class RelationAnnotator
 * Currently, it reads a textannoation file and prints
 * predicted relations and gold relations
 */
public class RelationAnnotatorTester {
    public static void main(String[] args){
        try {
            ACEReader aceReader = new ACEReader("data/original", false);
            RelationAnnotator relationAnnotator = new RelationAnnotator();
            ServerClientAnnotator annotator = new ServerClientAnnotator();
            annotator.setUrl("http://austen.cs.illinois.edu", "3283");
            annotator.setViews("RELATION_EXTRACTION_RELATIONS");
            int count = 0;
            for (TextAnnotation ta : aceReader){
                count++;
                //if (count < 210) continue;
                //relationAnnotator.addView(ta);
                annotator.addView(ta);
                System.out.println(ta.hasView("RELATION_EXTRACTION_RELATIONS"));
                View relationView = ta.getView("RELATION_EXTRACTION_RELATIONS");

                System.out.println("Predicted Relations");
                for (Relation r : relationView.getRelations()){
                    System.out.println("[Source:] " + r.getSource().toString() + " [Target:] " + r.getTarget().toString() + " [Tag:] " + r.getRelationName());
                }
                System.out.println("Gold Relations");
                for (Relation r : ta.getView(ViewNames.MENTION_ACE).getRelations()){
                    System.out.println("[Source:] " + r.getSource().toString() + " [Target:] " + r.getTarget().toString() + " [Tag]: " + r.getAttribute("RelationSubtype"));
                }

                if (relationView != null){
                    System.out.println(ta.getId());
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
