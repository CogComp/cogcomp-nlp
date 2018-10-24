/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This iterator will enumerate all the documents for a given language
 * from the OntoNotes 5.0 corpus. The provided path must be the directory
 * above the language directories. The documents are read from the file system,
 * assuming the same hierarchical structure as the official OntoNotes distribution. 
 * @author redman
 */
public class DocumentIterator implements Iterator<File> {
    /**
     * available languages.
     * @author redman
     */
    public enum Language {
        /** arabic */
        arabic, 
        
        /** chinese. */
        chinese,
        
        /** english */
        english,
        
        /** the ontology */
        ontology
    }
    
    /** the language we are using. */
    private Language language = Language.english;
    
    /**
     * the type of file representation.
     * @author redman
     */
    public enum FileKind {
        
        /** named entity */
        name,
        
        /** coreference data  */
        coref,
        
        /** frames */
        onf,
        
        /** don't know */
        parallel,
        
        /** treebank representation of sentences. */
        parse,
        
        /** propbank data */
        prop,
        
        /** verb sense encoding */
        sense,
        
        /** speaker data */
        speaker
    }
    
    /** the type of files to produce. */
    private FileKind kind = FileKind.name;
    
	/** the documents are read when we initialize this class, handed out later.*/
	private ArrayList<File> documents = new ArrayList<File>();
	
	/** the directory we want to traverse. */
	private File directory = null;
	
	/** the index of the current document. */
	int which = 0;
	
	/**
	 * Create the iterator to iterate over documents in a particular language from
	 * the OntoNotes 5.0 corpus. This constructor takes all the info needed to 
	 * make a connection, closing the connection when the data has been pulled.
	 * @param path path to the directory we want to search for matching files.
	 * @param lang the language we want.
	 * @param kind the type of mark-up we want, NE, treebank so on.
	 * @throws IOException if there are any kind of file system issues.
	 * @throws IllegalArgumentException illegal arguments passed in.
	 */
	public DocumentIterator(String path, Language lang, FileKind kind) throws IOException,IllegalArgumentException {
	    
	    // make sure we have everything we need.
        if (path == null) {
            throw new IllegalArgumentException("You must provide a path.");
        }
	    if (lang == null) {
            throw new IllegalArgumentException("You must provide a language.");
	    }
	    this.language = lang;
        if (kind == null) {
            throw new IllegalArgumentException("You must provide a file kind.");
        }
	    this.kind = kind;
	    
	    // First, move down into the directory containing the language.
	    this.directory = new File(path+File.separator+language.toString());
	    if (directory.exists()) {
    	    if (directory.isDirectory()) {
    	        
    	        // we have a language directory
    	        traverse(directory);
    	    } else {
    	        throw new IOException("The language directory, \""+directory.toString()+
    	            "\", was not a directory.");
    	    }
	    } else {
            throw new IOException("The language directory, \""+directory.toString()+
                            "\", did not exist.");
	    }
	}
	
	/**
	 * Traverse the directory, collecting each requested file kind in the array.
	 * @param directory the directory to search.
	 */
	private void traverse(File directory) {
	    File [] files = directory.listFiles();
	    for (File file : files) {
	        if (file.isDirectory())
	            traverse(file);
	        else {
	            if (file.toString().endsWith(kind.toString())) {
	                documents.add(file);
	            }
	        }
	    }
	}
	
	@Override
	public boolean hasNext() {
		return which < documents.size();
	}
	
	/**
	 * This method fetches the next file, if there is an IO error, it will
	 * throw an IllegalArgumentException.
	 */
	@Override
	public File next() throws IllegalArgumentException {
		if (!hasNext())
			return null;
		File next = documents.get(which);
        which++;
        return next;
	}
	
	/**
	 * test it works.
	 * @param args none.
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static void main (String[] args) throws IllegalArgumentException, IOException {
	    DocumentIterator di = new DocumentIterator("/Users/redman/Desktop/ontonotes-release-5.0/data/files/data/",
	        Language.chinese, FileKind.parse);
	    int count = 0;
	    while(di.hasNext()) {
            System.out.println("---- "+di.documents.get(di.which).toString()+" ----");
	        System.out.println(di.next());
	        System.out.println();
	        count++;
	    }
	    System.out.println("Read "+count+" files.");
	}
}
