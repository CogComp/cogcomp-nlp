/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.depparse.core.DepInst;
import edu.illinois.cs.cogcomp.depparse.core.DepStruct;
import edu.illinois.cs.cogcomp.depparse.core.LabeledChuLiuEdmondsDecoder;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class DepAnnotator extends Annotator {
    private static Logger logger = LoggerFactory.getLogger(DepAnnotator.class);
    private static SLModel model = null;
    private static final String TEMP_MODEL_FILE_NAME = "tmp345673.model";

    /**
     * default: don't use lazy initialization
     */
    public DepAnnotator() {
        this(false);
    }

    /**
     * Constructor parameter allows user to specify whether or not to lazily initialize.
     *
     * @param lazilyInitialize If set to 'true', models will not be loaded until first call
     *        requiring Chunker annotation.
     */
    public DepAnnotator(boolean lazilyInitialize) {
        this(lazilyInitialize, new DepConfigurator().getDefaultConfig());
    }

    public DepAnnotator(boolean lazilyInitialize, ResourceManager rm) {
        super(ViewNames.DEPENDENCY, new String[] {ViewNames.POS, ViewNames.SHALLOW_PARSE,
                ViewNames.LEMMA}, lazilyInitialize, new DepConfigurator().getConfig(rm));
    }

    @Override
    public void initialize(ResourceManager rm) {
        try {
            // TODO Ugly hack: SL doesn't accept streams and can't create a file from inside a jar
            File dest = new File(TEMP_MODEL_FILE_NAME);
            String modelName = rm.getString(DepConfigurator.MODEL_NAME.key);
            URL fileURL = IOUtils.lsResources(DepAnnotator.class, modelName).get(0);
            logger.info("Loading {} into temp file: {}", modelName, TEMP_MODEL_FILE_NAME);
            FileUtils.copyURLToFile(fileURL, dest);
            model = SLModel.loadModel(TEMP_MODEL_FILE_NAME);
            ((LabeledChuLiuEdmondsDecoder) model.infSolver).loadDepRelDict();
            if (!dest.delete())
                throw new IOException("Could not delete temporary model file "
                        + TEMP_MODEL_FILE_NAME);
        } catch (IOException | ClassNotFoundException | URISyntaxException e) {
            e.printStackTrace();
            File dest = new File(TEMP_MODEL_FILE_NAME);
            if (!dest.delete())
                throw new RuntimeException("Could not delete temporary model file "
                        + TEMP_MODEL_FILE_NAME);
        }
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        for (String reqView : requiredViews)
            if (!ta.hasView(reqView))
                throw new AnnotatorException("TextAnnotation must have view: " + reqView);

        DepInst sent = new DepInst(ta);
        DepStruct deptree;
        try {
            deptree = (DepStruct) model.infSolver.getBestStructure(model.wv, sent);
        } catch (Exception e) {
            throw new AnnotatorException("Sentence cannot be parsed");
        }

        TreeView treeView = new TreeView(ViewNames.DEPENDENCY, ta);
        int rootPos = findRoot(deptree);
        // All the node positions are -1 to account for the extra <root> node added
        Pair<String, Integer> nodePair = new Pair<>(sent.forms[rootPos], rootPos - 1);
        Tree<Pair<String, Integer>> tree = new Tree<>(nodePair);
        populateChildren(tree, deptree, sent, rootPos);
        treeView.setDependencyTree(0, tree);
        ta.addView(ViewNames.DEPENDENCY, treeView);
    }

    private void populateChildren(Tree<Pair<String, Integer>> tree, DepStruct struct, DepInst sent,
            int pos) {
        for (int i : struct.deps.get(pos)) {
            // All the node positions are -1 to account for the extra <root> node added
            Pair<String, Integer> nodePair = new Pair<>(sent.forms[i], i - 1);
            Tree<Pair<String, Integer>> childTree = new Tree<>(nodePair);
            tree.addSubtree(childTree, new Pair<>(struct.deprels[i], i - 1));
            populateChildren(childTree, struct, sent, i);
        }
    }

    private int findRoot(DepStruct struct) {
        for (int i = 1; i < struct.heads.length; i++) {
            if (struct.heads[i] == 0)
                return i;
        }
        return -1;
    }
}
