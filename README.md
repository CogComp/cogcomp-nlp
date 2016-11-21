# illinois-cogcomp-nlp

[![Build Status](https://semaphoreci.com/api/v1/cogcomp/illinois-cogcomp-nlp/branches/master/badge.svg)](https://semaphoreci.com/cogcomp/illinois-cogcomp-nlp)
[![Build Status](http://morgoth.cs.illinois.edu:8080/buildStatus/icon?job=cogcomp-nlp)](http://morgoth.cs.illinois.edu:8080/job/cogcomp-nlp/)
[![Build status](https://ci.appveyor.com/api/projects/status/f53iv8435rq875ex/branch/master?svg=true)](https://ci.appveyor.com/project/bhargavm/illinois-cogcomp-nlp/branch/master)


This project collects a number of core libraries for Natural Language Processing (NLP) developed 
by the University of Illinois' [Cognitive Computation Group](https://cogcomp.cs.illinois.edu).  

## CogComp's main NLP libraries

Each library contains detailed readme and instructions on how to use it. In addition the javadoc of the whole project is available [here](http://cogcomp.cs.illinois.edu/software/doc/apidocs/). 

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


## Using each library programmatically 

To include one of the modules in your Maven project, add the following snippet with the
   `#modulename#` and `#version` entries replaced with the relevant module name and the 
   version listed in this project's pom.xml file. Note that you also add to need the
   `<repository>` element for the CogComp maven repository in the `<repositories>` element.
    
```xml 
    <dependencies>
         ...
        <dependency>
            <groupId>edu.illinois.cs.cogcomp</groupId>
            <artifactId>#modulename#</artifactId>
            <version>#version#</version>
        </dependency>
        ...
    </dependencies>
    ...
    <repositories>
        <repository>
            <id>CogcompSoftware</id>
            <name>CogcompSoftware</name>
            <url>http://cogcomp.cs.illinois.edu/m2repo/</url>
        </repository>
    </repositories>
```
