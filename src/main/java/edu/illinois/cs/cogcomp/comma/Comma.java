package edu.illinois.cs.cogcomp.comma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.comma.bayraktar.BayraktarPatternLabeler;
import edu.illinois.cs.cogcomp.comma.utils.PrettyPrint;
import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.QueryableList;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

/**
 * A data structure containing all the information related to a comma.
 */
public class Comma implements Serializable {
    private String[] sentence;
    private String role;
    public int commaPosition;
    private TextAnnotation goldTA;
    private TextAnnotation TA;
    private Sentence s;

    private static final long serialVersionUID = 715976951486905421l;

    private static boolean GOLD, NERlexicalise, POSlexicalise, USE_NEW_LABEL_SET;
    private static String CONSTITUENT_PARSER;

    static {
    	CommaProperties properties = CommaProperties.getInstance();
        GOLD = properties.useGold();
        USE_NEW_LABEL_SET = properties.useNewLabelSet();
        NERlexicalise = properties.lexicaliseNER();
        POSlexicalise = properties.lexicalisePOS();
        CONSTITUENT_PARSER = properties.getConstituentParser();
    }
    
    public static void useGoldFeatures(boolean useGold){
    	GOLD = useGold;
    }
    
    public String getCommaID(){
    	return role + " " + commaPosition + " " + goldTA.getId();
    }
    
    /**
     * A default constructor used during training.
     * @param commaPosition The token index of the comma
     * @param role The gold-standard role of the comma
     * @param sentence The tokenized string of the sentence
     * @param TA The TextAnnotation containing all required views (POS, SRL, NER, etc)
     */
    public Comma(int commaPosition, String role, String[] sentence, TextAnnotation TA, Sentence s) {
    	this.commaPosition = commaPosition;
        if (role != null) {
            switch (role) {
                case "Entity attribute":
                    this.role = "Attribute";
                    break;
                case "Entity substitute":
                    this.role = "Substitute";
                    break;
                default:
                    this.role = role;
                    break;
            }
        }
        this.sentence = sentence;
        this.TA = TA;
    }


    /**
     * A constructor used during training, if gold-standard feature annotations are available.
     * @param commaPosition The token index of the comma
     * @param role The gold-standard role of the comma
     * @param rawText The tokenized string of the sentence
     * @param TA The TextAnnotation containing all required views (POS, SRL, NER, etc)
     * @param goldTA The TextAnnotation containing gold-standard views for training
     */
    public Comma(int commaPosition, String role, String[] sentence, TextAnnotation TA, TextAnnotation goldTA, Sentence s) {
        this(commaPosition, role, sentence, TA, s);
    	this.goldTA = goldTA;
    }

    /**
     * A constructor used at test time (for prediction only). Assumes no gold label;
     * @param commaPosition The token index of the comma
     * @param rawText The tokenized string of the sentence
     * @param TA The TextAnnotation containing all required views (POS, SRL, NER, etc)
     */
    public Comma(int commaPosition, String[] sentence, TextAnnotation TA, Sentence s) {
        this(commaPosition, null, sentence, TA, s);
        useGoldFeatures(false);
    }

    /**
     * @return The label as annotated by Vivek. If Vivek's label was Other, and the USE_NEW_LABEL_SET is true, then the refined label is returned 
     */
    public String getVivekNaveenRole(){
    	String vivekLabel = role;
    	if(vivekLabel.equals("Other") && USE_NEW_LABEL_SET)
    		return NaveenLabeler.getNaveenLabel(this);
    	else
			return vivekLabel;
    }
    
    /**
     * @return The label as annotated by Vivek. If Vivek's label was Other, the USE_NEW_LABEL_SET is true, and the annotation for the Bayraktar-patterns is available, then the Bayraktar label is returned
     */
    public String getVivekBayraktarRole() {
    	String vivekLabel = role;
    	String bayraktarLabel = getBayraktarLabel();
    	if(USE_NEW_LABEL_SET && vivekLabel.equals("Other") && BayraktarPatternLabeler.isNewLabel(bayraktarLabel))
    		return bayraktarLabel;
    	else
			return vivekLabel;
    }
    
