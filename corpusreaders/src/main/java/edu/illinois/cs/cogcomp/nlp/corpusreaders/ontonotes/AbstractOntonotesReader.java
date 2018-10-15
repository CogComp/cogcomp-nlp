 /**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AnnotationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator;

/**
 * This class will traverse a directory hierarchy searching for any file with a 
 * certain file extension. The file is expected to be a file in Ontonotes 5 corpus
 * in text format. It will go any number of levels deep. The iteration produces a
 * TextAnnotation for each file. Subclass must provide the method to parse the actual
 * text provided.
 * @author redman
 */
abstract public class AbstractOntonotesReader extends AnnotationReader<TextAnnotation> {

    /** the home directory to traverse. */
    protected final String homeDirectory;
    
    /** the list of files, compiled during initialization, used to iterate over the parse trees. */
    protected ArrayList<File> filelist = new ArrayList<File> ();
    
    /** the index of the current file we are looking at. */
    protected int fileindex = 0;
    
    /** the current file ready to be read. */
    protected String currentfile = null;
    
    /** list of files that did not parse because of errors. */
    protected ArrayList<String> badFiles = new ArrayList<>();
    
    /**
     * Reads the specified sections from penn treebank
     * @param viewname the name of the view, used to create resource manager.
     * @param dir  the directory required where the files are located.
     * @param language the language we want.
     * @param fileKind the type of files (parsed by subclasses).
     * @throws IllegalArgumentException Subclasses might throw this.
     * @throws IOException any file system issues, like boink directory.
     */
    public AbstractOntonotesReader(String viewname, String dir, String language, DocumentIterator.FileKind fileKind) 
                    throws IllegalArgumentException, IOException {
        super(CorpusReaderConfigurator.buildResourceManager(viewname, dir, dir, "."+fileKind.name(), "."+fileKind.name()));
        homeDirectory = dir;
        
        // compile the list of all treebank annotation files
        DocumentIterator di = new DocumentIterator(homeDirectory, DocumentIterator.Language.valueOf(language), 
            fileKind);
        while (di.hasNext()) {
            filelist.add(di.next());
        }
    }
    
    /**
     * Reads the specified sections from penn treebank
     * @param viewname the name of the view, used to create resource manager.
     * @param dir  the directory required where the files are located.
     * @param language the language we want.
     * @param fileKind the type of files (parsed by subclasses).
     * @param files Instead of using the document iterator, we accept a list of files to use.
     * @throws IllegalArgumentException Subclasses might throw this.
     * @throws IOException any file system issues, like boink directory.
     */
    public AbstractOntonotesReader(String viewname, String dir, String language, DocumentIterator.FileKind fileKind, 
            ArrayList<File> files) {
        super(CorpusReaderConfigurator.buildResourceManager(viewname, dir, dir, "."+fileKind.name(), "."+fileKind.name()));
        homeDirectory = dir;
        filelist = files;
    }

    /**
     * we assume all files found are correct, hence if we have another file, we will produce
     * another text annotation.
     */
    @Override
    public boolean hasNext() {
        if (fileindex == filelist.size())
            return false;
        else {
            this.currentfile = filelist.get(fileindex).getAbsolutePath();
            fileindex++;
            return true;
        }
    }
    
    /** the annotation exception, or null if none. */
    protected Exception error = null;
    
    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     * @return an annotation object.
     */
    @Override
    public TextAnnotation next() {
        ArrayList<String> lines;
        try {
            lines = LineIO.read(currentfile);
        } catch (FileNotFoundException e1) {
            error = e1;
            e1.printStackTrace();
            return null;
        }
        try {
            TextAnnotation ta = parseLines(lines);
            return ta;
        } catch (AnnotatorException e) {
            error = e;
            this.badFiles.add(this.currentfile);
            return null;
        }
    }

    /**
     * parse the file, producing an annotation covering the data. Subclass must provide
     * the logic for this method.
     * @param lines the data from the file, each line.
     * @return the text annotation.
     * @throws AnnotatorException
     */
    abstract protected TextAnnotation parseLines(ArrayList<String> lines) throws AnnotatorException;

    /**
     * Not further actions typically required here.
     */
    @Override
    protected void initializeReader() {
    }
}
