/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * Classes here are used to read the LDC ERE data corpuse. The EREDocumentReader
 * will read an ERE XML source document and produce a TextAnnotation that preserves
 * the character offsets for retained text, while stripping XML markup. There are
 * utilitites included here that allow conversion of the ERE source and labeled
 * data to CoNLL 2002 format for use with NER as well.
 * @author redman
 *
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader;