/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ctsai12 on 10/25/16.
 */
public enum Language {
    English("en"),
    Spanish("es"),
    Chinese("zh"),
    German("de"),
    Dutch("nl"),
    Turkish("tr"),
    Tagalog("tl"),
    Yoruba("yo"),
    Bengali("bn"),
    Tamil("ta"),
    Swedish("sv"),
    Cebuano("ceb"),
    French("fr"),
    Russian("ru"),
    Italian("it"),
    Waray("war"),
    Polish("pl"),
    Vietnamese("vi"),
    Japanese("ja"),
    Portuguese("pt"),
    Ukrainian("uk"),
    Catalan("ca"),
    Persian("fa"),
    Norwegian("no"),
    Arabic("ar"),
    SerboCroatian("sh"),
    Finnish("fi"),
    Hungarian("hu"),
    Indonesian("id"),
    Romanian("ro"),
    Czech("cs"),
    Korean("ko"),
    Serbian("sr"),
    Malay("ms"),
    Basque("eu"),
    Esperanto("eo"),
    Bulgarian("bg"),
    Minangkabau("min"),
    Danish("da"),
    Kazakh("kk"),
    Slovak("sk"),
    Armenian("hy"),
    Hebrew("he"),
    Lithuanian("lt"),
    Croatian("hr"),
    Chechen("ce"),
    Slovenian("sl"),
    Estonian("et"),
    Galician("gl"),
    Uzbek("uz"),
    Latin("la"),
    Greek("el"),
    Belarusian("be"),
    Volapuk("vo"),
    Hindi("hi"),
    Thai("th"),
    Azerbaijani("az"),
    Georgian("ka"),
    Urdu("ur"),
    Macedonian("mk"),
    Occitan("oc"),
    Malagasy("mg"),
    Welsh("cy"),
    Latvian("lv"),
    Newar("new"),
    Bosnian("bs"),
    Tatar("tt"),
    Tajik("tg"),
    Telugu("te")
    ;

    private static Logger logger = LoggerFactory.getLogger(Language.class);

    private String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode(){
        return code;
    }

    public static Language getLanguageByCode(String code){

        for(Language lang: Language.values()){
            if(lang.getCode().equals(code))
                return lang;
        }

        logger.error("Unknown language code: "+code);
        return null;
    }
}
