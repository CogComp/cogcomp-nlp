# Illinois Similarity

This module specifies a simple API for NLP components that compare
objects -- especially Strings -- and return a score indicating how
similar they are.  It is used in our WordNet-, Named Entity-, embedding-,
and paraphrase-based similarity code to simplify integration of
different similarity resources.

## Download Resource File

To download resource file including: Word2vec, Glove, ESA, simply run the main function in Datastore class in `edu.illinois.cs.cogcomp.sim package`. It will download those file from Cogcomp server to `src/main/resources/1.5` in your local machine. You can also comment out the resource that you don't want or use your own file. Notice: "paragram" is already included in the `src/main/resources/`.

## Configure File

The default configure file is `config/configurations.properties.`

`wordMetric` is the  word comparison metric. It can be chosen from "word2vec", "paragram", "esa", "glove", "wordnet" or "phrase2vec". Notice: This metric will also be used as word comparator inLLM.

`usePhraseSim` option will automatically tokenized the sentence into phrase-based units when comparing sentences and notice it should be used with "phrase2vec".

`useNER` option will run NER on sentence and compare name-entity using NE comparison metrics in LLM.

`word2vec` is the word2vec file location in your computer. Similarly,  other resource use the same way to specify the location.


## Getting Started
To use word comparison metric ("word2vec" for example )
```java
ResourceManager rm_ = new SimConfigurator().getConfig(new ResourceManager(file));
WordSim ws = new WordSim(rm_,metric);
double score=ws.compare("word","sentence",metric);
```
And the metric can be chosen from "word2vec", "paragram", "esa", "glove", "wordnet" or "phrase2vec".

To use name entity comparison metric
```java
NESim nesim=new NESim();
double score=nesim.compare("Donald Trump", "Trump");
```

To use lexical level matching comparison:
```java
String config = "config/configurations.properties";
Metric llm =new LLMStringSim(config);
String s1="please turn off the light";
String s1="please turn on the monitor";
double score=nesim.compare(s1,s2);
```
