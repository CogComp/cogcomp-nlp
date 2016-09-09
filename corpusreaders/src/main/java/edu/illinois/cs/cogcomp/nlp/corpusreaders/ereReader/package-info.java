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