/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * This is the state machine used to parse text.
 * </p>
 * 
 * <p>
 * The parsing states are defined by the {@link TokenizerState} enumeration, the state transition
 * set is defined by the {@link TokenType} enumeration. These two enumerations define the dimensions
 * of the state machine matrix.
 * </p>
 * 
 * <p>
 * States are assumed to be nested rather than linear, so there is a state stack that contains
 * {@link State} objects that encapsulate the state object as well as logic to manage popping and
 * pushing that state. So the state objects do the work of actually creating the TextAnnotation
 * objects.
 * </p>
 * 
 * <p>
 * The StateProcessor interface defines the interface class instances that operate on state
 * transitions must adhere to to process new tokens as they are presented to the state machine
 * </p>
 * 
 * Potential issues:
 * <ol>
 * <li>URLs, when we encounter a ":", we check for protocol, if we find a protocol name following a
 * space, we will skip to the next non white space, make the rest a url.
 * <li>contractions with abbr, like can't, won't and those. if no spaces surround the "'", we will
 * leave the word together including the "'" unless it is one of the common contractions like 's,
 * 'm, 're, et cetera.
 * </ol>
 * 
 * @author redman
 */
public class TokenizerStateMachine {
    /** the state stack, since state can be nested. */
    protected ArrayList<State> stack;
    /** the state stack, since state can be nested. */
    protected ArrayList<State> completed;
    /**
     * <p>
     * This is the state machine. Cardinality of 1st dim indexed by tokenizer
     * states(TokenizerState), 2nd dimension is indexed by of token types (TokenType enum). The
     * values of this array are implementations of StateProcessor interface, are responsible for
     * individually processing a character at a time, but they call also look ahead and back, as
     * they have full access to the contents of this class.
     * </p>
     *
     * <p>
     * This class is initialized using Java's new lambda expressions, solely for the brevity, and
     * considering the number of entries here, the expressiveness of a verbose interface
     * implementation does not seem necessary. The {@link StateProcessor} interface has only one
     * method, that method takes a char.
     * </p>
     */
    protected StateProcessor[][] statemachine;
    /** the text to process. */
    protected char[] text;
    /** the text to process. */
    protected String textstring;
    /** the character offset. */
    protected int current;
    /** the state we are in currently. */
    protected int state;
    
