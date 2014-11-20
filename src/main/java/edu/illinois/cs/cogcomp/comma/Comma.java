package edu.illinois.cs.cogcomp.comma;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

/**
 * A data structure containing all the information related to a comma.
 */
public class Comma {
    private String[] sentence;
    private String role;
    public int commaPosition;
    TextAnnotation ta;

    public Comma(int commaPosition, String role, String sentence, TextAnnotation ta) {
        this.commaPosition = commaPosition;
        if (role.equals("Entity attribute")) this.role = "Attribute";
        else if (role.equals("Entity substitute")) this.role = "Substitute";
        else this.role = role;
        this.sentence = sentence.split("\\s+");
        this.ta = ta;
    }

    public String getRole() {
        return role;
    }

    public String getWordToRight(int distance) {
        // Dummy symbol for sentence end (in case comma is the second to last word in the sentence)
        if (commaPosition + distance >= sentence.length)
            return "###";
        return sentence[commaPosition + distance];
    }

    public String getWordToLeft(int distance) {
        // Dummy symbol for sentence start (in case comma is the second word in the sentence)
        if (commaPosition - distance < 0)
            return "$$$";
        return sentence[commaPosition - distance];
    }
    
    public String getPOSToLeft(int distance){
    	TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);
    	return posView.getLabel(commaPosition - distance);
    }
    
    public String getPOSToRight(int distance){
    	TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);
    	return posView.getLabel(commaPosition + distance);
    }
}
