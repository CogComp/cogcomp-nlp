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
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.EventConstants;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.Paragraph;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.EventConstants.*;

/**
 * reads a set of related ACE files corresponding to a single document,
 *    builds an ACEDocument object
 *
 * Created by mssammon on 8/27/15.
 */
public class AceFileProcessor
{
    private static final String NAME = AceFileProcessor.class.getCanonicalName();
    private static Set<String> stopWords;
    private static boolean useFilter = false;
    private final TextAnnotationBuilder taBuilder;
    private static final boolean isDebug = false;

    private static Pattern isCapPattern = Pattern.compile("^[A-Z]");

    public AceFileProcessor( TextAnnotationBuilder taBuilder )
    {
        this.taBuilder = taBuilder;
        String[] stopwordToks = { "I", "You", "They", "He", "She", "It", "We", "My" };

        stopWords = new HashSet<>();

        for ( String tok : stopwordToks )
            stopWords.add( tok );
    }

    /**
     * given a subfolder entry corresponding to a single ACE document, build an ACEDocument object
     *   with a simple TextAnnotation for the text (with tokenization and sentence splits), and
     *   fields representing the different types of ACE annotation (entities, relations, events, etc.)
     *
     * @param subFolderEntry
     * @param annotationFile full path to main annotation file (.apf.xml)
     * @return null if file cannot be parsed, ACEDocument otherwise
     */
    public ACEDocument processAceEntry( File subFolderEntry, String annotationFile )
            throws FileNotFoundException {

        ACEDocumentAnnotation annotationACE = null;
        try {
            annotationACE = ReadACEAnnotation.readDocument(annotationFile);
        } catch (XMLException e) {
            e.printStackTrace();
            return null;
        }
        return processAceEntry( subFolderEntry, annotationACE, annotationFile );
    }

    /**
     * processes the main annotation file-- accessing other files with same prefix in same directory --
     *   to create an ACEDocument structure. The ACEDocument has a list of TextAnnotations corresponding to paragraphs,
     *   and which also have a view recording character offsets corresponding to ACE gold annotations, but no views
     *   corresponding to the gold annotations themselves (relations, entities etc.).
     *
     * The gold annotations are kept in lists of ACE-specific data structures.
     *
     * @param subFolderEntry
     * @param annotationACE
     * @param annotationFile
     * @return
     */

    public ACEDocument processAceEntry(File subFolderEntry, ACEDocumentAnnotation annotationACE, String annotationFile)
            throws FileNotFoundException {
        boolean is2004mode = ReadACEAnnotation.is2004mode;
        ACEDocument aceDoc = new ACEDocument();

        String docFile = annotationFile.replace(".apf.xml", ".sgm");
        List<String> lines = LineIO.read(docFile);
        String content = "";
        for (int i = 0; i < lines.size(); ++i) {
            content += lines.get(i) + "\n";
        }

        // Some files have stray "<ANNOTATION>" tags. Removing them before processing.
        content = content.replaceAll("<ANNOTATION>", "").replaceAll("</ANNOTATION>", "");
        String contentWithoutEnter = content.replaceAll("\n", " ");
        String contentRemovingTags = contentWithoutEnter;
        while (contentRemovingTags.contains("<")) {
            int p = contentRemovingTags.indexOf('<');
            int q = contentRemovingTags.indexOf('>');
            contentRemovingTags = contentRemovingTags.substring(0,p)
                    + contentRemovingTags.substring(q+1, contentRemovingTags.length());
        }


        List<Pair<String, Paragraph>> paraList = null;
        if (subFolderEntry.getAbsolutePath().endsWith("bc")) {
            paraList = ACE_BC_Reader.parse(contentWithoutEnter, contentRemovingTags);
        }
        if (subFolderEntry .getAbsolutePath().endsWith("bn")) {
            paraList = ACE_BN_Reader.parse(contentWithoutEnter, contentRemovingTags, is2004mode);
        }
        if (subFolderEntry .getAbsolutePath().endsWith("cts")) {
            paraList = ACE_CTS_Reader.parse(contentWithoutEnter, contentRemovingTags);
        }
        if (subFolderEntry .getAbsolutePath().endsWith("nw")) {
            paraList = ACE_NW_Reader.parse(contentWithoutEnter, contentRemovingTags);
        }
        if (subFolderEntry .getAbsolutePath().endsWith("un")) {
            paraList = ACE_UN_Reader.parse(contentWithoutEnter, contentRemovingTags);
        }
        if (subFolderEntry .getAbsolutePath().endsWith("wl")) {
            paraList = ACE_WL_Reader.parse(contentWithoutEnter, contentRemovingTags);
        }
        if ( null != paraList )
        {
            for (int j = 0; j < paraList.size(); ++j) {
                if (paraList.get(j).getFirst().equals("text")) {
                    String text = StringCleanup.normalizeToAscii(paraList.get(j).getSecond().content);
                    text = text.replaceAll(" -", "  ");
                    text = text.replaceAll("- ", "  ");

                    text = text.replace('<', '(');
                    text = text.replace('>', ')');

                    if (text.equals(""))
                        continue;

                    String corpusID = is2004mode ? "ACE2004" : "ACE2005";
                    TextAnnotation ta = taBuilder.createTextAnnotation( corpusID, annotationACE.id + "_" + j, text );
                    alignTokenToCharOffset(contentRemovingTags, text, paraList.get(j).getSecond().offsetFilterTags, ta);

                    aceDoc.taList.add(ta);
                }
            }
        }

        aceDoc.aceAnnotation = annotationACE;
        aceDoc.orginalContent = content;
        aceDoc.contentRemovingTags = contentRemovingTags;
        aceDoc.originalLines = lines;
        aceDoc.paragraphs = paraList;

        return aceDoc;
    }


