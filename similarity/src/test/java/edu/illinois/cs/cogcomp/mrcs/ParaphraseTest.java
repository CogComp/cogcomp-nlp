package edu.illinois.cs.cogcomp.mrcs;

import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by sling3 on 4/12/17.
 */
public class ParaphraseTest {

    public class SentencePair {

        public String s1;
        public String s2;
        public String id;
        public double score;

        public SentencePair(String s1, String s2, String id, String score){
            this.s1=s1;
            this.s2=s2;
            this.id=id;
            this.score=Double.parseDouble(score);
        }
    }


    public HashMap<SentencePair,Double> score=new HashMap();
    public static void main(String[] args) {
        try {
            new ParaphraseTest().load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        LlmStringComparator llm = new LlmStringComparator();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/shared/bronte/sling3/data/SICK_test_annotated.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {

                line = line.trim();
                if(line.length() > 0) {
                    String[] arr = line.split("\t");
                    SentencePair s = new SentencePair(arr[1], arr[2], arr[0], arr[3]);
                    double sc = llm.compareStrings(arr[1], arr[2]);
                    score.put(s, sc);
                    //System.out.println(sc);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double[] d1=new double[score.size()];
        double[] d2=new double[score.size()];
        int i=0;
        for(SentencePair s: score.keySet()){
            d1[i]=score.get(s);
            d2[i]=s.score;
            i++;
        }
        System.out.println("pscore: "+new PearsonsCorrelation().correlation(d1,d2));

    }
}