    /**
     * Init the state machine decision matrix and the text annotation.
     * @param splitOnDash true to split tokens on a "-"
     * @param splitOnTwoNewlines if true split a paragraph on two new lines.
     */
    public TokenizerStateMachine(final boolean splitOnDash, final boolean splitOnTwoNewlines) {
        // cardinality of 1st dim the number of states(TokenizerState), 2nd is the number of token
        // types (TokenType enum)
        StateProcessor[][] toopy = {

                // process tokens in sentence, we are only in a sentences while processing white
                // space. There is always a sentence on top of the stack.
                {
                    
                /** get punctuation while in sentence. This starts a new word. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        
                        // we have something, so the paragraph has mass.
                        stack.get(stack.size()-1).hasMass = true;
                        if (token == '$') {
                            Character next = peek(1);
                            if (Character.isDigit(next) || ( next == '.' && Character.isDigit(peek(2)))) {
                                push(new State(TokenizerState.IN_WORD), current);
                            } else {
                                push(new State(TokenizerState.IN_SPECIAL), current);
                            }
                        } else {
                            // this was just push IN_SPECIAL, added the push in_word to match the
                            // old tokenizer
                            push(new State(TokenizerState.IN_SPECIAL), current);
                        }
                    }
                },

                /** get alpha numeric text while in sentence. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        // we have something, so the paragraph has mass.
                        stack.get(stack.size()-1).hasMass = true;
                        push(new State(TokenizerState.IN_WORD), current);
                    }
                },

                /** get whitespace while in sentence, different processer if we sentence split on two newlines */
                splitOnTwoNewlines ? 
                    
                    new StateProcessor() {
                        @Override
                        public void process(char token) {
                            if (stack.get(stack.size()-1).hasMass && token == '\n' && peek(-1) == '\n' && peek(-2) != '\n') {
                                // we are in a sentence, but we will pop it
                                pop(current);
                            }
                        }
                    }
                    :
                    new StateProcessor() {
                        @Override
                        public void process(char token) {}
                    },

                /** get unprintable character while in sentence. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {}
                }}, 
            
                // Token handlers while processing within a word
                {

                /** get punctuation while in word, the punctuation itself would be a word. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        switch (token) {
                            case '_':
                                break;
                            case '/':
                                // numbers well may contain a comma or a period, check for an
                                // entirely numeric word.
                                if (getCurrent().isDate()) {
                                    int advance = 1;
                                    while (true) {
                                        char next = peek(advance);
                                        if (Character.isDigit(next) || next == '/') {
                                            advance++;
                                        } else {
                                            if (advance > 1
                                                    && (next == '\000' || Character
                                                            .isWhitespace(next))) {
                                                // LAM-WRONG couple of numbers separated by a '/',
                                                // make it one word.
                                                return;
                                            }
                                            break;
                                        }
                                    }
                                }
                                pop(current); // the current word is finished.
                                push(new State(TokenizerState.IN_SPECIAL), current); // No matter
                                                                                     // what we push
                                                                                     // a new word
                                                                                     // token.
                                break;
                            case ',':
                                // numbers well may contain a comma or a period, check for an
                                // entirely numeric word.
                                if (getCurrent().isNumeric()) {
                                    if (current < (text.length - 1)
                                            && Character.isDigit(text[current + 1])) {
                                        return; // next char is a digit, this is likely just part of
                                                // the number, continue word
                                    }
                                } else {
                                    // we need a space after a "," to recognize as punctuation
                                    char next = peek(1);
                                    if (Character.isDigit(next) || Character.isAlphabetic(next))
                                        return;
                                }
                                pop(current); // the current word is finished.
                                push(new State(TokenizerState.IN_SPECIAL), current); // No matter
                                                                                     // what we push
                                                                                     // a new word
                                                                                     // token.
                                break;
                            case '-': {
                                // If there is a character before and after, this is a word.
                                char after = peek(1);
                                char before = peek(-1);
                                if (!(Character.isDigit(before) && Character.isDigit(after))) {
                                    /*
                                         if (!((Character.isAlphabetic(before) || Character.isDigit(before)) && (Character
                                                .isAlphabetic(after) || Character.isDigit(after)))) {*/

                                    if (splitOnDash == true) {
                                        pop(current); // the current word is finished.
                                        push(new State(TokenizerState.IN_SPECIAL), current);
                                    }
                                }
                                return;
                            }
                            case '\'':
                            case '`':
                            case '’':
                                char nc = peek(1);
                                char nnc = peek(2);
                                char nnnc = peek(3);
                                char pc = peek(-1);

                                // if it ends with "in'", and the first character is upper case,
                                // it's part of the word.
                                if (token == '\'' && (pc == 'n' && peek(-2) == 'i')) {
                                    State s = getCurrent();
                                    if (!Character.isUpperCase(text[s.start]))
                                        // dropped the 'g' off of 'ing', the single quote is part of
                                        // the word.
                                        return;
                                }

                                if (Character.isLetter(nc) && (Character.isLetter(pc) || pc == '.')) {

                                    // look for contractions, and if we find one, separate at the
                                    // word boundry.
                                    if ((nc == 's' || nc == 'm' || nc == 'd')
                                            && Character.isWhitespace(nnc)) {
                                        // it is an "'s" or "'m", like it's or I'm.
                                        pop(current); // the current word is finished.
                                        push(new State(TokenizerState.IN_WORD), current); // make 's
                                                                                          // words
                                                                                          // in
                                                                                          // their
                                                                                          // own
                                                                                          // right.
                                    } else if ((nc == 'r' && nnc == 'e')
                                            && (nnnc == '\000' || Character.isWhitespace(nnnc))) {
                                        pop(current);
                                        push(new State(TokenizerState.IN_WORD), current);
                                    } else if (nc == 't' && pc == 'n'
                                            && (nnc == '\000' || Character.isWhitespace(nnc))) {
                                        pop(current - 1);
                                        push(new State(TokenizerState.IN_WORD), current - 1);
                                    } else if (nc == 'v' && nnc == 'e'
                                            && (nnnc == '\000' || Character.isWhitespace(nnnc))) {
                                        pop(current);
                                        push(new State(TokenizerState.IN_WORD), current);
                                    } else if (nc == 'l' && nnc == 'l'
                                            && (nnnc == '\000' || Character.isWhitespace(nnnc))) {
                                        pop(current);
                                        push(new State(TokenizerState.IN_WORD), current);
                                    }
                                    return;
                                } else {
                                    pop(current);
                                    push(new State(TokenizerState.IN_SPECIAL), current);
                                }
                                return;
                            case ':': {
                                if (isURL())
                                    return;
                                char prev = peek(-1);
                                char next = peek(1);

                                // this gives only slightly better than breaking on whitespace.
                                if (Character.isDigit(prev) && Character.isDigit(next))
                                    // it's a time, or bible passage.
                                    return;
                                pop(current); // the current word is finished.
                                push(new State(TokenizerState.IN_SPECIAL), current);
                                break;
                            }
                            case '@':
                                if (isEmail()) {
                                    return;
                                }
                                pop(current); // the current word is finished.
                                push(new State(TokenizerState.IN_SPECIAL), current);
                                break;

                            case '.': {
                                // we have a period, this is often an end-of-sentence marker. There
                                // are other examples of areas where it is not, No.2, U.S., US.,
                                // Hi., and Feb. 4. Rule here is that if it is followed by a printable
                                // character, it is just part of the word.
                                // If it is followed by a space, but appears to be part of an
                                // acronym(starts with a period), it's part word, otherwise, it's punctuation.
                                // Decimal numbers are also handled here.
                                char c = peek(1);
                                if (!Character.isWhitespace(c)
                                        && (c != ',' && c != '"' && c != '.' && c != '?'
                                                && c != '!' && c != '-' && c != '\'' && c != '`')) {

                                    // probably not an end of line if next character is not a space.
                                    if (getCurrent().isNumeric()) {

                                        // the current word is entirely numeric, if the next
                                        // character is numeric,
                                        // it's still a number, and hence a word.
                                        char n = peek(1);
                                        if (Character.isDigit(n)) {
                                            return; // next is a digit, continue word
                                        } else {
                                            // punctuation then, pass through
                                        }
                                    } else {
                                        if ( (Character.isAlphabetic(c)/* && Character.isUpperCase(c)*/) || Character.isDigit(c))
                                            // the next character is not white space, so the period
                                            // is part of the word
                                            return;
                                    }
                                } else {
                                    // check for all uppercase and periods back to the start of the
                                    // word or a "-"
                                    char nextnextChar = peek(2);
                                    if (getCurrent().isAbbr())
                                        return; // previous was upper case, acronym and word
                                                // continues
                                    else if (Character.isLowerCase(nextnextChar))
                                        return; // when the next char is white space and the next next char
                                                // is lowercase, we know that the next word is not start of
                                                // a sentence, so we continue.
                                    else
                                        ; // we will pass through, this is not an acronym, so must
                                          // be a special character.
                                }
                            }
                            default:
                                pop(current); // the current word is finished.
                                push(new State(TokenizerState.IN_SPECIAL), current);
                                break;
                        }
                    }
                },

                /** get text while in word. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {}
                },

                /** get whitespace while in word. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        pop(current);
                    }
                },

                /** get unprintable character while in word. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        pop(current);
                    }
                }}, {

                // process tokens within runs of special characters
                /** get special while in specials, the special character itself would be a word. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {

                        // we will keep like special characters together.
                        if (peek(-1) != token) {
                            pop(current); // the current token is finished.
                            
                            if (token == '$') {
                                Character next = peek(1);
                                if (Character.isDigit(next) || ( next == '.' && Character.isDigit(peek(2)))) {
                                    push(new State(TokenizerState.IN_WORD), current);
                                    return;
                                }
                            }
                            push(new State(TokenizerState.IN_SPECIAL), current);
                        }
                    }
                },

                /** get text while in special. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        String cword = getCurrent().getWord();
                        // let's see if this is a contraction.
                        if (cword.equals("'")) {
                            String word = getNextWord();
                            if (Contractions.contains(word)) {

                                // just change the state type to text, this will end up being a
                                // word.
                                getCurrent().stateindex = TokenType.TEXT.ordinal();
                                state = getCurrent().stateindex;
                                return;
                            }
                        } else if (cword.equals(".") && Character.isDigit(token)) {
                            // This is a decimal number (probably), just keep the current state and
                            // make it a word token
                            getCurrent().stateindex = TokenType.TEXT.ordinal();
                            state = getCurrent().stateindex;
                            return;
                        }
                        pop(current);
                        push(new State(TokenizerState.IN_WORD), current);
                    }
                },

                /** get whitespace while in special. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        pop(current); // pop the special off the stack.
                        State state = completed.get(completed.size() - 1);
                        if (state != null) {
                            char c = text[state.start];
                            switch (c) {
                                case '.':
                                case '!':
                                case '?':
                                    if (!isContinue()) {
                                        pop(current); // the sentence
                                    }
                                    break;
                                case '"':
                                case '\'':
                                    // if the current special is a quote, see if there is a line
                                    // ender inside the quote
                                    if (completed.size() > 2) {
                                        state = completed.get(completed.size() - 2);
                                        c = text[state.start];
                                        if (c == '.' || c == '!' || c == '?') {
                                            pop(current);
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                },

                /** get unprintable character while in word. */
                new StateProcessor() {
                    @Override
                    public void process(char token) {
                        pop(current);
                    }
                }}};
        this.statemachine = toopy;
    }

    /**
     * Any number of periods beyond two will continue the sentence rather than ending it..
     * 
     * @return return true if continue previous sentence.
     */
    protected boolean isContinue() {
        // we popped the word already, check it.
        State cs = this.completed.get(this.completed.size() - 1);
        if ((cs.end - cs.start) <= 2)
            return false;
        for (int i = cs.start; i < cs.end; i++) {
            char c = text[i];
            if (c != '.')
                return false;
        }
        return true;
    }

    /**
     * We have encountered a colon in the input data stream, check to see if it is a URL, and if it
     * is, advance the cursor and return true, or return false.
     * 
     * @return return true if it is a url.
     */
    protected boolean isURL() {
        char nc = peek(1);
        switch (nc) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
            case '"':
            case '\'':
                return false; // we require something non-white space after the colon.
        }

        State cs = this.getCurrent();
        UrlValidation syntaxvalid = new UrlValidation();
        int where;
        if ((where = syntaxvalid.isValid(text, cs.start)) != -1) {
            String ss = new String(text, cs.start, where - cs.start);
            try {
                if (new URI(ss) != null) {
                    this.current = cs.start + (ss.length() - 1);
                    this.pop(this.current + 1);
                    return true;
                }
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }
    
    /** this regex finds emails addresses. */
    private static final Pattern emailRegex = 
                    Pattern.compile("^[A-Za-z0-9\\._%\\+\\-]+@[A-Za-z0-9\\-]+\\.[A-Za-z]+\\b");
    
    /** match emails with only one term in the domain name. */
    private static final Pattern emailRegex2 = 
                    Pattern.compile("^[A-Za-z0-9\\._%\\+\\-]+@[A-Za-z0-9\\-\\.]+\\b");
    
    /**
     * We have encountered a colon in the input data stream, check to see if it is a URL, and if it
     * is, advance the cursor and return true, or return false.
     * 
     * @return return true if it is a url.
     */
    protected boolean isEmail() {
        int start = this.getCurrent().start;
        String tmp = new String (text).substring(start);
        Matcher matcher = emailRegex2.matcher(tmp);
        if (matcher.find()) {
            
            // has to match from the start
            if (matcher.start() != 0)
                return false;
            int end = matcher.end();
            current = start + (end-1);
            this.pop(this.current + 1);
            return true;
        } else {
            matcher = emailRegex.matcher(tmp);
            if (matcher.find()) {
                int end = matcher.end();
                current = start + (end-1);
                this.pop(this.current + 1);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * get the character at the given offset from the current position.
     * 
     * @param offset the offset from the current position.
     * @return the next character that will be processed.
     */
    protected char peek(int offset) {
        int next = current + offset;
        if (next < 0 || next >= text.length)
            return '\000';
        else
            return text[next];
    }

    /**
     * get the current state.
     * 
     * @return the current word.
     */
    protected State getCurrent() {
        return stack.get(stack.size() - 1);
    }

    /**
     * Pop the current state identifier off the stack.
     * 
     * @param where the position to terminate the previous token and start the new one.
     * @return true if popped the last item off the stack.
     */
    protected boolean pop(int where) {
        State s = stack.remove(stack.size() - 1);
        s.pop(where);
        if (s.size() > 0)
            completed.add(s);
        if (stack.size() == 0) {
            push(new State(TokenizerState.IN_SENTENCE), where + 1);
            this.state = stack.get(stack.size() - 1).stateIndex();
            return true;
        } else {
            this.state = stack.get(stack.size() - 1).stateIndex();
            return false;
        }
    }

    /**
     * Push a new state identifier off the stack.
     * 
     * @param newState the new state to push.
     * @param where the start position.
     */
    protected void push(State newState, int where) {
        this.state = newState.stateIndex();
        newState.push(where);
        stack.add(newState);
    }

    /**
     * Get the next word, this is a lookahead operation.
     * 
     * @return the next word.
     */
    String getNextWord() {
        int texttype = TokenType.TEXT.ordinal();
        int n = current;
        for (; n < this.text.length; n++) {
            char character = this.text[n];
            int tokentype = classify(character);
            if (tokentype != texttype)
                return textstring.substring(current, n);
        }
        return textstring.substring(current, n);
    }

    /**
     * Process the input text delineating sentences and words. Purpose is to produce a
     * TextAnnotation object, that requires these:
     * <ul>
     * <li>corpusId - this string identifier IDs the corpus of data.</li>
     * <li>id - this string identifies IDs the data.</li>
     * <li>text - this is the text.</li>
     * <li>characterOffsets - this array of integer pairs (IntPair[]) delineate the words.</li>
     * <li>tokens - This array of Strings (String[]) contains text tokens.</li>
     * <li>sentenceEndPositions - this array of integers(int[]) indicates the end of the sentence
     * objects.</li>
     * </ul>
     * 
     * @param intext the text to parse.
     */
    protected void parseText(String intext) {
        // preserve trailing whitespace (disabled find index of last non whitespace)
        int i = intext.length();
        stack = new ArrayList<>();
        completed = new ArrayList<>();
        if (i == 0)
            return;
        this.text = new char[i];
        intext.getChars(0, i, this.text, 0);
        this.textstring = intext;
        this.text = this.textstring.toCharArray();
        current = 0;
        this.push(new State(TokenizerState.IN_SENTENCE), current);
        for (current = 0; current < this.text.length; current++) {
            char character = this.text[current];
            int tokentype = classify(character);
            statemachine[state][tokentype].process(character);
        }

        // pop the last sentence off.
        while (!this.pop(current));
    }

    /**
     * classify the character.
     * 
     * @param c the character to categorize.
     * @return the index of the associated type.
     */
    private int classify(char c) {
        if (c == '_')
            return TokenType.TEXT.ordinal();
        if (Character.isAlphabetic(c) || Character.isDigit(c)) {
            return TokenType.TEXT.ordinal();
        } else if (Character.isWhitespace(c)) {
            return TokenType.WHITESPACE.ordinal();
        } else {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (!Character.isISOControl(c) && c != KeyEvent.CHAR_UNDEFINED && block != null
                    && block != Character.UnicodeBlock.SPECIALS)
                return TokenType.PUNCTUATION.ordinal();
            else
                return TokenType.UNPRINTABLE.ordinal();
        }
    }


    // define our states
    /**
     * State when we are in a sentence.
     * 
     * @author redman
     */
    final class State {
        /** the index of the first character in the sentence. Must be alphanumeric. */
        int start;

        /** the index of the last character in the sentence plus one. */
        int end = -1;

        /** the index of the state. */
        int stateindex;

        /** this is set only in paragraphs when it contains something, sentences with 
         * only white space are of no use. */
        boolean hasMass = false;
        
        /**
         * Create a new span.
         * 
         * @param s the state at the start.
         */
        State(TokenizerState s) {
            this.stateindex = s.ordinal();
        }

        /**
         * check to see if the word is all uppercase and periods, indicating an acronym
         * 
         * @return true if all uppercase and periods.
         */
        public boolean isAbbr() {
            String term = getWord();
            int lastdash = term.lastIndexOf("-");
            if (lastdash != -1)
                term = term.substring(lastdash + 1);
            if (term.length() == 0)
                return false;
            ArrayList<String> abbrs = Acronyms.get(term.charAt(0));
            if (abbrs != null && abbrs.contains(term))
                return true;

            // From this point, an acronym is a string of alphas, <= MAX_CHARS_PER_ACR long starting
            // with
            // a cap and ending with a period, followed by a similar pattern.
            int max = end == -1 ? current : end;
            final int MAX_CHARS_PER_ACR = 1;

            // look for a non acronym.
            boolean needcap = true;
            int smallcnt = 0;
            for (int i = start; i < max; i++) {
                char c = text[i];
                switch (c) {
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'g':
                    case 'h':
                    case 'i':
                    case 'j':
                    case 'k':
                    case 'l':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'r':
                    case 's':
                    case 't':
                    case 'u':
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':
                    case 'z':
                        if (needcap)
                            return false;
                        smallcnt++;
                        if (smallcnt >= MAX_CHARS_PER_ACR)
                            return false;
                        break;
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'G':
                    case 'H':
                    case 'I':
                    case 'J':
                    case 'K':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'O':
                    case 'P':
                    case 'Q':
                    case 'R':
                    case 'S':
                    case 'T':
                    case 'U':
                    case 'V':
                    case 'W':
                    case 'X':
                    case 'Y':
                    case 'Z':
                        if (needcap == false)
                            return false;
                        needcap = false;
                        smallcnt = 0;
                        break;
                    case '.':
                        needcap = true;
                        smallcnt = 0;
                        break;
                    case '-':
                    case '/':
                        needcap = true;
                        smallcnt = 0;
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }

        /**
         * get the current word if we can.
         * 
         * @return the word for this token.
         */
        public String getWord() {
            if (end == -1)
                return new String(text, start, current - start);
            else
                return new String(text, start, end - start);
        }

        /**
         * get the current word if we can.
         * 
         * @return true if the word is numeric.
         */
        public boolean isDate() {
            int max = end == -1 ? current : end;
            for (int i = start; i < max; i++) {
                char c = text[i];
                if (!Character.isDigit(c) && c != '/')
                    return false;
            }
            return true;
        }
        
        /**
         * get the current word if we can.
         * 
         * @return true if the word is numeric.
         */
        public boolean isNumeric() {
            int max = end == -1 ? current : end;
            for (int i = start; i < max; i++) {
                char c = text[i];
                if (!Character.isDigit(c) && c != '.' && c != ',')
                    return false;
            }
            return true;
        }

        /**
         * The content is being pushed onto the stack.
         * 
         * @param where the index of the text for this span.
         */
        public void push(int where) {
            start = where;
        }

        /**
         * the state is being popped off.
         * 
         * @param where where we are during the pop.
         */
        public void pop(int where) {
            this.end = where;
        }

        /**
         * the index of the enumeration.
         * 
         * @return index of the enumeration.
         */
        public int stateIndex() {
            return stateindex;
        }

        /**
         * the size fo the content.
         * 
         * @return the size of the span.
         */
        public int size() {
            return end - start;
        }
    }
    
    /**
     * This is just used to test whatever doesn't parse correctly.
     * @param args
     */
    static public void main(String[] args) {
        final Pattern emailRegex2 = 
                        Pattern.compile("^[A-Za-z0-9\\._%\\+\\-]+@[A-Za-z0-9\\-\\.]+\\b");
        String tmp = "robert_serafin@kmz.com to robert-serafin@kmz.com";
        Matcher match = emailRegex2.matcher(tmp);
        while (match.find()) {
           System.out.println(tmp.substring(match.start(), match.end()));
        }
    }
}
