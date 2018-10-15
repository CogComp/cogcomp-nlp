/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * This package contains the command line tagger and associated required files and interfaces. The
 * {@link edu.illinois.cs.cogcomp.ner.tagger.NamedEntityTagger} class is a command line tool
 * allowing it's user to read data form a file or directory and to produce the output(s) likewise to
 * a file or directory.
 * <p>
 * 
 * This command line tool operates on plain text and produces output in bracketed file format.
 * However, it was designed to be easily subclasses to support any style of input and to produce any
 * style of output. There is a subclass for example that tags using the stanford parser, this is
 * very useful for testing.
 * <p>
 * 
 * To develop such a subclass, implement {@link edu.illinois.cs.cogcomp.ner.tagger.AnnotationJob},
 * and override NamedEntityTagger to provide the getAnnotationJob method appropriately.
 * 
 * @author redman
 */
package edu.illinois.cs.cogcomp.ner.tagger;