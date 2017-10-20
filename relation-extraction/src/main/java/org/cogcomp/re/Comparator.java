package org.cogcomp.re;

import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.sim.*;

import org.cogcomp.re.LbjGen.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xuany on 10/12/2017.
 */
public class Comparator {
    private WordSim wordSim = null;
    private Metric llmStringSim = null;
    List<Relation> examples = IOHelper.inputRelationsNonBinary("preprocess/relations/PHYS_MAN_NON_BIN.txt");
    public Comparator(){
        String config = "config/configurations.properties";
        llmStringSim = new LLMStringSim(config);
        ResourceManager rm_ = new SimConfigurator().getDefaultConfig();
        wordSim = new WordSim(rm_, "word2vec");
    }
    public static List<String> compareRelationsHelper(Relation a){
        List<String> ret = new ArrayList<>();
        TreeView parse = (TreeView) a.getSource().getTextAnnotation().getView(ViewNames.DEPENDENCY_STANFORD);
        Constituent source = a.getSource();
        Constituent target = a.getTarget();
        Constituent source_head = RelationFeatureExtractor.getEntityHeadForConstituent(source, source.getTextAnnotation(), "EntityHeads");
        Constituent target_head = RelationFeatureExtractor.getEntityHeadForConstituent(target, target.getTextAnnotation(), "EntityHeads");
        ret.add(source_head.toString());
        ret.add(target_head.toString());

        List<Constituent> source_parsed_list = parse.getConstituentsCoveringToken(source_head.getStartSpan());
        List<Constituent> target_parsed_list = parse.getConstituentsCoveringToken(target_head.getStartSpan());
        String fullPath = "";
        String lastWord = "";
        String p2Word = "";
        String p32Word = "";
        if (source.getSentenceId() == target.getSentenceId()) {
            try {
                if (source_parsed_list.size() != 0 && target_parsed_list.size() != 0) {
                    Constituent source_parsed = parse.getConstituentsCoveringToken(source_head.getStartSpan()).get(0);
                    Constituent target_parsed = parse.getConstituentsCoveringToken(target_head.getStartSpan()).get(0);
                    List<Constituent> paths = PathFeatureHelper.getPathConstituents(source_parsed, target_parsed, 100);
                    for (int i = 1; i < paths.size() - 1; i++) {
                        fullPath += paths.get(i).toString() + " ";
                        if (i == paths.size() - 2) {
                            lastWord = paths.get(i).toString();
                            p32Word += lastWord;
                        }
                        if (i == paths.size() - 3){
                            p2Word = paths.get(i).toString();
                            p32Word += p2Word + " ";
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ret.add(fullPath);
        ret.add(lastWord);
        ret.add(p2Word);
        ret.add(p32Word);
        ret.add(source_head.toString() + " " + p32Word + " " + target_head.toString());

        int posStart = Math.min(source_head.getEndSpan(), target_head.getEndSpan());
        int posEnd = Math.max(source_head.getStartSpan(), target_head.getStartSpan());
        View posView = source.getTextAnnotation().getView(ViewNames.POS);
        String[] keep = {"VB", "VBG", "VBD", "VBN", "VBP", "VBZ", "TO", "IN"};
        List<String> keepList = Arrays.asList(keep);
        String posPath = source_head + " ";
        for (int i = posStart; i < posEnd; i++){
            String curPosLabel = posView.getConstituentsCoveringToken(i).get(0).getLabel();
            if (keepList.contains(curPosLabel)) {
                posPath += posView.getConstituentsCoveringToken(i).get(0).toString() + " ";
            }
        }
        posPath += target_head;
        ret.add(posPath);
        return ret;
    }

    public List<Double> compareRelations(Relation a, Relation b){
        List<Double> ret = new ArrayList<>();

        List<String> af = compareRelationsHelper(a);
        List<String> bf = compareRelationsHelper(b);
        for (int i = 0; i < af.size(); i++){
            String as = af.get(i);
            String bs = bf.get(i);
            MetricResponse compare = null;
            if (i == 2 || i >= 5){
                compare = llmStringSim.compare(as, bs);
            }
            else {
                compare = wordSim.compare(as, bs);
            }
            if (compare == null){
                ret.add(0.0);
            }
            else {
                double result = compare.score;
                if (Double.isNaN(result)){
                    ret.add(0.0);
                }
                else {
                    ret.add(compare.score);
                }
            }
        }
        return ret;
    }

    public double[] compareRelationWithDefs_avg(List<Relation> defs, Relation r){
        double[] avgs = new double[8];
        for (int i = 0; i < avgs.length; i++){
            avgs[i] = 0.0;
        }
        for (Relation def : defs){
            List<Double> scores = compareRelations(def, r);
            for (int i = 0; i < scores.size(); i++){
                avgs[i] += scores.get(i);
            }
        }
        for (int i = 0; i < avgs.length; i++){
            if (avgs[i] != 0) {
                avgs[i] = avgs[i] / (double) defs.size();
            }
        }
        return avgs;
    }

    public double[] compareRelationWithDefs_max(List<Relation> defs, Relation r){
        double[] maxs = new double[8];
        for (int i = 0; i < maxs.length; i++){
            maxs[i] = 0.0;
        }
        for (Relation def : defs){
            List<Double> scores = compareRelations(def, r);
            for (int i = 0; i < scores.size(); i++){
                if (scores.get(i) > maxs[i]){
                    maxs[i] = scores.get(i);
                }
            }
        }
        return maxs;
    }

    public double[] score(Relation r, String type){
        double[] a = compareRelationWithDefs_avg(examples, r);
        double[] b = compareRelationWithDefs_max(examples, r);
        double[] ret = new double[a.length + b.length];
        for (int i = 0; i < ret.length; i++){
            if (i < a.length){
                ret[i] = a[i];
            }
            else {
                ret[i] = b[i - a.length];
            }
        }
        return ret;
    }
}
