package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.lm.NeuralLM;
import edu.illinois.cs.cogcomp.utils.Utils;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by mayhew2 on 12/16/15.
 */
public class HMMProductionLearner {

    static String wikidata = "/shared/corpora/transliteration/wikidata/";
    static String sourcefile = wikidata + String.format("wikidata.%s", "French");

    public static void getProductions() throws Exception {
        // process: this is just HMM where I already know the transition probabilities.
        // these are given by a character language model in the target language.

        // now, the output symbols are the source alphabet chars
        // the hidden symbols are the target alphabet chars

        // the input sequences are names in the source language.

        // goal is to estimate the output probabilities of each letter in the source language.
        // these are effectively the productions we need. OK!

        // in terms of Jurafsky and Martin, we already have a.
        // and we want b.

        List<Example> examples = Utils.readWikiData(sourcefile);
        List<String> sourcenames = new ArrayList<>();

        for(Example e : examples){
            sourcenames.add(e.sourceWord);
        }

        // load LM for target language
        NeuralLM lm = NeuralLM.from_file("/home/mayhew2/IdeaProjects/illinois-transliteration/lm/russian/nplm-ru.txt");

        // initialize B: matrix of target->source productions.
        // keep as a hashmap, and update as necessary, no initialization needed.

        HashMap<Production, Double> b = new HashMap<>();

        // The entire russian alphabet... the set of hidden states in this case.
        // String tgtalphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"; // these are italics
        String tgtalphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

        String srcalphabet = "abcdefghijklmnopqrstuvwxyz";

        // Let's precalculate a, and make sure it is normalized. +2 because of start and end symbols
        double[][] transition = new double[tgtalphabet.length()][tgtalphabet.length()];

        for(int i = 0; i < tgtalphabet.length(); i++){
            double total = 0;
            for(int j = 0; j < tgtalphabet.length(); j++){
                char[] ngram = {tgtalphabet.charAt(i), tgtalphabet.charAt(j)};
                transition[i][j] = Math.exp(lm.ngram_prob(ngram));
                total += transition[i][j];
            }
            System.out.println(total);
        }

        for(double[] row : transition){
            System.out.println(Arrays.toString(row));
        }


        Random r = new Random();
        int numexamples = 20;
        int N = tgtalphabet.length();

        int iterations = 3;

        for(int iter = 0; iter < iterations; iter++) {
            int cnt = 0;
            System.out.println("iteration " + (iter+1));
            HashMap<String, Double[][]> gammas = new HashMap<>();

            double avgprob = 0;
            for (String sourcename : sourcenames) {

                if (sourcename.length() < 4) {
                    continue;
                }

                if (gammas.containsKey(sourcename)) {
                    continue;
                }

                cnt++;
                if (cnt > numexamples) {
                    break;
                }

                int T = sourcename.length();

                // first calculate alpha, initializes to zeros.
                // N+1 allows for a final state
                // Last row of alpha is the q_F state.
                // First row is the q_1 state (ignore start state)
                double[][] alpha = new double[N + 1][T];

                // initialize alpha
                for (int s = 0; s < N; s++) {
                    List<String> ngrams = new ArrayList<>();
                    ngrams.add("<s>");
                    ngrams.add(tgtalphabet.charAt(s) + "");
                    double ngl = lm.ngram_prob(ngrams);
                    double a_0_s = Math.exp(ngl);

                    Production p = new Production(tgtalphabet.charAt(s) + "", sourcename.charAt(0) + "");
                    double bval;
                    if (b.containsKey(p)) {
                        bval = b.get(p);
                    } else {
                        // initialize b randomly.
                        bval = r.nextDouble();
                        b.put(p, bval);
                    }

                    alpha[s][0] = a_0_s * bval;
                }

                // intentionally at 1
                for (int t = 1; t < T; t++) {
                    for (int s = 0; s < N; s++) {

                        double sum = 0;
                        for (int sp = 0; sp < N; sp++) {
                            char[] ngram = {tgtalphabet.charAt(sp), tgtalphabet.charAt(s)};
                            double a_sp_s = Math.exp(lm.ngram_prob(ngram));

                            Production p = new Production(tgtalphabet.charAt(s) + "", sourcename.charAt(t) + "");
                            double bval;
                            if (b.containsKey(p)) {
                                bval = b.get(p);
                            } else {
                                // initialize b randomly.
                                bval = r.nextDouble();
                                b.put(p, bval);
                            }

                            sum += alpha[sp][t - 1] * a_sp_s * bval;
                        }

                        alpha[s][t] = sum;
                    }
                }

                // final part.
                double sum = 0;
                for (int s = 0; s < N; s++) {
                    List<String> ngrams = new ArrayList<>();
                    ngrams.add(tgtalphabet.charAt(s) + "");
                    ngrams.add("</s>");

                    double a_s_qf = Math.exp(lm.ngram_prob(ngrams));

                    sum += alpha[s][T - 1] * a_s_qf;
                }

                // index N is the last in the first dimension
                // T-1 is the last in the second dimension
                alpha[N][T - 1] = sum;

                avgprob += sum;

                // ======== NOW BACKWARD =========
                double[][] beta = new double[N + 1][T];

                for (int i = 0; i < N; i++) {
                    List<String> ngrams = new ArrayList<>();
                    ngrams.add(tgtalphabet.charAt(i) + "");
                    ngrams.add("</s>");
                    double fin = lm.ngram_prob(ngrams);
                    double a_s_qf = Math.exp(fin);

                    // reserve beta[0] for the start state
                    beta[i + 1][T - 1] = a_s_qf;
                }

                // main loop
                for (int t = T - 2; t >= 0; t--) {
                    for (int i = 0; i < N; i++) {
                        sum = 0;

                        for (int j = 0; j < N; j++) {
                            List<String> ngrams = new ArrayList<>();
                            ngrams.add(tgtalphabet.charAt(i) + "");
                            ngrams.add(tgtalphabet.charAt(j) + "");
                            double a_ij = Math.exp(lm.ngram_prob(ngrams));

                            Production p = new Production(tgtalphabet.charAt(j) + "", sourcename.charAt(t + 1) + "");
                            double bval;
                            if (b.containsKey(p)) {
                                bval = b.get(p);
                            } else {
                                // initialize b randomly.
                                bval = r.nextDouble();
                                b.put(p, bval);
                            }

                            sum += a_ij * bval * beta[j + 1][t + 1];
                        }

                        beta[i + 1][t] = sum;
                    }
                }

                // final for beta
                sum = 0;
                for (int j = 0; j < N; j++) {
                    List<String> ngrams = new ArrayList<>();
                    ngrams.add("<s>");
                    ngrams.add(tgtalphabet.charAt(j) + "");
                    double a_0_j = Math.exp(lm.ngram_prob(ngrams));

                    Production p = new Production(tgtalphabet.charAt(j) + "", sourcename.charAt(0) + "");
                    double bval;
                    if (b.containsKey(p)) {
                        bval = b.get(p);
                    } else {
                        // initialize b randomly.
                        bval = r.nextDouble();
                        b.put(p, bval);
                    }

                    sum += a_0_j * bval * beta[j + 1][0];
                }

                beta[0][0] = sum;


                // ========== get gamma =========
                Double[][] gamma = new Double[N][T];

                for (int t = 0; t < T; t++) {
                    for (int j = 0; j < N; j++) {
                        double v = alpha[j][t] * beta[j+1][t] / sum;
                        if (Double.isNaN(v)) {
                            System.out.println("THIS IS NAN GAMMA");
                        }
                        gamma[j][t] = v;
                    }
                }

                gammas.put(sourcename, gamma);

            }

            System.out.println("data prob: " + avgprob / numexamples);

            // ========== Now update b =========
            for (char vk : srcalphabet.toCharArray()) {
                for (int j = 0; j < N; j++) {

                    double denom = 0;
                    double num = 0;

                    for (String sname : gammas.keySet()) {
                        int T = sname.length();

                        Double[][] gamma = gammas.get(sname);

                        for (int t = 0; t < T; t++) {
                            denom += gamma[j][t];

                            if (sname.charAt(t) == vk) {
                                num += gamma[j][t];
                            }
                        }
                    }

                    Production p = new Production(tgtalphabet.charAt(j) + "", vk + "");

                    double val = num / denom;
                    if (Double.isNaN(val)) {
                        System.out.println("NOW IT'S NAN");
                    }

                    b.put(p, val);
                }
            }

            HashSet<Production> removes = new HashSet<>();
            for(Production p : b.keySet()){
                if(srcalphabet.indexOf(p.getSecond()) == -1){
                    removes.add(p);
                }
            }

            for(Production p : removes){
                b.remove(p);
            }

        }

//        for(Production p : b.keySet()){
//            System.out.println(p + " : " + b.get(p));
//        }

    }


    public static void main(String[] args) throws Exception {
        getProductions();
    }

}
