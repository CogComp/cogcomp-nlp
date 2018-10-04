/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import org.cogcomp.md.MentionAnnotator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/*
 * The feature extractor class for RE
 * Each extraction function returns a List of string features
 * Includes some helper functions
 */

public class RelationFeatureExtractor {

    /**
     * Return the head Constituent
     * @param extentConstituent The extent Constituent
     * @param textAnnotation The TextAnnotation that contains extentConstituent
     * @param viewName The desired view name for the output Constituent
     * @return A Constituent that represents the head
     *
     * Note: ACEReader represents head in a different way as MentionAnnotator does
     *       This method checks which way the input uses and adapt automatically.
     */
    public static Constituent getEntityHeadForConstituent(Constituent extentConstituent,
                                                           TextAnnotation textAnnotation,
                                                           String viewName) {
        if (extentConstituent.getAttribute("IsPredicted") != null){
            return extentConstituent;
        }
        if (extentConstituent.getAttribute("EntityHeadStartSpan") != null){
            return MentionAnnotator.getHeadConstituent(extentConstituent, viewName);
        }
        if (!extentConstituent.hasAttribute(ACEReader.EntityHeadStartCharOffset)){
            return extentConstituent;
        }
        int startCharOffset =
                Integer.parseInt(extentConstituent
                        .getAttribute(ACEReader.EntityHeadStartCharOffset));
        int endCharOffset =
                Integer.parseInt(extentConstituent.getAttribute(ACEReader.EntityHeadEndCharOffset)) - 1;
        int startToken = textAnnotation.getTokenIdFromCharacterOffset(startCharOffset);
        int endToken = textAnnotation.getTokenIdFromCharacterOffset(endCharOffset);

        if (startToken >= 0 && endToken >= 0 && !(endToken - startToken < 0)) {
            Constituent cons =
                    new Constituent(extentConstituent.getLabel(), 1.0, viewName, textAnnotation,
                            startToken, endToken + 1);

            for (String attributeKey : extentConstituent.getAttributeKeys()) {
                cons.addAttribute(attributeKey, extentConstituent.getAttribute(attributeKey));
            }
            return cons;
        }

        return null;
    }

    /**
     * Checks if the input relation forms possessive structure
     * @param r The input relation
     * @return A boolean
     */
    public static boolean isPossessive(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        View posView = ta.getView(ViewNames.POS);
        if ((source_head.getStartSpan() >= target.getStartSpan() && source_head.getEndSpan() <= target.getEndSpan())
            || (target_head.getStartSpan() >= source.getStartSpan() && target_head.getEndSpan() <= source.getEndSpan())){
            if (source_head.getStartSpan() > target_head.getStartSpan()){
                front = target;
                front_head = target_head;
                back = source;
                back_head = source_head;
            }
            else{
                front = source;
                front_head = source_head;
                back = target;
                back_head = target_head;
            }
        }
        if (front == null){
            return false;
        }
        for (int i = front_head.getEndSpan(); i < back_head.getStartSpan(); i++){
            if (ta.getToken(i).equals("'s")){
                return true;
            }
            if (i < back_head.getStartSpan() - 1 && ta.getToken(i).equals("'") && ta.getToken(i+1).equals("s")){
                return true;
            }
        }
        if (posView.getLabelsCoveringToken(front_head.getEndSpan()).get(0).equals("POS")){
            return true;
        }
        if (posView.getLabelsCoveringToken(front_head.getEndSpan() - 1).get(0).equals("PRP$")
                || posView.getLabelsCoveringToken(front_head.getEndSpan() - 1).get(0).equals("WP$")){
            return true;
        }
        return false;
    }

    /**
     * A helper function that checks if the POS tag is noun
     * @param posTag POS Tag
     * @return A boolean
     */
    public static boolean isNoun(String posTag){
        if (posTag.startsWith("NN") || posTag.startsWith("RB") || posTag.startsWith("WP")){
            return true;
        }
        return false;
    }

