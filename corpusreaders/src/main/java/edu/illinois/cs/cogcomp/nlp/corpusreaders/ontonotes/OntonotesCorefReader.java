package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.CoreferenceView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.SimpleXMLParser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils.DocumentIterator.FileKind;

/**
 * This is the ontonotes coref reader. 
 * @author redman
 */
public class OntonotesCorefReader extends AbstractOntonotesReader {

    /** the name of our view. */
    static final public String VIEW_NAME = "ONTONOTES_5_GOLD_COREF";
    
    /** name of the attribute with teh mention type. */
    static final public String MENTION_TYPE_LABEL = "Mention Type";

    /** we will use this to generate the text annotation with the treebank parse, 
     * then we will add the coref parse over that. Treebank is the reference data
     * for tokenization and sentence splitting. */
    private OntonotesTreebankReader otr = null;

    /** NER view used to get named entities to identify proper nouns. */
    private OntonotesNamedEntityReader oner = null;

    /** turn on to debug. */
    static final private boolean debug = false;
        
    /** the number successfully processed. */
    int processed = 0;
    
    /**
     * INit the reader with a directory containing language subdirectories.
     * @param dir the directory containing the data.
     * @param language the language.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public OntonotesCorefReader(String dir, String language) throws IllegalArgumentException, IOException {
        super(VIEW_NAME, dir, language, FileKind.coref);
        
        // Need to be able to iterate over the ".parse" files so we can vet the required 
        // treebank data. We have to make sure for each propbank file we have, we have the
        // corresponding treebank file. treebank file has same name with .parse extension.
        ArrayList<File> treefilelist = new ArrayList<File> ();
        ArrayList<File> nerfilelist = new ArrayList<File> ();
        for (File propfile : this.filelist) {
            String name = propfile.getName();
            name = name.replaceAll(".coref", ".parse");
            File nf = new File(propfile.getParentFile(), name);
            if (nf.exists()) {
                treefilelist.add(nf);
            } else {
                
                // we require treebank file to process
                throw new IllegalArgumentException("The treebank parse file is required and did not exist for "+propfile);
            }
            
            // ner is optional, sort of.
            name = propfile.getName().replaceAll(".coref", ".name");
            nf = new File(propfile.getParentFile(), name);
            nerfilelist.add(nf);
        }
        otr = new OntonotesTreebankReader(dir, language, treefilelist);
        oner = new OntonotesNamedEntityReader(dir, language, nerfilelist);
    }
    
    /**
     * Lines have no meaning, since the input data is XML. We will construct an
     * input buffer, replacing each line with a "\n" so we can use an XML parser
     * to produce the data.
     */
    @Override
    protected TextAnnotation parseLines(ArrayList<String> lines) throws AnnotatorException {
        
        // get the treebank pase data
        if (!this.otr.hasNext())
            throw new RuntimeException("There were not as many treebank files as there were coref files.");
        
        // get the treebank parse using the ontonotes treebank reader.
        TextAnnotation resultTA = this.otr.next();
        if (resultTA == null)
            return null;
        
        View nerView = null;
        String[] nerTokens = null;
        TextAnnotation nerTA = null;
        
        // get the named entity data.
        if (!this.oner.hasNext()) {
            return null; // no NER, return.
        } else {
            
            // All this code is just to get the named entity
            XmlTextAnnotation xmlta = this.oner.next();
            if (xmlta != null) {
                nerTA = xmlta.getTextAnnotation();
                if (nerTA == null) {
                    System.err.println("There was no NER text annotation in \""+this.oner.currentfile+"\"");
                    return null;
                } else {
                    nerView = nerTA.getView(OntonotesNamedEntityReader.VIEW_NAME);
                    if (nerView == null) {
                        System.err.println("There was no NER view in \""+this.oner.currentfile+"\"");
                        return null;
                    }
                    nerTokens = nerTA.getTokens();
                }
            } else {
                return null; // the file did nto exist.
            }
        }
        
        // nothing to work on, just return.
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
        
        // Get a list of coref mentions object wrappers, these contain all the info we need
        // to construct the coref chains.
        ArrayList<CorefMention> hits = new ArrayList<>();
        traverse(resultTA, 0, hits, doc.getDocumentElement(), "");
        
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

        CoreferenceView corefView = new CoreferenceView(VIEW_NAME, VIEW_NAME, resultTA, 0.0);
        for (Entry<String, ArrayList<CorefMention>> entry : chains.entrySet()) {
            ArrayList<CorefMention> mentions = entry.getValue();
            CorefMention head = mentions.get(0);
            Constituent headconst = new Constituent(head.id, VIEW_NAME, resultTA, head.location.getFirst(), 
                head.location.getSecond());
            head.constituent = headconst;
            
            // These are added by the addCorefEdges call. corefView.addConstituent(headconst);
            if (debug)
                System.out.println(head+" -> "+head.constituent.getSurfaceForm());
            ArrayList<Constituent> referants = new ArrayList<>();
            ArrayList<HashMap<String,String>> attributes = new ArrayList<>();
            for (int i = 1; i < mentions.size(); i++) {
                CorefMention cm = mentions.get(i);
                Constituent constituent = new Constituent(cm.id, VIEW_NAME, resultTA, cm.location.getFirst(), 
                    cm.location.getSecond());
                cm.constituent = constituent;
                // These are added by the addCorefEdges call. corefView.addConstituent(constituent);
                referants.add(constituent);
                
                // set up the attributes for the relation, just type and subtype.
                HashMap<String, String> attribute = new HashMap<>();
                if (cm.type != null) 
                    attribute.put("TYPE", cm.type);
                if (cm.subtype != null)
                    attribute.put("SUBTYPE", cm.subtype);
                if (cm.speaker != null)
                    attribute.put("SPEAKER", cm.speaker);
                attributes.add(attribute);
                
                if (debug)
                    System.out.println("    "+cm+" -> "+cm.constituent.getSurfaceForm());
            }
            corefView.addCorefEdges(headconst, referants, attributes);
        }
        
        // now for each constituent in our view, determine what type of mention it is. 
        // Here we will project the named entities from the ".name" file onto this annotation
        // and create a named entity view
        String [] coreftokens = resultTA.getTokens();
        
        // allign token offsets, for each coref token, the corresponding offset of the START of the ner token.
        int [] tokenAlignment = new int[nerTokens.length]; // this aligns token offsets.
        for (int ci = 0, ni = 0; ci < coreftokens.length && ni < nerTokens.length ;) {
            tokenAlignment[ni] = ci;
            if (coreftokens[ci].equals(nerTokens[ni])) {
                ni++;
                ci++;
            } else {
                
                // our tokens didn't align. Some symbols are treated differently
                // so where we see "&", "$", "-" and so on, there maybe different 
                // tokenizations, so try to append successive ner tokens to see if they
                // then match.
                String ctok = coreftokens[ci];
                String ntok = nerTokens[ni];
                int niplus = 0;
                
                // as long as the appended ner token contains the coref token, possible match
                while (true) {
                    if (ctok.equals(ntok)) {
                        break;
                    } else {
                        if (ctok.contains(ntok)) {
                            niplus++;
                            ntok += nerTokens[ni+niplus];
                        } else if (ntok.contains("-")) {
                            // check for XML escapes.
                            if (this.compareWithXMLEscapesIgnoreGarbageIn(ctok, ntok)) {
                                break;
                            } else {
                                niplus++;
                                if (ni+niplus >= nerTokens.length)
                                    break;  // give up.
                                ntok += nerTokens[ni+niplus];
                            }
                        } else {
                            System.out.flush();
                            System.err.println("\nTokens were simply different in "+this.currentfile+" around "+ni+" and "+ci);
                            for (int cci = ci - 10, i = 0 ; i < 30; i++, cci++) {
                                System.err.print(" "+coreftokens[cci]);
                            }
                            System.err.println();
                            for (int nni = ni - 8, i = 0 ; i < 30; i++, nni++) {
                                if (nni == ni) System.err.print(" *");
                                else System.err.print(" ");
                                System.err.print(nerTokens[nni]);
                            }
                            System.err.println();
                            System.err.flush();
                            return null;
                        }
                    }
                }
                if (ctok.equals(ntok) || this.compareWithXMLEscapes(ctok, ntok)) {

                    // we matched
                    ni += niplus;
                    ni++;
                    ci++;
                } else {
                    System.out.flush();
                    System.err.println("\nNo alignment in "+this.currentfile+" around "+ni+" and "+ci);
                    for (int cci = ci - 10, i = 0 ; i < 30; i++, cci++) {
                        System.err.print(" "+coreftokens[cci]);
                    }
                    System.err.println();
                    for (int nni = ni - 8, i = 0 ; i < 30; i++, nni++) {
                        if (nni == ni) System.err.print(" *");
                        else System.err.print(" ");
                        System.err.print(nerTokens[nni]);
                    }
                    System.err.println();
                    System.err.flush();
                    return null;
                }
            }
        }
        
        // now transpose the NER view to the coref tokenization.
        SpanLabelView tv = new SpanLabelView(OntonotesNamedEntityReader.VIEW_NAME, resultTA);
        for (Constituent c : nerView.getConstituents()) {
            int start = tokenAlignment[c.getStartSpan()];
            int end = c.getEndSpan() >= tokenAlignment.length ? tokenAlignment[tokenAlignment.length - 1] :
                tokenAlignment[c.getEndSpan()];
            try {
                String lbl = c.getLabel();
                tv.addSpanLabel(start, end, lbl, c.getConstituentScore());
            } catch (IllegalArgumentException iae) {
                // overlapping constituent which span label view apparently does not support.
            }                   
        }
        
        if (resultTA != null) {
            resultTA.addView(OntonotesCorefReader.VIEW_NAME, corefView);
            resultTA.addView(OntonotesNamedEntityReader.VIEW_NAME, tv);
            View posView = (View)resultTA.getView(OntonotesTreebankReader.VIEW_NAME);
            
            // new identify mention types.
            System.out.println("\n-------\nMention Types for "+this.currentfile);
            for (Constituent c : corefView.getConstituents()) {
                this.setMentionType(c, tv, posView);
                System.out.println(c.getSurfaceForm()+" -> "+c.getAttribute(MENTION_TYPE_LABEL));
            }
            processed++;
        }
        return resultTA;
    }
    
