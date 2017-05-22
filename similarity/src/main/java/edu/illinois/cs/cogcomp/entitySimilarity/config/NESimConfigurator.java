package edu.illinois.cs.cogcomp.entitySimilarity.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.*;


public abstract class NESimConfigurator extends Configurator {

    public static final Property ACRONYM_FILE = new Property( "acronymFile", "acronyms.txt");
    public static final Property CL_FILE = new Property( "countryLanguageFile", "countrylanguage.txt");
    public static final Property HONORIFICS_FILE = new Property( "honorificsFile", "honorifics.txt");
    public static final Property LOCATION_FILE = new Property( "locationFile", "locations.txt");
    public static final Property NICKNAME_FILE = new Property( "nicknameFile", "nicknames.txt");
    public static final Property PEOPLE_FILE = new Property( "peopleFile", "people.txt");
    public static final Property SHORTCUT_FILE = new Property( "shortcutFile", "shortcuts.txt");
    public static final Property SIMILARITY_THRESHOLD = new Property( "similarityThreshold", "0.5" );
    
    /**
     * get a ResourceManager object with the default key/value pairs for this configurator
     *
     * @return a non-null ResourceManager with appropriate values set.
     */
    @Override
    public ResourceManager getDefaultConfig() {
        Property [] props = {ACRONYM_FILE, CL_FILE, HONORIFICS_FILE, LOCATION_FILE, NICKNAME_FILE,
        		PEOPLE_FILE, SHORTCUT_FILE, SIMILARITY_THRESHOLD};
        return new ResourceManager( generateProperties( props ) );
    }
    
}