    /**
     * Checks if the input relation forms preposition structure
     * @param r The input relation
     * @return A boolean
     */
    public static boolean isPreposition(Relation r){
        if (RelationFeatureExtractor.isPossessive(r)){
            //return false;
        }
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        int SentenceStart = ta.getSentence(ta.getSentenceId(source)).getStartSpan();
        View posView = ta.getView(ViewNames.POS);
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front = target;
            front_head = target_head;
            back = source;
            back_head = source_head;
        }
        else{
            front = source;
            front_head = source_head;
            back = target;
            back_head = target_head;
        }
        boolean found_in_to = false;
        boolean noNp = true;
        for (int i = front_head.getEndSpan(); i < back.getStartSpan(); i++){
            if (isNoun(posView.getLabelsCoveringToken(i).get(0))){
                noNp = false;
            }
            if (posView.getLabelsCoveringToken(i).get(0).equals("IN") || posView.getLabelsCoveringToken(i).get(0).equals("TO")){
                found_in_to = true;
            }
        }
        if (found_in_to && noNp){
            return true;
        }
        boolean found_in = false;
        found_in_to = false;
        noNp = true;
        boolean non_overlap = false;
        for (int i = front.getEndSpan(); i < back.getStartSpan(); i++){
            non_overlap = true;
            if (isNoun(posView.getLabelsCoveringToken(i).get(0))){
                noNp = false;
            }
            if (posView.getLabelsCoveringToken(i).get(0).equals("IN") || posView.getLabelsCoveringToken(i).get(0).equals("TO")){
                found_in_to = true;
            }
        }
        if (found_in_to && noNp){
            return true;
        }
        found_in = false;
        for (int i = front.getStartSpan() - 1; i >= SentenceStart; i--){
            if (isNoun(posView.getLabelsCoveringToken(i).get(0))){
                noNp = false;
            }
            if (posView.getLabelsCoveringToken(i).get(0).equals("IN")){
                found_in = true;
            }
        }
        if (found_in && noNp && non_overlap) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the input relation forms formulaic structure
     * @param r The input relation
     * @return A boolean
     */
    public static boolean isFormulaic(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        View posView = ta.getView(ViewNames.POS);
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front = target;
            front_head = target_head;
            back = source;
            back_head = source_head;
        }
        else{
            front = source;
            front_head = source_head;
            back = target;
            back_head = target_head;
        }
        for (int i = front_head.getEndSpan(); i < back_head.getStartSpan() - 1; i++){
            if (!posView.getLabelsCoveringToken(i).get(0).startsWith("NN")
                    && !posView.getLabelsCoveringToken(i).get(0).equals(",")){
                return false;
            }
        }
        if ((front.getAttribute("EntityType").equals("PER") || front.getAttribute("EntityType").equals("ORG") || front.getAttribute("EntityType").equals("GPE")) &&
                (back.getAttribute("EntityType").equals("ORG") || back.getAttribute("EntityType").equals("GPE"))){
            return true;
        }
        return false;
    }

    /**
     * A helper that checks if the two inputs have only noun phrases between
     * @param front The Constituent at the front
     * @param back The second Consituent
     * @return A boolean
     */
    public static boolean onlyNounBetween(Constituent front, Constituent back){
        TextAnnotation ta = front.getTextAnnotation();
        View posView = ta.getView(ViewNames.POS);
        for (int i = front.getEndSpan(); i < back.getStartSpan(); i++){
            if (!posView.getLabelsCoveringToken(i).get(0).startsWith("NN")){
                    //&& !posView.getLabelsCoveringToken(i).get(0).startsWith("JJ")){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the input relation forms pre-modifier structure
     * @param r The input relation
     * @return A boolean
     */
    public static boolean isPremodifier(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        View posView = ta.getView(ViewNames.POS);
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front = target;
            front_head = target_head;
            back = source;
            back_head = source_head;
        }
        else{
            front = source;
            front_head = source_head;
            back = target;
            back_head = target_head;
        }
        if (front == null){
            return false;
        }
        if (front.getStartSpan() >= back.getStartSpan()) {
            if (front_head.getEndSpan() == back_head.getStartSpan() ||
                    (front_head.getEndSpan() == back_head.getStartSpan() - 1 && ta.getToken(front_head.getEndSpan()).contains(".")) ||
                    onlyNounBetween(front_head, back_head)) {
                if (front_head.getStartSpan() == back.getStartSpan()) {
                    if (posView.getLabelsCoveringToken(front_head.getStartSpan()).equals("PRP$")) {
                        return false;
                    }
                    return true;
                }
                for (int i = back.getStartSpan(); i < front_head.getStartSpan(); i++) {
                    if (!posView.getLabelsCoveringToken(i).get(0).startsWith("JJ") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("RB") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("VB") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("CD") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("DT") &&
                            !posView.getLabelsCoveringToken(i).get(0).startsWith("PD")) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Lexical Feature Extractor Part A
     * @Extract source mention BOW
     * @return A List of String features
     */
    public List<String> getLexicalFeaturePartA(Relation r){

        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        TextAnnotation ta = source.getTextAnnotation();
        for (int i = source.getStartSpan(); i < source.getEndSpan(); i++){
            ret_features.add(ta.getToken(i));
        }
        return ret_features;
    }

    /**
     * Lexical Feature Extractor Part B
     * @Extract target mention BOW
     * @return A List of String features
     */
    public List<String> getLexicalFeaturePartB(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent target = r.getTarget();
        TextAnnotation ta = target.getTextAnnotation();
        for (int i = target.getStartSpan(); i < target.getEndSpan(); i++){
            ret_features.add(ta.getToken(i));
        }
        return ret_features;
    }

    /**
     * Lexical Feature Extractor Part C
     * @Extract the only word (if exists) between two argument extents
     * @return A List of String features
     */
    public List<String> getLexicalFeaturePartC(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        if (source.getEndSpan() == target.getStartSpan() - 1){
            ret_features.add("singleword_" + source.getTextAnnotation().getToken(source.getEndSpan()));
        }
        else if (target.getEndSpan() == source.getStartSpan() - 1){
            ret_features.add("singleword_" + target.getTextAnnotation().getToken(target.getEndSpan()));
        }
        else {
            ret_features.add("No_singleword");
        }
        return ret_features;
    }

    /**
     * Lexical Feature Extractor Part C-C
     * @Extract BOW between mention heads
     * @return A List of String features
     */
    public List<String> getLexicalFeaturePartCC(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target = r.getTarget();
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        if (source_head.getEndSpan() < target_head.getStartSpan()){
            for (int i = source_head.getEndSpan(); i < target_head.getStartSpan(); i++) {
                ret_features.add("bowbethead_" + source.getTextAnnotation().getToken(i));
            }
        }
        if (target_head.getEndSpan() < source_head.getStartSpan()){
            for (int i = target_head.getEndSpan(); i < source_head.getStartSpan(); i++) {
                ret_features.add("bowbethead_" + source.getTextAnnotation().getToken(i));
            }
        }
        return ret_features;
    }

    /**
     * Lexical Feature Extractor Part D
     * @Extract First word between mentions
     *           BOW between mentions
     * @return A List of String features
     */
    public List<String> getLexicalFeaturePartD(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        if (target.getStartSpan() - source.getEndSpan() > 1) {
            ret_features.add("between_first_" + ta.getToken(source.getEndSpan()));
            ret_features.add("between_first_" + ta.getToken(target.getStartSpan() - 1));
            if (target.getStartSpan() - source.getEndSpan() > 2) {
                for (int i = source.getEndSpan() + 1; i < target.getStartSpan() - 1; i++) {
                    ret_features.add("in_between_" + ta.getToken(i));
                }
            }
        }
        else if (source.getStartSpan() - target.getEndSpan() > 1){
            ret_features.add("between_first_" + ta.getToken(target.getEndSpan()));
            ret_features.add("between_first_" + ta.getToken(source.getStartSpan() - 1));
            if (source.getStartSpan() - target.getEndSpan() > 2){
                for (int i = target.getEndSpan() + 1; i < source.getStartSpan() - 1; i++){
                    ret_features.add("in_between_" + ta.getToken(i));
                }
            }
        }
        else {
            ret_features.add("No_between_features");
        }
        return ret_features;
    }

    /**
     * Lexical Feature Extractor Part E
     * @Extract Structural words
     * @return A List of String features
     */
    public List<String> getLexicalFeaturePartE(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        int sentenceStart = ta.getSentence(source.getSentenceId()).getStartSpan();
        int sentenceEnd = ta.getSentence(source.getSentenceId()).getEndSpan();
        if (source.getStartSpan() - sentenceStart > 0){
            ret_features.add("fwM1_" + ta.getToken(source.getStartSpan() - 1));
            if (source.getStartSpan() - sentenceEnd > 1){
                ret_features.add("swM1_" + ta.getToken(source.getStartSpan() - 2));
            }
            else{
                ret_features.add("swM1_NULL");
            }
        }
        else{
            ret_features.add("fwM1_NULL");
            ret_features.add("swM1_NULL");
        }
        if (sentenceEnd - target.getEndSpan() > 0){
            ret_features.add("fwM2_" + ta.getToken(target.getEndSpan()));
            if (sentenceEnd - target.getEndSpan() > 1){
                ret_features.add("swM2_" + ta.getToken(target.getEndSpan() + 1));
            }
            else {
                ret_features.add("swM2_NULL");
            }
        }
        else {
            ret_features.add("fwM2_NULL");
            ret_features.add("swM2_NULL");
        }
        return ret_features;
    }

    /**
     * Lexical Feature Extractor Part F
     * @Extracts Head forms
     * @return A List of String features
     */
    public List<String> getLexicalFeaturePartF(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();
        ret_features.add("HM1_" + sourceHeadWord);
        ret_features.add("HM2_" + targetHeadWord);
        ret_features.add("HM12_" + sourceHeadWord + "_" + targetHeadWord);
        return ret_features;
    }

    /**
     * @Extract Collocations features in
     *          http://cogcomp.org/papers/ChanRo10.pdf Table 1
     * @return A list of String features
     */
    public List<String> getCollocationsFeature(Relation r){
        List<String> ret_features = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent sourceHead = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent targetHead = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        String sourceHeadWord = sourceHead.toString();
        String targetHeadWord = targetHead.toString();

        //Source Features
        String source_c_m1_p1 = "s_m1_p1_";
        for (int i = sourceHead.getStartSpan() - 1; i < sourceHead.getEndSpan() + 1; i++) {
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_m1_p1 = source_c_m1_p1 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_m1_p1);

        String source_c_m2_m1 = "s_m2_m1_";
        for (int i = sourceHead.getStartSpan() - 2; i < sourceHead.getStartSpan(); i++){
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_m2_m1 = source_c_m2_m1 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_m2_m1);

        String source_c_p1_p2 = "s_p1_p2_";
        for (int i = sourceHead.getEndSpan(); i < sourceHead.getEndSpan() + 2; i++){
            if (i >= source.getStartSpan() && i < source.getEndSpan()) {
                source_c_p1_p2 = source_c_p1_p2 + source.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(source_c_p1_p2);

        if (sourceHead.getStartSpan() > source.getStartSpan()) {
            ret_features.add("s_m1_m1_" + source.getTextAnnotation().getToken(sourceHead.getStartSpan() - 1));
        }
        else{
            ret_features.add("s_m1_m1_null");
        }
        if (sourceHead.getEndSpan() < source.getEndSpan()) {
            ret_features.add("s_p1_p1_" + source.getTextAnnotation().getToken(sourceHead.getEndSpan()));
        }
        else {
            ret_features.add("s_p1_p1_null");
        }

        //Target Features
        String target_c_m1_p1 = "t_m1_p1_";
        for (int i = targetHead.getStartSpan() - 1; i < targetHead.getEndSpan() + 1; i++) {
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_m1_p1 = target_c_m1_p1 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_m1_p1);

        String target_c_m2_m1 = "t_m2_m1_";
        for (int i = targetHead.getStartSpan() - 2; i < targetHead.getStartSpan(); i++){
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_m2_m1 = target_c_m2_m1 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_m2_m1);

        String target_c_p1_p2 = "t_p1_p2_";
        for (int i = targetHead.getEndSpan(); i < targetHead.getEndSpan() + 2; i++){
            if (i >= target.getStartSpan() && i < target.getEndSpan()) {
                target_c_p1_p2 = target_c_p1_p2 + target.getTextAnnotation().getToken(i);
            }
        }
        ret_features.add(target_c_p1_p2);

        if (targetHead.getStartSpan() > target.getStartSpan()) {
            ret_features.add("t_m1_m1_" + target.getTextAnnotation().getToken(targetHead.getStartSpan() - 1));
        }
        else {
            ret_features.add("t_m1_m1_null");
        }
        if (targetHead.getEndSpan() < target.getEndSpan()) {
            ret_features.add("t_p1_p1_" + target.getTextAnnotation().getToken(targetHead.getEndSpan()));
        }
        else{
            ret_features.add("t_p1_p1_null");
        }

        return ret_features;
    }

    /**
     * @Extract Structural features in
     *          http://cogcomp.org/papers/ChanRo10.pdf Table 1
     * @return A list of String features
     */
    public List<String> getStructualFeature(Relation r){
        List<String> ret = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        String mentionViewName = ViewNames.MENTION_ACE;
        if (!r.getSource().getTextAnnotation().hasView(mentionViewName)){
            mentionViewName = ViewNames.MENTION_ERE;
        }
        if (!r.getSource().getTextAnnotation().hasView(mentionViewName)){
            mentionViewName = ViewNames.MENTION;
        }
        View mentionView = source.getTextAnnotation().getView(mentionViewName);
        if (target.getStartSpan() > source.getEndSpan()){
            List<Constituent> middle = mentionView.getConstituentsCoveringSpan(source.getEndSpan(), target.getStartSpan() - 1);
            ret.add("middle_mention_size_" + Integer.toString(middle.size()));
            ret.add("middle_word_size_" + Integer.toString(target.getStartSpan() - source.getEndSpan()));
        }
        else if (source.getStartSpan() > target.getEndSpan()){
            List<Constituent> middle = mentionView.getConstituentsCoveringSpan(target.getEndSpan(), source.getStartSpan() - 1);
            ret.add("middle_mention_size_" + Integer.toString(middle.size()));
            ret.add("middle_word_size_" + Integer.toString(source.getStartSpan() - target.getEndSpan()));
        }
        else{
            ret.add("middle_mention_size_null");
            ret.add("middle_word_size_null");
        }
        if (source.doesConstituentCover(target)){
            ret.add("m2_in_m1");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m2_in_m1");
        }
        else if (target.doesConstituentCover(source)){
            ret.add("m1_in_m2");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m1_in_m2");
        }
        else{
            ret.add("m1_m2_no_coverage");
            ret.add("cb1_" + source.getAttribute("EnityType") + "_" + target.getAttribute("EntityType")+ "_m1_m2_no_coverage");
        }
        return ret;
    }

    /**
     * @Extract Mention features in
     *          http://cogcomp.org/papers/ChanRo10.pdf Table 1
     * @return A list of String features
     */
    public List<String> getMentionFeature(Relation r){
        List<String> ret = new ArrayList<String>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        String source_m_lvl = source.getAttribute("EntityMentionType");
        String target_m_lvl = target.getAttribute("EntityMentionType");
        String source_main_type = source.getAttribute("EntityType");
        String target_main_type = target.getAttribute("EntityType");

        ret.add("source_mtype_" + source_main_type);
        ret.add("target_mtype_" + target_main_type);

        ret.add("mlvl_" + source_m_lvl + "_" + target_m_lvl);
        ret.add("mt_" + source_main_type + "_" + target_main_type);
        ret.add("mlvl_mt_" + source_m_lvl + "_" + source_main_type + "_" + target_m_lvl + "_" + target_main_type);

        if (target.doesConstituentCover(source)){
            ret.add("mlvl_cont_1_" + source_m_lvl + "_" + target_m_lvl + "_" + "True");
        }
        if (source.doesConstituentCover(target)){
            ret.add("mlvl_cont_2_" + source_m_lvl + "_" + target_m_lvl + "_" + "True");
        }
        return ret;
    }

    /**
     * @Extract Features utilizing dependency path
     * @return A list of Pair features (name, value)
     */
    public static List<Pair<String, String>> getDependencyFeature(Relation r){
        List<Pair<String, String>> ret = new ArrayList<>();
        TreeView parse = (TreeView) r.getSource().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        Constituent source_head = getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent target_head = getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        View annotatedView = source.getTextAnnotation().getView("RE_ANNOTATED");
        View posView = source.getTextAnnotation().getView(ViewNames.POS);
        List<Constituent> source_parsed_list = parse.getConstituentsCoveringToken(source_head.getStartSpan());
        List<Constituent> target_parsed_list = parse.getConstituentsCoveringToken(target_head.getStartSpan());
        if (source.getSentenceId() == target.getSentenceId()){
            try {
                if (source_parsed_list.size() != 0 && target_parsed_list.size() != 0) {
                    Constituent source_parsed = parse.getConstituentsCoveringToken(source_head.getStartSpan()).get(0);
                    Constituent target_parsed = parse.getConstituentsCoveringToken(target_head.getStartSpan()).get(0);
                    List<Constituent> paths = PathFeatureHelper.getPathConstituents(source_parsed, target_parsed, 100);
                    for (int i = 0; i < paths.size(); i++){
                        Constituent cur = paths.get(i);
                        //ret.add(new Pair(Integer.toString(i), cur.toString()));
                        ret.add(new Pair("tag_" + i, cur.getLabel()));
                        ret.add(new Pair("pos_tag_" + i, posView.getConstituentsCoveringToken(cur.getStartSpan()).get(0).getLabel()));
                        ret.add(new Pair("wordnettag_" + i, annotatedView.getConstituentsCoveringToken(cur.getStartSpan()).get(0).getAttribute("WORDNETTAG")));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * @Extract Features utilizing shallow parser result
     * @return A list of Pair features (name, value)
     */
    public List<Pair<String, String>> getShallowParseFeature(Relation r) {
        List<Pair<String, String>> ret = new ArrayList<>();
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        if (source_head.getStartSpan() == target_head.getStartSpan()){
            return ret;
        }
        if (source.getStartSpan() == target.getStartSpan()){
            return ret;
        }
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front_head = target_head;
            back_head = source_head;
        }
        else{
            front_head = source_head;
            back_head = target_head;
        }
        if (source.getStartSpan() > target.getStartSpan()){
            front = target;
            back = source;
        }
        else {
            front = source;
            back = target;
        }
        View spView = ta.getView(ViewNames.SHALLOW_PARSE);

        List<String> betweenHeads = spView.getLabelsCoveringSpan(front_head.getEndSpan(), back_head.getStartSpan() - 1);
        for (int i = 0; i < betweenHeads.size(); i++){
            ret.add(new Pair("chunker_between_heads_" + i, betweenHeads.get(i)));
        }
        if (back.getStartSpan() > front.getEndSpan()){
            List<String> betweenExtents = spView.getLabelsCoveringSpan(front.getEndSpan(), back.getStartSpan() - 1);
            for (int i = 0; i < betweenExtents.size(); i++) {
                ret.add(new Pair("chunker_between_extents_" + i, betweenExtents.get(i)));
            }
        }
        else{
            List<String> betweenExtents = spView.getLabelsCoveringSpan(front.getStartSpan(), back.getStartSpan());
            for (int i = 0; i < betweenExtents.size(); i++) {
                ret.add(new Pair("chunker_between_extents_inclusive_" + i, betweenExtents.get(i)));
            }
        }
        return ret;
    }

    /**
     * @Extract A template tag if the input fits any
     * @return A list of String features
     */
    public List<String> getTemplateFeature(Relation r){
        List<String> ret_features = new ArrayList<String>();
        if (isFormulaic(r)){
            ret_features.add("is_formulaic_structure");
        }
        if (isPreposition(r)){
            ret_features.add("is_preposition_structure");
        }
        if (isPossessive(r)){
            ret_features.add("is_possessive_structure");
        }
        if (isPremodifier(r)){
            ret_features.add("is_premodifier_structure");
        }
        return  ret_features;
    }

    /**
     * Checks if the two mentions are co-referencing each other
     * @return A boolean
     */
    public String getCorefTag(Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        if (source.getAttribute("EntityID") != null && target.getAttribute("EntityID") != null) {
            if (source.getAttribute("EntityID").equals(target.getAttribute("EntityID"))) {
                return "TRUE";
            }
        }
        return "FALSE";
    }

    /**
     * A special feature set that checks if the relation fits any pre-defined patterns
     * @return A list of String feature
     */
    public static List<String> patternRecognition(Constituent source, Constituent target){
        Set<String> ret = new HashSet<>();
        TextAnnotation ta = source.getTextAnnotation();
        Constituent source_head = getEntityHeadForConstituent(source, ta, "TEST");
        Constituent target_head = getEntityHeadForConstituent(target, ta, "TEST");
        if (source_head.getStartSpan() == target_head.getStartSpan()){
            ret.add("SAME_SOURCE_TARGET_EXCEPTION");
            return new ArrayList<>(ret);
        }
        if (source.getStartSpan() == target.getStartSpan()){
            ret.add("SAME_SOURCE_TARGET_EXTENT_EXCEPTION");
            return new ArrayList<>(ret);
        }
        Constituent front = null;
        Constituent back = null;
        Constituent front_head = null;
        Constituent back_head = null;
        if (source_head.getStartSpan() > target_head.getStartSpan()){
            front_head = target_head;
            back_head = source_head;
        }
        else{
            front_head = source_head;
            back_head = target_head;
        }
        if (source.getStartSpan() > target.getStartSpan()){
            front = target;
            back = source;
        }
        else {
            front = source;
            back = target;
        }

        //Check if the two arguments forms formulaic structure
        if (front.getEndSpan() < ta.getView(ViewNames.TOKENS).getEndSpan()) {
            if (ta.getToken(front_head.getEndSpan()).contains(",") && back_head.getStartSpan() - front_head.getEndSpan() < 3
                    || ta.getToken(front_head.getEndSpan()).contains(",") && ta.getToken(back_head.getStartSpan() - 1).contains(",")
                    || ta.getToken(front.getEndSpan()).contains(",") && back.getStartSpan() - front.getEndSpan() < 3 && back.getStartSpan() > front.getEndSpan()
                    || ta.getToken(front.getEndSpan()).contains(",") && ta.getToken(back.getStartSpan() - 1).contains(",") && back.getStartSpan() > front.getEndSpan()
                    || back.getStartSpan() < front.getEndSpan() && ta.getToken(back.getStartSpan() - 1).equals(",")) {
                if (back.getAttribute("EntityType").equals("LOC")
                        || back.getAttribute("EntityType").equals("ORG")
                        || back.getAttribute("EntityType").equals("GPE")) {
                    ret.add("FORMULAIC");
                }
            }
        }
        TreeView parse = (TreeView) source.getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);

        List<Constituent> source_parsed_list = parse.getConstituentsCoveringToken(source_head.getStartSpan());
        List<Constituent> target_parsed_list = parse.getConstituentsCoveringToken(target_head.getStartSpan());
        if (source.getSentenceId() == target.getSentenceId()){
            try {
                if (source_parsed_list.size() != 0 && target_parsed_list.size() != 0) {
                    Constituent source_parsed = parse.getConstituentsCoveringToken(source_head.getStartSpan()).get(0);
                    Constituent target_parsed = parse.getConstituentsCoveringToken(target_head.getStartSpan()).get(0);
                    List<Constituent> paths = PathFeatureHelper.getPathConstituents(source_parsed, target_parsed, 100);
                    if (paths.get(paths.size() - 2).getLabel().equals("prep") && paths.get(paths.size() - 1).getLabel().equals("pobj")){
                        ret.add("prep_pobj_dep_structure");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return new ArrayList<>(ret);
    }

}