    /**
     * from the NER data and the part of speech data, determine the best mention type
     * for the constituent at that location.
     * @param constituent the constituent to identify label type
     * @param nerView the named entity view.
     * @param posView the part of speech view.
     */
    private void setMentionType(Constituent constituent, View nerView, View posView) {
        List<Constituent> nerConsts = nerView.getConstituentsCovering(constituent);
        List<Constituent> posConsts = posView.getConstituentsCovering(constituent);
        for (int i = 0; i < posConsts.size(); i++) {
            Constituent posConst = posConsts.get(i);
            if (posConst.getStartSpan() == constituent.getStartSpan() && 
                            posConst.getEndSpan() == constituent.getEndSpan()) {
                String pL = posConst.getLabel();
                if (pL.equals("PRP$") || pL.equals("PRP")) {
                    constituent.addAttribute(MENTION_TYPE_LABEL, "Pronoun");
                    return;
                }
            }
        }
        for (int i = 0; i < nerConsts.size(); i++) {
            Constituent nerConst = nerConsts.get(i);
             
            // ONLY named entities that align exactly with the mention used here.
            if (nerConst.getStartSpan() == constituent.getStartSpan() && 
                            nerConst.getEndSpan() == constituent.getEndSpan()) {
                String nL = nerConst.getLabel();
                if (nL.startsWith("PER") || nL.startsWith("ORG") || nL.startsWith("GPE")) {
                    constituent.addAttribute(MENTION_TYPE_LABEL, "Name");
                    return;
                }
            }
        }
        constituent.addAttribute(MENTION_TYPE_LABEL, "Nominal");
    }

