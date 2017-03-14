package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.*;

/**
 * Store a list of transformations applied to an original string to produce a new String.
 * These transformations modify the character offsets of the remaining string (e.g. replacing xml-escaped
 *     characters with their ascii counterparts, without padding the replacement with whitespace).
 * This could include deletion of markup, in which case information associated with the markup might be stored.
 *
 * @author mssammon
 */
public class StringTransformation {

    private final String origText;
    // tracks whether transformedText field is out of date
    boolean isModified;
    private String transformedText;
    // store a change of length value at transformedText character for each operation performed on transformedText
    // these are temporary, until getTransformedText() is called. Order is useful, so use TreeMap.
    private TreeMap<Integer, Integer> currentOffsetModifications;
    // store a change of length value at origText character offset for each operation performed on origText.
    // these are permanent.
    private TreeMap<Integer, Integer> recordedOffsetModifications;
    private HashMap<IntPair, Map<String, String>> markup;
    // a list of edits, that respects the order in which they were specified. Client responsible for coherence.
    private List<Pair<IntPair, Pair<String, String>>> edits;
    private TreeMap<Integer, Integer> recordedInverseModifications;

    public StringTransformation(String origText) {
        this.origText = origText;
        this.transformedText = origText;
        currentOffsetModifications = new TreeMap();
        recordedOffsetModifications = new TreeMap<>();
        recordedInverseModifications = new TreeMap<>();
        markup = new HashMap<>();
        isModified = false;
        edits = new ArrayList<>();
    }


    public String getOrigText() {
        return origText;
    }



    public String getTransformedText() {
        if (isModified) {
            this.applyPendingEdits();
        }
        return transformedText;
    }


//    /**
//     * Remove the specified span, store specified markup attributes.
//     * Interprets offsets wrt current representation of transformed string -- i.e. multiple sequential calls to
//     *    transformMarkup() without a call to getModifiedString() must all refer to the string in the state it was
//     *    in when the first such call was made.  The goal here is to allow efficiency by avoiding repeated computation
//     *    of many offset changes for every new transformation.
//     *
//     * @param origStart start offset in string used by client
//     * @param origEnd end offset in string used by client (one-past-the-end)
//     * @param attributes list of properties to store associated with removed markup
//     */
//    public void removeMarkup(int origStart, int origEnd, Map<String, String> attributes) {
//
//        IntPair transformOffsets = transformString(origStart, origEnd, "");
//        markup.put(transformOffsets, attributes);
//    }


    /**
     * Modify the current version of the transformed text (as returned by getTransformedText()) by replacing the
     *    string between character offsets textStart and textEnd with newStr.
     * @param textStart
     * @param textEnd
     * @param newStr
     * @return the offsets in the current, internally transformed text corresponding to textStart and textEnd
     */
    public IntPair transformString(int textStart, int textEnd, String newStr) {

        int start = textStart;
        int end = textEnd;

        // need updated offsets for return value -- e.g. to use as key for transform attributes
        if (isModified) {
            start = computeCurrentOffset(textStart);
            end = computeCurrentOffset(textEnd);
            if (start < 0 || end < 0 ) {
                throw new IllegalStateException("ERROR: edit affects deleted span (offsets are negative). Reorder " +
                    "edits or filter overlapping edits.");
            }
        }
        // compute the net change in offset: negative for deletion/reduction, positive for insertion,
        //   zero for same-length substitution; store with indexes in current transformed text
        int newLen = newStr.length();
        int origLen = textEnd - textStart;
        int netDiff = newLen - origLen;
        if (netDiff < 0) // involves deleting chars: after new str, modify the offsets
            currentOffsetModifications.put(textStart + newLen, netDiff );
        else if (netDiff > 0) // involves insertion
            currentOffsetModifications.put(textStart + origLen, netDiff);
        // else just replaced, no offset changes needed.

        IntPair transformOffsets = new IntPair(start, end);
        String origStr = transformedText.substring(textStart, textEnd);
        // edit offsets encode affected substring allowing for previous edits in current pass
        edits.add(new Pair(transformOffsets, new Pair(origStr, newStr)));
        isModified = true;
        return transformOffsets;
    }



    /**
     * given an offset in the current transformedString stored by this object, return the corresponding
     *    offset in the original String.
     * @param modOffset offset in modified string
     * @return corresponding offset in original string
     */
    public int computeOriginalOffset(int modOffset) {
        if (isModified)
            applyPendingEdits();

        int currentChange = 0;
        for (Integer changeIndex : recordedOffsetModifications.keySet()) {
            if (changeIndex > modOffset)
                break;
            currentChange += recordedOffsetModifications.get(changeIndex);
        }
        return modOffset - currentChange; //inverse of edit effect
    }

