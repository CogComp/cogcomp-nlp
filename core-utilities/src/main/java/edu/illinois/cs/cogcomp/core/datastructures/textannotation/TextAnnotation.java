package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.nlp.utilities.SentenceUtils;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.Serializable;
import java.util.*;

/**
 * This class contains all annotation for a single piece of text (which could contain more than one
 * sentence.)
 * <p/>
 *
 * @author Vivek Srikumar
 */
public class TextAnnotation extends AbstractTextAnnotation implements Serializable {

    private static final long serialVersionUID = -1308407121595094945L;

    /**
     * An identifier for the corpus
     */
    protected String corpusId;

    /**
     * The identifier for this text annotation
     */
    protected String id;

    /**
     * The list of sentences contained in this text
     */
    protected List<Sentence> sentences;

    /**
     * A map from character offset to the token id. This will be instantiated only when the function
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation#getTokenIdFromCharacterOffset(int)}
     * is called the first time.
     */
    protected int[] characterOffsetsToTokens = null;

    protected TIntObjectMap<ArrayList<IntPair>> allSpans = null;

    /**
     * A symbol table.
     */
    final SymbolTable symtab;

    public TextAnnotation(String corpusId, String id, String text, IntPair[] characterOffsets,
            String[] tokens, int[] sentenceEndPositions) {
        super();

        if (sentenceEndPositions[sentenceEndPositions.length - 1] != tokens.length)
            throw new IllegalArgumentException("Invalid sentence boundary. "
                    + "Last element should be the number of tokens");

        this.corpusId = corpusId;
        this.id = id;
        this.text = text;

        this.symtab = new SymbolTable();

        this.setTokens(tokens, characterOffsets);
        SpanLabelView view = new SpanLabelView(ViewNames.SENTENCE, "UserSpecified", this, 1d);

        int start = 0;
        for (int s : sentenceEndPositions) {
            view.addSpanLabel(start, s, ViewNames.SENTENCE, 1d);
            start = s;
        }
        this.addView(ViewNames.SENTENCE, view);

        // Add a TOKENS view in order to access tokens the same way as everything else in the
        // sentence
        TokenLabelView tokenLabelView =
                new TokenLabelView(ViewNames.TOKENS, "UserSpecified", this, 1d);

        for (int i = 0; i < tokens.length; i++) {
            tokenLabelView.addConstituent(new Constituent("", ViewNames.TOKENS, this, i, i + 1));
        }
        this.addView(ViewNames.TOKENS, tokenLabelView);
    }

    /**
     * Adds a view that is generated by a {@link Annotator}
     */
    public void addView(Annotator annotator) throws AnnotatorException {

        addView(annotator.getViewName(), annotator.getView(this));
    }

    public String getCorpusId() {
        return this.corpusId;
    }

    public String getId() {
        return this.id;
    }

    public int getNumberOfSentences() {

        return this.sentences().size();
    }

    public Sentence getSentence(int sentenceId) {
        return this.sentences().get(sentenceId);
    }

    public int getSentenceId(Constituent constituent) {
        return getSentenceId(constituent.getStartSpan());
    }

    /**
     * Gets the index of the sentence that contains the token, indexed by tokenPosition. If no
     * sentence contains the token, then this function throws an IllegalArgumentException. The
     * sentence index can further be used to get the Sentence itself by calling
     * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation#getSentence(int)}
     * .
     *
     * @param tokenId The index of the token whose sentenceId is needed
     * @return The index of the sentence that contains this token
     * @throws IllegalArgumentException if no sentence contains the {@code tokenId}
     */
    public int getSentenceId(int tokenId) {
        for (int i = 0; i < this.getNumberOfSentences(); i++) {
            if (this.sentences.get(i).getSentenceConstituent().doesConstituentCover(tokenId))
                return i;
        }
        throw new IllegalArgumentException("No sentence contains token id " + tokenId);
    }

    /**
     * Gets the sentence containing the specified token
     */
    public Sentence getSentenceFromToken(int tokenId) {
        return this.getSentence(this.getSentenceId(tokenId));
    }

    public List<Sentence> getSentenceFromTokens(Set<Integer> tokens) {
        List<Sentence> mySentences = new ArrayList<>();
        Set<Integer> sentencesSeen = new HashSet<>();

        for (Sentence s : sentences()) {
            for (int token : tokens) {
                if (s.getSentenceConstituent().doesConstituentCover(token)) {
                    if (!sentencesSeen.contains(s.getStartSpan())) {
                        mySentences.add(s);
                        sentencesSeen.add(s.getStartSpan());
                    }
                    break;
                }
            }
        }

        return mySentences;
    }