    /** the text section. */
    static String partno = null;
    
    /**
     * traverse the document model, collecting hits and their locations as we go along. We 
     * also validate, ensuring the tokens gotten from the coref data matches the tokens parsed
     * for the treebank.
     * @param treeta the text annotation for the treebank.
     * @param tokenoffset the offset of the current word token.
     * @param hits CorefMentions are added to this list.
     * @param node the node we are looking at.
     * @param ident the indent to pretty up the output.
     * @param partno  the unique id of the text section.
     * @return the updated token offset.
     * @throws AnnotatorException 
     */
    public int traverse(TextAnnotation treeta, int tokenoffset, ArrayList<CorefMention> hits, Node node, 
            String ident)  throws AnnotatorException {     
        NodeList nodeList = node.getChildNodes();
        if (node.getNodeType() == Node.TEXT_NODE) {
            
            // this node is a chunk of text.
            String text = node.getTextContent();
            String trimmed = text.trim();
            String [] tokens = trimmed.split("[\\s]+");
            if (tokens.length == 0 || (tokens.length == 1 && tokens[0].length() == 0))
                return tokenoffset;
            int start = tokenoffset;
            int end = tokenoffset + tokens.length;
            int nontokens = 0;
            int consecutive = 0;
            for (int i = start ; i < end; i++) {
                String t1 = treeta.getToken(i-nontokens);
                String t2 = tokens[i-tokenoffset];
                if (!tokensEqual(t1, t2)) {
                    nontokens++; // These are tokens not in the text, inserted by linguists.
                    if (consecutive > 3) {
                        System.err.println("\nEncountered a problem that rendered the document useless in "+this.currentfile);
                        System.err.println("original text:");
                        System.err.println(trimmed);
                        System.err.println("treebank text:");
                        StringBuffer sb = new StringBuffer();
                        for (String t : treeta.getTokensInSpan(start, end)) {
                            sb.append(t);
                            sb.append(" ");
                        }
                        System.err.println(sb.toString());
                        throw new AnnotatorException("Tokens didn't align between treebank parse and tokens in "+this.currentfile);
                    }
                    consecutive++;
                } else
                    consecutive = 0;
            }
            return tokenoffset + tokens.length - nontokens;
        } else {
        
            // this is a content node.
            int intokenoffset = tokenoffset;
            if (node.getNodeName().equals("TEXT")) {
                NamedNodeMap nl = node.getAttributes();
                Node fo = nl.getNamedItem("PARTNO");
                partno = fo.getNodeValue();
                if (partno == null)
                    System.err.println("BADNESS");
            }
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);
                tokenoffset = traverse(treeta, tokenoffset, hits, currentNode, ident+"   ");
            }
            if (node.getNodeName().equals("COREF")) {
                // this is a coref node, collect up it's attributes and add a coref object
                NamedNodeMap attrs = node.getAttributes();
                String id = null, type = null, subtype = null, speaker = null;
                for (int i = 0; i < attrs.getLength(); ++i) {
                    Node attr = attrs.item(i);
                    String nodename = attr.getNodeName().toLowerCase();
                    if (nodename.equals("id")) {
                        id = partno+"-"+attr.getNodeValue();
                    } else if (nodename.equals("type")) {
                        type = attr.getNodeValue();
                    } else if (nodename.equals("subtype")) {
                        subtype = attr.getNodeValue();
                    } else if (nodename.equals("speaker")) {
                        speaker = attr.getNodeValue();
                    }
                }
                CorefMention cm = new CorefMention(id,type,subtype,speaker);
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
            return compareWithXMLEscapes(taToken, corefToken);
        }
    }

    /**
     * Compare the first token with the second token, but replace any encoded escapes in the 
     * second string before the compare. Brackets, parens, ampersands and things like that are
     * escaped to no break XML encoding.
     * @param plainToken the token without any potential XML escaped characters.
     * @param xmlToken the token with potential XML escaped characters.
     * @return true if they are the same with escapes removed.
     */
    private boolean compareWithXMLEscapes(String plainToken, String xmlToken) {
        // normalize, convert all -AMP- to & first.
        String ampless = xmlToken.replaceAll("-AMP-", "&");
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

        return plainToken.equals(ampless);
    }

    /**
     * Compare the first token with the second token, but replace any encoded escapes in the 
     * second string before the compare. Brackets, parens, ampersands and things like that are
     * escaped to no break XML encoding.
     * @param plainToken the token without any potential XML escaped characters.
     * @param xmlToken the token with potential XML escaped characters.
     * @return true if they are the same with escapes removed.
     */
    private boolean compareWithXMLEscapesIgnoreGarbageIn(String plainToken, String xmlToken) {
        // normalize, convert all -AMP- to & first.
        String ampless = xmlToken.replaceAll("-AMP-", "&");
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
        // this is just an annotator mistake. ampless = ampless.replaceAll("-RSB", "]");

        return plainToken.equals(ampless);
    }
    
    @Override
    public String generateReport() {
        return "Generated JSON representations of "+processed+" documents from a total of "+this.filelist.size()+" documents.";
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
        
        /** the type, IDENT or APPOS. */
        String subtype;
        
        /** the speaker, which may not be defined. */
        String speaker;
        
        /** the location of the span for this mention. */
        IntPair location = null;
        
        /** the constituent. */
        Constituent constituent = null;
        
        /**
         * Mark a mention.
         * @param id the chain id.
         * @param type the type, IDENT or APPOS.
         * @param subtype the subtype.
         * @param speaker the speaker or null.
         */
        CorefMention(String id, String type, String subtype, String speaker) {
            this.id = id;
            this.type = type;
            this.subtype = subtype;
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
        while (otr.hasNext()) {
            //System.out.println("# "+otr.currentfile);
            TextAnnotation ta = otr.next();
            if (ta != null) {
                String json = SerializationHelper.serializeToJson(ta);
                
                String outfile = otr.currentfile.replace(topdir, args[2]);
                File outputfile = new File(outfile);
                outputfile.getParentFile().mkdirs();
                try (PrintWriter out = new PrintWriter(outputfile)) {
                    out.print(json);
                }
                
                if ((otr.processed % 100) == 0)
                    System.out.println("Completed "+otr.processed+" of "+otr.filelist.size());
                if (debug) 
                    System.exit(0);
            }
        }
        System.out.println(otr.generateReport());
    }
}
