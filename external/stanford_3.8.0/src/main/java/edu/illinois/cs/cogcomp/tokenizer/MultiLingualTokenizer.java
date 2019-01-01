/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.tokenizer;


import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ctsai12 on 3/24/16.
 */
public class MultiLingualTokenizer {

    private static final String CHINESE_BASE_DIR = "chineseTokenizerResourceDir";
    private static Map<String, TextAnnotationBuilder> tokenizerMap;

    public MultiLingualTokenizer(ResourceManager rm )
    {
        String chineseBasedir = rm.getString( MultiLingualTokenizer.CHINESE_BASE_DIR );
    }

    public static TextAnnotationBuilder getTokenizer(String lang){

        if(tokenizerMap == null)
            tokenizerMap = new HashMap<>();

        if(!tokenizerMap.containsKey(lang)) {

            TextAnnotationBuilder tokenizer = null;

            if (lang.equals("en"))
                tokenizer = new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
            else if (lang.equals("es"))
                tokenizer = new TokenizerTextAnnotationBuilder(new StanfordAnalyzer());
            else if (lang.equals("zh"))
                tokenizer = new TokenizerTextAnnotationBuilder(new CharacterTokenizer());
            else if (lang.equals("th"))
                tokenizer = new TokenizerTextAnnotationBuilder(new ThaiTokenizer());
            else if (lang.equals("ja"))
                tokenizer = new TokenizerTextAnnotationBuilder(new JapaneseTokenizer());
            else
                tokenizer = new TokenizerTextAnnotationBuilder(new WhiteSpaceTokenizer());

            tokenizerMap.put(lang, tokenizer);
        }

        return tokenizerMap.get(lang);
    }

    public static void main(String[] args) {

        TextAnnotationBuilder tokenizer = MultiLingualTokenizer.getTokenizer("ja");
        String text = "\"ペンシルベニアドイツ語\",\"text\":\"ペンシルベニアドイツ語（標準ドイ"
                        + "ツ語：Pennsylvania-Dutch, Pennsilfaani-Deitsch、アレマン語：Pennsylvania-Ditsch、英語：Pennsylvania-German）"
                        + "は、北アメリカのカナダおよびアメリカ中西部でおよそ15万から25万人の人びとに話されているドイツ語の系統である。高地ドイツ語の"
                        + "うち上部ドイツ語の一派アレマン語の一方言である。ペンシルベニアアレマン語(Pennsilfaani-Alemanisch, Pennsylvania-Alemannic)"
                        + "とも呼ばれる。";

        TextAnnotation ta = tokenizer.createTextAnnotation(text);
        for(int i = 0; i < ta.getNumberOfSentences(); i++)
            System.out.println(ta.getSentence(i).getTokenizedText());

    }
}
