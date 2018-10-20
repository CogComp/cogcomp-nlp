/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.configuration;

import edu.illinois.cs.cogcomp.annotation.AnnotatorServiceConfigurator;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * a pattern to set default configuration options in a way that supports modular code, minimizes
 * size of config files, but which makes defaults very explicit. A lot of it is based on convention
 * -- defining public static members for config flags and their default values. It's OK for some
 * values to NOT have default values, in which case the constructor for the corresponding class will
 * either take a ResourceManager or a config file or explicit parameters -- ideally, the latter.
 *
 * Usage: - create class derived from Configurator, e.g. MyConfigurator. - specify flags and default
 * values using public static constants. - when instantiating the corresponding object, instantiate
 * a MyConfigurator object and either: - new MyNewClass( myConfigurator.getDefaultConfig() ) - new
 * MyNewClass( Configurator.mergeProperties( myConfigurator, anotherConfigurator ) );
 *
 * See {@link AnnotatorServiceConfigurator} for an example of a Configurator extension.
 *
 * Created by mssammon on 8/3/15.
 */

public abstract class Configurator {

    public static final String TRUE = Boolean.TRUE.toString();
    public static final String FALSE = Boolean.FALSE.toString();

    /**
     * combine two sets of properties to make a third Properties object; if both sets contain a
     * value for the same key, the value from the second ResourceManager is selected.
     *
     * @param first ResourceManager with first set of properties
     * @param second ResourceManager with second set of properties
     * @return a brand new ResourceManager with the union of the properties, favoring the second if
     *         the same property is set in both
     */
    public static ResourceManager mergeProperties(ResourceManager first, ResourceManager second) {
        Properties firstProps = first.getProperties();
        Properties secondProps = second.getProperties();

        Properties newProps = new Properties();

        for (String key : firstProps.stringPropertyNames())
            newProps.put(key, firstProps.getProperty(key));

        for (String key : secondProps.stringPropertyNames())
            newProps.put(key, secondProps.getProperty(key));

        return new ResourceManager(newProps);
    }

    /**
     * merge a list of ResourceManager objects
     *
     * @param rmList list of ResourceManager objects
     * @return single ResourceManager containing union of properties of objects in argument
     */
    public static ResourceManager mergeProperties(List<ResourceManager> rmList) {
        if (rmList.isEmpty())
            throw new IllegalArgumentException(
                    "ERROR: called with empty list of ResourceManager as argument.");

        ResourceManager finalRm = rmList.get(0);

        for (int i = 1; i < rmList.size(); ++i)
            finalRm = Configurator.mergeProperties(finalRm, rmList.get(i));

        return finalRm;
    }

    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    abstract public ResourceManager getDefaultConfig();

    /**
     * Creates the {@link java.util.Properties} that is passed on to {@link ResourceManager} from a
     * list of default {@link Property} entries.
     *
     * @param properties The list default {@link Property} entries
     * @return The {@link java.util.Properties} containing the defined properties
     */
    protected Properties generateProperties(Property[] properties) {
        Properties props = new Properties();
        for (Property property : properties)
            props.setProperty(property.key, property.value);
        return props;
    }

    /**
     * get a Properties object with default values except for those provided in the
     * 'nonDefaultValues' argument
     *
     * @param nonDefaultValues specify ONLY those values you wish to override
     * @return a {@link ResourceManager} containing the defined properties
     */
    public ResourceManager getConfig(Map<String, String> nonDefaultValues) {
        ResourceManager props = getDefaultConfig();
        Properties nonDefProps = new Properties();
        nonDefProps.putAll(nonDefaultValues);

        return mergeProperties(props, new ResourceManager(nonDefProps));
    }

    /**
     * get a Properties object with default values except for those provided in the
     * 'nonDefaultValues' argument
     *
     * @param nonDefaultRm specify ONLY those values you wish to override
     * @return a {@link ResourceManager} containing the defined properties
     */
    public ResourceManager getConfig(ResourceManager nonDefaultRm) {
        ResourceManager props = getDefaultConfig();
        Properties nonDefProps = new Properties();

        Enumeration<String> keys =
                (Enumeration<String>) nonDefaultRm.getProperties().propertyNames();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            nonDefProps.put(key, nonDefaultRm.getString(key));
        }
        return mergeProperties(props, new ResourceManager(nonDefProps));
    }

}
