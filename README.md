# CogcompNLP

[![Build Status](https://semaphoreci.com/api/v1/cogcomp/cogcomp-nlp-2/branches/master/badge.svg)](https://semaphoreci.com/cogcomp/cogcomp-nlp-2)
[![Build Status](http://morgoth.cs.illinois.edu:8080/buildStatus/icon?job=cogcomp-nlp)](http://morgoth.cs.illinois.edu:8080/job/cogcomp-nlp/)
[![Build status](https://ci.appveyor.com/api/projects/status/f53iv8435rq875ex/branch/master?svg=true)](https://ci.appveyor.com/project/bhargavm/illinois-cogcomp-nlp/branch/master)


This project collects a number of core libraries for Natural Language Processing (NLP) developed 
by [Cognitive Computation Group](https://cogcomp.cs.illinois.edu).  

## CogComp's main NLP libraries

Each library contains detailed readme and instructions on how to use it. In addition the javadoc of the whole project is available [here](http://cogcomp.cs.illinois.edu/software/doc/apidocs/). 

| Module | Description |
|----------|------------|
| [nlp-pipeline](pipeline/README.md) | Provides an end-to-end NLP processing application that runs a variety of NLP tools on input text. |
| [core-utilities](core-utilities/README.md) | Provides a set of NLP-friendly data structures and a number of  NLP-related utilities that support writing NLP applications, running experiments, etc. |
| [corpusreaders](corpusreaders/README.md) | Provides classes to read documents from corpora into `core-utilities` data structures. |
| [curator](curator/README.md) | Supports use of [Cogcomp NLP Curator](http://cogcomp.cs.illinois.edu/page/software_view/Curator), a tool to run NLP applications as services. |
| [edison](edison/README.md) | A library for feature extraction from `core-utilities` data structures.  | 
| [lemmatizer](lemmatizer/README.md)  |  An application that uses [WordNet](https://wordnet.princeton.edu/) and simple rules to find the root forms of words in plain text. | 
| [tokenizer](tokenizer/README.md) | An application that identifies sentence and word boundaries in plain text. | 
| [pos](pos/README.md)  | An application that identifies the part of speech (e.g. verb + tense, noun + number) of each word in plain text.  |  
| [ner](ner/README.md) | An application that identifies named entities in plain text according to two different sets of categories.  |
| [quantifier](quantifier/README.md) | This tool detects mentions of quantities in the text, as well as normalizes it to a standard form. |
| [inference](inference/README.md) |  A suite of unified wrappers to a set optimization libraries, as well as some basic approximate solvers. |
| [depparse](depparse/README.md) | An application that identifies the dependency parse tree of a sentence. |
| [prepsrl](prepsrl/README.md) | An application that identifies semantic relations expressed by prepositions and develops statistical learning models for predicting the relations. |
| [external-annotators](external/README.md) | A collection useful external annotators.  |

 - **Questions?** Have a look at our [FAQs](faq.md). 

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
