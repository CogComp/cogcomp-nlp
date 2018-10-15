/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.transliteration.SPModel;
import edu.illinois.cs.cogcomp.utils.TopList;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class TransliterationAnnotator extends Annotator {
    SPModel model;
    Language lang;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(TransliterationAnnotator.class);

    public TransliterationAnnotator() {
        this(true, Language.Arabic);
    }

    public TransliterationAnnotator(boolean lazilyInitialize) {
        this(lazilyInitialize, Language.Arabic);
    }

    public TransliterationAnnotator(boolean lazilyInitialize, Language lang) {
        super(ViewNames.TRANSLITERATION, new String[0], lazilyInitialize);
        this.lang = lang;
    }

    public static Language[] supportedLanguages = {
            Language.Abkhazian,
            Language.Afrikaans,
            Language.Arabic,
            Language.Aragonese,
            Language.Egyptian_Arabic,
            Language.Asturian,
            Language.Azerbaijani,
            Language.Bashkir,
            Language.Belarusian,
            Language.Bengali,
            Language.Bosnian,
            Language.Breton,
            Language.Bulgarian,
            Language.Valencian,
            Language.Czech,
            Language.Chechen,
            Language.Chuvash,
            Language.Sorani,
            Language.Danish,
            Language.Greek,
            Language.Esperanto,
            Language.Estonian,
            Language.Basque,
            Language.Persian,
            Language.Finnish,
            Language.French,
            Language.Western_Frisian,
            Language.Galician,
            Language.Gujarati,
            Language.Haitian,
            Language.Hebrew,
            Language.Croatian,
            Language.Upper_Sorbian,
            Language.Hungarian,
            Language.Armenian,
            Language.Interlingua,
            Language.Indonesian,
            Language.Icelandic,
            Language.Javanese,
            Language.Georgian,
            Language.Kazakh,
            Language.Kabardian,
            Language.Kirghiz,
            Language.Kurdish,
            Language.Latin,
            Language.Latvian,
            Language.Limburgish,
            Language.Lithuanian,
            Language.Luxembourgish,
            Language.Maithili,
            Language.Marathi,
            Language.Macedonian,
            Language.Malagasy,
            Language.Mongolian,
            Language.Malay,
            Language.Mazandarani,
            Language.Nahuatl,
            Language.Nepali,
            Language.Norwegian_Nynorsk,
            Language.Occitan,
            Language.Oriya,
            Language.Ossetian,
            Language.Punjabi,
            Language.Polish,
            Language.Portuguese,
            Language.Quechua,
            Language.Romansh,
            Language.Romanian,
            Language.Russian,
            Language.Sicilian,
            Language.Scots,
            Language.Slovak,
            Language.Slovenian,
            Language.Somali,
            Language.Castilian,
            Language.Albanian,
            Language.Serbian,
            Language.Sundanese,
            Language.Swahili,
            Language.Swedish,
            Language.Tamil,
            Language.Telugu,
            Language.Tajik,
            Language.Tagalog,
            Language.Turkmen,
            Language.Turkish,
            Language.Ukrainian,
            Language.Urdu,
            Language.Uzbek,
            Language.Vietnamese,
            Language.Waray,
            Language.Walloon,
            Language.Yiddish,
            Language.Zazaki
    };

    public void setLanguage(Language lang) {
        this.lang = lang;
        initialize(null); // load the new model
    }

    @Override
    public void initialize(ResourceManager rm) {
        try {
            Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File f = dsNoCredentials.getDirectory("org.cogcomp.transliteration", "transliteration-models", 1.3, false);
            String modelPath = f.getAbsolutePath() + File.separator + "transliteration-models-oct-2017" + File.separator + "probs-" + lang.getCode() + ".txt";
            if(new File(modelPath).exists()) {
                logger.info("Loading transliteration models for language: " + lang + " from " + modelPath);
                model = new SPModel(modelPath);
                model.setMaxCandidates(1);
            }
            else {
                logger.error("Model for language: " + lang + " don't exist: " + modelPath);
            }
        } catch (IOException | InvalidEndpointException | DatastoreException | InvalidPortException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {

        View v = new TokenLabelView(ViewNames.TRANSLITERATION, this.getClass().getName(), ta, 1.0);

        int index = 0;
        for(String tok : ta.getTokens()){
            try {
                TopList<Double, String> ll = model.Generate(tok.toLowerCase());
                if(ll.size() > 0) {
                    Pair<Double, String> toppair = ll.getFirst();
                    Constituent c = new Constituent(toppair.getSecond(), toppair.getFirst(), ViewNames.TRANSLITERATION, ta, index, index + 1);
                    v.addConstituent(c);
                }
            } catch (Exception e) {
                // print that this word has failed...
                e.printStackTrace();
            }

            index++;
        }
        ta.addView(ViewNames.TRANSLITERATION, v);
    }
}
