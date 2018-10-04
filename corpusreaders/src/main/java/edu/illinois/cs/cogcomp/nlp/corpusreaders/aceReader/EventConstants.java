/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader;

public class EventConstants {

    public static final String CHAR_START = "charStart";
    public static final String CHAR_END = "charEnd";

    public static final String NER_ACE_COARSE = "NER_ACE_COARSE";
    public static final String NER_ACE_FINE = "NER_ACE_FINE";
    public static final String TIME_ENTITY_TYPE = "TIME";
    public static final String NER_ACE_TIME = "NER_ACE_TIME";
    public static final String NER_ACE_QUANTITY = "NER_ACE_QUANTITY";

    public static boolean isServer = true;

    public static final String LEMMA_VIEW = "LEMMA_VIEW";
    public static final String TOKEN_WITH_CHAR_OFFSET = "TOKEN_WITH_CHAR_OFFSET";
    public static final String TRIGGER_CANDIDATE = "TRIGGER_CANDIDATE";

    public static final String IS_TRIGGER = "IS_TRIGGER";
    public static final String NOT_TRIGGER = "None";

    public static final String IS_Coref = "COREF";
    public static final String NOT_Coref = "NOTCOREF";

    public static final String MUC = "MUC";
    public static final String BCUB = "BCUB";
    public static final String CEAF = "CEAF";
    public static final String All_AVG = "All_AVG";

    public static final String TriggerESA = "TriggerESA";
    public static final String SentenceESA = "SentenceESA";

    // 2: TriggerType_8_trigger 3: TriggerType_8_all 4: TriggerType_33_trigger 5: TriggerType_33_all
    // 6: Seeds
    public static final String[] trainingTypes = {"None", // 0
            "IsTrigger_all_binary", // 1
            "TriggerType_8_types", // 2
            "TriggerType_8_types_including_None", // 3
            "TriggerType_33_types", // 4
            "TriggerType_33_types_including_None", // 5
            "TriggerType_33_OferIdoHeng_Seeds_including_None", // 6
    };

    public static final String[] triggerLabelInitializationType = {"None", // 0
            "8_types_names", // 1
            "33_subtypes_names", // 2
            "33_types_OferIdoHeng_seeds", // 3
    };
}