    /**
     * @return The original label as annotated by Vivek.
     */
    public String getVivekRole(){
    	return role;
    }
    
    public int getPosition(){
    	return commaPosition;
    }

    public Sentence getSentence() {
        return s;
    }
    
	public String getVivekAnnotatedText() {
		List<String> tokens = Arrays.asList(sentence);
		return StringUtils.join(" ", tokens.subList(0, commaPosition+1))
				+ "["
				+ role
				+ "] "
				+ StringUtils.join(" ", 
						tokens.subList(commaPosition + 1, tokens.size()));
	}
	
	public String getVivekNaveenAnnotatedText() {
		List<String> tokens = Arrays.asList(sentence);
		return StringUtils.join(" ", tokens.subList(0, commaPosition+1))
				+ "["
				+ getVivekNaveenRole()
				+ "] "
				+ StringUtils.join(" ", 
						tokens.subList(commaPosition + 1, tokens.size()));
	}
	
	public String getBayraktarAnnotatedText() {
		List<String> tokens = Arrays.asList(sentence);
		return StringUtils.join(" ", tokens.subList(0, commaPosition+1))
				+ "["
				+ getBayraktarLabel()
				+ "] "
				+ StringUtils.join(" ", 
						tokens.subList(commaPosition + 1, tokens.size()));
	}
	
	public String getText(){
		return StringUtils.join(" ", sentence);
	}
	
	public String getAllViews(){
		StringBuilder info = new StringBuilder();
		if(goldTA!=null){
			info.append("\n\nPARSE_GOLD\n");
			TreeView tv = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
			info.append(PrettyPrint.pennString(tv.getTree(0)));
		}
		if(TA!=null){
			info.append("\n\nPARSE_STANFORD\n");
			TreeView tv1 = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
			info.append(PrettyPrint.pennString(tv1.getTree(0)));
			info.append("\n\nPARSE_CHARNIAK\n");
			TreeView tv2 = (TreeView) TA.getView(ViewNames.PARSE_CHARNIAK);
			info.append(PrettyPrint.pennString(tv2.getTree(0)));
			info.append("\n\nNER\n");
			info.append(TA.getView(ViewNames.NER));
			info.append("\n\nSHALLOW_PARSE\n");
			info.append(TA.getView(ViewNames.SHALLOW_PARSE));
			info.append("\n\nPOS\n");
			info.append(TA.getView(ViewNames.POS));
			info.append("\n\nSRL_VERB\n");
			info.append(TA.getView(ViewNames.SRL_VERB));
			info.append("\n\nSRL_NORM\n");
			info.append(TA.getView(ViewNames.SRL_NOM));
			info.append("\n\nSRL_PREP\n");
			info.append(TA.getView(ViewNames.SRL_PREP));
		}
		return info.toString();
	}
	
	public TextAnnotation getTextAnnotation(boolean gold){
		return gold?goldTA:TA;
	}
    
    public String getWordToRight(int distance) {
        // Dummy symbol for sentence end (in case comma is the second to last word in the sentence)
        if (commaPosition + distance >= sentence.length)
            return "###";
        return sentence[commaPosition + distance];
    }

    public String getWordToLeft(int distance) {
        // Dummy symbol for sentence start (in case comma is the second word in the sentence)
        if (commaPosition - distance < 0)
            return "$$$";
        return sentence[commaPosition - distance];
    }
    
    public String getPOSToLeft(int distance){
		TokenLabelView posView;
		if (GOLD)
			posView = (TokenLabelView) goldTA.getView(ViewNames.POS);
		else
			posView = (TokenLabelView) TA.getView(ViewNames.POS);
		return posView.getLabel(commaPosition - distance);
	}
    
