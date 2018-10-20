/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteerTree.StringSplitterInterface;
import edu.illinois.cs.cogcomp.ner.IO.ResourceUtilities;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

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
     * @param pathToDictionaries the path to the gazetteers.
     * @throws IOException
     */
    TreeGazetteers(int phrase_length, String pathToDictionaries, Language language) throws IOException {
        init(phrase_length, pathToDictionaries, language);
    }

    /**
     * init all the gazetters, mangle each term in a variety of ways.
     *
     * @param pathToDictionaries the path to the gazetteers.
     * @param phrase_length the max length of the phrases we will consider.
     * @throws IOException
     */
    private void init(int phrase_length, String pathToDictionaries, final Language language) throws IOException {
        try {
                        
            // check the local file system for it.
            File gazDirectory = new File(pathToDictionaries);
            String pathToLists = gazDirectory.getPath() + File.separator + "gazetteers" + File.separator + "gazetteers-list.txt";
            InputStream stream = ResourceUtilities.loadResource(pathToLists);
            if (stream == null) {
                logger.info("Loading gazetteers from \""+pathToLists+"\" using the Minio cache.");
                // not in file system or classpath, try Minio.
                Datastore dsNoCredentials = new Datastore(new ResourceConfigurator().getDefaultConfig());
                gazDirectory = dsNoCredentials.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.6, false);
                stream = new FileInputStream(gazDirectory.getPath() + File.separator + "gazetteers" + File.separator + "gazetteers-list.txt");
            } else {
                logger.info("Loading gazetteers from \""+pathToLists+"\" from the local file system.");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            ArrayList<String> filenames = new ArrayList<>();
            while ((line = br.readLine()) != null)
                filenames.add(line);

            // init the dictionaries.
            dictionaries = new ArrayList<>(filenames.size());
            dictionariesIgnoreCase = new ArrayList<>(filenames.size());
            GazetteerTree gaz = new GazetteerTree(phrase_length, new StringSplitterInterface() {
                @Override
                public String[] split(String line) {

                    // character tokenization for Chinese
                    if(language == Language.Chinese) {
                        String[] chars = new String[line.length()];
                        for(int i = 0; i < line.length(); i++)
                            chars[i] = String.valueOf(line.charAt(i));
                        return chars;
                    } else
                        return line.split("[\\s]+");
                }

                @Override
                final public String normalize(String term) {
                    return term;
                }
            });
            GazetteerTree gazIC = new GazetteerTree(phrase_length, new StringSplitterInterface() {
                @Override
                public String[] split(String line) {
                    String tmp = line.toLowerCase();
                    if (tmp.equals("in") || tmp.equals("on") || tmp.equals("us") || tmp.equals("or")
                            || tmp.equals("am"))
                        return new String[0];
                    else {
                        // character tokenization for Chinese
                        if (language == Language.Chinese) {
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
            logger.info("Gazetteers from \""+pathToLists+"\" are loaded.");
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
