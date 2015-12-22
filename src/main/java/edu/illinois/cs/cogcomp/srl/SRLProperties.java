package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.srl.data.Dataset;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class SRLProperties {
	private static final Logger log = LoggerFactory.getLogger(SRLProperties.class);
	private static SRLProperties theInstance;
	private ResourceManager config;


    /**
     * if called with null url, will use default configuration.
     * @param url
     * @throws ConfigurationException
     */
	private SRLProperties(URL url) throws ConfigurationException {
//		config = new PropertiesConfiguration(url);
        this((null == url) ? null : url.getFile());
    }

    private SRLProperties( String configFile ) throws ConfigurationException {
        ResourceManager rm = new ResourceManager( new Properties() );
        if ( null != configFile )
            try {
                rm = new ResourceManager( configFile );
            } catch (IOException e) {
                e.printStackTrace();
                throw new ConfigurationException( e.getCause() );
            }

			config = new SrlConfigurator().getConfig( rm );
		if (config.containsKey("LoadWordNetConfigFromClassPath")
				&& config.getBoolean("LoadWordNetConfigFromClassPath")) {
			WordNetManager.loadConfigAsClasspathResource(true);
		}
    }

    /**
     * If SRLProperties has not yet been instantiated, initialize an instance with default values.
     * @throws Exception
     */
    public static void initialize() throws Exception {
        initialize( null );
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
		// first try to load the file from the file system
		URL url = null;
		if (IOUtils.exists(configFile)) {
			url = (new File(configFile)).toURI().toURL();
		}
		else {
			List<URL> list = IOUtils.lsResources(SRLProperties.class, configFile);
			if (list.size() > 0)
				url = list.get(0);
		}

		if (url == null) {
            if ( null != configFile )
            {
                log.error("Cannot find configuration file at {}.", configFile);
    			throw new Exception("Cannot find configuration file.");
            }
		}

		theInstance = new SRLProperties(url);
	}


    public static SRLProperties getInstance( String config )
    {
        if ( theInstance == null )
            try {
                initialize( config );
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
}
