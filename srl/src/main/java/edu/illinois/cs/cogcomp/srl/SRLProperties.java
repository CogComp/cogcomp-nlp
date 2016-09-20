/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.infer.ilp.ILPSolverFactory;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.data.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import java.io.IOException;
import java.util.Properties;

public class SRLProperties {
	private static final Logger log = LoggerFactory.getLogger(SRLProperties.class);
	private static final String NAME = SRLProperties.class.getCanonicalName();
	private static SRLProperties theInstance;
	private ResourceManager config;

    /**
     * configFile must have all parameters set, ideally using the SrlConfigurator class.
     * @param configFile file with configuration parameters
     */

    private SRLProperties( String configFile ) throws IOException {
        this(new ResourceManager(configFile));
    }

    /**
     * ResourceManager must have all parameters set, ideally using the SrlConfigurator class.
     * @param rm
     */
    private SRLProperties( ResourceManager rm ) {
        config = rm;
    }

    /**
     * If SRLProperties has not yet been instantiated, initialize an instance with default values.
     * @throws Exception
     */
    public static void initialize() throws Exception {
        initialize( new ResourceManager( new Properties()) );
    }

    /**
     *  If SRLProperties has not yet been instantiated, initialize
     *    new instance with the non-default parameters specified in the configFile named in the argument,
     *    and default parameters otherwise. If a null string is given as the argument, all default parameters
     *    are used.
     * @param configFile  if non-null, names a file in which non-default parameters are specified.
     * @throws Exception
     */
	public static void initialize(String configFile) throws Exception {
        initialize(new ResourceManager(configFile));
    }

    public static void initialize( ResourceManager rm )
    {
		log.info( "## initializing {}.", NAME );
		ResourceManager fullRm = new SrlConfigurator().getConfig(rm);
		theInstance = new SRLProperties(fullRm);
	}


    public static SRLProperties getInstance( ResourceManager rm )
    {
        if ( theInstance == null )
            try {
                initialize( rm );
            } catch (Exception e) {
                e.printStackTrace();
            }
        return theInstance;
    }

	public static SRLProperties getInstance() {
		if (theInstance == null) {
			System.out.println("SRL config not initialized. Instantiating with default parameters.");
			try {
				initialize();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return theInstance;
	}

	public ResourceManager getConfig() {
		return config;
	}

	public boolean useCurator() {
		return config.getBoolean("UseCurator");
	}

	String getFeatureCacheFile(SRLType SRLType, Models type, String featureSet, String parser, Dataset dataset) {
		return this.config.getString("CacheDirectory") + "/features."
				+ SRLType.name() + "." + type.name() + "." + featureSet + "."
				+ dataset + "." + parser + ".cache";
	}

	String getPrunedFeatureCacheFile(SRLType SRLType, Models type, String featureSet, String parser) {
		return this.config.getString("CacheDirectory") + "/features."
				+ SRLType.name() + "." + type.name() + "." + featureSet + "."
				+ parser + ".pruned.cache";
	}

	public String getPennTreebankHome() {
		return config.getString("PennTreebankHome");
	}

	public int getNumFeatExtThreads(){ return config.getInt("NumFeatExtThreads");}

	public String[] getAllTrainSections() {
		return new String[] { "02", "03", "04", "05", "06", "07", "08", "09",
				"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
				"20", "21", "22" };
	}

	public String getTestSections() {
		return "23";
	}

	public String[] getAllSections() {
		return new String[] { "02", "03", "04", "05", "06", "07", "08", "09",
				"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
				"20", "21", "22", "24", "23" };

	}

	public String[] getTrainDevSections() {
		return new String[] { "02", "03", "04", "05", "06", "07", "08",
				"09", "10", "11", "12", "13", "14", "15", "16", "17", "18",
				"19", "20", "21", "22", "24" };
	}

	public String[] getDevSections() {
		return new String[] { "24" };
	}

	public String getPropbankHome() {
		return config.getString("PropbankHome");
	}

	public String getNombankHome() {
		return config.getString("NombankHome");
	}

	public String getSentenceDBFile() {
		return this.config.getString("CacheDirectory") + "/sentences.db";
	}

	public String getDefaultParser() {
		return config.getString("DefaultParser");
	}

	public String getModelsDir() {
		return config.getString("ModelsDirectory");
	}

	public String getOutputDir() {
		if (config.containsKey("OutputDirectory"))
			return config.getString("OutputDirectory");
		return null;
	}

	public String getSRLVersion() {
		return Constants.systemVersion;
	}

    public String getLearnerConfig() {
        return this.config.getString("LearnerConfig");
    }

	public String getPipelineConfig() {
        return this.config.getString("PipelineConfig");
    }

	public ILPSolverFactory.SolverType getILPSolverType(boolean isEvaluating) {
		String solver = config.getString("ILPSolver");
		switch (solver) {
			case "Gurobi":
				if (isEvaluating)
					return ILPSolverFactory.SolverType.JLISCuttingPlaneGurobi;
				else return ILPSolverFactory.SolverType.Gurobi;
			case "OJAlgo":
				return ILPSolverFactory.SolverType.OJAlgo;
			default:
				log.info("Using default ILP Solver: OJAlgo");
				return ILPSolverFactory.SolverType.OJAlgo;
		}
	}
}
