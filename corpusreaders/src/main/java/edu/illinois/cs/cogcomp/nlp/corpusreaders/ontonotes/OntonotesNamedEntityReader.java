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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.XmlTextAnnotationMaker;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor;
import edu.illinois.cs.cogcomp.core.utilities.XmlDocumentProcessor.SpanInfo;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AnnotationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.CoNLL2002Writer;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * This reader will traverse a directory, reading a producing a named entity text annotation
 * for ".name" file found in the directory. It is expected to be an ontonotes 5 directory.
 * @author redman
 */
public class OntonotesNamedEntityReader extends AnnotationReader<XmlTextAnnotation> {
    
    /** this is what we will name our view of this gold standard data. */
    static final public String VIEW_NAME = "NER_ONTONOTES_5_GOLD";
    
    /** tags containing text. */
    static final private Set<String> tagsWithText = new HashSet<>();
    
    /** tags we can just ignore. */
    static final private Set<String> dropTags = new HashSet<>();

    /** define the attributes we want to keep for the tags we have. */
    static final private Map<String, Set<String>> tagsWithAtts = new HashMap<>();
    static {
        Set<String> docAttrs = new HashSet<>();
        docAttrs.add("docno");
        tagsWithAtts.put("doc", docAttrs);
        Set<String> nameAttrs = new HashSet<>();
        nameAttrs.add("type");
        tagsWithAtts.put("enamex", nameAttrs);
    }
    /** list of files that did not parse because of errors. */
    protected ArrayList<String> badFiles = new ArrayList<>();

    /** the home directory to traverse. */
    protected final String homeDirectory;
    
    /** the list of files, compiled during initialization, used to iterate over the parse trees. */
    protected ArrayList<File> filelist = new ArrayList<File> ();
    
    /** the index of the current file we are looking at. */
    protected int fileindex = 0;
    
    /** the current file ready to be read. */
    protected String currentfile = null;
    
    /** contains a count of the number of each entity type encountered. */
    private HashMap<String, Integer> entityCounts = new HashMap<String, Integer>();
    
