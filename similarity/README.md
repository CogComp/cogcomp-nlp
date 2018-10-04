# CogComp Similarity

This module specifies a simple API for NLP components that compare
objects -- especially Strings -- and return a score indicating how
similar they are.  It is used in our WordNet-, Named Entity-, embedding-,
and paraphrase-based similarity code to simplify integration of
different similarity resources.

## Downloading the Resources

When you first use the specific metrics, the system will automatically download corresponding resource files from CogComp server to `user.home` directory in your local machine.
Notice: Some resource file is very large and it may take a while to download. "paragram" is already included in the `src/main/resources/`.

## Configure File

See the default config in `SimConfigurator` in `edu.illinois.cs.cogcomp.config` package.


## Word similarity
When you want to compare the similarity between two words, you can use word comparison metric below:

```java
//initialization
ResourceManager rm_ = new SimConfigurator().getConfig(new ResourceManager(file));
WordSim ws = new WordSim(rm_, metric);
//ws.compare(word1,word2,metric);
double score=ws.compare("word", "sentence", metric);
```

And the metric can be chosen from "word2vec", "paragram", "esa", "glove", "wordnet", "phrase2vec" or "customized".

In config file, `customized` gives your option to use your own embedding file. Just put the location of the file at this field and the dimension of the embedding at the filed `customized_embedding_dim`.


## Name Entities Comparison
When you want to compare Name Entities, you can use name entity comparison metric below:

```java
NESim nesim=new NESim();
double score=nesim.compare("Donald Trump", "Trump");
```

You can also provide the types of one mention or both of the mentions. When specifying the type of only one mention, use null for the type of the other mention. For example,

```java
NESim nesim = new NESim();
double score=nesim.compare("Donald Trump", "Trump", "PER", "PER");
double score2=nesim.compare("Obama", "chair", "PER", null);
```

## Lexical Level Matching
When you want to compute similarity score between two sentences, you can use lexical level matching comparison:

```java
String config = "config/configurations.properties";
Metric llm =new LLMStringSim(config);
String s1="please turn off the light";
String s1="please turn on the monitor";
double score=nesim.compare(s1,s2);
```

To use this metric properly, you need to specify some configurations in the Config file.

`wordMetric` is the  word comparator metric used in LLM. It can be chosen from "word2vec", "paragram", "esa", "glove", "wordnet" "phrase2vec" or "customized" (your own embedding file).

`usePhraseSim` option will automatically split the sentence into phrase-based units when comparing sentences. To use this option, set it as true and use `phrase2vec` as `wordMetric`. The system splits the text into phrases, then matches those phrases using a phrase similarity metric that can match different formulations of many phrases, E.g. "please turn the light on" => "please turn-on the light". Notice: When we split sentences into phrases, this phrases identification process depends on the generalized phrases dictionary we extracted from Wordnet (see `src/main/resources/phrases.txt`).

`useNER` option will run NER on sentence and compare name-entity using NE comparison metrics in LLM. To use this option, set it as true. The system will run NER on the sentences first and comparing name entity and words separately. Notice: the NER initialization takes a lot of memory. See [NER detail here](http://cogcomp.org/page/software_view/NETagger).

To get the basic LLM similarity score, just set `usePhraseSim` and `useNER` as false in config file (which is also the default setting).


## Citation

Do, Quang, et al. "Robust, light-weight approaches to compute lexical similarity." Computer Science Research and Technical Reports, University of Illinois (2009): 94.


```
@article{do2009robust,
  title={Robust, light-weight approaches to compute lexical similarity},
  author={Do, Quang and Roth, Dan and Sammons, Mark and Tu, Yuancheng and Vydiswaran, V}
}
```
