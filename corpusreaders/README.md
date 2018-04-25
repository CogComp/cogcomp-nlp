# CogComp Corpusreaders

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
  - TAC KBP reader
  - MASC Lemma, POS, Shallow Parse, NER Reader

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

## TAC KBP Reader

Reader for TAC 2016 KBP corpus. This data was originally created for the TAC KBP EDL track and can be obtained with a
license here: http://nlp.cs.rpi.edu/kbp/2017/index.html

This reader assumes that the data is structured like PATH-TO-DATA/LANG/TYPE where LANG is one of {cmn,eng,spa} and TYPE
is either nw or df, for newswire and discussion forum data, respectively.

## MASC Reader

mascReader.MascXCESReader is a reader for the entire MASC dataset from Open American National Corpus.

It processes Lemma, POS, Sentence, Shallow Parse, NER CoNLL (LOC, ORG, PER),
and NER Ontonotes (DATE, LOCATION, ORGANIZATION, PERSON) annotations.

The reader takes XCES XML format as input.
Please check MascXCESReader.java for details on how to generate the input files.

## Corpus Splitter

corpusutils.CreateTrainDevTestSplit is a utility that divides a corpus in to training, development, and test
subsets. In addition to respecting fractions specified by the user, it *stratifies* the subsets to balance
characteristics specified by the user, preferring to balance the least frequent.  This was developed to support
use cases where the corpus should be divided in a way that prevents certain kinds of overlap between training
and test data, to avoid "memorization" by a learner applied to the task (so, for example, making sure all
examples from a given document are assigned to the same subset). For cases where the annotation
can be read into TextAnnotation objects, use a corpusutils.TextAnnotationLabelCounter; otherwise, use a
corpusUtils.ListExampleLabelCounter and generate a flat boolean-featured representation using the characteristics
you care about balancing across dev/train/test, and where each ListExample represents the non-divisible unit of
assignment (e.g. a document, or a complete game, etc.).
