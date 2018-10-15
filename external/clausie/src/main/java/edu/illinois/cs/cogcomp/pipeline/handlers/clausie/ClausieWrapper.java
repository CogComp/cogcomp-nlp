/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers.clausie;

import de.mpii.clausie.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * @author nikett
 */
public class ClausieWrapper {

    private static List<String> copularVerbs;
    private static ClausIE clausIE;
    private static boolean clausieDeDupliClauses, dropCopularVerbs;

    public static void main(String[] args) throws Exception {
        String s =
                "He went to a small private school in the city "
                        + "and had good teachers to play with.";
        // "X considered as JJ, but actually consist of Y";
        while (!s.equals("q")) {
            for (SVO e : ClausieUtil.nullableIter(extract(s))) {
                System.out.println(e);
            }
            s = ClausieUtil.readStringFromUser("input to clausie: ");
        }
    }

    // TODO phrase splitter. using an IPhraseSplitter.
    // public static List<String> clauseSplitter(String sentence){...}

    public static void initClausie() throws IOException {
        clausIE = new ClausIE();
        clausIE.initParser();
        clausIE.getOptions().lemmatize = false;
        copularVerbs = new ArrayList<>(Arrays.asList(new String[]{"be", "has", "have",
                "had", "is", "was", "are", "were"}));
        clausieDeDupliClauses = true;
        dropCopularVerbs = false;
    }

    /**
     * str = "Thirteen million Londoners have to cope with this, " +
     * "and bake beans and allbran and rape, and I'm sitting in this bloody " +
     * "shack and I can't cope with Withnail. "; <BR>
     * verbFolder either already containing semiambiguous/ambiguous or list of
     * verbs will be generated in this folder. e.g. ./data/verbs
     *
     * @return
     */
    public static List<SVO> extract(String sentence) {
        try {

            if (!predictClausieSuccess(sentence)) return null;

            if (clausIE == null)
                initClausie();

            List<SVO> results = new ArrayList<>();

            List<SVO> extractions = new ArrayList<>();
            clausIE(sentence, clausIE, extractions, false);
            List<SVO> candidates = new ArrayList<>();
            for (SVO c : extractions)
                if (!c.hasRealVerb() && dropCopularVerbs)
                    continue;
                else
                    candidates.add(c);

            if (!clausieDeDupliClauses)
                return candidates;

            if (candidates.size() <= 1) {
                for (SVO c : candidates)
                    results.add(c);
            } else {
                Set<Integer> blackListed = new HashSet<>();
                for (int i = 0; i < candidates.size(); i++) {
                    for (int j = i; j < candidates.size(); j++) {
                        SVO c = candidates.get(i);
                        SVO c2 = candidates.get(j);
                        int comparison = c.compareMeWith(c2);
                        if (comparison == 1) {
                            blackListed.add(i); // Someone bigger than c exists.
                            break;
                        } else if (comparison == -1) {
                            blackListed.add(j);
                        }
                    }
                }
                for (int i = 0; i < candidates.size(); i++) {
                    if (!blackListed.contains(i))
                        results.add(candidates.get(i));
                }

            }
            if (results.isEmpty())
                return extractWithoutClausie(sentence);
            else
                return results;
        } catch (Exception | Error e) {
            // e.printStackTrace();;
            e.printStackTrace();
            return extractWithoutClausie(sentence);
        }

    }

    private static List<SVO> extractWithoutClausie(String sentence) {
        return null;
    }

    private static String add_(String w) {
        return w.replace(' ', '_');
    }