    public String getPOSToRight(int distance){
    	TokenLabelView posView;
		if (GOLD)
			posView = (TokenLabelView) goldTA.getView(ViewNames.POS);
		else
			posView = (TokenLabelView) TA.getView(ViewNames.POS);
    	return posView.getLabel(commaPosition + distance);
    }
    
    /*public List<String> getPOSNGrams(int ngramLength, int width){
    	TreeView parseView;
    	TokenLabelView posView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
		Constituent comma = getCommaConstituentFromTree(parseView);
		Constituent parent = TreeView.getParent(comma);
		List<Constituent> siblings = IteratorUtils.toList(parseView.where(Queries.isSiblingOf(comma)).iterator());
		List<String> posNgramStrings = new ArrayList<String>();
		posNgramStrings.addAll(getPOSNGramsAroundPosition(siblings.get(0).getStartSpan(), width, ngramLength));
		posNgramStrings.addAll(getPOSNGramsAroundPosition(commaPosition, width, ngramLength));
		posNgramStrings.addAll(getPOSNGramsAroundPosition(siblings.get(siblings.size()-1).getEndSpan(), width, ngramLength));
    }
    
    public List<String> getPOSNGramsAroundPosition(int position, int width, int ngramLength){
    	TokenLabelView posView;
    	if (GOLD)
    		posView = (TokenLabelView) goldTA.getView(ViewNames.POS);
    	else
			posView = (TokenLabelView) TA.getView(ViewNames.POS);
		List<Constituent> POSConstituentsAroundPosition= posView.getConstituentsCoveringSpan(position - width, position + width + 1);
		Set<Feature> posNgramFeatures = FeatureNGramUtility.getLabelNgramsOrdered(POSConstituentsAroundPosition, ngramLength);
		List<String> posNgramStrings = new ArrayList<String>();
		for(Feature posFeature : posNgramFeatures)
			posNgramStrings.add(posFeature.toString());
		return posNgramStrings;
    }*/

    public Constituent getChunkToRightOfComma(int distance){
    	//We don't have gold SHALLOW_PARSE
    	TextAnnotation chunkTA = TA;
    	SpanLabelView chunkView = (SpanLabelView) chunkTA.getView(ViewNames.SHALLOW_PARSE);
    	
    	
		List<Constituent> chunksToRight= chunkView.getSpanLabels(commaPosition+1, TA.getTokens().length);
		Collections.sort(chunksToRight,
				TextAnnotationUtilities.constituentStartComparator);
		
		Constituent chunk;
		if(distance<=0 || distance>chunksToRight.size())
			chunk = null;
		else 
			chunk = chunksToRight.get(distance-1);
		return chunk;
    }
    
    public Constituent getChunkToLeftOfComma(int distance){
    	//We don't have gold SHALLOW_PARSE
    	TextAnnotation chunkTA = TA;
    	SpanLabelView chunkView = (SpanLabelView) chunkTA.getView(ViewNames.SHALLOW_PARSE);
    	
		
		List<Constituent> chunksToLeft = chunkView.getSpanLabels(0, commaPosition+1);
		Collections.sort(chunksToLeft,
				TextAnnotationUtilities.constituentStartComparator);
		
		Constituent chunk;
		if(distance<=0 || distance>chunksToLeft.size())
			chunk = null;
		else 
			chunk = chunksToLeft.get(distance-1);
		return chunk;
    }

    public Constituent getPhraseToLeftOfComma(int distance){
    	TreeView parseView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
		Constituent comma = getCommaConstituentFromTree(parseView);

        return getSiblingToLeft(distance, comma, parseView);
    }
    
    public Constituent getPhraseToRightOfComma(int distance){
    	TreeView parseView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
		Constituent comma = getCommaConstituentFromTree(parseView);

        return getSiblingToRight(distance, comma, parseView);
    }
    
