/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * The code in this package is stand-alone, and aligns an older version of the training data with newer
 * tokenization and POS tagging produced by the POS tagger. A tree data structure is constructed
 * of the original data, as well as the new POS data, then the updated POS data is transposed onto the older
 * chunker training data repacing the previous construct there.
 * @author redman
 */
package edu.illinois.cs.cogcomp.chunker.realign;