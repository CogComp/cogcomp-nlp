package edu.illinois.cs.cogcomp.question_typer;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by daniel on 1/18/18.
 */
public class QuestionTyperFeatureExtractorsUtils {

    public static String[] questonTerms = {"what", "when", "where", "which", "who", "whom", "whose", "why", "why don't", "how", "how far", "how long", "how many", "how much", "how old", "how come", "do", "did"};
    public static Set<String> questionTermsSet = new HashSet<>(Arrays.asList(questonTerms));

    public static Constituent getConstituent(TextAnnotation s) {
        return s.getView(QuestionTyperConfigurator.questionTypeViewName).getConstituents().get(0);
    }

    public static String getFineLabel(TextAnnotation s) {
        Constituent c = getConstituent(s);
        return c.getAttribute(QuestionTyperConfigurator.goldFineLabelAttributeName);
    }

    public static String getCoarseLabel(TextAnnotation s) {
        Constituent c = getConstituent(s);
        return c.getAttribute(QuestionTyperConfigurator.goldCoarseLabelAttributeName);
    }

    public static String getFirstLabel(TextAnnotation s, String label) {
        List<Constituent> list = s.getView(ViewNames.SHALLOW_PARSE).getConstituents();
        String nounPhrase = "";
        for(Constituent c : list) {
            if(c.getLabel().contains(label)) {
                nounPhrase = c.getSurfaceForm();
            }
        }
        return nounPhrase;
    }

    public static String getFirstConstituents(TextAnnotation s, String viewName, int k, boolean getLabel) {
        List<Constituent> list = s.getView(viewName).getConstituents();
        String output = "";
        for(int i = 0; i < k && i < list.size(); i++) {
            if(getLabel)
                output += list.get(i).getLabel();
            else
                output += list.get(i).getSurfaceForm();
        }
        return output;
    }

    public static  List<String> getPosConjLemma(TextAnnotation s) {
        List<Constituent> lemma = s.getView(ViewNames.LEMMA).getConstituents();
        List<Constituent> pos = s.getView(ViewNames.LEMMA).getConstituents();
        ArrayList output = new ArrayList();
        for(int i = 0; i < lemma.size(); i++) {
            output.add(lemma.get(i) + "-" + pos.get(i));
        }
        return output;
    }


    /**
     * ner: bag of { ner labels } + bag of { ner labels conj with surface string }
     bag of pos conj words
     bag of chunk labels and chunk surface strings
     chunk heads: bag of { first noun phrase, first verb phrase, their conjunction }
     list of words: whether question contains food term, mountain term, profession term
     wordnet features
     word lists; like whether the question contains words related to art
     */

    public static HashMap list = new HashMap();

    public static void readLists() throws IOException {
        List<URL> files = IOUtils.getListOfFilesInDir("question-type/data/lists/");
        assert files.size() > 0 : "list of files not found";
        for (URL u : files) {
            File file = new File(u.getPath());
            String fileContents = FileUtils.readFileToString(file);
            String[] strs = fileContents.split("\n");
            list.put(file.getName(), new HashSet<String>(Arrays.asList(strs)));
        }
    }

    public static ServerClientAnnotator serverClient = new ServerClientAnnotator();
    static {
        serverClient.setUrl("http://austen.cs.illinois.edu", "5800");
        serverClient.setViewsAll(new String[]{ViewNames.LEMMA, ViewNames.NER_CONLL, ViewNames.NER_ONTONOTES, ViewNames.POS, ViewNames.SHALLOW_PARSE});
        serverClient.useCaching("questionTyperAnnotation.cache");
    }

}
