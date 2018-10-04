/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.mascReader;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;

/**
 * Configuration options for MASC readers
 *
 * @author mssammon
 */
public class MascReaderConfigurator extends CorpusReaderConfigurator{

    public static final Property GENRES = new Property("genres",
            "blog,email,essays,ficlets,fiction,govt-docs,jokes,journal,letters,movie-script,newspaper,non-fiction," +
                    "technical,travel-guides,twitter");
//    public static final Property SOURCES = new Property("sources", "nyt");
    public static final Property ANNOTATIONS = new Property("annotations", "SENTENCE,TOKENS");
    public static final Property READ_PENN = new Property("readPenn", FALSE);



    @Override
    public ResourceManager getDefaultConfig() {
        Property[] properties = new Property[]{
                GENRES,
//                SOURCES,
                ANNOTATIONS,
                READ_PENN,
                new Property(CorpusReaderConfigurator.CORPUS_NAME.key, "MASC"),
                new Property(CorpusReaderConfigurator.SUPPRESS_FILE_ERRORS.key, TRUE),
        };

        return new ResourceManager(generateProperties(properties));
    }


}
