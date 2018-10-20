 /**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes;
/**
 * <p>The files in this directory are used to produce appropriate text annotations and views from the OntoNotes 5.0 corpus. 
 * The implementations come in the form of Readers. They are used in the same way as other readers, they read the 
 * data and produce Text annotation objects.</p>
 * 
 * <p>There are currently four readers. The OntonotesTreebankReader reads treebank data from the ontonotes corpus, the 
 * OntonotesPropbankReader will read propbank data. These classes also include main methods that can be used to produce
 * json output representing the data. The OntonotesNamedEntityReader produces text annotations containing the named enties.
 * The OntonotesCorefReader produces coref data.</p>
 */