/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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
import edu.illinois.cs.cogcomp.edison.features.helpers.GazetteerTree;

/**
 * This class contains all the gazetteer data and the tree used to search for 
 * term and phrase matches. This class SHOULD ONLY BE INSTANTIATED once per 
 * gazetteer set, as the gazetteers are quite large and thread safe.
 * 
 * @author redman
 */
public class SimpleGazetteerAnnotator extends Annotator {

    /** this hash tree contains the terms as exactly as they are. */
    ArrayList<GazetteerTree> dictionaries = new ArrayList<>();

    /** this hash tree contains the terms in lowercase. */
    ArrayList<GazetteerTree> dictionariesIgnoreCase = new ArrayList<>();

    /**
     * Making this private ensures singleton.
     * @param phrase_length the max length of the phrases we will consider.
     * @throws IOException
     * @throws URISyntaxException 
     */
    public SimpleGazetteerAnnotator(String pathToDictionaries) throws IOException, URISyntaxException {
        super(ViewNames.TREE_GAZETTEER, new String[] {ViewNames.SENTENCE, ViewNames.TOKENS});
        init(4, pathToDictionaries);
    }

    /**
     * Making this private ensures singleton.
     * @param phrase_length the max length of the phrases we will consider.
     * @throws IOException
     * @throws URISyntaxException 
     */
    public SimpleGazetteerAnnotator(int phrase_length, String pathToDictionaries) throws IOException, URISyntaxException {
        super(ViewNames.TREE_GAZETTEER, new String[] {ViewNames.SENTENCE, ViewNames.TOKENS});
        init(phrase_length, pathToDictionaries);
    }

    /**
     * Lists the contents of a directory that exists either in the local path or in the classpath
     * 
     * @param directory The name of the directory containing the gazetteers
     * @return An array of URL objects to be read
     * @throws IOException
     * @throws  
     */
    public URL[] listGazetteers(String resourceDir)
            throws IOException {
        List<URL> files = new ArrayList<>();
        try {
			for (URL url : IOUtils.lsResources(SimpleGazetteerAnnotator.class, resourceDir)) {
			    files.add(url);
			}
		} catch (URISyntaxException e) {
			throw new IOException("URI syntax error.",e);
		}
        return files.toArray(new URL[files.size()]);
    }
    
    /**
     * init all the gazetters, read the terms from a file.
     * 
     * @param pathToDictionaries the path to the dictionary files.
     * @param phrase_length the max length of the phrases we will consider.
     * @throws IOException
     * @throws URISyntaxException 
     */
    private void init(int phrase_length, String pathToDictionaries) throws IOException {
        ArrayList<URL> filenames = new ArrayList<>();

        // List the Gazetteers directory (either local or in the classpath)
        URL[] allfiles = listGazetteers(pathToDictionaries);
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
        GazetteerTree gaz = new GazetteerTree(phrase_length);
        GazetteerTree gazIC = new GazetteerTree(phrase_length, new GazetteerTree.StringSplitterInterface() {
            @Override
            public String[] split(String line) {
                String tmp = line.toLowerCase();
                if (tmp.equals("in") || tmp.equals("on") || tmp.equals("us") || tmp.equals("or")
                        || tmp.equals("am"))
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
        	InputStream is = file.openStream();
        	try {
        		gaz.readDictionary(IOUtils.getFileName(file.getPath()), "", is);
        	} finally {
        		is.close();
        	}
        	is = file.openStream();
        	try {
        		gazIC.readDictionary(IOUtils.getFileName(file.getPath()), "(IC)", is);
        	} finally {
        		is.close();
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
		SpanLabelView slv = new SpanLabelView(this.getViewName(), this.getClass().getName(), ta, 1d, true);
		for(int constindx = 0; constindx < constituents.size(); constindx++) {
	        for (int dictindx = 0; dictindx < dictionaries.size(); dictindx++) {
	            dictionaries.get(dictindx).match(constituents, constindx, slv);
	            dictionariesIgnoreCase.get(dictindx).match(constituents, constindx, slv);
	        }
		}
		ta.addView(slv.getViewName(), slv);
	}
}