    /**
     * Reads the specified sections from penn treebank
     * @param nerHome The directory that points to the merged (mrg) files of the WSJ portion
     * @param language the language
     * @param fl the list of files
     * @param annotationFileExtension the name of the annotation file
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public OntonotesNamedEntityReader(String nerHome, String language, ArrayList<File> fl) 
                    throws IllegalArgumentException, IOException {
        super(CorpusReaderConfigurator.buildResourceManager(VIEW_NAME, nerHome, nerHome, ".name", ".name"));
        homeDirectory = nerHome;
        filelist = fl;
    }

    /**
     * Reads the specified sections from penn treebank
     * @param nerHome The directory that points to the merged (mrg) files of the WSJ portion
     * @param language the language
     * @param annotationFileExtension the name of the annotation file
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public OntonotesNamedEntityReader(String nerHome, String language) 
                    throws IllegalArgumentException, IOException {
        super(CorpusReaderConfigurator.buildResourceManager(VIEW_NAME, nerHome, nerHome, ".name", ".name"));
        homeDirectory = nerHome;
        
        // compile the list of all treebank annotation files
        DocumentIterator di = new DocumentIterator(homeDirectory, DocumentIterator.Language.valueOf(language), 
            DocumentIterator.FileKind.name);
        while (di.hasNext()) {
            filelist.add(di.next());
        }
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

    /**
     * return the next annotation object. Don't forget to increment currentAnnotationId.
     *
     * @return an annotation object.
     */
    @Override
    public XmlTextAnnotation next() {
        String data;
        try {
            data = LineIO.slurp(currentfile);
        } catch (FileNotFoundException e1) {
            this.badFiles.add(this.currentfile);
            return null;
        } catch (Throwable e1) {
            e1.printStackTrace();
            return null;
        }
        try {
            XmlTextAnnotation ta = nextAnnotation(data, currentfile);
            return ta;
        } catch (AnnotatorException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    /**
     * parse the pen treebank parse file, producing an annotation covering the entire file.
     * @param data the data from the file, each line.
     * @param docid the id representing the document name.
     * @return the text annotation.
     * @throws AnnotatorException
     */
    private XmlTextAnnotation nextAnnotation(String data,String docid) throws AnnotatorException {
        
        // we keep everything.
        XmlDocumentProcessor xmlProcessor = new XmlDocumentProcessor(tagsWithText, tagsWithAtts, dropTags, true);
        StatefulTokenizer st = new StatefulTokenizer();
        TokenizerTextAnnotationBuilder taBuilder = new TokenizerTextAnnotationBuilder(st);
        XmlTextAnnotationMaker xtam = new XmlTextAnnotationMaker(taBuilder, xmlProcessor);
 
        // read the file and create the annotation.
        XmlTextAnnotation xta = xtam.createTextAnnotation(data, "OntoNotes 5.0", docid);
        TextAnnotation ta = xta.getTextAnnotation();
        List<SpanInfo> fudge = xta.getXmlMarkup();
 
        // create the named entity vi
        View nerView = new SpanLabelView(VIEW_NAME, ta);
        for (SpanInfo si : fudge) {
            if ("enamex".equalsIgnoreCase(si.label)) {
                IntPair charOffsets = si.spanOffsets;
                Pair<String, IntPair> neLabelPair = si.attributes.get("type");
                String neLabel = neLabelPair.getFirst();
                int cleanTextCharStart = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getFirst());
                int cleanTextCharEnd = xta.getXmlSt().computeModifiedOffsetFromOriginal(charOffsets.getSecond());
                int cleanTextNeTokStart = ta.getTokenIdFromCharacterOffset(cleanTextCharStart);
                int cleanTextNeTokEnd = ta.getTokenIdFromCharacterOffset(cleanTextCharEnd-1); // StringTransformation returns one-past-the-end index; TextAnnotation maps at-the-end index
                if (cleanTextNeTokStart == -1 || cleanTextNeTokEnd == -1) {
                    for (Constituent c : nerView.getConstituents()) {
                        System.err.println(c);
                    }
                    System.err.println("Something wonky in \""+docid+"\", at "+charOffsets+", "+cleanTextCharStart+" - "+cleanTextCharEnd
                        +" = "+ta.text.substring(cleanTextCharStart, cleanTextCharEnd));
                } else {
                    if (entityCounts.containsKey(neLabel)) {
                        entityCounts.put(neLabel, (entityCounts.get(neLabel)+1));
                    } else {
                        entityCounts.put(neLabel, 1);
                    }
                    Constituent neCon = new Constituent(neLabel, nerView.getViewName(), ta, cleanTextNeTokStart, cleanTextNeTokEnd + 1); //constituent token indexing uses one-past-the-end
                    nerView.addConstituent(neCon);
                }
            }
        }
        ta.addView(VIEW_NAME, nerView);
        return xta;
    }

    /**
     * TODO: generate a human-readable report of annotations read from the source file (plus whatever
     * other relevant statistics the user should know about).
     */
    public String generateReport() {
        if (filelist.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Read "+fileindex+" of "+filelist.size()+" files. Entity counts follow:\n");
            for (String entity : entityCounts.keySet()) {
                sb.append(entity+" : "+entityCounts.get(entity)+"\n");
            }
            return sb.toString();
        } else {
            return "No files were read.";
        }
    }

    @Override
    protected void initializeReader() {
    }
    
    /**
     * This class will read the ontonotes data from the provided directory, and write the resulting
     * NER view data to the specified output directory in CoNLL column format. It will retain
     * the directory structure of the original data.
     * @param args command lines args specify input data directory, language and output directory.
     * @throws IOException
     */
    static public void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("This executable requires three arguments:\n"
                    + " OntonotesTreebankReader <OntoNotes Directory> <language> <output_directory>");
            System.exit(-1);
        }
        
        String topdir = args[0];
        String outputdir = args[2];
        OntonotesNamedEntityReader otr = new OntonotesNamedEntityReader(topdir, args[1]);
        int count = 0;
        final boolean producejson = true;
        while (otr.hasNext()) {
            XmlTextAnnotation xta = otr.next();
            String path = otr.currentfile;
            if (producejson) {
                try {
                    String json = SerializationHelper.serializeToJson(xta.getTextAnnotation());
                    String outfile = otr.currentfile.replace(topdir, args[2]);
                    File outputfile = new File(outfile);
                    outputfile.getParentFile().mkdirs();
                    try (PrintWriter out = new PrintWriter(outputfile)) {
                        out.print(json);
                    }
                } catch (Throwable t) {
                    System.out.println(otr.currentfile+" produced the incorrect offset.");
                }
            } else {
                TextAnnotation ta = xta.getTextAnnotation();
                path = outputdir+path.substring(topdir.length());
                path += ".conll";
                CoNLL2002Writer.writeViewInCoNLL2003Format(ta.getView(VIEW_NAME), ta, path);
            }
            count++;
            if ((count % 10) == 0)
                System.out.println("Completed "+count+" of "+otr.filelist.size());
        }
        System.out.println(otr.generateReport());
    }
}