    /**
     * apply any pending edits, update the modified string
     */
    private void applyPendingEdits() {

        String currentStr = transformedText;
        if (isModified) {
            /*
              immediately set flag, as we'll be calling other methods that check this condition, which could call this
                 method
             */
            isModified = false;
            for ( Pair<IntPair, Pair<String, String>> edit : edits ) {
                IntPair editOffsets = edit.getFirst();
                String before = currentStr.substring(0, editOffsets.getFirst());
                String after = currentStr.substring(editOffsets.getSecond());
                currentStr = before + edit.getSecond().getSecond() + after;
            }
            transformedText = currentStr;

            /*
             * store pending recorded offsets while computing absolute offsets for all current edits
             */
            Map<Integer, Integer> toAdd = new HashMap();

            for (Integer modOffset : currentOffsetModifications.keySet()) {
                Integer currentMod = currentOffsetModifications.get(modOffset);
                /*
                 * recorded offset mods MUST be made with respect to ORIGINAL offsets -- not the current transformed
                 *     string.
                 */
                Integer absoluteModOffset = computeOriginalOffset(modOffset);

                if (recordedOffsetModifications.containsKey(absoluteModOffset))
                    currentMod += recordedOffsetModifications.get(absoluteModOffset);

                if (toAdd.containsKey(absoluteModOffset))
                    currentMod += toAdd.get(absoluteModOffset);

                toAdd.put(absoluteModOffset, currentMod);
            }

            for (int key : toAdd.keySet())
                recordedOffsetModifications.put(key, toAdd.get(key));

            /**
             * compute inverse mapping (from transformed text offsets to original offsets)
             *    using the complete set of transformations to date: store as offset modifiers
             *    at transform string indexes where changes occur, such that adding the offset modifier
             *    to the current transform index yields the corresponding offset in the original string.
             */
            int runningMod = 0;
            for (Integer transformModIndex : recordedOffsetModifications.keySet()) {
                int baseOffset = transformModIndex + runningMod;
                int transformMod = recordedOffsetModifications.get(transformModIndex);
                if (transformMod < 0) // deletion; transformMod is negative
                    recordedInverseModifications.put(baseOffset, runningMod - transformMod);
                else // for insertion, map all intermediate offsets to first character in orig string
                    for (int i = 1; i <= transformMod; ++i) {
                        recordedInverseModifications.put(baseOffset + i, --runningMod);
                    }
            }

            /**
             * cleanup: remove temporary state that has now been resolved
             */
            currentOffsetModifications.clear();
            edits.clear();

        }
    }


    /**
     * given an offset in the current transformedString stored by this offset, return the corresponding
     *    offset if all other pending edits are performed.
     * @param modOffset character index in modified string
     * @return corresponding character index in modified string after current edits are performed
     */
    private int computeCurrentOffset(int modOffset) {
        int currentChange = 0;
        for (Integer changeIndex : currentOffsetModifications.keySet()) {
            if (changeIndex > modOffset)
                break;
            currentChange += currentOffsetModifications.get(changeIndex);
        }
        return modOffset + currentChange;
    }


    /**
     * given a character offset in the original string, find the corresponding offset in the
     *   modified string.
     * Characters that were deleted are mapped to the next <emph>following</emph> non-deleted character offset.
     * @param origOffset offset in original string
     * @return offset in modified string
     */
    public int computeModifiedOffsetFromOriginal(int origOffset) {
        int currentChange = 0;

        for (Integer changeIndex : recordedOffsetModifications.keySet()) {
            if (changeIndex > origOffset) // + currentChange) // account for previous changes in computing current position
                break;
            currentChange += recordedOffsetModifications.get(changeIndex);
        }

        return origOffset + currentChange;
    }

    /**
     * given a pair of offsets into the transformed text, retrieve the corresponding offsets in the original text.
     * @param transformStart start of transformed text substring
     * @param transformEnd end of transformed text substring
     * @return offsets in original text corresponding to the specified span in the transformed string
     */
    public IntPair getOriginalOffsets(int transformStart, int transformEnd) {

        int origStart = transformStart;
        int origEnd = transformEnd;

        for (Integer changeIndex : this.recordedInverseModifications.keySet()) {
            if (changeIndex < origStart)
                origStart = transformStart + recordedInverseModifications.get(changeIndex);
            if (changeIndex <= origEnd)
                origEnd = transformEnd + recordedInverseModifications.get(changeIndex);
        }

        return new IntPair(origStart, origEnd);
    }
}
