/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.Serializable;
import java.util.*;

/**
 * Store a list of transformations applied to an original string to produce a new String.
 * These transformations modify the character offsets of the remaining string (e.g. replacing xml-escaped
 *     characters with their ascii counterparts, without padding the replacement with whitespace).
 * This could include deletion of markup, in which case information associated with the markup might be stored.
 *
 * @author mssammon
 */
public class StringTransformation implements Serializable {

    private static final long serialVersionUID = 1526472295622723447L;

    private static final boolean DEBUG = false;

        /** source text: immutable record of the starting point for all transformations */
    private final String origText; ;
    /** tracks whether transformedText field is out of date. */
    boolean isModified;
    /**
     * The state of the text after edits are applied (excluding pending edits).
     * The client gets this String, computes a set of desired changes in a single pass, then applies the updates.
     */
    private String transformedText;
    /**
     * store a change of length value at transformedText character for each operation performed on transformedText
     *     These are temporary, until getTransformedText() is called. Order is useful, so use TreeMap.
     */
    private TreeMap<Integer, Pair<Integer, EditType>> currentOffsetModifications;
    /** store a change of length value at origText character offset for each operation performed on origText. */
    private TreeMap<Integer, Pair<Integer, EditType>> recordedOffsetModifications;
    /** records changes to offsets to transformed string to retrieve corresponding character in original string */
    private TreeMap<Integer, Pair<Integer, EditType>> recordedInverseModifications;
    /** a list of edits, that respects the order in which they were specified. Client responsible for coherence. */
    private List<Edit> edits;
    /** stores deleted regions, for which no mappings can be generated */
    private TreeMap<Integer, IntPair> unmappedOffsets;

    /**
     * Constructor.
     * @param origText the original form of the string whose modifications you want to track.
     */
    public StringTransformation(String origText) {
        this.origText = origText;
        this.transformedText = origText;
        currentOffsetModifications = new TreeMap();
        recordedOffsetModifications = new TreeMap<>();
        recordedInverseModifications = new TreeMap<>();
        isModified = false;
        edits = new ArrayList<>();
    }

    /**
     * get the original text, before all edits were applied.
     * @return the original text.
     */
    public String getOrigText() {
        return origText;
    }

    /**
     * get the updated text, after edits are applied. Performs any pending edits first.
     * @return the transformed text.
     */
    public String getTransformedText() {
        if (isModified) {
            this.applyPendingEdits();
        }
        return transformedText;
    }

    /**
     * Modify the current version of the transformed text (as returned by getTransformedText()) by replacing the
     *    string between character offsets textStart and textEnd with newStr.
     * @param textStart character offset start of edit in transformed text
     * @param textEnd character offset end of edit in transformed text
     * @param newStr string to replace specified character span
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
            if (start > end) {
                throw new IllegalStateException("ERROR: edit has end offset less than start offset. Some kind of " +
                    "unhelpful interaction between edits - could be a bug.");
            }
        }
        // compute the net change in offset: negative for deletion/reduction, positive for insertion,
        //   zero for same-length substitution; store with indexes in current transformed text
        int newLen = newStr.length();
        int origLen = textEnd - textStart;
        int netDiff = newLen - origLen;
        EditType editType = EditType.SUBST;

        if (netDiff != 0) { // else just replaced, no offset changes needed.
            int putIndex = textStart + origLen; // for insertion, add the modifier at the end of the original span

            if (netDiff < 0) { // involves deleting chars: after new str, modify the offsets
                putIndex = textStart + newLen;
                editType = (newLen == 0) ? EditType.DELETE : EditType.REDUCE;
            }
            else  // expanding or inserting
                editType = (origLen == 0) ? EditType.INSERT : EditType.EXPAND;

            // account for any previous modifications at this index
            if (currentOffsetModifications.containsKey(putIndex))
                netDiff += currentOffsetModifications.get(putIndex).getFirst();

            currentOffsetModifications.put(putIndex, new Pair(new Integer(netDiff), editType));
        }
        IntPair transformOffsets = new IntPair(start, end);
        String origStr = transformedText.substring(textStart, textEnd);

        // edit offsets encode affected substring allowing for previous edits in current pass
        edits.add(new Edit(transformOffsets, origStr, newStr, editType));
        isModified = true;
        return transformOffsets;
    }

    /**
     * given an offset in the current transformedString stored by this object, return the corresponding
     *    offset in the original String.
     * NOTE: this does NOT account for pending edits. If you wish this, first call applyPendingEdits.
     * @param modOffset offset in modified string
     * @return corresponding offset in original string
     */
    private int computeOriginalOffset(int modOffset) {

        int currentChange = 0;
        EditType editType = EditType.DELETE;

        for (Integer changeIndex : recordedOffsetModifications.keySet()) {
            if (changeIndex > modOffset - currentChange)
                break;
            currentChange += recordedOffsetModifications.get(changeIndex).getFirst();
            editType = recordedOffsetModifications.get(changeIndex).getSecond();

        }
        return modOffset - currentChange; //inverse of edit effect
    }

