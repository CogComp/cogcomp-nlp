package edu.illinois.cs.cogcomp.core.utilities;

/**
 * A container for a property name and its default value to be using with {@link Configurator}s
 *
 * @author Christos Christodoulopoulos
 */
public class Property {
    public String key;
    public String value;

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