    private static boolean predictClausieSuccess(String sentence) {
        if (sentence == null || sentence.length() < 10)
            return false;

        String[] processed = sentence.split(" ");

        if (processed.length > 50)
            return false;

        int numCharacters = 0;
        for (int i = 0; i < processed.length; i++) {
            if (processed[i].length() > 1)
                continue; // cannot be a character.

            if (ClausieUtil.isNonAlphanumeric(processed[i])
                    && !processed[i].equals("."))
                numCharacters++;
        }

        if (numCharacters > 8)
            return false;

        // TODO conjunctions wordenumlist (or,and)
        // No conjunctions, clausie will lose.
        // fewer the commas (nonwords), clausie will lose.
        int numConjuctions = 0;
        for (int i = 0; i < processed.length; i++)
            if (!ClausieUtil.CONJUNCTION.contains(processed[i]))
                numConjuctions++;

        if (numConjuctions == 0 && numCharacters == 0)
            return false;

        return true;
    }

    /**
     * he, is cooking, in a frying pan. he, is cooking, in a frying pan that is
     * greasy. gets the shorter one.
     *
     * @param sentence
     * @param clausIE
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private static void clausIE(String sentence, ClausIE clausIE,
                                List<SVO> result, boolean longVerbPhrases) throws SQLException,
            IOException {
        // parse tree
        clausIE.parse(sentence);
        clausIE.detectClauses();
        clausIE.generatePropositions();// Instead play at Clause level.

        // headverb possible positions.
        List<Integer> headWords = new ArrayList<>();
        for (Clause c : ClausieUtil.nullableIter(clausIE.getClauses())) {
            Constituent verbConstituent = c.getConstituents().get(c.getVerb());
            if (verbConstituent instanceof IndexedConstituent) {
                headWords.add(((IndexedConstituent) verbConstituent).getRoot().index());
            }
        }

        for (Proposition p : clausIE.getPropositions()) {

            String s = p.subject().toLowerCase(); // TODO pair.
            String r = p.relation().toLowerCase();
            // with a cucumber and a plate in hands , the person returned to the
            // counter
            // TODO fix inside Clausie.
            if (!r.contains("@")) // be returned
            {
                if (r.startsWith("be ")) {
                    r = r.substring("be ".length());
                    String[] words = sentence.split(" ");
                    for (int i = 0; i < words.length; i++) {
                        if (r.equals(words[i])) {
                            r += "@" + (i + 1); // Stanford wordPos starts from
                            // 1.
                            break;
                        }
                    }
                }
            }

            if (r.endsWith(" and and"))
                r = r.substring(0, r.indexOf(" and and")).trim();
            String verb = "";
            String o = "";
            String addToR = "";
            for (int i = 0; i < p.noArguments(); i++) {
                String[] wordsOfArg =
                        p.argument(i).toLowerCase().trim().split(" ");

                boolean foundPrep = false;
                for (int j = 0; j < wordsOfArg.length; j++) {

                    if (!longVerbPhrases) {
                        // We accept whatever Clausie gives us.
                        o += " " + wordsOfArg[j];
                    } else {
                        // Till you hit the first preposition, keep adding.
                        if (!foundPrep) {
                            if (ClausieUtil.PREPOSITIONS
                                    .contains(wordsOfArg[j].split("@")[0]))
                                foundPrep = true;
                            addToR += " " + wordsOfArg[j];
                            continue;
                        } else {
                            o += " " + wordsOfArg[j];
                        }
                    }
                }
            }

            // result.add(new SVO(s, (r + addToR).trim(), o.trim(), s, verb,
            // o.trim()));
            S svoSubject = new S(s, s);
            V svoVerb = new V(r.toLowerCase().trim(), r, r);
            // identify head verb.
            svoVerb.head = headVerb(svoVerb.word, headWords);
            o = o.toLowerCase().trim();
            O svoObject = new O(o, o);
            result.add(new SVO(svoSubject, svoVerb, svoObject));
        }

    }

    // move@3 away@4 => move@3 ; try@3 to@4 squeeze@5 => squeeze@5
    private static String headVerb(String word, List<Integer> headWords) {
        for (String w : word.split(" ")) {
            for (int hvPos : headWords) {
                if (w.endsWith("@" + hvPos))
                    return w;
            }
        }
        return word;
    }

    public static class S {

        public String word;
        public String lemma;
        public String head;
        public String type;

        public S(String word, String lemma) {
            super();
            this.word = word;
            this.lemma = lemma;
            this.head = lemma; // TODO no operation is performed here
        }

        @Override
        public String toString() {
            return lemma;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            S other = (S) obj;
            if (lemma == null) {
                if (other.lemma != null)
                    return false;
            } else if (!lemma.equals(other.lemma))
                return false;
            return true;
        }

    }

    public static class V {

        public String word;
        public String lemma;
        public String head;
        public String type;

        public V(String word, String lemma, String head) {
            super();
            this.word = word;
            this.lemma = lemma;
            this.head = head;
        }

        @Override
        public String toString() {
            //return new StringBuilder(lemma).append(" [").append(head).append("]").toString();
            return lemma;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((head == null) ? 0 : head.hashCode());
            result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            V other = (V) obj;
            if (head == null) {
                if (other.head != null)
                    return false;
            } else if (!head.equals(other.head))
                return false;

            if (lemma == null) {
                if (other.lemma != null)
                    return false;
            } else if (!lemma.equals(other.lemma))
                return false;
            return true;
        }

    }

    public static class O {

        public String word;
        public String lemma;
        public String head;
        public String type;

        public O(String word, String lemma) {
            super();
            this.word = word;
            this.lemma = lemma;
            this.head = lemma; // TODO no operation is performed here.
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            O other = (O) obj;
            if (lemma == null) {
                if (other.lemma != null)
                    return false;
            } else if (!lemma.equals(other.lemma))
                return false;
            return true;
        }

        @Override
        public String toString() {
            //return new StringBuilder(lemma).append(" [").append(head).append("]").toString();
            return lemma;
        }

    }

    public static class SVO {

        public S s;
        public V v;
        public O o;
        public boolean isFilled;

        public SVO(S s, V v, O o) {
            super();
            this.s = s;
            this.v = v;
            this.o = o;
            isFilled =
                    s != null && v != null && o != null && s.head != null
                            && !s.head.isEmpty() && v.head != null && !v.head.isEmpty()
                            && o.head != null && !o.head.isEmpty();
        }

        /**
         * <PRE>
         * -1 : this should be accepted (is longer)
         * +1 : svo2 should be accepted (is longer)
         * 0 : same
         * -2 : incomparable.
         * </PRE>
         *
         * @param svo2
         * @return
         */
        public int compareMeWith(SVO svo2) {

            String svo2Str =
                    svo2.s.lemma + " " + svo2.v.lemma + " " + svo2.o.lemma;
            String svo1Str = s.lemma + " " + v.lemma + " " + o.lemma;
            if (svo2Str.equals(svo1Str))
                return 0;
            if (svo2Str.contains(svo1Str))
                return 1;
            else if (svo1Str.contains(svo2Str))
                return -1;

			/*
             * TODO: pick the larger proposition (we miss few cases)
			 * String s1="he went to the market to buy a carrot"; String s2 =
			 * "he buy a carrot"; // diff(s1,s2) = to the market to //
			 * if(s1.contains(diff(s1,s2)) then s1 contains s2.
			 *
			 *
			 *
			 * else if(s.word.contains(svo2.s.word) &&
			 * v.word.contains(svo2.v.word) && o.word.contains(svo2.o.word)){
			 * return -1; } else if(svo2.s.word.contains(s.word) &&
			 * svo2.v.word.contains(v.word) && svo2.o.word.contains(o.word)){
			 * return -1; }
			 */
            else
                return -2;
        }

        public boolean hasRealVerb() {
            String w = v.head;
            if (w == null || w.isEmpty())
                return false;
            if (w.contains("@"))
                w = w.split("@")[0];
            return !copularVerbs.contains(w.toLowerCase());
            // return !copularVerbs.contains(v.lemma) && !v.head.isEmpty();
        }

        @Override
        public String toString() {
            return new StringBuilder(s.toString()).append(",").append(
                    v.toString()).append(",").append(o.toString()).toString();
        }
    }


}

