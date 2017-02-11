package edu.illinois.cs.cogcomp.depparse.features;

import edu.illinois.cs.cogcomp.depparse.core.DepInst;
import edu.illinois.cs.cogcomp.depparse.core.DepStruct;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LabeledDepFeatureGenerator extends AbstractFeatureGenerator implements Serializable {
    private Lexiconer lm;

    public LabeledDepFeatureGenerator(Lexiconer lm) {
        this.lm = lm;
    }

    @Override
    public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
        DepInst sent = (DepInst) x;
        DepStruct tree = (DepStruct) y;
        return extractFeatures(sent, tree);
    }

    private IFeatureVector extractFeatures(DepInst sent, DepStruct tree) {
        FeatureVectorBuffer fb = new FeatureVectorBuffer();
        for (int i = 1; i <= sent.size(); i++)
            fb.addFeature(getCombineEdgeFeatures(tree.heads[i], i, sent, tree.deprels[i]));
        return fb.toFeatureVector();
    }

    public void previewFeature(DepInst sent) {
        for (int i = 1; i <= sent.size(); i++) {
            int j = sent.heads[i];
            Set<Feature> feats = getLabeledEdgeFeatureSet(j, i, sent, sent.deprels[i]);
            for (Feature f : feats)
                lm.addFeature(f.getName());
            Set<Feature> chunkfeats = getChunkFeatureSet(j, i, sent);
            for (Feature f : chunkfeats)
                lm.addFeature(f.getName());
        }
    }

    private FeatureVectorBuffer featureVectorBufferFromFeature(Set<Feature> features) {
        Map<String, Float> featureMap = new HashMap<>();
        for (Feature f : features) {
            if (lm.containFeature(f.getName()))
                featureMap.put(f.getName(), f.getValue());
        }

        SparseFeatureVector sfv = (SparseFeatureVector) lm.convertToFeatureVector(featureMap);
        return new FeatureVectorBuffer(sfv);
    }

    private Set<Feature> POSwinConj(int head, int dep, DepInst sent, String deprel) {
        String header = "POSwin: ";
        String headleft = "Left: " + (head > 0 ? sent.strPos[head - 1] : "null") + " ";
        String headcenter = "Center: " + sent.strPos[head] + " ";
        String headright =
                "Right: " + (head + 1 < sent.strPos.length ? sent.strPos[head + 1] : "null") + " ";
        String depleft = "Left: " + (dep > 0 ? sent.strPos[dep - 1] : "null") + " ";
        String depcenter = "Center: " + sent.strPos[dep] + " ";
        String depright =
                "Right: " + (dep + 1 < sent.strPos.length ? sent.strPos[dep + 1] : "null") + " ";
        String arcdir = "Arc-dir: " + (head < dep) + " ";
        String arclength = "Arc-length: " + (head - dep) + " ";

        Set<Feature> feats = new HashSet<>();
        feats.add(new DiscreteFeature(header + headcenter + depcenter + arcdir + deprel));
        feats.add(new DiscreteFeature(header + headcenter + depcenter + arclength + deprel));
        feats.add(new DiscreteFeature(header + headleft + headcenter + headright + depleft
                + depcenter + depright + arcdir + deprel));
        return feats;
    }


    private Set<Feature> POSBrownConj(int head, int dep, DepInst sent, String deprel) {
        String header = "POSBrown: ";
        String brownhead = sent.strBrown[head] + " ";
        String poshead = sent.strPos[head] + " ";
        String browndep = sent.strBrown[dep] + " ";
        String posdep = sent.strPos[dep] + " ";
        String arcdir = "Arc-dir: " + (head < dep) + " ";

        Set<Feature> feats = new HashSet<>();
        feats.add(new DiscreteFeature(header + brownhead + browndep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + poshead + browndep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + brownhead + posdep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + poshead + brownhead + posdep + browndep + arcdir
                + deprel));
        return feats;
    }

    private Set<Feature> PrefixConj(int head, int dep, DepInst sent, String deprel) {
        String header = "Prefix: ";
        String prefixhead =
                sent.strLemmas[head].substring(0, Math.min(sent.strLemmas[head].length(), 5)) + " ";
        String prefixdep =
                sent.strLemmas[dep].substring(0, Math.min(sent.strLemmas[dep].length(), 5)) + " ";
        String poshead = sent.strPos[head] + " ";
        String posdep = sent.strPos[dep] + " ";
        String arcdir = "Arc-dir: " + (head < dep) + " ";

        Set<Feature> feats = new HashSet<>();
        feats.add(new DiscreteFeature(header + prefixhead + posdep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + poshead + prefixdep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + prefixhead + prefixdep + arcdir + deprel));
        return feats;
    }

    private Set<Feature> SuffixConj(int head, int dep, DepInst sent, String deprel) {
        String header = "Suffix: ";
        String suffixhead =
                sent.strLemmas[head].substring(Math.max(sent.strLemmas[head].length() - 3, 0))
                        + " ";
        String suffixdep =
                sent.strLemmas[dep].substring(Math.max(sent.strLemmas[dep].length() - 3, 0)) + " ";
        String poshead = sent.strPos[head] + " ";
        String posdep = sent.strPos[dep] + " ";
        String arcdir = "Arc-dir: " + (head < dep) + " ";

        Set<Feature> feats = new HashSet<>();
        feats.add(new DiscreteFeature(header + suffixhead + posdep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + poshead + suffixdep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + suffixhead + suffixdep + arcdir + deprel));
        return feats;
    }

    private Set<Feature> ChunkConj(int head, int dep, DepInst sent, String deprel) {
        String header = "POSChunk: ";
        String chunkhead = sent.strChunk[head] + " ";
        String poshead = sent.strPos[head] + " ";
        String chunkdep = sent.strChunk[dep] + " ";
        String posdep = sent.strPos[dep] + " ";
        String arcdir = "Arc-dir: " + (head < dep) + " ";
        String arclength = "Arc-length " + (head - dep) + " ";

        Set<Feature> feats = new HashSet<>();
        feats.add(new DiscreteFeature(header + chunkhead + chunkdep + arcdir + deprel));
        feats.add(new DiscreteFeature(header + chunkhead + chunkdep + arclength + deprel));
        feats.add(new DiscreteFeature(header + chunkhead + poshead + chunkdep + posdep + arcdir
                + deprel));
        feats.add(new DiscreteFeature(header + chunkhead + poshead + chunkdep + posdep + arclength
                + deprel));
        return feats;
    }

    private Set<Feature> getLabeledEdgeFeatureSet(int head, int dep, DepInst sent, String deprel) {
        Set<Feature> feats = new HashSet<>();
        feats.addAll(POSBrownConj(head, dep, sent, deprel));
        feats.addAll(PrefixConj(head, dep, sent, deprel));
        feats.addAll(SuffixConj(head, dep, sent, deprel));
        feats.addAll(POSwinConj(head, dep, sent, deprel));
        feats.addAll(ChunkConj(head, dep, sent, deprel));
        return feats;
    }

    public FeatureVectorBuffer getLabeledEdgeFeatures(int head, int dep, DepInst sent, String deprel) {
        FeatureVectorBuffer feat =
                featureVectorBufferFromFeature(getLabeledEdgeFeatureSet(head, dep, sent, deprel));
        feat.shift((int) Math.pow(2, 0));
        return feat;
    }

    public FeatureVectorBuffer getCombineEdgeFeatures(int head, int dep, DepInst sent, String deprel) {
        FeatureVectorBuffer fb = new FeatureVectorBuffer();
        // TODO Check if needed
        // fb.addFeature(getEdgeFeatures(head, dep, sent));
        fb.addFeature(getLabeledEdgeFeatures(head, dep, sent, deprel));
        return fb;
    }

    private Set<Feature> getChunkFeatureSet(int head, int dep, DepInst sent) {
        String header = "POSChunk: ";
        String chunkhead = sent.strChunk[head] + " ";
        String poshead = sent.strPos[head] + " ";
        String chunkdep = sent.strChunk[dep] + " ";
        String posdep = sent.strPos[dep] + " ";
        String arcdir = "Arc-dir: " + (head < dep) + " ";
        String arclength = "Arc-length " + (head - dep) + " ";

        Set<Feature> feats = new HashSet<>();
        feats.add(new DiscreteFeature(header + chunkhead + chunkdep));
        feats.add(new DiscreteFeature(header + chunkhead + chunkdep + arcdir));
        feats.add(new DiscreteFeature(header + chunkhead + chunkdep + arclength));
        feats.add(new DiscreteFeature(header + chunkhead + poshead + chunkdep + posdep));
        feats.add(new DiscreteFeature(header + chunkhead + poshead + chunkdep + posdep + arcdir));
        feats.add(new DiscreteFeature(header + chunkhead + poshead + chunkdep + posdep + arclength));
        return feats;
    }

    // TODO Check if needed
    // public FeatureVectorBuffer getEdgeFeatures(int head, int dep, DepInst sent) {
    // List<Integer> fList = new ArrayList<>();
    // List<Float> valList = new ArrayList<>();
    // int dist = Math.abs(head-dep);
    // dist = (dist > 10)? 10 : (dist>5)? 5: dist;
    // dist *= (head > dep)? templateCode *191021: templateCode *350377;
    // addSurroundingPOS(head, dep, dist, sent, fList);
    // addInBetweenPOS(head, dep, dist, sent, fList);
    // addLexFeats(head, dep, dist, sent, fList);
    // addLexPrefixFeats(head, dep, dist, sent, fList);
    //
    // for(int t=0; t< fList.size();t++)
    // valList.add(1.0f);
    // FeatureVectorBuffer fvb = new FeatureVectorBuffer(fList, valList);
    //
    // fvb.addFeature(featureVectorBufferFromFeature(getChunkFeatureSet(head, dep, sent)), (int)
    // Math.pow(2, 0));
    //
    // return fvb;
    // }
}
