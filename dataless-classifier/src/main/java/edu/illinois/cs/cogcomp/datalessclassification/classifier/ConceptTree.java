/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.datalessclassification.representation.AEmbedding;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVectorOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ConceptTree has the same link structure as a {@link LabelTree}, with the addition that
 * it contains vector representations for each node (labelID).
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class ConceptTree<T extends Serializable> extends AClassifierTree<ConceptTreeNode<T>> {

    private static Logger logger = LoggerFactory.getLogger(ConceptTree.class);

    private static final long serialVersionUID = 1L;

    private final static String NIL = "NIL";

    private transient Map<T, Double> globalConceptWeights;

    private transient AEmbedding<T> embedding;

    protected transient int numConcepts;

    public ConceptTree(LabelTree labelTree) {
        super(labelTree);
    }

    public ConceptTree(LabelTree labelTree, AEmbedding<T> embedding, Map<T, Double> conceptWeights) {
        this(labelTree, embedding, conceptWeights, 500);
    }

    public ConceptTree(LabelTree labelTree, AEmbedding<T> embedding, Map<T, Double> conceptWeights,
            int embeddingSize) {
        super(labelTree);
        this.embedding = embedding;
        this.globalConceptWeights = conceptWeights;
        this.numConcepts = embeddingSize;

        initializeTree();
    }

    /**
     * Generates and Returns a ConceptTree using the provided LabelTree, and the
     *  LabelID -> Embeddings Map
     */
    public static ConceptTree<Integer> generateDenseEmbeddedTreeFromLabelEmbeddingMap(
            LabelTree labelTree, Map<String, SparseVector<Integer>> labelEmbeddings) {
        ConceptTree<Integer> conceptTree = new ConceptTree<>(labelTree);

        for (ConceptTreeNode<Integer> node : conceptTree.getNodes()) {
            String labelID = node.getLabelID();
            node.setLabelDescription(labelTree.getLabelDescription(labelID));

            SparseVector<Integer> conceptVector = labelEmbeddings.get(labelID);
            node.setConceptVector(conceptVector);
        }

        return conceptTree;
    }

    /**
     * Generates and Returns a ConceptTree using the provided LabelTree, and the
     *  File containing the String representation of the LabelID -> Embeddings Map
     */
    public static ConceptTree<Integer> generateDenseEmbeddedTreeFromFile(
            LabelTree labelTree, String repFile) {
        logger.info("Reading Label Embeddings from " + repFile);
        File inputFile = new File(repFile);

        Map<String, SparseVector<Integer>> labelEmbeddings = new HashMap<>();

        try(BufferedReader bf = new BufferedReader(new FileReader(inputFile))) {
            String line;

            while ((line = bf.readLine()) != null) {
                line = line.trim();

                if (line.length() == 0)
                    continue;

                String[] tokens = line.trim().split("\t", 2);
                String[] stringVec = tokens[1].split(" ");

                String label = tokens[0].trim();

                if (label.length() == 0)
                    continue;

                Map<Integer, Double> scores = new HashMap<>();

                int i = 0;

                for (String dim : stringVec) {
                    scores.put(i, Double.parseDouble(dim));
                    i++;
                }

                SparseVector<Integer> vec = new SparseVector<>(scores);

                labelEmbeddings.put(label, vec);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("File not found at " + repFile);
            throw new RuntimeException("File not found at " + repFile);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error while reading file");
            throw new RuntimeException("Error while reading file");
        }

        ConceptTree<Integer> conceptTree =
                generateDenseEmbeddedTreeFromLabelEmbeddingMap(labelTree, labelEmbeddings);
        return conceptTree;
    }

    /**
     * Copy Constructor
     */
    public ConceptTree(ConceptTree<T> thatTree) {
        super(new LabelTree(thatTree.getLabelTree()));

        for (ConceptTreeNode<T> node : getNodes()) {
            if (isRoot(node))
                continue;

            String labelID = node.getLabelID();
            ConceptTreeNode<T> thatNode = thatTree.getNodeFromLabel(labelID);

            String description = thatNode.getLabelDescription();
            node.setLabelDescription(description);

            SparseVector<T> vector = SparseVector.deepCopy(thatNode.getConceptVector());
            node.setConceptVector(vector);
        }
    }


    /**
     * Initializes the Root node of the tree
     */
    @Override
    protected void initializeRoot(String root_label) {
        ConceptTreeNode<T> root = ConceptTreeNode.makeBasicTypedNode(root_label);
        initializeRoot(root);
    }


    /**
     * Returns all the ChildNodes of a particular node (labelID)
     */
    @Override
    public Set<ConceptTreeNode<T>> getChildren(String label) {
        Set<ConceptTreeNode<T>> set =
                getChildren(ConceptTreeNode.makeBasicTypedNode(label));

        if (set == null)
            return null;

        if (set.isEmpty())
            return Collections.emptySet();

        Set<ConceptTreeNode<T>> newSet = new HashSet<>(set.size());

        newSet.addAll(set);

        return newSet;
    }


    /**
     * Returns the Parent Node of a particular node (labelID)
     */
    @Override
    public ConceptTreeNode<T> getParent(String label) {
        ConceptTreeNode<T> parent =
                getParent(ConceptTreeNode.makeBasicTypedNode(label));
        return parent;
    }


    /**
     * Adds an edge between a ParentNode and a ChildNode
     */
    @Override
    public boolean addEdge(String parent, String child) {
        ConceptTreeNode<T> parentNode = ConceptTreeNode.makeBasicTypedNode(parent);
        ConceptTreeNode<T> childNode = ConceptTreeNode.makeBasicTypedNode(child);

        return addEdge(parentNode, childNode);
    }

    /**
     * Returns the depth of a particular node (labelID)
     */
    public int getDepth(String label) {
        return getDepth(ConceptTreeNode.makeBasicTypedNode(label));
    }

    /**
     * Returns all the parent nodes of a particular node (labelID)
     */
    public List<ConceptTreeNode<T>> getAllParents(String label) {
        List<ConceptTreeNode<T>> parentNodes =
                getAllParents(ConceptTreeNode.makeBasicTypedNode(label));

        if (parentNodes == null)
            return null;

        List<ConceptTreeNode<T>> parents = new ArrayList<>(parentNodes.size());

        parents.addAll(parentNodes);

        return parentNodes;
    }

    /**
     * This function initializes the representations of the nodes using the LabelTree and the Embedding Objects
     * -- Uses the LabelTree as the Tree Structure, and
     * -- Uses the labelDescription of each node to get the corresponding vector representation
     */
    public void initializeTree() {
        for (ConceptTreeNode<T> node : getNodes()) {
            String labelID = node.getLabelID();
            String description = labelTree.getLabelDescription(labelID);

            node.setLabelDescription(description);

            SparseVector<T> concepts =
                    embedding.getVector(node.getLabelDescription(), numConcepts);
            concepts.updateNorm(globalConceptWeights);

            node.setConceptVector(concepts);
        }
    }

    /**
     * This Utility function takes multiple ConceptTrees as input, and returns a ConceptTree
     * that averages the representations at each node.
     */
    public static <T extends Serializable> ConceptTree<T> getAvgConceptTree(
            List<ConceptTree<T>> conceptTreeList) {
        ConceptTree<T> avgTree = new ConceptTree<>(conceptTreeList.get(0));

        for (ConceptTreeNode<T> node : avgTree.getNodes()) {
            if (avgTree.isRoot(node))
                continue;

            String currentLabelID = node.getLabelID();
            List<SparseVector<T>> vectors = new ArrayList<>();

            for (ConceptTree<T> tree : conceptTreeList) {
                vectors.add(tree.getNodeFromLabel(currentLabelID).getConceptVector());
            }

            SparseVector<T> avgVector = SparseVectorOperations.averageMultipleVectors(vectors);
            node.setConceptVector(avgVector);
        }

        return avgTree;
    }

    /**
     * This Utility function dumps a text representation of the tree to the disk.
     */
    public void dumpTreeAsString(String filePath) {
        try(FileWriter writer = new FileWriter(filePath)) {

            List<ConceptTreeNode<T>> nodeList = getBreadthOrderedNodeList();

            for (ConceptTreeNode<T> node : nodeList) {
                String parent;

                if (isRoot(node))
                    parent = NIL;
                else
                    parent = getParent(node).getLabelID();

                writer.write(parent + "\t" + node.getLabelID() + "\t" + node.getLabelDescription()
                        + "\t" + node.getConceptVector().toString() + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing to file at " + filePath);
            throw new RuntimeException("Error writing to file at " + filePath);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Reads and returns a serialized ConceptTree from a file
     */
    public static <K extends Serializable> ConceptTree<K> loadTree(String labelRepFile) {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(labelRepFile))) {
            ConceptTree<K> tree = (ConceptTree<K>) in.readObject();
            return tree;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("File not found at " + labelRepFile);
            throw new RuntimeException("File not found at " + labelRepFile);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error reading from file");
            throw new RuntimeException("Error reading from file");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("Error deserializing the ConceptTree");
            throw new RuntimeException("Error deserializing the ConceptTree");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        List<ConceptTreeNode<T>> nodes = getBreadthOrderedNodeList();

        for (ConceptTreeNode<T> node : nodes) {
            if (!isLeaf(node)) {
                for (ConceptTreeNode<T> child : getChildren(node)) {
                    sb.append(node.getLabelID()).append("\t");
                    sb.append(child).append("\n");
                }
            }
        }

        return sb.toString();
    }
}