    /**
     * apply any pending edits, update the modified string
     */
    public void applyPendingEdits() {

        String currentStr = transformedText;
        if (isModified) {
            /*
              immediately set flag, as we may call other methods that check this condition, which could call this
                 method
             */
            isModified = false;
            /*
             * it's OK for edits to be unsorted: all edit offsets are computed relative to the previous edits
             *    in the sequence
             */
            for ( Edit edit : edits ) {
                IntPair editOffsets = edit.offsets;
                String before = currentStr.substring(0, editOffsets.getFirst());
                String after = currentStr.substring(editOffsets.getSecond());
                currentStr = before + edit.newString + after;
            }
            transformedText = currentStr;

            /*
             * store pending recorded offsets while computing absolute offsets for all current edits
             */
            Map<Integer, Pair<Integer, EditType>> toAdd = new TreeMap();

            for (Integer modOffset : currentOffsetModifications.keySet()) {

                Integer currentMod = currentOffsetModifications.get(modOffset).getFirst();
                EditType currentEditType = currentOffsetModifications.get(modOffset).getSecond();
                /*
                 * recorded offset mods MUST be made with respect to ORIGINAL offsets -- not the current transformed
                 *     string.
                 */
                Integer absoluteModOffset = computeOriginalOffset(modOffset);

                // TODO: verify that it's OK to just keep the original edit type
                if (toAdd.containsKey(absoluteModOffset))
                    currentMod += toAdd.get(absoluteModOffset).getFirst();

                toAdd.put(absoluteModOffset, new Pair<>(currentMod, currentEditType));
            }

            /**
             * The entries in toAdd *cannot* conflict, because they come from a single pass
             * Now we need to merge them with previously recorded offset mods
             */
            if (recordedOffsetModifications.isEmpty())
                recordedOffsetModifications.putAll(toAdd);
            else {
                TreeMap<Integer, Pair<Integer,EditType>> safeAdds = new TreeMap<>();

                int lastKeyPos = 0; // stores position of greatest of last key, or the last key's effective edit position
                for (int key : toAdd.keySet()) {
                    int mod = toAdd.get(key).getFirst();
                    EditType editType = toAdd.get(key).getSecond();

                    if (key < lastKeyPos)
                        key = lastKeyPos; // move to after last entry key + edit
                    /*
                     * it gets a bit tricky if a new deletion overlaps older edits: you need to split up the new edit.
                     * TODO: merge edits instead
                     */
                    for (int oldKey : recordedOffsetModifications.keySet()) {
                        if (mod == 0)
                            break;
                        // am I at the same index?
                        if (oldKey == key) { //if edit is an expansion, still advance one position
                            key = Math.max(key + 1, key - recordedOffsetModifications.get(oldKey).getFirst()); //move on...
                        }
                        // am I within the window of a prior edit?
                        else if (oldKey < key) {
                            int oldMod = recordedOffsetModifications.get(oldKey).getFirst();
                            int diff = oldKey - key; // negative, to compare with negative mod
                            if (diff > oldMod) { // edits interfere; can't happen if oldMod is positive (insertion)
                                key = oldKey - oldMod; // modifier doesn't change: edit not applied yet; update edit
                                // position to just past old edit
                            }
                        } else if (oldKey > key) { // Is next edit within window of my edit?
                            int diff = key - oldKey; // negative, to compare with -ve mod
                            if (diff > mod) { //if diff > mod, mod is negative and edits interfere.
                                safeAdds.put(key, new Pair<>(diff, editType)); // delete up to current edit
                                mod = mod - diff; // part of modification not accounted for; again, recall both negative
                                key = oldKey - recordedOffsetModifications.get(oldKey).getFirst(); // move to index after old edit
                            } else { // either mod is positive, or next edit does not interfere
                                safeAdds.put(key, new Pair<>(mod, editType));
                                lastKeyPos = Math.max(key, key - mod); // update if -ve mod
                                mod = 0; //break from the loop
                            }
                        }
                    }

                    if (mod != 0) // past all old edits, haven't added it yet...
                        safeAdds.put(key, new Pair<>(mod, editType));
                }
                recordedOffsetModifications.putAll(safeAdds);
            }

            /*
             * compute inverse mapping (from transformed text offsets to original offsets)
             *    using the complete set of transformations to date: store as offset modifiers
             *    at transform string indexes where changes occur, such that adding the offset modifier
             *    to the current transform index yields the corresponding offset in the original string.
             */
            recordedInverseModifications.clear();

            /*
             * recordedOffsetModifications: at char index X, modify running offset modifier by Y
             */
            int cumulativeOffset = 0;
            for (Integer transformModIndex : recordedOffsetModifications.keySet()) {
                int baseIndex = transformModIndex;
                int transformMod = recordedOffsetModifications.get(transformModIndex).getFirst();
                EditType editType = recordedOffsetModifications.get(transformModIndex).getSecond();
                /*
                 * suppose tranform offset is 33, and modifier is -33 (delete the first 33 chars of the orig string).
                 * Therefore we want index 0 of the transformed string to map to offset 33 of the orig string.
                 * So we update the cumulative offset *after* adding the current mod.
                 * Subsequent edits to orig string increase the total difference between the transformed string
                 *    base index and the corresponding orig string index, hence the need for cumulative offset to
                 *    be subtracted from the orig index. (mod is -ve, therefore subtraction even though it's added
                 *    to the offset from the perspective of the original string
                 */
                int effectiveIndex = baseIndex - cumulativeOffset;
                int effectiveMod = transformMod;
                if (recordedInverseModifications.containsKey(effectiveIndex))
                    effectiveMod -= recordedInverseModifications.get(effectiveIndex).getFirst();

                // TODO: verify that using most recent transform type is correct if there was already an edit in RIM
                recordedInverseModifications.put(effectiveIndex,  new Pair<>(-effectiveMod, editType));
                cumulativeOffset -= transformMod;
            }

            if (DEBUG) {
                int lastIndex = 0;
                int lastOrigOffset = 0;
                for (int revInd : recordedInverseModifications.keySet()) {
                    int diff = revInd - lastIndex;
                    String origSub = origText.substring(lastOrigOffset, lastOrigOffset + diff);
                    System.err.println(lastIndex + "-" + revInd + ": " + origSub);
                    lastOrigOffset = lastOrigOffset + diff + recordedInverseModifications.get(revInd).getFirst();
                    lastIndex = revInd;
                }
            }
            /*
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
            if (changeIndex >= modOffset) {
                EditType mod = currentOffsetModifications.get(changeIndex).getSecond();
                if (mod.equals(EditType.DELETE))
                    break;
                else if (changeIndex > modOffset) // increase in current offsets
                    break;

            }

            currentChange += currentOffsetModifications.get(changeIndex).getFirst();
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
            int change = recordedOffsetModifications.get(changeIndex).getFirst();
            EditType editType = recordedOffsetModifications.get(changeIndex).getSecond();

            if (origOffset + change < changeIndex) // change is deletion, orig offset in deleted window
                change = changeIndex - origOffset; // set to index recorded for deletion, rather than prior index

            //TODO: logic for behavior based on edit type; only when edit boundary touches origOffset

            currentChange += change;
        }

        return origOffset + currentChange;
    }

    /**
     * given a pair of offsets into the transformed text, retrieve the corresponding offsets in the original text.
     * This behavior treats the end offset differently from the start offset (accounts for changes after the end)
     * @param transformStart start of transformed text substring
     * @param transformEnd end of transformed text substring
     * @return offsets in original text corresponding to the specified span in the transformed string
     */
    public IntPair getOriginalOffsets(int transformStart, int transformEnd) {

        int origStart = transformStart;
        int origEnd = transformEnd;

        for (Integer changeIndex : this.recordedInverseModifications.keySet()) {

            EditType editType = recordedInverseModifications.get(changeIndex).getSecond();
            int offsetMod = recordedInverseModifications.get(changeIndex).getFirst();

            if (changeIndex <= transformStart) {
                // earlier -- always apply edit
                origStart += offsetMod;
            }
//            else if (changeIndex == transformStart) {
//                if (EditType.DELETE.equals(editType)) {
//                    origStart += offsetMod;
//                }
//            }
            if (changeIndex < transformEnd) {
                origEnd += offsetMod;
            }
            else if (changeIndex == transformEnd) {
                if (!EditType.DELETE.equals(editType))
                    origEnd += offsetMod;
            }
        }

        return new IntPair(origStart, origEnd);
    }


public enum  EditType { INSERT, DELETE, REDUCE, SUBST, EXPAND }

    private class Edit {

        public final IntPair offsets;
        public final String origString;
        public final String newString;
        public final EditType type;

        public Edit( IntPair offsets, String origStr, String newStr, EditType type) {
            this.offsets = offsets;
            this.origString = origStr;
            this.newString = newStr;
            this.type = type;
        }
    }
}