    /**
     * generates an additional view of Tokens with character offsets as string attributes,
     *    where those character offsets correspond to offsets in the original source document
     *
     * @param origDocWithoutTags
     * @param paraText
     * @param textOffset
     * @param ta
     */
    public static void alignTokenToCharOffset(String origDocWithoutTags, String paraText,
                                              int textOffset, TextAnnotation ta) {

        int pointer=0, start=0, end=-1;

        SpanLabelView offsetView = new SpanLabelView(TOKEN_WITH_CHAR_OFFSET, "Default", ta, 1.0, false);
        String[] tokens = ta.getTokens();

        for(int i=0; i<tokens.length; i++) {
            String token = tokens[i];
            int tokenLength = token.length();
            start = paraText.indexOf(token, pointer);
            assert start!=-1;

            end = start+tokenLength - 1;

            String testText = origDocWithoutTags.substring(textOffset + start, textOffset + end + 1);

            if (start == -1 || end == -1) {
                System.out.println(token);
            }

            if (isDebug) {
                System.out.println(token + "-->" + testText);
            }

            Constituent con = new Constituent(token, TOKEN_WITH_CHAR_OFFSET, ta, i, i+1);
            con.addAttribute(CHAR_START, new Integer(start + textOffset).toString());
            con.addAttribute(CHAR_END, new Integer(end + textOffset).toString());
            offsetView.addConstituent(con);
            pointer = end;
        }
        //System.out.println("");
        ta.addView(TOKEN_WITH_CHAR_OFFSET, offsetView);

    }

    /**
     * read the aceDocument annotations and populate TextAnnotation with corresponding views.
     * WARNING: currently, only populates Entity view (name: EventConstants.NER_ACE_COARSE)
     *        quantity view ( name: EventConstants.NER_ACE_QUANTITY) and timex view
     *        (name: EventConstants.NER_ACE_TIME)
     *
     * @param aceDocument
     * @return
     */
    public static List< TextAnnotation > populateTextAnnotation( ACEDocument aceDocument )
    {
        List<TextAnnotation> taList  = aceDocument.taList;

        addEntityViews(aceDocument, taList);
        addQuantityView(aceDocument, taList);
        addTimexView( aceDocument, taList );

        return taList;
    }

    private static void addTimexView(ACEDocument aceDocument, List<TextAnnotation> taList) {

        List<ACETimeEx> times = aceDocument.aceAnnotation.timeExList;
        Map< TextAnnotation, List< Constituent > > taToTimes = new HashMap<>();

        for ( ACETimeEx e : times ) {
            String type = EventConstants.TIME_ENTITY_TYPE;

            for (ACETimeExMention m : e.timeExMentionList ) {

                if ( !useFilter || !isFiltered(m.extent) ) {
                    TextAnnotation ta = findTextAnnotation(m.extentStart, m.extentEnd, taList);

                    if ( null == ta ) // can happen: the text we read doesn't contain all mentions, apparently.
                        continue;

                    IntPair taTokenOffsets = findTokenOffsets(ta, m.extentStart, m.extentEnd);

                    if (null != taTokenOffsets) // will be null for e.g. sub-token mentions
                    {
                        Constituent c = new Constituent(type, 1.0, EventConstants.NER_ACE_TIME, ta, taTokenOffsets.getFirst(), taTokenOffsets.getSecond());
                        addConstituentToTaNeMap( taToTimes, ta, c );
                    }
                }
            }
        }
        for ( TextAnnotation ta : taToTimes.keySet() )
            processEntities(ta, taToTimes.get(ta), EventConstants.NER_ACE_TIME );


    }

