/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.config.SimpleGazetteerAnnotatorConfigurator;
import edu.illinois.cs.cogcomp.edison.features.helpers.GazetteerTree;

/**
 * This class contains all the gazetteer data and the tree used to search for term and phrase
 * matches. This class SHOULD ONLY BE INSTANTIATED once per gazetteer set, as the gazetteers are
 * quite large and thread safe. By default, uses lazy initialization.
 * 
 * @author redman
 */
public class SimpleGazetteerAnnotator extends Annotator {
    // ? should this not have a viewName?
    /** this hash tree contains the terms as exactly as they are. */
    ArrayList<GazetteerTree> dictionaries;
    /** this hash tree contains the terms in lowercase. */
    ArrayList<GazetteerTree> dictionariesIgnoreCase;
    private int phraseLength;
    private String pathToDictionaries;


    /**
     * Loads phrases of length 4 or less from gazetteers specified in default parameter
     * {@link SimpleGazetteerAnnotatorConfigurator#PATH_TO_DICTIONARIES}
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    public SimpleGazetteerAnnotator() throws IOException, URISyntaxException {
        this(new SimpleGazetteerAnnotatorConfigurator().getDefaultConfig());
    }

    /**
     * ResourceManager can override properties in {@link SimpleGazetteerAnnotatorConfigurator}
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    public SimpleGazetteerAnnotator(ResourceManager nonDefaultRm) throws IOException,
            URISyntaxException {
        super(ViewNames.TREE_GAZETTEER, new String[] {ViewNames.SENTENCE, ViewNames.TOKENS},
                new SimpleGazetteerAnnotatorConfigurator().getConfig(nonDefaultRm));

    }

    /**
     * Lists the contents of a directory that exists either in the local path or in the classpath
     * 
     * @param resourceDir The name of the directory containing the gazetteers
     * @return An array of URL objects to be read
     * @throws IOException
     * @throws
     */
    public URL[] listGazetteers(String resourceDir) throws IOException {
        List<URL> files = new ArrayList<>();
        try {
            for (URL url : IOUtils.lsResources(SimpleGazetteerAnnotator.class, resourceDir)) {
                files.add(url);
            }
        } catch (URISyntaxException e) {
            throw new IOException("URI syntax error.", e);
        }
        return files.toArray(new URL[files.size()]);
    }

    /**
     * init all the gazetters, read the terms from a file.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Override
    public void initialize(ResourceManager rm) {
        this.phraseLength = rm.getInt(SimpleGazetteerAnnotatorConfigurator.PHRASE_LENGTH);
        this.pathToDictionaries =
                rm.getString(SimpleGazetteerAnnotatorConfigurator.PATH_TO_DICTIONARIES);
        // int phrase_length, String pathToDictionaries) throws IOException {
        ArrayList<URL> filenames = new ArrayList<>();

        // List the Gazetteers directory (either local or in the classpath)
        URL[] allfiles = new URL[0];
        try {
            allfiles = listGazetteers(pathToDictionaries);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (URL file : allfiles) {
            filenames.add(file);
        }
        Arrays.sort(allfiles, new Comparator<URL>() {
            @Override
            public int compare(URL o1, URL o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        // init the dictionaries.
        dictionaries = new ArrayList<>(filenames.size());
        dictionariesIgnoreCase = new ArrayList<>(filenames.size());
        GazetteerTree gaz = new GazetteerTree(phraseLength);
        GazetteerTree gazIC =
                new GazetteerTree(phraseLength, new GazetteerTree.StringSplitterInterface() {
                    @Override
                    public String[] split(String line) {
                        String tmp = line.toLowerCase();
                        if (tmp.equals("in") || tmp.equals("on") || tmp.equals("us")
                                || tmp.equals("or") || tmp.equals("am"))
                            return new String[0];
                        else
                            return normalize(line).split("[\\s]+");

                    }

                    @Override
                    public String normalize(String term) {
                        return term.toLowerCase();
                    }
                });

        // for each dictionary, compile each of the gaz trees for each phrase permutation.
        for (URL file : filenames) {

            try (InputStream is = file.openStream()) {
                gaz.readDictionary(IOUtils.getFileName(file.getPath()), "", is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            try (InputStream is = file.openStream()) {
                gazIC.readDictionary(IOUtils.getFileName(file.getPath()), "(IC)", is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        gaz.trimToSize();
        gazIC.trimToSize();
        dictionaries.add(gaz);
        dictionariesIgnoreCase.add(gazIC);
    }

    /**
     * The view will consist of potentially overlapping constituents representing those tokens that
     * matched entries in the gazetteers. Some tokens will match against several gazetteers.
     */
    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException {
        View view = ta.getView(ViewNames.TOKENS);
        List<Constituent> constituents = view.getConstituents();
        SpanLabelView slv =
                new SpanLabelView(this.getViewName(), this.getClass().getName(), ta, 1d, true);
        for (int constindx = 0; constindx < constituents.size(); constindx++) {
            for (int dictindx = 0; dictindx < dictionaries.size(); dictindx++) {
                dictionaries.get(dictindx).match(constituents, constindx, slv);
                dictionariesIgnoreCase.get(dictindx).match(constituents, constindx, slv);
            }
        }
        ta.addView(slv.getViewName(), slv);
    }
}
