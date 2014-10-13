package edu.illinois.cs.cogcomp.comma;

/**
 * A data structure containing all the information related to a comma.
 */
public class Comma {
    private String[] sentence;
    private String role;
    private int commaPosition;

    public Comma(int commaPosition, String role, String sentence) {
        this.commaPosition = commaPosition;
        if (role.equals("Entity attribute")) this.role = "Attribute";
        else if (role.equals("Entity substitute")) this.role = "Substitute";
        else this.role = role;
        this.sentence = sentence.split("\\s+");
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
}
