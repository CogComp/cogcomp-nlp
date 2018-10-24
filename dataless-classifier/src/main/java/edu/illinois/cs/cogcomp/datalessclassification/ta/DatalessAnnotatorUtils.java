/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.ta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Collection of utility functions
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

class DatalessAnnotatorUtils {

    private static Logger logger = LoggerFactory.getLogger(DatalessAnnotatorUtils.class);

    /**
     * Reads the "labelID - labelName" mapping from the file
     */
    static Map<String, String> getLabelNameMap(String filePath) {
        Map<String, String> labelNameMap = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                String[] tokens = line.split("\t", 2);

                String labelId = tokens[0].trim();
                String labelName = tokens[1].trim();

                labelNameMap.put(labelId, labelName);
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the file at " + filePath);
            throw new RuntimeException("IO Error while reading the file at " + filePath);
        }

        return labelNameMap;
    }

    /**
     * Reads the "labelID - labelDescription" mapping from the file
     */
    static Map<String, String> getLabelDescriptionMap(String filePath) {
        Map<String, String> labelDesriptionMap = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                String[] tokens = line.split("\t", 2);

                String labelId = tokens[0].trim();
                String labelDesc = tokens[1].trim();

                labelDesriptionMap.put(labelId, labelDesc);
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the file at " + filePath);
            throw new RuntimeException("IO Error while reading the file at " + filePath);
        }

        return labelDesriptionMap;
    }

    /**
     * Reads the top-level nodes from the hierarchy file
     */
    static Set<String> getTopNodes(String hierarchyPath) {
        Set<String> topNodes = new HashSet<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(hierarchyPath))) {
            String line = reader.readLine();

            String[] nodes = line.split("\t");

            topNodes.addAll(Arrays.asList(nodes));

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the file at " + hierarchyPath);
            throw new RuntimeException("IO Error while reading the file at " + hierarchyPath);
        }

        return topNodes;
    }

    /**
     * Reads the "parentNode - childNodes" mapping from the hierarchy file
     */
    static Map<String, Set<String>> getParentChildMap(String hierarchyPath) {
        Map<String, Set<String>> parentChildMap = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(hierarchyPath))) {
            reader.readLine();

            String line;
            String[] nodes;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                String[] tokens = line.split("\t", 2);

                String parentID = tokens[0].trim();
                String childIDs = tokens[1].trim();
                Set<String> childIDSet = new HashSet<>();

                nodes = childIDs.split("\t");

                childIDSet.addAll(Arrays.asList(nodes));

                parentChildMap.put(parentID, childIDSet);
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error while reading the file at " + hierarchyPath);
            throw new RuntimeException("IO Error while reading the file at " + hierarchyPath);
        }

        return parentChildMap;
    }
}
