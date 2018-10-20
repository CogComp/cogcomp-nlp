/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import gnu.trove.map.hash.THashMap;

/**
 * this is a class optimized to match an expression that may be a word or a phrase. It is a THashSet
 * on the outside, but every match entry is an object that may indicate a match, or may require
 * additional matches down the tree.
 * 
 * @author redman
 */
public class GazetteerTree {

    /** the entries for this tree. */
    final private THashMap<String, GazEntry> gaz = new THashMap<>();

    /** this is the maximum number of terms in a phrase we will be able to match. */
    final private int maxPhraseLength;

    /** String splitter, by default split on 1 or more white space. */
    private StringSplitterInterface splitter = null;

    /**
     * instances of this interface can be passed in to control how strings are split. If not
     * interface is passed in explicitly, strings are split on the pattern "[\\s]+"
     * 
     * @author redman
     */
    public interface StringSplitterInterface {

        /**
         * return the term or phrase appropriately split.
         * 
         * @param line the line to split.
         * @return the split line.
         */
        String[] split(String line);


        /**
         * return the normalized form of the string to produce the form of the term that should be
         * used to match the dictionary.
         * 
         * @param term the line to normalize.
         * @return the normalized term.
         */
        String normalize(String term);

    }

    /**
     * this indicates no match, unless terms further down match.
     */
    class GazEntry {

        /** indicates if this is a leaf node indicating a match. */
        ArrayList<DictionaryNames> names;

        /** the children or null. */
        THashMap<String, GazEntry> requiredChildren;

        /**
         * provided with the split phrase, either make the new terminal leaf entry with match true,
         * or make the entry with match not true, and children. The first term passed in is the key,
         * so we won't store it here, but if there are subsequent terms, we need to construct a tree
         * path.
         * @param entries the entries for this node.
         * @param n dictionary names.
         */
        GazEntry(String[] entries, DictionaryNames n) {
            if (entries.length == 1) {

                // This is a single term, so there is a match at this node
                addMatch(n);
                requiredChildren = null;
            } else {

                // this is a new phrase, not just a term, since this is the
                // first word, it is not a match.
                names = null;

                // add the children
                requiredChildren = new THashMap<>();
                requiredChildren.put(entries[1], new GazEntry(entries, 2, n));
            }
        }

        /**
         * Term from a phrase, beyond the first term. If this is the last term, it indicates a
         * match, otherwise it does not, and the children must be constructed.
         * @param entries the entries
         * @param i the location in the list.
         * @param n dict names.
         */
        GazEntry(String[] entries, int i, DictionaryNames n) {
            if (entries.length == i) {
                addMatch(n);
                requiredChildren = null;
            } else {

                // this is a new phrase, not just a term, so not a match
                names = null;

                // add the children
                requiredChildren = new THashMap<>();
                requiredChildren.put(entries[i], new GazEntry(entries, i + 1, n));
            }
        }


        /**
         * Add a match to the list of possible matches.
         * 
         * @param n the name to add.
         */
        private void addMatch(DictionaryNames n) {
            if (names == null)
                names = new ArrayList<>();
            if (!names.contains(n)) {
                names.add(n);
            }
        }

        /**
         * Add a phrase, or if final word in phrase (or single word phrase), simply set the match to
         * true.
         * 
         * @param terms the term to add
         * @param i the index of the term.
         */
        final void addChild(String[] terms, int i, DictionaryNames n) {
            if (terms.length == i) {
                addMatch(n);
            } else {
                // have a path item to add for a phrase.
                String term = terms[i];
                if (requiredChildren == null) {

                    // construct a new path.
                    requiredChildren = new THashMap<>();
                    requiredChildren.put(term, new GazEntry(terms, i + 1, n));
                } else {

                    // we already have a path at this level, see if we have another hit.
                    GazEntry ge = requiredChildren.get(term);
                    if (ge == null) {
                        requiredChildren.put(term, new GazEntry(terms, i + 1, n));
                    } else {
                        ge.addChild(terms, i + 1, n);
                    }
                }
            }
        }

        /**
         * Add a gazetteer name to the list of gazetteers matched by this term, exclude duplicates.
         * 
         * @param word the word that matched.
         * @param gazname the name of the gazetteer.
         */
        private void addGazetteerToWord(NEWord word, String gazname) {
            ArrayList<String> gazes = word.gazetteers;
            gazes.add(gazname);
        }

        /**
         * Phrase or word found, mark it.
         * 
         * @param words the previous words in this phrase.
         * @param word the last word of this phrase, the one that matched
         */
        private void mark(ArrayList<NEWord> words, NEWord word) {
            if (words != null) {
                for (DictionaryNames dn : names) {

                    // add the begin marker.
                    this.addGazetteerToWord(words.get(0), dn.markers[DictionaryNames.BEGIN]);

                    // add inner markers.
                    String mark = dn.markers[DictionaryNames.INSIDE];
                    for (int i = 1; i < words.size(); i++)
                        this.addGazetteerToWord(words.get(i), mark);

                    // add end marker.
                    this.addGazetteerToWord(word, dn.markers[DictionaryNames.END]);
                }
            } else {
                for (DictionaryNames dn : names) {
                    this.addGazetteerToWord(word, dn.markers[DictionaryNames.UNIT]);
                }
            }
        }

