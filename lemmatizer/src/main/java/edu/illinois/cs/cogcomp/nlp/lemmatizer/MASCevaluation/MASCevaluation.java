/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

import java.util.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.w3c.dom.*;

public class MASCevaluation {

    private File[] pennFileList;
    private HashMap<String, ArrayList<String[]>> testData;
    private static Logger logger = LoggerFactory.getLogger(MASCevaluation.class);

    /**
     * Constructor
     * 
     * @param path to MASC/data/written Directory
     */
    public MASCevaluation(String path) {
        String MASCPath = path;
        try {
            pennFileList = this.searchAllFiles(new File(MASCPath));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        testData = new HashMap<String, ArrayList<String[]>>();
        this.constructTestingEnvironment();
        logger.info(String.valueOf(testData.size()));
    }

    /**
     * Get all files ending with -penn.xml
     * 
     * @param file - The directory/file to search from.
     */
    private File[] searchAllFiles(File file) {
        ArrayList<File> file_array = new ArrayList<File>();

        if (file.isDirectory()) {
            for (File temp : file.listFiles()) {
                Collections.addAll(file_array, searchAllFiles(temp));
            }
        } else {
            if (file.getAbsolutePath().endsWith("-penn.xml"))
                file_array.add(file);
        }

        File[] result = new File[file_array.size()];
        file_array.toArray(result);
        return result;
    }

    /**
     * Load testing data
     */
    private void constructTestingEnvironment() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        for (File file : this.pennFileList) {
            String filename = file.getAbsolutePath();

            // Init builder
            DocumentBuilder builder = null;
            try {
                builder = builderFactory.newDocumentBuilder();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ArrayList<String[]> dataLists = new ArrayList<String[]>();
            // Create data set for each file
            try {
                logger.info("Constructing testing data set for file " + file.getName());
                Document document = builder.parse(new FileInputStream(filename));
                NodeList nodes = document.getElementsByTagName("fs");
                for (int i = 0; i < nodes.getLength(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    Element ele = (Element) nodes.item(i);

                    NodeList subnodes = ele.getElementsByTagName("f");
                    boolean valid = true;
                    for (int j = 0; j < subnodes.getLength(); j++) {
                        String tag = ((Element) subnodes.item(j)).getAttribute("name");
                        // TODO: Add more filtering
                        if (Objects.equals(tag, "subkind") || Objects.equals(tag, "position")) {
                            valid = false;
                            break;
                        } else {
                            map.put(tag, ((Element) subnodes.item(j)).getAttribute("value"));
                        }
                    }
                    if (valid) {
                        String dataList[] = new String[3];
                        try {
                            dataList[0] = map.get("string").toLowerCase();
                            dataList[1] = map.get("msd");
                            dataList[2] = map.get("base").toLowerCase();
                            dataLists.add(dataList);
                        } catch (Exception ignored) {
                            // do nothing
                        }
                    }
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
            this.testData.put(filename, dataLists);
        }
    }

    /**
     * Process the evaluation
     * 
     * @param lemmatizer - An implementation of LemmatizerInterface
     */
    public MASCevaluationResult[] evaluation(LemmatizerInterface lemmatizer) {
        ArrayList<MASCevaluationResult> results = new ArrayList<MASCevaluationResult>();

        Set<String> keyset = this.testData.keySet();
        String[] keylist = new String[keyset.size()];
        keyset.toArray(keylist);

        for (String filename : keylist) {
            ArrayList<String[]> list = this.testData.get(filename);
            MASCevaluationResult result = new MASCevaluationResult(filename, list.hashCode());
            // logger.info(filename);
            for (int i = 0; i < list.size(); i++) {
                String[] temp = (String[]) list.get(i);
                result.addTestItem(temp, lemmatizer.getLemma(temp[0], temp[1]));
                // logger.info(lemmatizer.getLemma(temp[0], temp[1]));
            }
            results.add(result);
        }
        MASCevaluationResult[] resultList = new MASCevaluationResult[results.size()];
        results.toArray(resultList);
        return resultList;
    }

    public static LemmatizerInterface initLemmatizer(String type) {
        LemmatizerInterface lem = null;

        if (type.equalsIgnoreCase("illinois")) {
            lem = new IllinoisLemmatizerInterface();
        } else if (type.equalsIgnoreCase("stanford")) {
            lem = new StanfordLemmatizerInterface();
        } else if (type.equalsIgnoreCase("jwi")) {
            lem = new JWILemmatizerInterface();
        } else if (type.equalsIgnoreCase("morpha")) {
            lem = new MorphaLemmatizerInterface();
        } else {
            logger.info("Invalid lemmatizer type. Exiting.");
            System.exit(-1);
        }

        return lem;
    }

    public static void main(String[] args) {
        String masc = "";
        String output = "";
        String type = "";
        String analysis = "";
        if (args.length > 0) {
            masc = args[0];
            output = args[1];
            analysis = args[2];
            type = args[3];
        } else {
            // set defaults
            System.out.println("No arguments, using defaults.");
            masc = "MASC-3.0.0/data/written/";
            output = "output/new_illinois_result_wn5.txt";
            analysis = "analysis/new_illinois_result_wn5.txt";
            type = "illinois";
        }

        MASCevaluation m = new MASCevaluation(masc);
        LemmatizerInterface lem = initLemmatizer(type);

        lem.initLemmatizer();
        MASCevaluationResult[] lem_result = m.evaluation(lem);

        try {
            File file = new File(analysis);
            FileWriter writer = new FileWriter(file, false);
            PrintWriter out = new PrintWriter(writer);
            out.println(MASCevaluationResult.getOverallAnalytics(lem_result));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File file = new File(output);
            FileWriter writer = new FileWriter(file, false);
            PrintWriter out = new PrintWriter(writer);
            int total = 0;
            int correct = 0;
            for (MASCevaluationResult aLem_result1 : lem_result) {
                total += aLem_result1.getTotal();
                correct += aLem_result1.getCorrect();
            }
            out.printf("Overall Accuracy: %f %%\n", (double) correct / (double) total * 100);
            for (MASCevaluationResult aLem_result : lem_result)
                out.println(aLem_result.toString());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
