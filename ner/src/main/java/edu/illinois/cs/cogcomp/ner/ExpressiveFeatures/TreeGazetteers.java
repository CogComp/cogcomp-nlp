/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import cogcomp.Datastore;
import cogcomp.DatastoreException;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteerTree.StringSplitterInterface;
import edu.illinois.cs.cogcomp.ner.IO.ResourceUtilities;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * This singleton class contains all the gazetteer data and dictionaries. Can only be accessed via
 * the get() method all constructors are private.
 *
 * @author redman
 */
public class TreeGazetteers implements Gazetteers {

    /** the logger. */
    static private Logger logger = LoggerFactory.getLogger(TreeGazetteers.class);

    /** this hash tree contains the terms as exactly as they are. */
    private ArrayList<GazetteerTree> dictionaries = new ArrayList<>();

    /** this hash tree contains the terms in lowercase. */
    private ArrayList<GazetteerTree> dictionariesIgnoreCase = new ArrayList<>();

    /**
     * Making this private ensures singleton.
     *
     * @param phrase_length the max length of the phrases we will consider.
     * @throws IOException
     */
    TreeGazetteers(int phrase_length, String pathToDictionaries) throws IOException {
        init(phrase_length, pathToDictionaries);
    }

    /**
     * init all the gazetters, mangle each term in a variety of ways.
     *
     * @param pathToDictionaries
     * @param phrase_length the max length of the phrases we will consider.
     * @throws IOException
     */
    private void init(int phrase_length, String pathToDictionaries) throws IOException {
        try {
            ArrayList<String> filenames = new ArrayList<>();
            Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazDirectory = dsNoCredentials.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.5, false);
            // We are not loading the resources from classpath anymore. Instead we are calling them programmatically
            // InputStream stream = ResourceUtilities.loadResource(pathToDictionaries + "/gazetteers-list.txt");
            InputStream stream = new FileInputStream(gazDirectory.getPath() + File.separator + "gazetteers" + File.separator + "gazetteers-list.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = br.readLine()) != null)
                filenames.add(line);

            // init the dictionaries.
            dictionaries = new ArrayList<>(filenames.size());
            dictionariesIgnoreCase = new ArrayList<>(filenames.size());
            GazetteerTree gaz = new GazetteerTree(phrase_length);
            GazetteerTree gazIC = new GazetteerTree(phrase_length, new StringSplitterInterface() {
                @Override
                public String[] split(String line) {
                    String tmp = line.toLowerCase();
                    if (tmp.equals("in") || tmp.equals("on") || tmp.equals("us") || tmp.equals("or")
                            || tmp.equals("am"))
                        return new String[0];
                    else {
                        // character tokenization for Chinese
                        if (ParametersForLbjCode.currentParameters.language == Language.Chinese) {
                            String[] chars = new String[line.length()];
                            for (int i = 0; i < line.length(); i++)
                                chars[i] = String.valueOf(line.charAt(i));
                            return chars;
                        } else
                            return normalize(line).split("[\\s]+");
                    }
                }

                @Override
                public String normalize(String term) {
                    return term.toLowerCase();
                }
            });

            // for each dictionary, compile each of the gaz trees for each phrase permutation.
            for (String file : filenames) {
                String fileName = gazDirectory.getAbsolutePath() + File.separator + file;
                gaz.readDictionary(file, "", ResourceUtilities.loadResource(fileName));
                gazIC.readDictionary(file, "(IC)", ResourceUtilities.loadResource(fileName));
            }
            gaz.trimToSize();
            gazIC.trimToSize();
            dictionaries.add(gaz);
            dictionariesIgnoreCase.add(gazIC);
            if (ParametersForLbjCode.currentParameters.debug) {
                logger.info("found " + dictionaries.size() + " gazetteers");
            }
        } catch (InvalidPortException | InvalidEndpointException e) {
            e.printStackTrace();
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search each gazetteer for a contiguous set of matching words to form the longest possible
     * expression (up to 5 words) matching an entry in any gazetteer. Matching begins with this word
     * and preceeds till the end of the sentence (or null word.next in any event).
     *
     * @param startword the word to match around.
     */
    public void annotate(final NEWord startword) {
        for (int i = 0; i < dictionaries.size(); i++) {
            dictionaries.get(i).match(startword);
            dictionariesIgnoreCase.get(i).match(startword);
        }
    }
}