        /**
         * this gaz entry matched the word passed in, if this is a leaf (or match more aptly), add
         * the word to the gaz matches for the word. But continue down the tree looking for deeper
         * matches if possible. This method is only called by the Gazetteer tree, the private method
         * is called by the GazEntry only.
         * 
         * @param word the word that matches.
         */
        final public void compileMatches(ArrayList<NEWord> words, NEWord word) {

            // is this match a leaf, if so, we have a real match.
            if (names != null) {

                // We have a match, mark all the matching terms appropriately
                this.mark(words, word);
            }

            // do we need to check depth
            if (requiredChildren != null) {
                NEWord next = (NEWord) word.next;
                if (next == null)
                    return; // we are done.
                GazEntry ge = requiredChildren.get(splitter.normalize(next.form));
                if (ge != null) {
                    if (words == null)
                        words = new ArrayList<>();
                    words.add(word);
                    ge.compileMatches(words, next);
                }
            }
        }

        /**
         * print each of the terms matchec by this GazEntry on a single line. This is used for
         * debugging only.
         */
        final public void stringRepresentation(String key, String phrase, StringBuffer sb) {
            if (names != null) {
                sb.append(phrase);
                sb.append('\n');
            }
            if (requiredChildren != null) {
                for (String nextkey : requiredChildren.keySet()) {
                    GazEntry ge = requiredChildren.get(nextkey);
                    String newsb = phrase + ' ' + nextkey;
                    ge.stringRepresentation(nextkey, newsb, sb);
                }
            }
        }

        /**
         * when we are done loading up all the gazetteers, we will trim all the collections down to
         * size to save memory.
         */
        public void trimToSize() {
            if (names != null)
                names.trimToSize();
            if (requiredChildren != null) {
                requiredChildren.trimToSize();
                for (GazEntry nextkey : requiredChildren.values()) {
                    nextkey.trimToSize();
                }
            }
        }
    }

    /**
     * Initialize the tree, data will be read via a separate call.
     * 
     * @param phrase_length the max number of terms to match per phrase.
     * @param splitr this interface implementation will determin how we split strings.
     */
    GazetteerTree(int phrase_length, StringSplitterInterface splitr) {
        this.maxPhraseLength = phrase_length;
        this.splitter = splitr;
    }

    /**
     * Read the file, constructing an entry for each line in the file, for each line containing
     * multiple words create a path farther down in the tree, with a match only at the end. If any
     * entry already exists for the name, ensure the entry class is the correct one, add entries
     * farther down if necessary.
     * 
     * @param phrase_length the max number of terms to match per phrase.
     * @param splitr this interface implementation will determin how we split strings.
     * @throws IOException
     */
    GazetteerTree(int phrase_length, String filename, String suffix, InputStream res,
            StringSplitterInterface splitr) throws IOException {
        this(phrase_length, splitr);
        this.readDictionary(filename, suffix, res);
    }

    /**
     * read the given dictionary file from the input stream.
     * 
     * @param filename the file nae.
     * @param suffix the parse tag suffix.
     * @param res the input stream to read the data from.
     * @throws IOException
     */
    void readDictionary(String filename, String suffix, InputStream res) throws IOException {
        DictionaryNames nms = new DictionaryNames(filename, suffix);
        BufferedReader in = new BufferedReader(new InputStreamReader(res));
        String line;
        while ((line = in.readLine()) != null) {
            String[] terms = splitter.split(line);
            
            // ignore any phrases more than maxPhraseLength words.
            if (terms.length > maxPhraseLength)
                continue;
            
             // just ignore blank lines, or lines stripped by the splitter
            if (terms.length == 0)
                continue;
            
            // make the entry.
            GazEntry ge = gaz.get(terms[0]);
            if (ge == null) {
                gaz.put(terms[0], new GazEntry(terms, 1, nms));
            } else {
                ge.addChild(terms, 1, nms);
            }
        }
        in.close();
    }

    /**
     * to string prints a representation of every string contained in the gazetteer.
     * 
     * @return a string representation of the gazetteer.
     */
    public String toString() {
        StringBuffer printstring = new StringBuffer();
        for (String key : gaz.keySet()) {
            GazEntry ge = gaz.get(key);
            ge.stringRepresentation(key, key, printstring);
        }
        return printstring.toString();
    }

    /**
     * return true if a match is found.
     * 
     * @param word the token to match.
     */
    final public void match(NEWord word) {
        GazEntry ge = gaz.get(splitter.normalize(word.form));
        if (ge != null) {
            ge.compileMatches(null, word);
        }
    }

    /**
     * trim off excess size dead space.
     */
    final public void trimToSize() {
        for (GazEntry ge : gaz.values()) {
            ge.trimToSize();
        }
    }
}
