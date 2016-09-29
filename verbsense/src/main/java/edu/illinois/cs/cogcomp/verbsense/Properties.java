package edu.illinois.cs.cogcomp.verbsense;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.verbsense.data.Dataset;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;

public class Properties {
	private static final Logger log = LoggerFactory.getLogger(Properties.class);
	private static Properties theInstance;
	private PropertiesConfiguration config;
	private final String curatorHost;
	private final int curatorPort;
	private final String wordNetFile;

	private Properties(URL url) throws ConfigurationException {
		config = new PropertiesConfiguration(url);

		curatorHost = config.getString("CuratorHost", "");
		curatorPort = config.getInt("CuratorPort", -1);

		this.wordNetFile = config.getString("WordNetConfig");

		if (config.containsKey("LoadWordNetConfigFromClassPath")
				&& config.getBoolean("LoadWordNetConfigFromClassPath")) {
			WordNetManager.loadConfigAsClasspathResource(true);
		}
	}

	public static void initialize(String configFile) throws Exception {
		// first try to load the file from the file system
		URL url = null;
		if (IOUtils.exists(configFile)) {
			url = (new File(configFile)).toURI().toURL();
		}
		else {
			List<URL> list = IOUtils.lsResources(Properties.class, configFile);
			if (list.size() > 0)
				url = list.get(0);
		}

		if (url == null) {
			log.error("Cannot find configuration file at {}.", configFile);
			throw new Exception("Cannot find configuration file.");
		}

		theInstance = new Properties(url);
	}

	public static Properties getInstance() {

		if (theInstance == null) {
			System.out.println("SRL config not initialized. Loading verbsense-config.properties from the classpath");
			try {
				initialize("verbsense-config.properties");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return theInstance;
	}

	public boolean useCurator() {
		return config.getBoolean("UseCurator");
	}

	public String getWordNetFile() {
		return wordNetFile;
	}

	String getFeatureCacheFile(String featureSet, Dataset dataset) {
		return this.config.getString("CacheDirectory") + "/features." + featureSet + "." + dataset + ".cache";
	}

	String getPrunedFeatureCacheFile(String featureSet) {
		return this.config.getString("CacheDirectory") + "/features." + featureSet + ".pruned.cache";
	}

	public String getPennTreebankHome() {
		return config.getString("PennTreebankHome");
	}

	public String[] getAllTrainSections() {
		return new String[] { "02", "03", "04", "05", "06", "07", "08", "09",
				"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
				"20", "21", "22", "24" };
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
				"19", "20", "21", "22" };
	}

	public String[] getDevSections() {
		return new String[] { "24" };
	}

	public String getPropbankHome() {
		return config.getString("PropbankHome");
	}

	public String getSentenceDBFile() {
		return this.config.getString("CacheDirectory") + "/sentences.db";
	}

	public String getModelsDir() {
		return config.getString("ModelsDirectory");
	}

	public String getPipelineConfigFile() {
		return config.getString("PipelineConfigFile");
	}

	public String getCuratorHost() {
		return curatorHost;
	}

	public int getCuratorPort() {
		return curatorPort;
	}
}
