# illinois-cogcomp-nlp

This project collects a number of core libraries for Natural Language Processing (NLP) developed 
by the University of Illinois' [Cognitive Computation Group](https://cogcomp.cs.illinois.edu).  

To include one of the modules in your Maven project, add the following snippet with the
`#modulename#` and `#version` entries replaced with the relevant module name and the 
version listed in this project's pom.xml file:
 
 ```
     <dependencies>
          ...
         <dependency>
             <groupId>edu.illinois.cs.cogcomp</groupId>
             <artifactId>#modulename#</artifactId>
             <version>#version#</version>
         </dependency>
```


Main project: 
[![Build Status](https://semaphoreci.com/api/v1/projects/5303a5fc-469c-42a8-84c9-fbef6382179a/579314/badge.svg)](https://semaphoreci.com/danyaljj/illinois-cogcomp-nlp)

Dev project: 
[![Build Status](https://semaphoreci.com/api/v1/projects/80f49761-69dc-4a02-8ea9-a6895338a115/580384/badge.svg)](https://semaphoreci.com/cogcomp-dev/illinois-cogcomp-nlp)

CogComp's main NLP libraries

- [illinois-core-utilities](core-utilities/README.md)
Provides a set of NLP-friendly data structures and a number of 
NLP-related utilities that support writing NLP applications, running experiments, etc.

- [illinois-corpusreaders](corpusreaders/README.md)
Provides classes to read documents from corpora into `illinois-core-utilities` data structures.

- [illinois-curator](curator/README.md)
Supports use of [Illinois' NLP Curator](http://cogcomp.cs.illinois.edu/page/software_view/Curator), 
 a tool to run NLP applications as services.

- [illinois-edison](edison/README.md)
A library for feature extraction from `illinois-core-utilities` data structures.

- [illinois-lemmatizer](lemmatizer/README.md)
An application that uses [WordNet](https://wordnet.princeton.edu/) and simple rules to find the
root forms of words in plain text.

- [illinois-tokenizer](tokenizer/README.md)
An application that identifies sentence and word boundaries in plain text.

- [illinois-pos](pos/README.md)
An application that identifies the part of speech (e.g. verb + tense, noun + number) of each word
in plain text.

- [illinois-ner](ner/README.md)
An application that identifies named entities in plain text according to two different sets of categories. 