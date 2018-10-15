/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.configuration;

import edu.illinois.cs.cogcomp.core.constants.CoreConfigNames;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

/**
 * @author mssammon, chanys
 * @author Christos Christodoulopoulos
 */
public class ResourceManager {
    private Properties properties;

    public ResourceManager(String configFile_) throws IOException {
        properties = new Properties();

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configFile_);

        if (is == null) {
            is = new FileInputStream(configFile_);
        }
        try {
            properties.load(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }

    }

    /**
     * construct a ResourceManager with a Properties object
     * 
     * @param props_ a Properties object with the properties set
     */
    public ResourceManager(Properties props_) {
        properties = props_;
    }


    /**
     * return the Properties object built by this ResourceManager
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * read a set of values from a file, expecting one value per line
     * 
     * @param resourceName file (may be on classpath)
     * @param myset set to populate
     * @param lowercase if 'true', lowercase the set elements on read
     * @param padSpace if 'true', add whitespace on each side of element read
     */
    public static void readSet(String resourceName, Set<String> myset, boolean lowercase,
            boolean padSpace) {
        try {
            InputStream is =
                    ResourceManager.class.getClassLoader().getResourceAsStream(resourceName);

            // perhaps it is a path, even though just the file name is fine.
            if (is == null) {
                is = new FileInputStream(resourceName);
            }

            // if it is STILL null, then give up.
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            try {
                if (lowercase) {
                    if (padSpace) {
                        while ((line = br.readLine()) != null)
                            myset.add(" " + line.toLowerCase() + " ");
                    } else {
                        while ((line = br.readLine()) != null)
                            myset.add(line.toLowerCase());
                    }
                } else {
                    if (padSpace) {
                        while ((line = br.readLine()) != null)
                            myset.add(" " + line + " ");
                    } else {
                        while ((line = br.readLine()) != null)
                            myset.add(line);
                    }
                }
                br.close();
            } catch (Exception e) {
                System.err.println(e + " Line:" + line);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Enumeration<Object> getKeys() {
        return properties.keys();
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public double getDouble(String propertyName_) {
        String value = getString(propertyName_);
        return Double.parseDouble(value);
    }

    public double getDouble(Property property) {
        return getDouble(property.key);
    }

    public boolean getBoolean(String propertyName_) {
        String value = getString(propertyName_);
        return Boolean.parseBoolean(value);
    }

    public boolean getBoolean(Property property) {
        return getBoolean(property.key);
    }

    public int getInt(String propertyName_) {
        String value = getString(propertyName_);
        return Integer.parseInt(value);
    }

    public int getInt(Property property) {
        return getInt(property.key);
    }

    public String getString(String propertyName_) {
        String value = properties.getProperty(propertyName_);

        if (null == value) {
            throw new IllegalArgumentException("ERROR: ResourceLoader.getString(): "
                    + "no entry for key '" + propertyName_ + "'.");
        }

        return value;
    }

    public String getString(Property property) {
        return getString(property.key);
    }

    public String[] getCommaSeparatedValues(String propertyName_) {
        String valueList = properties.getProperty(propertyName_);

        if (null == valueList) {
            throw new IllegalArgumentException("ERROR: ResourceLoader.getCommaSeparatedValues(): "
                    + "no entry for key '" + propertyName_ + "'.");
        }
        String[] values = new String[1];
        values[0] = valueList;

        if (valueList.contains(","))
            values = valueList.split(",");

        return values;
    }

    public String[] getCommaSeparatedValues(Property property) {
        return getCommaSeparatedValues(property.key);
    }


    /**
     * getters with default values -- won't throw exceptions
     */

    public double getDouble(String propertyName_, String defaultVal_) {
        String value = getString(propertyName_, defaultVal_);
        return Double.parseDouble(value);
    }


    public boolean getBoolean(String propertyName_, String defaultVal_) {
        String value = getString(propertyName_, defaultVal_);
        return Boolean.parseBoolean(value);
    }

    public int getInt(String propertyName_, String defaultVal_) {
        String value = getString(propertyName_, defaultVal_);
        return Integer.parseInt(value);
    }


    public String getString(String propertyName_, String defaultVal_) {
        String value = properties.getProperty(propertyName_);

        if (null == value)
            value = defaultVal_;

        return value;
    }

    /**
     * define some commonly used config value getters
     */

    public String getCuratorHost() {
        return getString(CoreConfigNames.CURATOR_HOST);
    }

    public int getCuratorPort() {
        return getInt(CoreConfigNames.CURATOR_PORT);
    }

    public boolean getCuratorForceUpdate() {
        return getBoolean(CoreConfigNames.CURATOR_FORCE_UPDATE);
    }

    public boolean getDebug() {
        return getBoolean(CoreConfigNames.DEBUG);
    }
}
