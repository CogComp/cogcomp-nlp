# Illinois Similarity

This module specifies a simple API for NLP components that compare
objects -- especially Strings -- and return a score indicating how
similar they are.  It is used in our WordNet-, Named Entity-, embedding-,
and paraphrase-based similarity code to simplify integration of
different similarity resources.

## Download Resource File

Once you first use the specific metrics, the system will automatically download corresponding resource file from Cogcomp server to `user.home` directory in your local machine.
Notice: Some resource file is very large and it may take a while to download. "paragram" is already included in the `src/main/resources/`.

## Configure File

The default configure file is `config/configurations.properties.` And see the default config in `SimConfigurator` in `edu.illinois.cs.cogcomp.config` package.

`wordMetric` is the  word comparison metric. It can be chosen from "word2vec", "paragram", "esa", "glove", "wordnet" "phrase2vec" or "customized" (your own embedding file). Notice: This metric will also be used as word comparator in LLM.

`usePhraseSim` option will automatically tokenized the sentence into phrase-based units when comparing sentences and notice it should be used with "phrase2vec".

`useNER` option will run NER on sentence and compare name-entity using NE comparison metrics in LLM.

`customized` gives your option to use your own embedding file. Just put the location of the file at this field and the dimension of the embedding at the filed `customized_embedding_dim`.

## Word similarity
To use word comparison metric:
```java
//initialization
ResourceManager rm_ = new SimConfigurator().getConfig(new ResourceManager(file));
WordSim ws = new WordSim(rm_, metric);
//ws.compare(word1,word2,metric);
double score=ws.compare("word", "sentence", metric);
```
And the metric can be chosen from "word2vec", "paragram", "esa", "glove", "wordnet" or "phrase2vec" (provided you have downloaded the relevant data resource -- see above).


## Name Entity Comparison
To use name entity comparison metric:

```java
NESim nesim=new NESim();
double score=nesim.compare("Donald Trump", "Trump");
```

## Lexical Level Matching
To use lexical level matching comparison:
```java
String config = "config/configurations.properties";
Metric llm =new LLMStringSim(config);
String s1="please turn off the light";
String s1="please turn on the monitor";
double score=nesim.compare(s1,s2);
```

To get the basic LLM similarity score, just set `usePhraseSim` and `useNER` as false in config file (default setting).

To use `usePhraseSim` option, set it as true and use `phrase2vec` as `wordMetric`. The system can tokenized the sentence into phrase-based units and it will reformat the sentence. E.g. "please turn the light on" => "please turn-on the light".

To use `useNER` option, set it as true. The system will run NER on the sentences first and comparing name entity and words separately. Notice: the NER initialization takes a lot of memory.
