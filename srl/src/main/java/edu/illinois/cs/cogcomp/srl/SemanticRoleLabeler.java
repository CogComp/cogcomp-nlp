/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.experiment.TextPreProcessor;
import edu.illinois.cs.cogcomp.srl.inference.SRLILPInference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SemanticRoleLabeler extends Annotator {
    private final static Logger log = LoggerFactory.getLogger(SemanticRoleLabeler.class);
    public SRLManager manager;
    private static SRLProperties properties;


    public static void main(String[] arguments) {
        if (arguments.length < 1) {
            System.err.println("Usage: <config-file> [Verb | Nom]");
            System.exit(-1);
        }
        String configFile = arguments[0];
        ResourceManager rm = null;
        try {
            rm = new SrlConfigurator().getConfig(new ResourceManager(configFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        String srlType;
        // If no second argument is provided it means we need all the SRL types
        srlType = arguments.length == 1 ? null : arguments[1];

        String input;
        List<SemanticRoleLabeler> srlLabelers = new ArrayList<>();

        Properties props = new Properties();
        props.setProperty(SrlConfigurator.SRL_TYPE.key, SRLType.Verb.name());
        props.setProperty(SrlConfigurator.INSTANTIATE_PREPROCESSOR.key, SrlConfigurator.TRUE);
        SrlConfigurator.mergeProperties(rm, new ResourceManager(props));

        try {
            if (srlType != null)
                srlLabelers.add(new SemanticRoleLabeler(rm));
            else {
                for (SRLType type : SRLType.values()) {
                    // srlType = type.name();
                    props.setProperty(SrlConfigurator.SRL_TYPE.key, type.name());
                    SrlConfigurator.mergeProperties(rm, new ResourceManager(props));

                    srlLabelers.add(new SemanticRoleLabeler(rm));
                }
            }
        } catch (Exception e) {
            log.error("Unable to initialize SemanticRoleLabeler:");
            e.printStackTrace();
            System.exit(-1);
        }

        do {
            System.out.print("Enter text (underscore to quit): ");
            input = System.console().readLine().trim();
            if (input.equals("_"))
                return;

            if (!input.isEmpty()) {
                // XXX Assuming that all SRL types require the same views
                TextAnnotation ta;
                try {
                    ta = TextPreProcessor.getInstance().preProcessText(input);
                } catch (Exception e) {
                    log.error("Unable to pre-process the text:");
                    e.printStackTrace();
                    continue;
                }

                for (SemanticRoleLabeler srl : srlLabelers) {
                    if (srlLabelers.size() > 1)
                        System.out.println(srl.getViewName());

                    PredicateArgumentView p;
                    try {
                        p = srl.getSRL(ta);
                    } catch (Exception e) {
                        log.error("Unable to produce SRL annotation:");
                        e.printStackTrace();
                        continue;
                    }

                    System.out.println(p);
                    System.out.println();
                }
            }
        } while (!input.equals("_"));
    }

    /**
     * default loads Verb srl
     * 
     * @throws Exception
     */
    public SemanticRoleLabeler() throws Exception {
        this(new ResourceManager(new Properties()));
    }

    public SemanticRoleLabeler(ResourceManager rm) throws Exception {
        this(rm, false);
    }

    /**
     * @param rm fully populated configuration (all fields from SrlConfigurator must be set)
     * @param lazilyInitialize if 'true', defer loading resources until getView() is called
     * @throws Exception
     */
    public SemanticRoleLabeler(ResourceManager rm, boolean lazilyInitialize) throws Exception {
        this(new SrlConfigurator().getConfig(rm), lazilyInitialize, false);
    }

    /**
     * protected because ResourceManager argument must have all entries in SrlConfigurator set
     * before super() is called.
     * 
     * @param config fully populated configuration (all fields from SrlConfigurator must be set)
     * @param lazilyInitialize if 'true', defer loading resources until getView() is called
     * @param irrelevantFlag a spurious argument to distinguish this protected constructor
     */
    protected SemanticRoleLabeler(ResourceManager config, boolean lazilyInitialize,
            boolean irrelevantFlag) {
        super(getViewNameForType(config.getString(SrlConfigurator.SRL_TYPE.key)),
                TextPreProcessor.requiredViews, lazilyInitialize, config);
    }

    // public SemanticRoleLabeler(ResourceManager rm, String srlType, boolean isLazilyInitialized )
    // throws Exception {
    // super(getViewNameForType(srlType), TextPreProcessor.requiredViews, isLazilyInitialized, new
    // SrlConfigurator().getConfig(rm));
    // }

    @Override
    public void initialize(ResourceManager rm) {
        SRLProperties.initialize(rm);
        properties = SRLProperties.getInstance();
        boolean initialize = rm.getBoolean(SrlConfigurator.INSTANTIATE_PREPROCESSOR.key);

        if (initialize) {
            TextPreProcessor.initialize(properties);
        }

        String srlType = rm.getString(SrlConfigurator.SRL_TYPE);
        try {
            manager = Main.getManager(SRLType.valueOf(srlType), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // log.info("Loading models");
        try {
            loadModels();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getViewNameForType(String srlType) {
        if (srlType.equals(SRLType.Verb.name()))
            return ViewNames.SRL_VERB;
        else if (srlType.equals(SRLType.Nom.name()))
            return ViewNames.SRL_NOM;
        else
            throw new IllegalArgumentException("ERROR: type '" + srlType + "' not recognized.");
    }

    public String getSRLCuratorName() {
        return manager.getSRLSystemIdentifier();
    }

    public String getVersion() {
        return properties.getSRLVersion();
    }

    private void loadModels() throws Exception {
        for (Models m : Models.values()) {
            if (manager.getSRLType() == SRLType.Verb && m == Models.Predicate)
                continue;

            log.info("Loading model {}", m);
            manager.getModelInfo(m).loadWeightVector();
        }

        log.info("Finished loading all models");
    }

    public PredicateArgumentView getSRL(TextAnnotation ta) throws Exception {
        log.debug("Input: {}", ta.getText());

        List<Constituent> predicates;
        if (manager.getSRLType() == SRLType.Verb)
            predicates = manager.getHeuristicPredicateDetector().getPredicates(ta);
        else
            predicates = manager.getLearnedPredicateDetector().getPredicates(ta);

        if (predicates.isEmpty())
            return null;
        ILPSolverFactory s = new ILPSolverFactory(properties.getILPSolverType(false));
        SRLILPInference inference = new SRLILPInference(s, manager, predicates);

        return inference.getOutputView();
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        // Check if all required views are present

        if (ta.hasView(ViewNames.PARSE_STANFORD) && !ta.hasView(ViewNames.CLAUSES_STANFORD))
            ta.addView(ClauseViewGenerator.STANFORD);

        for (String view : getRequiredViews()) {
            if (!ta.hasView(view)) {
                throw new AnnotatorException("Missing required view: " + view);
            }
        }

        try {
            View srlView = getSRL(ta);
            ta.addView(getViewName(), srlView);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AnnotatorException(e.getMessage());
        }
    }

    @Override
    public String getViewName() {
        if (manager.getSRLType() == SRLType.Verb) {
            return ViewNames.SRL_VERB;
        } else if (manager.getSRLType() == SRLType.Nom)
            return ViewNames.SRL_NOM;
        return null;
    }

}