    public Constituent getPhraseToLeftOfParent(int distance){
    	TreeView parseView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
		Constituent comma = getCommaConstituentFromTree(parseView);
		Constituent parent = TreeView.getParent(comma);
        return getSiblingToLeft(distance, parent, parseView);
    }

    public Constituent getPhraseToRightOfParent(int distance){
    	TreeView parseView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
		Constituent comma = getCommaConstituentFromTree(parseView);
		Constituent parent = TreeView.getParent(comma);
        return getSiblingToRight(distance, parent, parseView);
    }
    
    public String[] getLeftToRightDependencies(){
    	TreeView depTreeView = (TreeView) TA.getView(ViewNames.DEPENDENCY_STANFORD);
    	List<Constituent> constituentsOnLeft = depTreeView.getConstituentsCoveringSpan(0, commaPosition); 
		List<Relation> ltors = new ArrayList<Relation>();
		
		for (Constituent constituent : constituentsOnLeft) {
			for (Relation relation : constituent.getOutgoingRelations()) {
				Constituent target = relation.getTarget();
				if(target.getStartSpan() > commaPosition)
					ltors.add(relation);
			}
		}
		
		String[] ltorNames = new String[ltors.size()];
		for(int i=0; i<ltorNames.length; i++)
			ltorNames[i] = ltors.get(i).getRelationName();
		return ltorNames;
    }
    
    public String[] getRightToLeftDependencies(){
    	TreeView depTreeView = (TreeView) TA.getView(ViewNames.DEPENDENCY_STANFORD);
    	List<Constituent> constituentsOnLeft = depTreeView.getConstituentsCoveringSpan(0, commaPosition); 
		List<Relation> rtols = new ArrayList<Relation>();
		
		for (Constituent constituent : constituentsOnLeft) {
			for (Relation relation : constituent.getIncomingRelations()) {
				Constituent target = relation.getSource();
				if(target.getStartSpan() > commaPosition)
					rtols.add(relation);
			}
		}
		String[] rtolNames = new String[rtols.size()];
		for(int i=0; i<rtolNames.length; i++)
			rtolNames[i] = rtols.get(i).getRelationName();
		return rtolNames;
    }

