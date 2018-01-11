package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.SimpleXMLParser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator.FileKind;

/**
 * This is the ontonotes coref reader. 
 * @author redman
 */
public class OntonotesCorefReader extends AbstractOntonotesReader {

    /** name for the corpus. */
    static final String COREF_ONTONOTES_5 = "COREF_ONTONOTES_5";
   
    /** the name of our view. */
    static final String COREF_VIEW_NAME = "ONTONOTES_5_GOLD_COREF";
    
    /** we will use this to generate the text annotation with the treebank parse, 
     * then we will add the coref parse over that. Treebank is the reference data
     * for tokenization and sentence splitting. */
    private OntonotesTreebankReader otr = null;

    /** turn on to debug. */
    private boolean debug = false;
    /**
     * 
     * @param viewname
     * @param dir
     * @param language
     * @param fileKind
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public OntonotesCorefReader(String dir, String language) throws IllegalArgumentException, IOException {
        super(COREF_ONTONOTES_5, dir, language, FileKind.coref);
        
        // Need to be able to iterate over the ".parse" files so we can vet the required 
        // treebank data. We have to make sure for each propbank file we have, we have the
        // corresponding treebank file. treebank file has same name with .parse extension.
        ArrayList<File> treefilelist = new ArrayList<File> ();
        for (File propfile : this.filelist) {
            String name = propfile.getName();
            name = name.replaceAll(".coref", ".parse");
            File nf = new File(propfile.getParentFile(), name);
            if (nf.exists()) {
                treefilelist.add(nf);
            } else
                throw new IllegalArgumentException("The treebank parse file is required and did not exist for "+propfile);
        }
        otr = new OntonotesTreebankReader(dir, language, treefilelist);
    }
    
    /**
     * Lines have no meaning, since the input data is XML. We will construct an
     * input buffer, replacing each line with a "\n" so we can use an XML parser
     * to produce the data.
     */
    @Override
    protected TextAnnotation parseLines(ArrayList<String> lines) throws AnnotatorException {
        
        if (!this.otr.hasNext())
            throw new RuntimeException("There were not as many treebank files as there were coref files.");
        
        // get the treebank parse using the ontonotes treebank reader.
        TextAnnotation tbta = this.otr.next();
        if (tbta == null)
            return null;

        if (lines.size() == 0) 
            return null;
        // construct a single string
        StringBuffer sb = new StringBuffer(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            sb.append(" ");
            sb.append(lines.get(i));
        }
        
        // produce a document object.
        String text = sb.toString();
        Document doc = null;
        try {
            doc = SimpleXMLParser.getDocument(
                    new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8.name())));
        } catch (UnsupportedEncodingException | XMLException e) {
            throw new AnnotatorException("Could not decode the text from the XML document.");
        }
        if (doc == null) {
            throw new AnnotatorException("Could not decode the text from the XML document.");
        }
        
        // Get a list of coref mentions
        ArrayList<CorefMention> hits = new ArrayList<>();
        try {
            traverse(tbta, 0, hits, doc.getDocumentElement(), "");
        } catch (AnnotatorException ape) {
            ape.printStackTrace();
        }
        
        // we have all the hits, organize them into referant chains.
        HashMap<String, ArrayList<CorefMention>> chains = new HashMap<>();
        for (CorefMention cm : hits) {
            ArrayList<CorefMention> chain = chains.get(cm.id);
            if (chain == null) {
                chain = new ArrayList<CorefMention>();
                chains.put(cm.id, chain);
            }
            chain.add(cm);
        }
        if (debug)
            System.out.println(this.currentfile+" produced "+chains.size()+" chains.");
        CoreferenceView corefView = new CoreferenceView(COREF_VIEW_NAME, COREF_VIEW_NAME, tbta, 0.0);
        for (Entry<String, ArrayList<CorefMention>> entry : chains.entrySet()) {
            String id = entry.getKey();
            ArrayList<CorefMention> mentions = entry.getValue();
            CorefMention head = mentions.get(0);
            Constituent headconst = new Constituent(head.id, COREF_VIEW_NAME, tbta, head.location.getFirst(), 
                head.location.getSecond());
            head.constituent = headconst;
            // These are added by the addCorefEdges call. corefView.addConstituent(headconst);
            ArrayList<Constituent> referants = new ArrayList<>();
            for (int i = 1; i < mentions.size(); i++) {
                CorefMention cm = mentions.get(i);
                Constituent constituent = new Constituent(cm.id, COREF_VIEW_NAME, tbta, cm.location.getFirst(), 
                    cm.location.getSecond());
                cm.constituent = constituent;
                // These are added by the addCorefEdges call. corefView.addConstituent(constituent);
                referants.add(constituent);
            }
            corefView.addCorefEdges(headconst, referants);
        }
        tbta.addView(COREF_VIEW_NAME, corefView);
        
        // now we have the chains, we need to but construct a view.
        return tbta;
    }
    
    /**
     * traverse the document model, collecting hits and their locations as we go along. We 
     * also validate, ensuring the tokens gotten from the coref data matches the tokens parsed
     * for the treebank.
     * @param treeta the text annotation for the treebank.
     * @param tokenoffset the offset of the current word token.
     * @param hits CorefMentions are added to this list.
     * @param node the node we are looking at.
     * @return the updated token offset.
     */
    public int traverse(TextAnnotation treeta, int tokenoffset, ArrayList<CorefMention> hits, Node node, String ident) 
                    throws AnnotatorException {     
        NodeList nodeList = node.getChildNodes();
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent();
            String trimmed = text.trim();
            String [] tokens = trimmed.split("[\\s]+");
            if (tokens.length == 0 || (tokens.length == 1 && tokens[0].length() == 0))
                return tokenoffset;
            if (debug)
                System.out.println(tokenoffset+":"+(tokenoffset + tokens.length)+" - "+text);
            int start = tokenoffset;
            int end = tokenoffset + tokens.length;
            int nontokens = 0;
            int consecutive = 0;
            for (int i = start ; i < end; i++) {
                String t1 = treeta.getToken(i-nontokens);
                String t2 = tokens[i-tokenoffset];
                if (!tokensEqual(t1, t2)) {
                    if (debug)
                        System.out.println("** "+t1+" != "+t2);
                    nontokens++; // These are tokens not in the text, inserted by linguists.
                    if (consecutive > 3) {
                        System.out.println("\nEncountered a problem that rendered the document useless.");
                        System.out.println("original test:");
                        System.out.println(trimmed);
                        System.out.println("treebank ridiculousness");
                        StringBuffer sb = new StringBuffer();
                        for (String t : treeta.getTokensInSpan(start, end)) {
                            sb.append(t);
                            sb.append(" ");
                        }
                        System.out.println(sb.toString());
                        throw new AnnotatorException("Tokens didn't allign between treebank parse and tokens.");
                    }
                    consecutive++;
                } else
                    consecutive = 0;
            }
            return tokenoffset + tokens.length - nontokens;
        } else {
        
            // this is a content node.
            if (debug)
                System.out.println(ident+node.getNodeName());
            int intokenoffset = tokenoffset;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);
                tokenoffset = traverse(treeta, tokenoffset, hits, currentNode, ident+"   ");
            }
            if (node.getNodeName().equals("COREF")) {
                // this is a coref node, collect up it's attributes and add a coref object
                NamedNodeMap attrs = node.getAttributes();
                String id = null, type = null, speaker = null;
                for (int i = 0; i < attrs.getLength(); ++i) {
                    Node attr = attrs.item(i);
                    String nodename = attr.getNodeName().toLowerCase();
                    if (nodename.equals("id")) {
                        id = attr.getNodeValue();
                    } else if (nodename.equals("type")) {
                        type = attr.getNodeValue();
                    } else if (nodename.equals("speaker")) {
                        speaker = attr.getNodeValue();
                    }
                }
                CorefMention cm = new CorefMention(id,type,speaker);
                cm.location = new IntPair(intokenoffset, tokenoffset);
                hits.add(cm);
            }
            return tokenoffset;
        }
    }

    /**
     * Determine if the two tokens are equal. Might have to replace some embedded encodings
     * like -LRB-, -RRB- and -AMP-. If no match can be found after this mangling, the the 
     * corefToken must be implied, therefor not actually in the text.
     * @param taToken the token from the treebank text annotation.
     * @param corefToken the coref token.
     * @return true if the two tokens are equal.
     */
    private boolean tokensEqual(String taToken, String corefToken) {
        if (taToken.equals(corefToken)) {
            return true;
        } else {

            // normalize, convert all -AMP- to & first.
            String ampless = corefToken.replaceAll("-AMP-", "&");
            // Now, -LRB- can be ( or [ or {, so we need to try them all.
            ampless = ampless.replaceAll("-LSB-", "[");
            ampless = ampless.replaceAll("-LRB-", "(");
            ampless = ampless.replaceAll("-RRB-", ")");
            ampless = ampless.replaceAll("-RSB-", "]");
            ampless = ampless.replaceAll("-RAB-", ">");
            ampless = ampless.replaceAll("-LAB-", "<");
            ampless = ampless.replaceAll("-LCB-", "{");
            ampless = ampless.replaceAll("-RCB-", "}");

            // garbage in, take a best guess
            ampless = ampless.replaceAll("-RSB", "]");

            return taToken.equals(ampless);
        }
    }

    @Override
    public String generateReport() {
        return null;
    }
    /**
     * This wrapper class contains info about each mention. The ID's associate
     * relations
     * @author redman
     */
    class CorefMention {
        
        /** the ID of the entity. */
        String id;
        
        /** the type, IDENT or APPOS. */
        String type;
        
        /** the speaker, which may not be defined. */
        String speaker;
        
        /** the location of the span for this mention. */
        IntPair location = null;
        
        /** the constituent. */
        Constituent constituent = null;
        
        /**
         * Mark a mention.
         * @param id the chain id.
         * @param type the type, IDENT or APPOS
         * @param speaker the speaker or null.
         */
        CorefMention(String id, String type, String speaker) {
            this.id = id;
            this.type = type;
            this.speaker = speaker;
        }
        
        /**
         * @return the representative string.
         */
        public String toString() {
            return id+" : "+type+" : "+(speaker==null ? " : " : speaker+" : ")+location.toString();
        }
    }
    /**
     * This class will read the ontonotes data from the provided directory, and write the resulting
     * serialized json form of the penn bank data to the specified output directory. It will retain
     * the directory structure of the original data.
     * @param args command lines args specify input data directory, language and output directory.
     * @throws IOException
     */
    static public void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("This executable requires four arguments:\n"
                    + " OntonotesTreebankReader <OntoNotes Directory> <language> <output_directory>");
            System.exit(-1);
        }
        String topdir = args[0];
        OntonotesCorefReader otr = new OntonotesCorefReader(topdir, args[1]);
        int count = 0;
        while (otr.hasNext()) {
            TextAnnotation ta = otr.next();
            if (ta != null) {
                String json = SerializationHelper.serializeToJson(ta);
                
                String outfile = otr.currentfile.replace(topdir, args[2]);
                File outputfile = new File(outfile);
                outputfile.getParentFile().mkdirs();
                try (PrintWriter out = new PrintWriter(outputfile)) {
                    out.print(json);
                }
                
                count++;
                if ((count % 100) == 0)
                    System.out.println("Completed "+count+" of "+otr.filelist.size());
            }
        }
        System.out.println(otr.generateReport());
    }

}