    private static void addQuantityView(ACEDocument aceDocument, List<TextAnnotation> taList) {

        List<ACEValue> quantities = aceDocument.aceAnnotation.valueList;
        Map< TextAnnotation, List< Constituent > > taToQuantities = new HashMap<>();

        for ( ACEValue e : quantities ) {
            String type = e.type;

            for (ACEValueMention m : e.valueMentionList ) {

                if ( !useFilter || !isFiltered(m.extent) ) {
                    TextAnnotation ta = findTextAnnotation(m.extentStart, m.extentEnd, taList);

                    if ( null == ta ) // can happen: the text we read doesn't contain all mentions, apparently.
                        continue;

                    IntPair taTokenOffsets = findTokenOffsets(ta, m.extentStart, m.extentEnd );

                    if (null != taTokenOffsets) // will be null for e.g. sub-token mentions
                    {
                        Constituent c = new Constituent(type, 1.0, EventConstants.NER_ACE_QUANTITY, ta, taTokenOffsets.getFirst(), taTokenOffsets.getSecond());
                        addConstituentToTaNeMap( taToQuantities, ta, c );
                    }
                }
            }
        }
        for ( TextAnnotation ta : taToQuantities.keySet() )
            processEntities(ta, taToQuantities.get(ta), EventConstants.NER_ACE_QUANTITY );

    }

    /**
     * Adds two NE views: a coarse NER view and a fine NER view.
     * The first uses only coarse-grained types.
     * The second uses fine-grained types where available, or the coarse-grained types when fine-grained is
     *   not specified.
     *
     * @param aceDocument
     * @param taList
     */
    private static void addEntityViews(ACEDocument aceDocument, List<TextAnnotation> taList)
    {
        // note that TextAnnotations have an extra view for tokens with global char offsets
        List<ACEEntity> entities = aceDocument.aceAnnotation.entityList;

        Map< TextAnnotation, List< Constituent > > taToCoarseNeEntities = new HashMap<>();
        Map< TextAnnotation, List< Constituent > > taToFineNeEntities = new HashMap<>();

        for ( ACEEntity e : entities ) {
            String type = e.type;
            String fineType = e.subtype;

            for (ACEEntityMention m : e.entityMentionList) {

                if ( !useFilter || !isFiltered(m.extent) ) {
                    TextAnnotation ta = findTextAnnotation(m.extentStart, m.extentEnd, taList);

                    if ( null == ta ) // can happen: the text we read doesn't contain all mentions, apparently.
                        continue;

                    IntPair taTokenOffsets = findTokenOffsets(ta, m.extentStart, m.extentEnd);

                    if (null != taTokenOffsets) // will be null for e.g. sub-token mentions
                    {
                        Constituent c = new Constituent(type, 1.0, EventConstants.NER_ACE_COARSE, ta, taTokenOffsets.getFirst(), taTokenOffsets.getSecond());
                        addConstituentToTaNeMap( taToCoarseNeEntities, ta, c );

                        Constituent f = new Constituent(type, 1.0, EventConstants.NER_ACE_FINE, ta, taTokenOffsets.getFirst(), taTokenOffsets.getSecond());

                        if ( null != fineType )
                            f = new Constituent(fineType, 1.0, EventConstants.NER_ACE_FINE, ta, taTokenOffsets.getFirst(), taTokenOffsets.getSecond());

                        addConstituentToTaNeMap( taToFineNeEntities, ta, f );
                    }
                }
            }
        }
        for ( TextAnnotation ta : taToCoarseNeEntities.keySet() )
            processEntities(ta, taToCoarseNeEntities.get(ta), EventConstants.NER_ACE_COARSE );

        for ( TextAnnotation ta : taToFineNeEntities.keySet() )
            processEntities(ta, taToFineNeEntities.get(ta), EventConstants.NER_ACE_FINE );

    }

    private static boolean isFiltered(String mentionText )
    {
        if ( !isCapitalized( mentionText ) || stopWords.contains( mentionText ) )
            return true;

        return false;
    }

    /**
     * remove overlapping entities, put them in a View and add them to ta
     * @param ta
     * @param neConstituents
     */

    private static void processEntities(TextAnnotation ta, List<Constituent> neConstituents, String viewName )
    {
        neConstituents = removeOverlappingEntities( neConstituents );

        View nerView = new View( viewName, NAME, ta, 1.0 );
        for ( Constituent c : neConstituents )
            nerView.addConstituent( c );

        ta.addView( viewName, nerView );
    }

