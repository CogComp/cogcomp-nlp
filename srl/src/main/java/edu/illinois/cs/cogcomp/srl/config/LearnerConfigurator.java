/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

/**
 * A configuration helper to allow centralization of config options in dependencies with
 *    clear default settings (only values that override defaults need to be specified).
 *
 * Created by mssammon on 12/21/15.
 */
public class LearnerConfigurator extends Configurator {


    /**
     *     Available learning models: {L2LossSSVM, StructuredPerceptron}
     */
    public static final Property LEARNING_MODEL = new Property( "LEARNING_MODEL", "L2LossSSVM" );

    /**
     *     Available solver types: {DCDSolver, ParallelDCDSolver, DEMIParallelDCDSolver}
     */
    public static final Property L2_LOS_SSVM_SOLVER_TYPE = new Property( "L2_LOSS_SSVM_SOLVER_TYPE", "ParallelDCDSolver" );


    public static final Property NUM_THREADS = new Property( "NUMBER_OF_THREADS", "8");

    /**
     * Regularization parameter
     */
     public static final Property REGULARIZATION = new Property( "C_FOR_STRUCTURE", "1.0" );


/**
 * Mini-bat ch for 'warm' start
 */
    public static final Property USE_TRAINMINI = new Property( "TRAINMINI", TRUE );
    public static final Property TRAINMINI_SIZE = new Property( "TRAINMINI_SIZE", "10000" );


/**
 *  Suppress optimatility check
 */
    public static final Property CHECK_INFERENCE_OPT = new Property( "CHECK_INFERENCE_OPT", FALSE );

/**
 * Number of training rounds
  */

    public static final Property MAX_NUM_ITER = new Property( "MAX_NUM_ITER", "100" );



    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = {LEARNING_MODEL, L2_LOS_SSVM_SOLVER_TYPE, NUM_THREADS, REGULARIZATION, USE_TRAINMINI,
                TRAINMINI_SIZE, CHECK_INFERENCE_OPT, MAX_NUM_ITER };

        return new ResourceManager( generateProperties( properties ));
    }
}
