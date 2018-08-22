# PARC 3.0 Reader

## Overview

Penn Attribution Relation Corpus 3.0 (PARC 3.0) [paper link](https://pdfs.semanticscholar.org/0f70/5a748ec78c8b2d6b6373ac3abc8d9679edd5.pdf)

Contact the owner of the corpus, [Silvia Pareti](http://homepages.inf.ed.ac.uk/s1052974/ "Silvia Pareti Homepage") for access to the corpus (You will need valid LDC licenses to PTB & PDTB). 

## Implementation details
Given an source directory, the reader will look for all files with ".xml" extension in all nested sub-directories. Each document is read into a [`TextAnnotation`](../../core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/TextAnnotation.java) instance with the following views defined in the [`ViewNames`](../../core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/ViewNames.java) class:

  - **`TOKENS`**: [`TokenLabelView`](../../core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/TokenLabelView.java) that keeps gold tokenization from corpus. 
  - **`SENTENCE`**: [`TokenLabelView`](../../core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/TokenLabelView.java) that keeps gold sentence split from corpus. 
  - **`ATTRIBUTION_RELATION`**: [`PredicateArgumentView`](../../core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/PredicateArgumentView.java). Each Attribution Relation corresponds to one predicate argument set. The "Cue" in each Attribution Relation serves as a "predicate", and "source"s and "span"s in that relation serves as arguments.
  - (optional)**`POS`**: [`TokenLabelView`](../../core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/TokenLabelView.java) that keeps POS tags from corpus
  - (optional)**`LEMMA`**: [`TokenLabelView`](../../core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/TokenLabelView.java) that keeps lemma of each token from corpus
## Usage

### Directory Structure
Standard WSJ directory structure.

```
\PARC3
	\train
   		\00
			wsj-0001.xml
 	      	...
       	\01
        	wsj-0101.xml
            ...
        ...
    \test
    	\23
        	...
    \dev
    	\24
        	...
```

### Usage in Java
```java
import edu.illinois.cs.cogcomp.nlp.corpusreaders.parcReader.PARC3Reader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.parcReader.PARC3ReaderConfigurator;

// Read all training data, with defualt settings (discard gold POS and LEMMA)
PARC3Reader reader = new PARC3Reader("data/PARC3/train"); 
```
or specify your own settings by creating a `*.properties` file. See [`PARC3ReaderConfigurator`](../src/main/java/edu/illinois/cs/cogcomp/nlp/corpusreaders/parcReader/PARC3ReaderConfigurator) for what fields you should specify.
```java
PARC3Reader reader = new PARC3Reader(new ResourceManager("my-parc3-config.properties"))
```

`PARC3Reader` implements `Iterable<TextAnnotation>` interface.



```java
while (reader.hasNext()) {
	TextAnnotation doc = reader.next();
	...
}
```
or

```java
for (TextAnnotation doc : reader) {
	...
}
```
