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

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.helpers.GazetteerTree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;

/**
 * This class contains all the gazetteer data and the tree used to search for 
 * term and phrase matches. This class SHOULD ONLY BE INSTANTIATED once per 
 * gazetteer set, as the gazetteers are quite large and thread safe.
 * 
 * @author redman
 */
public class SimpleGazetteerAnnotator extends Annotator {

    /** the logger. */
    static private Logger logger = LoggerFactory.getLogger(SimpleGazetteerAnnotator.class);

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
     * If the path points to something in a jar file in the classpath, this method will return
     * a list of all the resources in that directory for further processing.
     * @param pathToDirectories the directory to find.
     * @return a list of files in the directory.
     * @throws IOException
     * @throws URISyntaxException 
     */
    private String[] listFilesInJar (String pathToDirectories) throws IOException, URISyntaxException {
    	URI uri = SimpleGazetteerAnnotator.class.getResource(pathToDirectories).toURI();
        Path myPath = null;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            myPath = fileSystem.getPath(pathToDirectories);
        } else {
            myPath = Paths.get(uri);
        }

        final ArrayList<String> paths = new ArrayList<String>();
        Files.walkFileTree(myPath, new FileVisitor<Path>() {
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				paths.add(file.toString());
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.TERMINATE;
			}
        });
        logger.debug("Identified "+paths.size()+" gazetteer files to load.");
        return paths.toArray(new String[paths.size()]);
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
        ArrayList<String> filenames = new ArrayList<>();

        // List the Gazetteers directory (either local or in the classpath)
        String[] allfiles = null;
        File gazdir = new File(pathToDictionaries);
        if (gazdir.exists())
        	allfiles = gazdir.list();
        else {
        	try {
				allfiles = listFilesInJar(pathToDictionaries);
			} catch (URISyntaxException e) {
				throw new IOException("Could not find the gazetteers in classpath or file system!");
			}
        }
        
        for (String file : allfiles) {
            if (!IOUtils.isDirectory(file)) {
                filenames.add(file);
            }
        }
        Arrays.sort(allfiles);

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
        for (String file : filenames) {
            gaz.readDictionary(this.getFeatureName(file), "", this.loadResource(file));
            gazIC.readDictionary(this.getFeatureName(file), "(IC)", this.loadResource(file));
        }
        gaz.trimToSize();
        gazIC.trimToSize();
        dictionaries.add(gaz);
        dictionariesIgnoreCase.add(gazIC);
    }
    
    /**
     * Mangle the file name, strip off the path and the file extension, returning what
     * we will define here to be the feature name.
     * @param file the name of the resource.
     * @return and input stream to the resource.
     * @throws FileNotFoundException 
     */
    private String getFeatureName(String file) throws FileNotFoundException {
    	File asfile = new File (file);
    	String filename = null;
    	if (asfile.exists())
    		filename = asfile.getName();
    	else {
    		String url = this.getClass().getResource(file).toString();
    		filename = url.substring(url.lastIndexOf('/')+1, url.length());
    	}
    	
    	// strip the file extension.
		int lasti = filename.lastIndexOf(".");
		if (lasti != -1)
			return filename.substring(0, lasti);
		else
			return filename;
	}


    /**
     * Create a stream first looking in the classpath, then in a directory.
     * @param file the name of the resource.
     * @return and input stream to the resource.
     * @throws FileNotFoundException 
     */
    private InputStream loadResource(String file) throws FileNotFoundException {
    	File asfile = new File (file);
    	if (asfile.exists())
    		return new FileInputStream(asfile);
    	else {
    		return this.getClass().getResourceAsStream(file);
    	}
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