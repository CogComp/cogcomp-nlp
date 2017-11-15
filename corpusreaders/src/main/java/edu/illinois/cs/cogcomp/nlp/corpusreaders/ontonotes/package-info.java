 /**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;
/**
 * <p>The files in this directory are used to produce appropriate text annotations and views from the OntoNotes 5.0 corpus. 
 * The implementations come in the form of Readers. They are used in the same way as other readers, they read the 
 * data and product Text annotation objects.</p>
 * 
 * <p>There are currently two readers. The @see OntonotesTreebankReader reads treebank data from the ontonotes corpus, the 
 * @see OntonotesPropbankReader will read propbank data in. These classes also include main methods that can be used to produce
 * json output representing the data.</p>
 */