# Cogcomp Corpusreaders

## Overview

This module includes NLP Corpus readers that reads into datastructures
provided by the `cogcomp-core-utilities` package.

## List of Corpus Readers
  - PennTreebank Reader
  - Propbank Reader
  - Treebank Chunk Reader
  - Ontonotes Reader
  - PennTreebank POS Reader
  - Nombank Reader
  - [ACE Reader](doc/ACEReader.md) 
  - Ontonotes 5.0 Named Entity Reader
  - Ontonotes 5.0 POS Reader

## Citation

If you use this code in your research, please provide the URL for this github repository in the relevant publications.
Thank you for citing us if you use us in your work! 

## Ontonotes 5.0 Readers

The set of available readers for the Onotonotes 5.0 corpus is still evolving, but there currently exists two utilities.
One converts the named entities found in the Ontonotes 5.0 corpus to CoNLL column format, the other converts the 
tree bank parse data to JSON format. See edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.OntonotesNamedEntityReader
for details on the named entity reader class (that can generate column format), or see
edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.OntonotesTreebankReader for details on generating JSON treebank 
data. 
