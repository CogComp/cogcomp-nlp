/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

import java.util.ArrayList;
import java.lang.StringBuffer;
import java.util.HashMap;
import java.util.*;

public class MASCevaluationResult {

    private String source;
    private ArrayList<String[]> results;
    private ArrayList<Integer> errors;
    private int testDataIdentifier;
    private int correct;

    MASCevaluationResult(String source, int hashCode) {
        this.source = source;
        this.results = new ArrayList<String[]>();
        this.errors = new ArrayList<Integer>();
        this.correct = 0;
        this.testDataIdentifier = hashCode;
    }

    public int getCorrect() {
        return correct;
    }

    public int getTotal() {
        return results.size();
    }

    public double getAccuracy() {
        return (double) (this.correct) / (double) (this.results.size());
    }

    public ArrayList<String[]> getErrors() {
        ArrayList<String[]> allErrors = new ArrayList<String[]>();
        for (Integer error : errors) {
            String[] temp = results.get(error);
            allErrors.add(temp);
        }
        return allErrors;
    }

    public void addTestItem(String[] testData, String result) {
        String[] newResult = new String[testData.length + 1];
        System.arraycopy(testData, 0, newResult, 0, testData.length);
        newResult[newResult.length - 1] = result;
        results.add(newResult);
        if (result.equals(testData[testData.length - 1])) {
            correct++;
        } else {
            errors.add(results.size() - 1);
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("==============================================================\n");
        buffer.append("Source:  ").append(source.substring(source.lastIndexOf('/') + 1))
                .append("\n");
        buffer.append("Id:      ").append(this.testDataIdentifier).append("\n");
        buffer.append("Correct: ").append(this.correct).append("\n");
        buffer.append("Total:   ").append(this.results.size()).append("\n");
        buffer.append("Accuracy:")
                .append((double) (this.correct) / (double) (this.results.size()) * 100)
                .append("%\n");
        buffer.append("Errors: \n");
        buffer.append(String.format("%20s%5s%20s%20s\n", "RAW", "POS", "BASE", "RESULT"));
        for (Integer error : errors) {
            String[] temp = results.get(error);
            buffer.append(String.format("%20s%5s%20s%20s\n", temp[0], temp[1], temp[2], temp[3]));
        }
        buffer.append("==============================================================\n");
        return buffer.toString();

    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1))
                        .getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Object aList : list) {
            Map.Entry entry = (Map.Entry) aList;
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public static String getOverallAnalytics(MASCevaluationResult[] input) {
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        HashMap<String, Integer> count = new HashMap<String, Integer>();
        int total = 0;
        int correct = 0;
        for (MASCevaluationResult anInput : input) {
            total += anInput.getTotal();
            correct += anInput.getCorrect();
            ArrayList<String[]> allErrors = anInput.getErrors();
            for (String[] allError : allErrors) {
                String key = allError[0] + ":" + allError[1];
                if (map.containsKey(key)) {
                    count.put(key, count.get(key) + 1);
                } else {
                    count.put(key, 1);
                    map.put(key, allError);
                }
            }
        }
        count = sortByValues(count);
        StringBuffer buffer = new StringBuffer();
        buffer.append("==============================================================\n");
        buffer.append("Overall Accuracy:").append((double) (correct) / (double) (total) * 100)
                .append("%\n");
        buffer.append("Error Analytics: \n");
        buffer.append(String.format("%20s%5s%20s%20s%20s\n", "RAW", "POS", "BASE", "RESULT",
                "COUNT"));

        Set set = count.entrySet();
        for (Object aSet : set) {
            Map.Entry me = (Map.Entry) aSet;
            String key = (String) me.getKey();
            int counter = (Integer) me.getValue();
            String[] temp = map.get(key);
            buffer.append(String.format("%20s%5s%20s%20s%20d\n", temp[0], temp[1], temp[2],
                    temp[3], counter));
        }

        buffer.append("==============================================================\n");
        return buffer.toString();
    }
}
