/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.question_typer;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureNGramUtility;
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.apache.commons.io.FileUtils;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by daniel on 1/18/18.
 */
public class QuestionTyperFeatureExtractorsUtils {

    static String resourcesFolder = null;

    static {
        Datastore dsNoCredentials = null;
        try {
            dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File f = dsNoCredentials.getDirectory("org.cogcomp.question-typer", "question-typer-resources", 1.0, false);
            resourcesFolder = f.getPath() + "/question-typer-resources/";
        } catch (InvalidPortException | DatastoreException | InvalidEndpointException e) {
            e.printStackTrace();
        }
    }

    public static String[] questionTerms = {"what", "when", "where", "which", "who", "whom", "whose", "why", "why don't", "how", "how far", "how long", "how many", "how much", "how old", "how come", "do", "did"};
    public static Set<String> questionTermsSet = new HashSet<>(Arrays.asList(questionTerms));

    private static HashSet<String> readFileAsSet(String filePath) {
        String fileContents = null;
        try {
            fileContents = FileUtils.readFileToString(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] strs = fileContents.split("\n");
        return new HashSet<>(Arrays.asList(strs));
    }

    public static Set<String> occupations = readFileAsSet(resourcesFolder + "prof.txt");
    public static Set<String> food = readFileAsSet(resourcesFolder + "food.txt");
    public static Set<String> mountain = readFileAsSet(resourcesFolder + "mount.txt");

    public static boolean[] getOverlapWithSets(TextAnnotation s) {
        boolean mountainOverlap = false;
        boolean profOverlap = false;
        boolean foodOverlap = false;
        for(Constituent c: s.getView(ViewNames.LEMMA).getConstituents()) {
            if(mountainOverlap && profOverlap && foodOverlap) break;
            if(!profOverlap && occupations.contains(c.getLabel())) profOverlap = true;
            if(!foodOverlap && food.contains(c.getLabel())) foodOverlap = true;
            if(!mountainOverlap && mountain.contains(c.getLabel())) mountainOverlap = true;
        }
        return new boolean[] { mountainOverlap, profOverlap, foodOverlap };
    }

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

    private static boolean isCapitalized(String c) {
        String upperCase = c.toUpperCase();
        return upperCase.substring(0, 1).equals(c.substring(0, 1));
    }

    public static boolean[] getStringPatternsBoolean(TextAnnotation s) {
        List<Constituent> list = s.getView(ViewNames.POS).getConstituents();
        boolean cb = list.get(1).getLabel().contains("NN");
        boolean what = list.get(0).getSurfaceForm().toLowerCase().contains("what");
        boolean where = list.get(0).getSurfaceForm().toLowerCase().contains("where");
        boolean when = list.get(0).getSurfaceForm().toLowerCase().contains("when");

        String secondWordCapitalization = list.get(1).getSurfaceForm();
        boolean whereOrWhenAndCapitapized = (where || when) && isCapitalized(secondWordCapitalization.substring(0, 1));

        boolean containsBodyOrPart = s.text.contains(" body") || s.text.contains(" part ");
        boolean whatNationality = s.text.toLowerCase().contains("what") && s.text.contains("nationality");

        return new boolean[]{ cb && what, whereOrWhenAndCapitapized, containsBodyOrPart};
    }

    public static String[] getStringPatternsDiscrete(TextAnnotation s) {
        List<Constituent> posCons = s.getView(ViewNames.POS).getConstituents();
        List<Constituent> lemmaCons = s.getView(ViewNames.LEMMA).getConstituents();
        boolean what = lemmaCons.get(0).getSurfaceForm().toLowerCase().contains("what");
        String whatNounNoun = "";
        if(what && posCons.size() >= 2) {
            whatNounNoun = lemmaCons.get(1).getLabel() + "-" + lemmaCons.get(2).getLabel();
        }

        return new String[]{ whatNounNoun };
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

    public static List<String> getWordGroupFeatures(TextAnnotation s) {
        List<Constituent> lemma = s.getView(ViewNames.LEMMA).getConstituents();
        Set<String> lemmaLabels = new HashSet<>();
        for(Constituent c : lemma) lemmaLabels.add(c.getLabel());

        List<String> overlapLabels = new ArrayList<>();
        for(Object label : list.keySet()) {
            HashSet set = (HashSet) list.get(label);
            HashSet lemmaLabelsClone = new HashSet(lemmaLabels);
            lemmaLabelsClone.retainAll(set);
            if(lemmaLabelsClone.size() > 0) overlapLabels.add((String)label);
        }
        return overlapLabels;
    }

    public static HashMap list = new HashMap();

    static {
        List<URL> files = IOUtils.getListOfFilesInDir(resourcesFolder + "lists/");
        assert files.size() > 0 : "list of files not found";
        for (URL u : files) {
            File file = new File(u.getPath());
            String fileContents = null;
            try {
                fileContents = FileUtils.readFileToString(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] strs = fileContents.split("\n");
            list.put(file.getName(), new HashSet<>(Arrays.asList(strs)));
        }
    }

    public static void getNgrams(TextAnnotation s) {
        List cons = s.getView(ViewNames.LEMMA).getConstituents();
        for(int i = 2; i <= 4; i++) {
            List ngramOrdred = FeatureUtilities.getFeatureSet(FeatureNGramUtility.getLabelNgramsOrdered(cons, i));
            String label = i + "gramTokensOrdered";
            for(int ii = 0; ii < ngramOrdred.size(); ii++) {
                System.out.println((String) ngramOrdred.get(ii));
            }
        }
    }

    public static ServerClientAnnotator serverClient = new ServerClientAnnotator();
    static {
        serverClient.setUrl("http://austen.cs.illinois.edu", "5800");
        serverClient.setViewsAll(new String[]{ViewNames.LEMMA, ViewNames.NER_CONLL, ViewNames.NER_ONTONOTES, ViewNames.POS, ViewNames.SHALLOW_PARSE});
        serverClient.useCaching("questionTyperAnnotation.cache");
    }

}
