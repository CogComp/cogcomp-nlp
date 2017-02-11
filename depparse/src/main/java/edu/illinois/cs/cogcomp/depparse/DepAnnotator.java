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
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class DepAnnotator extends Annotator {
    private SLModel model = null;
    private String modelName = "struc-perceptron-ipm.model";
    private String tempModelFileName = "tmp345673.model";

    public DepAnnotator() {
        super(ViewNames.DEPENDENCY, new String[] {ViewNames.POS, ViewNames.SHALLOW_PARSE,
                ViewNames.LEMMA});
    }

    @Override
    public void initialize(ResourceManager rm) {
        try {
            // TODO Ridiculously ugly hack since SL doesn't accept streams we can't create a file
            // from inside a jar
            File dest = new File(tempModelFileName);
            URL fileURL = IOUtils.lsResources(DepAnnotator.class, modelName).get(0);
            FileUtils.copyURLToFile(fileURL, dest);
            model = SLModel.loadModel(tempModelFileName);
            if (!dest.delete())
                throw new IOException("Could not delete temporary model file " + tempModelFileName);
        } catch (IOException | ClassNotFoundException | URISyntaxException e) {
            e.printStackTrace();
            File dest = new File(tempModelFileName);
            if (!dest.delete())
                throw new RuntimeException("Could not delete temporary model file "
                        + tempModelFileName);
        }
    }

    @Override
    public void addView(TextAnnotation record) throws AnnotatorException {
        for (String reqView : requiredViews)
            if (!record.hasView(reqView))
                throw new AnnotatorException("TextAnnotation must have view: " + reqView);

        DepInst sent = new DepInst(record);
        DepStruct deptree;
        try {
            deptree = (DepStruct) model.infSolver.getBestStructure(model.wv, sent);
        } catch (Exception e) {
            throw new AnnotatorException("Sentence cannot be parsed");
        }

        TreeView treeView = new TreeView(ViewNames.DEPENDENCY, record);
        Pair<String, Integer> nodePair = new Pair<>(sent.forms[0], 0);
        Tree<Pair<String, Integer>> tree = new Tree<>(nodePair);
        populateChildren(tree, deptree, sent, 0);
        treeView.setDependencyTree(0, tree);
        record.addView(ViewNames.DEPENDENCY, treeView);
    }

    private void populateChildren(Tree<Pair<String, Integer>> tree, DepStruct struct, DepInst sent,
            int pos) {
        for (int i : struct.deps.get(pos)) {
            Pair<String, Integer> nodePair = new Pair<>(sent.forms[i], i);
            Tree<Pair<String, Integer>> childTree = new Tree<>(nodePair);
            tree.addSubtree(childTree, new Pair<>(struct.deprels[i], -1));
            populateChildren(childTree, struct, sent, i);
        }
    }
}