    @Override
    public int hashCode() {
        return this.corpusId.hashCode() * 13 + this.id.hashCode() * 17 + this.text.hashCode() * 19
                + (tokens == null ? 0 : Arrays.hashCode(tokens) * 43)
                + (this.sentences().hashCode() * 31);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof TextAnnotation))
            return false;

        TextAnnotation that = (TextAnnotation) obj;

        if (!this.corpusId.equals(that.corpusId) || !this.id.equals(that.id)
                || !this.text.equals(that.text))
            return false;

        if (this.tokens == null && that.tokens == null)
            return true;
        else if (this.tokens == null)
            return false;
        else if (that.tokens == null)
            return false;

        if (this.tokens.length != that.tokens.length)
            return false;

        for (int i = 0; i < this.tokens.length; i++) {
            if (!this.tokens[i].equals(that.tokens[i]))
                return false;
        }
        return this.sentences().equals(that.sentences());
    }

    public List<Sentence> sentences() {
        if (sentences == null) {
            synchronized (this) {
                if (sentences == null) {
                    View sentenceView = getView(ViewNames.SENTENCE);

                    sentences = new ArrayList<>();

                    for (Constituent c : sentenceView.getConstituents()) {
                        Sentence sentence = new Sentence(c);

                        sentences.add(sentence);
                    }
                    Collections.sort(sentences, TextAnnotationUtilities.sentenceStartComparator);
                }
            }
        }
        return sentences;
    }

    /**
     * Get the position of token that corresponds to the character offset that is passed as a
     * parameter. This function could be useful when dealing with corpora that specify annotation in
     * terms of character offsets. In particular, the CuratorClient uses this function to convert
     * views from the Curator representation.
     */
    public int getTokenIdFromCharacterOffset(int characterOffset) {
        if (characterOffsetsToTokens == null) {

            characterOffsetsToTokens = new int[this.getText().length()];
            for (int i = 0; i < characterOffsetsToTokens.length; i++) {
                characterOffsetsToTokens[i] = -1;
            }

            int characterId = 0;

            for (int tokenId = 0; tokenId < this.size(); tokenId++) {

                // whitespace first. eat up all the whitespace
                // characters. Assumption: Any
                // whitespace characters that come before the a token belong
                // to the token.
                while (characterId < this.getText().length()
                        && Character.isWhitespace(this.getText().charAt(characterId))) {
                    characterOffsetsToTokens[characterId] = tokenId;
                    characterId++;
                }

                int start = characterId;
                for (; characterId < start + this.getToken(tokenId).length(); characterId++) {
                    characterOffsetsToTokens[characterId] = tokenId;
                }

                if (characterId >= this.getText().length()) {
                    // if we have run out of characters, we should have no more
                    // tokens left. Let's make sure that this is the case.
                    if (tokenId != this.size() - 1) {
                        throw new IllegalStateException(
                                "Error converting character offsets to tokenIds");
                    }
                    break;
                }
            }

            // all the whitespace characters that come after this token also
            // point to the end.
            while (characterId < this.getText().length()
                    && Character.isWhitespace(this.getText().charAt(characterId))) {
                characterOffsetsToTokens[characterId] = this.size();
                characterId++;
            }
        }

        if (characterOffset < 0 || characterOffset > characterOffsetsToTokens.length) {
            throw new IllegalArgumentException("Invalid character offset. The character position "
                    + characterOffset + " does not correspond to any token.");
        }

        // If the characterOffset is the number of characters (and hence doesn't
        // correspond to any character with a zero indexed scheme), return the
        // number of tokens + 1.
        if (characterOffset == characterOffsetsToTokens.length) {
            return this.size();
        }

        return characterOffsetsToTokens[characterOffset];
    }

    public String toString() {
        return "TextAnnotation: " + this.getText();
    }

    public List<IntPair> getSpansMatching(String text) {
        if (allSpans == null) {
            synchronized (this) {
                if (allSpans == null) {

                    this.allSpans =
                            TCollections
                                    .synchronizedMap(new TIntObjectHashMap<ArrayList<IntPair>>());

                    for (int start = 0; start < this.size() - 1; start++) {
                        StringBuilder sb = new StringBuilder();

                        for (int end = start; end < this.size(); end++) {
                            String token = tokens[end];

                            token = token.replaceAll("``", "\"").replaceAll("''", "\"");
                            token = SentenceUtils.convertFromPTBBrackets(token);

                            sb.append(token).append(" ");

                            int hash = sb.toString().trim().hashCode();

                            if (!allSpans.containsKey(hash))
                                allSpans.put(hash, new ArrayList<IntPair>());

                            List<IntPair> list = allSpans.get(hash);
                            list.add(new IntPair(start, end));
                        }
                    }
                }
            }
        }

        int hashCode = text.trim().hashCode();
        int length = text.split("\\s+").length;

        List<IntPair> list = allSpans.get(hashCode);
        if (list == null)
            list = new ArrayList<>();

        // This is a hack to deal with the fact that sometimes, two strings in
        // Java could have the same hashCode even if they aren't identical. This
        // won't weed out all such cases, but will remove most.v
        List<IntPair> newList = new ArrayList<>();
        for (IntPair item : list) {
            if (item.getSecond() - item.getFirst() == length)
                newList.add(item);
        }

        return newList;
    }
}
