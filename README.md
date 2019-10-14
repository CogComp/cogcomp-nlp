# CogCompNLP
[![Build Status](http://morgoth.cs.illinois.edu:5800/app/rest/builds/buildType:(id:CogcompNlp_Build)/statusIcon)](http://morgoth.cs.illinois.edu:5800/)
[![Build status](https://ci.appveyor.com/api/projects/status/f53iv8435rq875ex/branch/master?svg=true)](https://ci.appveyor.com/project/bhargavm/illinois-cogcomp-nlp/branch/master)

This project collects a number of core libraries for Natural Language Processing (NLP) developed 
by [Cognitive Computation Group](https://cogcomp.org).  

## How to use it? 
Depending on what you are after, follow one of the items: 
 - **If you want to annotate your raw text** (i.e. no need to open the annotator boxes to retrain them) you should look into the [pipeline](pipeline/). 
 - **If you want to train and test an NLP annotator** (i.e. you want to open an annotator box), see the list of components below and choose the desired one. We recommend using JDK8, as no other versions are officially supported and tested.
 - **If you want to read a corpus** you should look into [the corpus-readers](corpusreaders) module. 
 - **If you want to do feature-extraction** you should look into [edison](edison) module. 


## CogComp's main NLP libraries

Each library contains detailed readme and instructions on how to use it. In addition the javadoc of the whole project is available [here](http://cogcomp.org/software/doc/apidocs/). 

| Module | Description |
|----------|------------|
| [nlp-pipeline](pipeline/README.md) | Provides an end-to-end NLP processing application that runs a variety of NLP tools on input text. |
| [core-utilities](core-utilities/README.md) | Provides a set of NLP-friendly data structures and a number of  NLP-related utilities that support writing NLP applications, running experiments, etc. |
| [corpusreaders](corpusreaders/README.md) | Provides classes to read documents from corpora into `core-utilities` data structures. |
| [curator](curator/README.md) | Supports use of [CogComp NLP Curator](http://cogcomp.org/page/software_view/Curator), a tool to run NLP applications as services. |
| [edison](edison/README.md) | A library for feature extraction from `core-utilities` data structures.  | 
| [lemmatizer](lemmatizer/README.md)  |  An application that uses [WordNet](https://wordnet.princeton.edu/) and simple rules to find the root forms of words in plain text. |
| [tokenizer](tokenizer/README.md) | An application that identifies sentence and word boundaries in plain text. |
| [transliteration](transliteration/README.md) | An application that transliterates names between different scripts. | 
| [pos](pos/README.md)  | An application that identifies the part of speech (e.g. verb + tense, noun + number) of each word in plain text.  |  
| [ner](ner/README.md) | An application that identifies named entities in plain text according to two different sets of categories.  |
| [md](md/README.md) | An application that identifies entity mentions in plain text.  |
| [relation-extraction](relation-extraction/README.md) | An application that identifies entity mentions, then identify relation pairs among the mentions detected.  |
| [quantifier](quantifier/README.md) | This tool detects mentions of quantities in the text, as well as normalizes it to a standard form. |
| [inference](inference/README.md) |  A suite of unified wrappers to a set optimization libraries, as well as some basic approximate solvers. |
| [depparse](depparse/README.md) | An application that identifies the dependency parse tree of a sentence. |
| [verbsense](verbsense/README.md) | This system addresses the verb sense disambiguation (VSD) problem for English. |
| [prepsrl](prepsrl/README.md) | An application that identifies semantic relations expressed by prepositions and develops statistical learning models for predicting the relations. |
| [commasrl](commasrl/README.md) | This software extracts relations that commas participate in. |
| [similarity](similarity/README.md) | This software compare objects --especially Strings-- and return a score indicating how similar they are. |
| [temporal-normalizer](temporal-normalizer/README.md) | A temporal extractor and normalizer.  |
| [dataless-classifier](dataless-classifier/README.md) | Classifies text into a user-specified label hierarchy from just the textual label descriptions |
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
            <id>CogCompSoftware</id>
            <name>CogCompSoftware</name>
            <url>http://cogcomp.org/m2repo/</url>
        </repository>
    </repositories>
```

### Citing 
If you are using the framework, please cite our paper: 
```
@inproceedings{2018_lrec_cogcompnlp,
    author = {Daniel Khashabi, Mark Sammons, Ben Zhou, Tom Redman, Christos Christodoulopoulos, Vivek Srikumar, Nicholas Rizzolo, Lev Ratinov, Guanheng Luo, Quang Do, Chen-Tse Tsai, Subhro Roy, Stephen Mayhew, Zhili Feng, John Wieting, Xiaodong Yu, Yangqiu Song, Shashank Gupta, Shyam Upadhyay, Naveen Arivazhagan, Qiang Ning, Shaoshi Ling, Dan Roth},
    title = {CogCompNLP: Your Swiss Army Knife for NLP},
    booktitle = {11th Language Resources and Evaluation Conference},
    year = {2018},
    url = "http://cogcomp.org/papers/2018_lrec_cogcompnlp.pdf",
}
```
