package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions related to tokens and tokenization.
 */
public class TokenUtils {

  /**
   * Takes a string and its tokenizied form, and returns an array of span index pairs.
   * @param sentence raw input text
   * @param tokens the tokenized form of the sentence
   * @return array of span index pairs.
   */
  public static IntPair[] getTokenOffsets(String sentence, String[] tokens) {
    List<IntPair> offsets = new ArrayList<>();

    int tokenId = 0;
    int characterId = 0;

    int tokenCharacterStart = 0;
    int tokenLength = 0;

    while (characterId < sentence.length() && Character.isWhitespace(sentence.charAt(characterId)))
      characterId++;

    while (characterId < sentence.length()) {
      if (tokenLength == tokens[tokenId].length()) {
        offsets.add(new IntPair(tokenCharacterStart, characterId));

        while (characterId < sentence.length()
            && Character.isWhitespace(sentence.charAt(characterId)))
          characterId++;

        tokenCharacterStart = characterId;
        tokenLength = 0;
        tokenId++;

      } else {
        assert sentence.charAt(characterId) == tokens[tokenId]
            .charAt(tokenLength) : sentence.charAt(characterId)
            + " expected, found "
            + tokens[tokenId].charAt(tokenLength)
            + " instead in sentence: " + sentence;

        tokenLength++;
        characterId++;

      }
    }

    if (characterId == sentence.length()
        && offsets.size() == tokens.length - 1) {
      offsets.add(new IntPair(tokenCharacterStart, sentence.length()));
    }

    assert offsets.size() == tokens.length : offsets;

    return offsets.toArray(new IntPair[offsets.size()]);
  }
}
