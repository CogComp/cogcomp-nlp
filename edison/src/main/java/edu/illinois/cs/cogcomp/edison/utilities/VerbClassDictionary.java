/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.features.factory.LevinVerbClassFeature;
import org.cogcomp.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Srikumar
 */
public class VerbClassDictionary {

    private static final Logger log = LoggerFactory.getLogger(VerbClassDictionary.class);
    public static String verbClassFile = "levin-verbClass.txt";
    private static VerbClassDictionary verbClassDictionary;
    // A map from verb to verb class
    Map<String, List<String>> verbClasses;

    /**
     * Load the verb class dictionary from the file. The file consists of Levin's verb classes and
     * the verbs they contain in the following format:
     * <p>
     * 
     * <pre>
     *  13.4.1
     * furnish issue leave present provide supply trust
     *  13.4.2
     * burden equip invest outfit ply
     * .
     * .
     * .
     * </pre>
     *
     * For more details, refer to <a href="www-personal.umich.edu/~jlawler/levin.verbs">this page</a>
     *
     * @throws java.io.FileNotFoundException
     */
    public VerbClassDictionary(String verbClassFile) throws FileNotFoundException {
        verbClasses = new HashMap<>();

        String verbClass = "";
        for (String line : LineIO.read(verbClassFile))
            verbClass = readVerbClasses(verbClass, line);

    }

    public VerbClassDictionary(InputStream inputStream) throws IOException {
        verbClasses = new HashMap<>();

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        String verbClass = "";
        String line;
        while ((line = in.readLine()) != null)
            verbClass = readVerbClasses(verbClass, line);
        in.close();
        log.info("Done reading the verb-classes. Size: " + verbClasses.size());
    }

    public static VerbClassDictionary getDictionaryFromDatastore() {
        if (verbClassDictionary == null) {
            synchronized (LevinVerbClassFeature.class) {

                if (verbClassDictionary == null) {
                    log.info("Reading verb class dictionary. Looking for " + verbClassFile + " in the datastore");
                    try {
                        Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
                        File f = dsNoCredentials.getFile("org.cogcomp.levin.verb.class", "levin-verbClass", 1.6);
                        InputStream resource = new FileInputStream(f);
                        verbClassDictionary = new VerbClassDictionary(resource);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Unable to read the verb class dictionary", e);
                        System.exit(-1);
                    }

                    List<String> strings = verbClassDictionary.getClass("give");
                    log.info("Loaded verb class dictionary. Test: classes for 'give' are {}", strings);
                }
            }
        }
        return verbClassDictionary;
    }

    @Deprecated
    public static VerbClassDictionary getDictionaryFromClassPath() {
        if (verbClassDictionary == null) {
            synchronized (LevinVerbClassFeature.class) {

                if (verbClassDictionary == null) {

                    log.info("Reading verb class dictionary. Looking for " + verbClassFile
                            + " in the classpath");
                    try {
                        URL url = IOUtils.lsResources(LevinVerbClassFeature.class, verbClassFile).get(0);
                        InputStream resource = url.openStream();
                        verbClassDictionary = new VerbClassDictionary(resource);
                    } catch (Exception e) {
                        log.error("Unable to read the verb class dictionary", e);
                        System.exit(-1);
                    }

                    List<String> strings = verbClassDictionary.getClass("give");
                    log.info("Loaded verb class dictionary. Test: classes for 'give' are {}", strings);
                }
            }
        }
        return verbClassDictionary;
    }

    private String readVerbClasses(String verbClass, String line) {
        if (line.startsWith(" ")) {
            verbClass = line;
        } else {
            for (String verb : line.split(" +")) {

                if (!verbClasses.containsKey(verb))
                    verbClasses.put(verb, new ArrayList<>());

                verbClasses.get(verb.trim()).add(verbClass.trim());
            }
        }
        return verbClass;
    }

    /**
     * Get all the verb classes for a given verb.
     */
    public List<String> getClass(String verb) {

        if (verbClasses.containsKey(verb))
            return verbClasses.get(verb);
        else
            return new ArrayList<>();
    }
}