    public Constituent getCommaConstituentFromTree(TreeView parseView){
		Constituent comma = null;
		for(Constituent c: parseView.getConstituents()){
			if(c.isConsituentInRange(commaPosition, commaPosition+1)){
				try {
					comma = parseView.getParsePhrase(c);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				break;
			}
		}
		return comma;
    }

    public Constituent getSiblingToLeft(int distance, Constituent c, TreeView parseView){
    	Constituent leftSibling = c;
    	IQueryable<Constituent> siblings = parseView.where(Queries.isSiblingOf(c));
    	while(distance-- > 0){
    		Iterator<Constituent> leftSiblingIt = siblings.where(Queries.adjacentToBefore(leftSibling)).iterator();
    		if(leftSiblingIt.hasNext())
    			leftSibling = leftSiblingIt.next();
    		else
    			return null;
    	}
    	return leftSibling;
    }

    public Constituent getSiblingToRight(int distance, Constituent c, TreeView parseView){
    	Constituent rightSibling = c;
    	IQueryable<Constituent> siblings = parseView.where(Queries.isSiblingOf(c));
    	while(distance-- > 0){
    		Iterator<Constituent> rightSiblingIt = siblings.where(Queries.adjacentToAfter(rightSibling)).iterator();
    		if(rightSiblingIt.hasNext())
    			rightSibling = rightSiblingIt.next();
    		else
    			return null;
    	}
    	return rightSibling;
    }


    /**
     * 
     * @return the list of commas that are children of the parent of the current comma, i.e. siblings of the current comma.
     */
    public List<Comma> getSiblingCommas(){
    	TreeView parseView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
    	List<Constituent> commaConstituents = new ArrayList<Constituent>();
    	Map<Constituent, Comma> constituentCommaMap = new HashMap<Constituent, Comma>();
		for(Comma c : s.getCommas()){
			Constituent commaConstituent = c.getCommaConstituentFromTree(parseView);
			commaConstituents.add(commaConstituent);
			constituentCommaMap.put(commaConstituent, c);
		}
		QueryableList<Constituent> qlCommas = new QueryableList<Constituent>(commaConstituents);
		Iterable<Constituent> siblingCommaConstituents = qlCommas.where(Queries.isSiblingOf(this.getCommaConstituentFromTree(parseView)));
		List<Comma> siblingCommas =  new ArrayList<Comma>();
		for(Constituent commaConstituent : siblingCommaConstituents)
			siblingCommas.add(constituentCommaMap.get(commaConstituent));
		return siblingCommas;
	}
    
    public boolean isSibling(Comma otherComma){
    	TreeView parseView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
    	Constituent thisCommaConstituent = getCommaConstituentFromTree(parseView);
    	Constituent otherCommmaConstituent = otherComma.getCommaConstituentFromTree(parseView);
    	return parseView.getParent(thisCommaConstituent) == parseView.getParent(otherCommmaConstituent);
    }
    
    /**
     * 
     * @return the first comma by position from the list of sibling commas
     */
    public Comma getSiblingCommaHead(){
    	List<Comma> siblingCommas = getSiblingCommas();
    	Comma head = siblingCommas.get(0);
    	for(Comma c : siblingCommas)
    		if(c.commaPosition < head.commaPosition)
    			head = c;
		return head;
	}
    
    public String getNotation(Constituent c){
    	if(c == null)
    		return "NULL";
    	String notation = c.getLabel();
    	
    	if(NERlexicalise)
    		notation += "-" + getNamedEntityTag(c);
    	
    	if(POSlexicalise){
			notation += "-";
			IntPair span = c.getSpan();
			TextAnnotation TA = c.getTextAnnotation();
			for (int tokenId = span.getFirst(); tokenId < span.getSecond(); tokenId++)
					notation += " " + POSUtils.getPOS(TA, tokenId);
	    }
    	
		return notation;
    }
    
    public String getStrippedNotation(Constituent c){
    	if(c == null)
    		return "NULL";
    	String notation = c.getLabel().split("-", 2)[0];
    	
    	if(NERlexicalise)
    		notation += "-" + getNamedEntityTag(c);
    	
    	if(POSlexicalise){
			notation += "-";
			IntPair span = c.getSpan();
			TextAnnotation TA = c.getTextAnnotation();
			for (int tokenId = span.getFirst(); tokenId < span.getSecond(); tokenId++)
					notation += " " + POSUtils.getPOS(TA, tokenId);
	    }
    	
		return notation;
    }
    
    public List<String> getContainingSRLs() {
        List<String> list = new ArrayList<String>();
        TextAnnotation srlTA = (GOLD)? goldTA : TA;
    	PredicateArgumentView pav;
        pav = (PredicateArgumentView)srlTA.getView(ViewNames.SRL_VERB);
        for(Constituent pred : pav.getPredicates()) {
            for (Relation rel : pav.getArguments(pred)) {
                if (rel.getTarget().getEndSpan() > commaPosition && rel.getTarget().getStartSpan() >= commaPosition)
                    list.add(pav.getPredicateLemma(rel.getSource()) + rel.getRelationName());
            }
        }
        pav = (PredicateArgumentView)srlTA.getView(ViewNames.SRL_NOM);
        for(Constituent pred : pav.getPredicates()) {
            for (Relation rel : pav.getArguments(pred)) {
                if (rel.getTarget().getEndSpan() > commaPosition && rel.getTarget().getStartSpan() >= commaPosition)
                    list.add(pav.getPredicateLemma(rel.getSource()) + rel.getRelationName());
            }
        }
        // We don't have gold prepSRL (for now)
        pav = (PredicateArgumentView)TA.getView(ViewNames.SRL_PREP);
        for(Constituent pred : pav.getPredicates()) {
            for (Relation rel : pav.getArguments(pred)) {
                if (rel.getTarget().getEndSpan() > commaPosition && rel.getTarget().getStartSpan() >= commaPosition)
                    list.add(pav.getPredicateLemma(rel.getSource()) + rel.getRelationName());
            }
        }
		return list;
    }

    public String getNamedEntityTag(Constituent c){
    	//We don't have gold NER
    	List<Constituent> NEs = TA.getView(ViewNames.NER).getConstituentsCovering(c);
    	String result = "";
    	/*String result = NEs.size()==0? "NO-NER" : NEs.get(0).getLabel();
    	for(int i = 1; i<NEs.size(); i++)
    		result += "+" + NEs.get(i).getLabel();*/
    	for(Constituent ne: NEs){
    		if(!ne.getLabel().equals("MISC") && c.doesConstituentCover(ne) && (ne.getNumberOfTokens()>=0.6*c.getNumberOfTokens()))
    			result += "+" + ne.getLabel();
    	}
    	return result;
    }
    
    public String getBayraktarLabel() {
    	String bayraktarLabel = BayraktarPatternLabeler.getLabel(this);
    	if(bayraktarLabel==null)
    		return "Other";//assigning majority label
    	else
    		return bayraktarLabel;
		
    }
    
   /* public String getBayraktarLabels() {
    	String[] labels;
    	if (GOLD){
    		labels = new String[1];
    		TreeView goldParseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    		String goldBayraktarPattern = getBayraktarPattern(goldParseView);
    		labels[0] = BayraktarPatternLabeler.getBayraktarLabel(goldBayraktarPattern);
    	}
    	else{
    		labels = new String[2];
    		
    		TreeView charniakParseView = (TreeView) TA.getView(ViewNames.PARSE_CHARNIAK);
    		String charniakBayraktarPattern = getBayraktarPattern(charniakParseView);
    		labels[0] = BayraktarPatternLabeler.getBayraktarLabel(charniakBayraktarPattern);
    		
    		TreeView stanfordParseView = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
    		String stanfordBayraktarPattern = getBayraktarPattern(stanfordParseView);
    		labels[1] = BayraktarPatternLabeler.getBayraktarLabel(stanfordBayraktarPattern);
    		if(labels[0]==null)
    			labels[0] = labels[1];
    	}
    	return labels[0];
    }*/
    
    public String getBayraktarPattern() {
    	TreeView parseView;
    	if (GOLD)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(CONSTITUENT_PARSER);
    	return getBayraktarPattern(parseView);
    }
    
    public String getBayraktarPattern(TreeView parseView) {
    	String pattern;
		Constituent comma = getCommaConstituentFromTree(parseView);
		Constituent parent = TreeView.getParent(comma);
		if( !ParseTreeProperties.isPunctuationToken(parent.getLabel()) && ParseTreeProperties.isPreTerminal(parent)){
			if(parent.getLabel().equals("CC"))
				pattern = parent.getSurfaceString();
			else
				pattern = "***";
		} else
			pattern = parent.getLabel().split("-")[0];
		pattern += " -->";
		for(Relation childRelation : parent.getOutgoingRelations()){
			Constituent child = childRelation.getTarget();
			if( !POSUtils.isPOSPunctuation(child.getLabel()) && ParseTreeProperties.isPreTerminal(child)){
				if(child.getLabel().equals("CC")){
					pattern += " " + child.getSurfaceString();
				} else if(!pattern.endsWith("***"))
					pattern += " ***";
			} else
				pattern += " " + ParseUtils.stripFunctionTags(child.getLabel());
		}
		return pattern;
    }
    
    public void setRole(String newRole) throws Exception{
    	if(role!=null)
    		throw new Exception("Comma role is already set to " + role + " Cannot change to " + newRole);
    	role = newRole;
    }
}
