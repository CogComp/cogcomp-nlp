package edu.illinois.cs.cogcomp.finetyper.finer.wordnet;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.IOException;
import java.util.*;

/**
 * Created by muddire2 on 1/10/17.
 */
public class WordNetUtils {

    private static WordNetUtils WORD_NET_UTILS;
    private static final Object lock = new Object();
    public Dictionary dictionary;
    private Synset entitySynset;

    private WordNetUtils() throws JWNLException {
        dictionary = Dictionary.getDefaultResourceInstance();
        entitySynset = getSynsets("entity").get(0);
    }

    public static WordNetUtils getInstance() throws JWNLException {
        synchronized (WordNetUtils.class) {
            if (WORD_NET_UTILS == null)
                WORD_NET_UTILS = new WordNetUtils();
        }
        return WORD_NET_UTILS;
    }

    public static WordNetUtils getInstanceFastOrNull() {
        return WORD_NET_UTILS;
    }


    public List<Synset> getSynsets(String lemma, POS pos) throws JWNLException {
        IndexWord w = dictionary.lookupIndexWord(pos, lemma);
        return (w == null) ? new ArrayList<>() : w.getSenses();
    }

    public List<Synset> getSynsets(String lemma) throws JWNLException {
        return getSynsets(lemma, POS.NOUN);
    }

    public PointerTargetTree getHyponymTree(Synset synset) throws JWNLException {
        PointerTargetTree hyponymTree = PointerUtils.getHyponymTree(synset);
        return hyponymTree;
    }

    public POS getPOS(String posString) {
        POS pos = null;
        switch (posString) {
            case "n":
                pos = POS.NOUN;
                break;
            case "v":
                pos = POS.VERB;
                break;
            case "a":
                pos = POS.ADJECTIVE;
                break;
            case "s":
                pos = POS.ADVERB;
                break;
            default:
                throw new IllegalArgumentException("Invalid POS string - " + posString);
        }
        return pos;
    }

    public Synset getSynsetFromNLTKString(String nltkSynsetString) throws JWNLException {
        String[] tokens = nltkSynsetString.split("\\.");
        assert tokens.length == 3;
        String lemma = tokens[0];
        POS pos = getPOS(tokens[1]);
        int id = Integer.parseInt(tokens[2]) - 1;
        IndexWord w = dictionary.lookupIndexWord(pos, lemma);
        if (w == null)
            throw new IllegalArgumentException("Word/Lemma not found in wordnet - " + nltkSynsetString);
        if (id >= w.getSenses().size())
            throw new IllegalArgumentException("Invalid word sense index - " + nltkSynsetString);
        return w.getSenses().get(id);
    }

    public List<Pointer> getPointers(Synset synset, PointerType type) {
        List<Pointer> pointers = new ArrayList<>();
        // strict matching i.e. instance hyponyms not in hyponyms
        for (Pointer p : synset.getPointers(type))
            if (p.getType().getKey().equals(type.getKey()))
                pointers.add(p);
        return pointers;
    }

    public Synset getFirstInTree(Synset targetSynset, PointerTargetTree tree) {
        Queue<PointerTargetTreeNode> bfsQueue = new LinkedList<>();
        bfsQueue.add(tree.getRootNode());
        PointerTargetTreeNode curNode;
        PointerTargetTreeNode result = null;
        while (result == null && bfsQueue.size() > 0) {
            curNode = bfsQueue.remove();
            if (curNode.hasValidChildTreeList())
                bfsQueue.addAll(curNode.getChildTreeList());
            if (curNode.getSynset().equals(targetSynset))
                result = curNode;
        }
        return (result == null) ? null : result.getSynset();
    }

    private String synsetToString(Synset synset) {
        StringBuilder words = new StringBuilder();

        words.append("( ");
        for (int i = 0; i < synset.getWords().size(); ++i) {
            if (i > 0) {
                words.append(", ");
            }

            words.append(((Word) synset.getWords().get(i)).getLemma());
        }
        words.append(" )");

        return words.toString();
    }

    private void recurseForward(Synset n, Synset t, Map<Synset, Integer> sn) throws JWNLException {
        sn.put(n, sn.getOrDefault(n, 0) + 1);
        for (Pointer hypPointer : getPointers(n, PointerType.HYPERNYM)) {
            Synset c = hypPointer.getTargetSynset();
            recurseForward(c, t, sn);
        }
    }

    private int recurseBackward(Synset n, Synset t, Map<Synset, Integer> nt) throws JWNLException {
        int tmp = 0;
        for (Pointer hypPointer : getPointers(n, PointerType.HYPERNYM)) {
            Synset c = hypPointer.getTargetSynset();
            if (!nt.containsKey(c))
                nt.put(c, recurseBackward(c, t, nt));
            tmp += nt.get(c);
        }
        nt.put(n, Math.max(1, tmp));
        return nt.get(n);
    }

    private Map<Synset, Double> getSynsetScores(Synset synset) throws JWNLException {
//        assert synset.getPOS() == POS.NOUN;
        if (synset.getPOS() != POS.NOUN)
            return new HashMap<>();
        Map<Synset, Double> synsetScores = new HashMap<>();

        Map<Synset, Integer> sn = new HashMap<>();
        Map<Synset, Integer> nt = new HashMap<>();

        recurseForward(synset, entitySynset, sn);
        recurseBackward(synset, entitySynset, nt);

        int numPaths = nt.get(synset);
//        System.out.println("Number of paths = " + numPaths);

        for (Synset countSynset : nt.keySet())
            synsetScores.put(countSynset, (double) nt.get(countSynset) * sn.get(countSynset) / numPaths);

        return synsetScores;
    }

    private Map<String, Double> getTypeScores(Synset synset, Map<String, List<Synset>> typeToSynsets) throws JWNLException {
        Map<Synset, Double> synsetScores = getSynsetScores(synset);
        Map<String, Double> typeScores = new HashMap<>();
        for (String type : typeToSynsets.keySet()) {
            double typeScore = 0;
            for (Synset typeSynset : typeToSynsets.get(type))
                if (synsetScores.containsKey(typeSynset))
                    typeScore += synsetScores.get(typeSynset);
            if (typeScore == 0.0) {
                continue;
            }
            typeScores.put(type, typeScore);
        }
        return typeScores;
    }

    public Map<String, Double> getTypeScores(String synsetOffsetPOS,
                                             Map<String, List<Synset>> typeToSynsets) throws JWNLException {
        Synset synset = getSynsetByOffset(synsetOffsetPOS);
        return getTypeScores(synset, typeToSynsets);
    }

    public Synset getSynsetOfNoun(String readableName) throws JWNLException {
        String[] parts = readableName.split("\\.");
        int idx = Integer.valueOf(parts[2]);
        Synset synset = dictionary.lookupIndexWord(POS.NOUN, parts[0]).getSenses().get(idx - 1);

        return synset;
    }

    public Synset getSynsetByOffset(String synsetOffsetPOS) throws JWNLException {
        String[] tokens = synsetOffsetPOS.split("_");
        POS synsetPOS = getPOS(tokens[1]);
        long synsetOffset = Long.parseLong(tokens[0]);
        Synset synset = dictionary.getSynsetAt(synsetPOS, synsetOffset);
        return synset;
    }

    public static void main(String[] args) throws JWNLException, CloneNotSupportedException, IOException {
        WordNetUtils wordNetUtils = WordNetUtils.getInstance();

        Synset quellSynset = wordNetUtils.getSynsets("quell", POS.VERB).get(0);
        System.out.println(quellSynset.getPOS());
    }
}