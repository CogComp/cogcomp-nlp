/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is solely used to validate URIs. You provide a String, and a starting positions, it
 * provides methods to determine if the URI is valid or not. Only syntax is checked by this class,
 * and it is not correct, as "extra" characters are not supported as these could easily be
 * punctuation in pure text. In retrospect, it would have been much better to write a URL validator
 * as the syntax for URLs is much more restrictive, and those would really be the entities we want
 * to produce.
 * 
 * @author redman
 */
public class UrlValidation {
    private static Logger logger = LoggerFactory.getLogger(UrlValidation.class);

    /**
     * defines the part of the url we are looking at.
     * 
     * @author redman
     */
    enum ParsingState {
        /** scheme part */
        scheme,

        /** path */
        path,

        /** encoded hex */
        encoding
    };

    /**
     * The character type.
     * 
     * @author redman
     */
    enum CharacterClass {
        /** asci alphanumeric */
        alphanumeric,

        /** legal special character, not necessarily legal in any part of a uri. */
        special,

        /** this is an illegal character, likely not ascii. */
        illegal,

        /** white space simply indicates the end of the url. */
        space
    }

    /** the parsing state. */
    private int state = ParsingState.scheme.ordinal();;

    /** set when we are done. */
    private boolean bad = false;

    /** the encoded characters. */
    private int encodedchars = 0;

    /** state machine definition. */
    StateProcessor[][] statemachine = { {
            // processing scheme, only characters, ascii only.
            /** get alphanumeric while in scheme, ok. */
            new StateProcessor() {
                @Override
                public void process(char token) {
                    switch (token) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            bad = true;
                        default:
                            return;
                    }
                }
            },

            /** Only "-" and ":" are allowed as part of the scheme. */
            new StateProcessor() {
                @Override
                public void process(char token) {
                    if (token == ':') {
                        // done, this is a valid scheme.
                        state = ParsingState.path.ordinal();
                    } else if (token == '+') {
                        // this is OK.
                    } else {
                        bad = true;
                    }
                }
            }}, {
            // processing path.
            /** get alphanumeric while in scheme, always ok. */
            new StateProcessor() {
                @Override
                public void process(char token) {}
            },

            /** Only "-" and ":" are allowed as part of the scheme. */
            new StateProcessor() {
                @Override
                public void process(char token) {
                    if (token == '%') {
                        state = ParsingState.encoding.ordinal();
                    }
                }
            }}, {
            // processing an encoding, a "%" followed by hex digits.

            /** Only "-" and ":" are allowed as part of the scheme. */
            new StateProcessor() {
                @Override
                public void process(char token) {
                    switch (token) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case 'q':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                            encodedchars++;
                            if (encodedchars == 2) {
                                state = ParsingState.path.ordinal();
                                encodedchars = 0;
                            }
                            break;
                        default:
                            bad = true;
                            break;

                    }
                    if (token == ':') {
                        state = ParsingState.path.ordinal();
                    } else if (token == '-') {
                    }
                }
            },

            /** get alphanumeric while in scheme, ok. */
            new StateProcessor() {
                @Override
                public void process(char token) {

                }
            }}};

    /**
     * classify a character.
     * 
     * @param c the character.
     * @return the character classification.
     */
    private int charClass(char c) {
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
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return CharacterClass.alphanumeric.ordinal();

                // allowed in schema.
            case '+':

                // safe characters.
            case '$':
            case '-':
            case '_':
            case '@':
            case '.':
            case '&':

                // extra characters. I disallow these as it creates more problems than
                // it solves.
                // case '!':case '*':case '"':case '\'':case '(':case ')':case ',':

                // reserved
            case '=':
            case ';':
            case '/':
            case '#':
            case '?':
            case ':':
                return CharacterClass.special.ordinal();
                // space
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                return CharacterClass.space.ordinal();

            default:
                return CharacterClass.illegal.ordinal();
        }
    }

    /**
     * Check the string to determine if it is valid, if it is, return the position of the first
     * character not part of the url.
     * 
     * @param data the data we are looking at.
     * @param offset the offset to look for a url.
     * @return the end of the URL or -1 if not valid url.
     */
    public int isValid(char[] data, int offset) {

        // the first character must be alphabet char, check that first.
        char firstchar = data[offset];
        int cc = this.charClass(firstchar);
        if (cc != CharacterClass.alphanumeric.ordinal())
            return -1;
        else {
            switch (firstchar) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    return -1;
            }
        }

        // check the rest.
        for (int i = offset + 1; i < data.length; i++) {
            char it = data[i];
            int charclass = this.charClass(it);
            if (charclass == CharacterClass.illegal.ordinal()) {
                return -1; // illegal character
            } else if (charclass == CharacterClass.space.ordinal()) {
                return i; // we found white space, still valid.
            } else {
                statemachine[state][charclass].process(it);
                if (bad)
                    return -1;
            }
        }
        return data.length;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        UrlValidation uv = new UrlValidation();
        int offset;
        String t = "This is a url called htt-ps://toopy.lang/";
        if ((offset = uv.isValid(t.toCharArray(), 21)) != -1) {
            logger.info(t.substring(21, offset));
        }
    }

}