    private static void addConstituentToTaNeMap(Map<TextAnnotation, List<Constituent>> taToNeEntities, TextAnnotation ta, Constituent c) {
        List< Constituent > neEntities = taToNeEntities.get( ta );
        if ( null == neEntities )
        {
            neEntities = new LinkedList< Constituent >();
            taToNeEntities.put( ta, neEntities );
        }
        neEntities.add( c );
        return;
    }


    public static List< Constituent > removeOverlappingEntities(List< Constituent > neConstituents ) {

        Collections.sort(neConstituents, new Comparator<Constituent>() {
            @Override
            public int compare(Constituent ca, Constituent cb) {
                if ( ca.getStartSpan() < cb.getStartSpan() )
                    return -1;
                else if ( ca.getStartSpan() > cb.getStartSpan() )
                    return 1;
                else if ( ca.getEndSpan() > cb.getEndSpan() )
                    return -1;
                else if ( ca.getEndSpan() < cb.getEndSpan() )
                    return 1;
                else
                    return 0;
            }
        });

        Set< Constituent > nesToRemove = new HashSet< Constituent >();

        int lastNeEnd = -1;
        Constituent prevNe = null;

        for ( Constituent ne : neConstituents ) {
            if (ne.getStartSpan() < lastNeEnd) {
                nesToRemove.add(prevNe);
            }
            lastNeEnd = ne.getEndSpan();
            prevNe = ne;
        }

        for ( Constituent e : nesToRemove )
            neConstituents.remove( e );

        return neConstituents;
    }

    private static boolean isCapitalized(String extent) {
        Matcher m = isCapPattern.matcher(extent);

        if ( m.find() )
            return true;

        return false;
    }

    /**
     * find the corresponding token id from the TextAnnotation ta for the char offset given.
     * Necessary because source text is split into multiple TextAnnotation objects, but char offsets
     *    are not natively preserved, but written into additional view.
     * @param ta    TextAnnotation to search for mention
     * @param mentionStart mention start offset
     * @param mentionEnd    mention end offset
     * @return  token offsets of corresponding Constituent from the TextAnnotation, or null if not found
     */
    private static IntPair findTokenOffsets(TextAnnotation ta, int mentionStart, int mentionEnd )
    {
        int tokenStart = -1;
        int tokenEnd = -1;

        IntPair tokenOffsets = null;
        View tokenOffsetView = ta.getView( TOKEN_WITH_CHAR_OFFSET );


        for ( Constituent t : tokenOffsetView.getConstituents() )
        {
            if ( Integer.parseInt( t.getAttribute( CHAR_START ) ) == mentionStart )
                tokenStart = t.getStartSpan();

            if ( Integer.parseInt(t.getAttribute(CHAR_END) ) == mentionEnd )
                tokenEnd = t.getEndSpan();
        }

        if ( tokenStart >= 0 && tokenEnd >= 0 )
        {
            tokenOffsets = new IntPair( tokenStart, tokenEnd );
        }

        return tokenOffsets;
    }

    /**
     * add constituent, creating view if needed (using constituent's viewname)
     * @param ta
     * @param c
     */
    private static void addConstituentToTextAnnotation(TextAnnotation ta, Constituent c) {

        String viewName = c.getViewName();
        View v = null;
        if ( !ta.hasView( viewName ) )
        {
            v = new SpanLabelView( viewName, NAME, ta, 1.0 );
            ta.addView( viewName, v );
        }
        else
            v = ta.getView( viewName );

        v.addConstituent( c );

        return;
    }

    /**
     * find the relevant textAnnotation from list, given mention
     * @param mentionStart  sstart char offset of mention
     * @param mentionEnd end char offset of mention
     * @param taList    list of TextAnnotations that could contain mention
     * @return  the TextAnnotation containing the mention, or null if not found
     */
    private static TextAnnotation findTextAnnotation( int mentionStart, int mentionEnd, List<TextAnnotation> taList) {

        TextAnnotation theTa = null;

        for ( TextAnnotation ta : taList )
        {
            if ( null != theTa )
                break;

            for ( Constituent c: ta.getView( TOKEN_WITH_CHAR_OFFSET ).getConstituents() )
            {
                int cStart = Integer.parseInt(c.getAttribute(EventConstants.CHAR_START ) );
                int cEnd = Integer.parseInt( c.getAttribute( EventConstants.CHAR_END ) );

                if ( mentionStart == cStart || mentionEnd == cEnd )
                {
                    theTa = ta;
                    break;
                }
            }
        }

        return theTa;
    }

}
