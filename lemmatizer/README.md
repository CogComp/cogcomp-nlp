# CogComp Lemmatizer

## CONTENTS

1. PURPOSE  
   1. The CogComp Lemmatizer 
   2. License 
2. System requirements 
3. Programmatic Use of the Lemmatizer
   1.  Lemmatizing Single Words
   2.  Lemmatizing Views
4. Download contents 
5. Compiling and Running the CogComp Lemmatizer
6. Contact Information 
7. Developer Notes


## 1. PURPOSE

Lemmatizers/Stemmers are intended to simplify text at the token level. The
underlying principle is that it can be helpful to treat uniformly all
versions of words with the same basic form and meaning -- for example, to
strip away prefixes and suffixes due to e.g. verb tense or plural forms.

Stemmers tend to be simpler, looking only at removing and/or replacing a 
limited number of characters at the end (and possibly the beginning) of
words in order to reduce unhelpful variability. For example, the two
sentences

a). "Many lions run fast" 

and 

b). "A lion running fast" 

have only one token in common ('fast'). A stemmer might be expected to 
strip the 's' from 'lions' and the 'ning' from 'running' to allow an
exact match to determine that the two sentences in fact have three words
in common -- 'lion', 'run', and 'fast'. However, because they use
heuristics, stemmers tend to generate ill-formed roots for a significant
number of cases. 

Lemmatizers are intended to be slightly more general, in that they 
typically have a dictionary of root forms of irregular verbs, and which
can therefore replace 'tries' with 'try', or 'ran' with 'run'. 
While slower than stemmers, lemmatizers tend to produce more consistent,
higher-quality output.  


### 1.1 The CogComp Lemmatizer

The CogComp Lemmatizer uses the JWNL library to access WordNet's
dictionaries to identify word root forms, and also uses some additional
resources to handle prefixes and some verb forms.


### 1.2 License

The CogComp Lemmatizer is available under a Research and Academic 
use license. For more details, visit the Curator website and click 
the download link.

## 2. SYSTEM REQUIREMENTS

This software was developed on the following platform:

Scientific Linux (2.6.32-279.5.2.el6.x86_64)
Java 1.7

This package depends on the Cognitive Computation Group (CCG)'s Curator 
libraries, which define Thrift-based data structures for NLP, and a 
number of other libraries and packages. These are included as part of
the tarball containing the Lemmatizer itself.


## 3. PROGRAMMATIC USE OF THE LEMMATIZER

Create an `IllinoisLemmatizer` object. There are three constructors.
One has a boolean parameter that when true, will give outputs
that match that of the Stanford lemmatizer for common words like
"her" to "she" and "me" to "i". This can be useful when using
this lemmatizer with Stanford CoreNLP. The other parameter is a
path to wordnet. There is also a default constructor that uses
Stanford output and points to WordNet 3.0 which is included. Lastly,
There is a constructor that takes a ResourceManager object based
on a config file. See the included config file in the config folder
for an example.

You can use it in two main ways:

a) Lemmatize a single word
-- return one or more candidate root forms of a word.

b) Lemmatize a Record data structure's tokens, creating a new view.
-- this use allows easy integration with the CogComp NLP Curator, which specifies
a set of data structures for integrating the output of multiple NLP tools. 


### 3.1 Lemmatizing Single Words

You will need to initialize the `IllinoisLemmatizer`:

```
IllinoisLemmatizer lem = new IllinoisLemmatizer();
```
The lemmatizer requires a single word and a Part of Speech (POS) tag. For information
about Part of Speech tagging, [see this link](http://cogcomp.org/page/demo_view/POS).

```java 
String lemma = lem.getSingleLemma( "leaders", "NNS" );
System.out.println( lemma );
// should return 'leader'
```

Note that there is also a version of this method that take as their arguments
a TextAnnotation object and a token index (see the Edison library -- 
http://cogcomp.org/page/software_view/Edison). 


### 3.2 Lemmatizing Views

There are also methods for creating Lemma views in Records (Curator data structure --
visit [here](http://cogcomp.org/page/software_view/Curator) and in `TextAnnotations` 
(Edison data structure -- visit [here](http://cogcomp.org/page/software_view/Edison).


## 4. DOWNLOAD CONTENTS

The CogComp Lemmatizer is released as either a Curator component or as 
part of the CogComp Preprocessor bundle. The Curator downloads a tarball 
as part of its installation process (from which it is to be assumed you 
have obtained this README). 

The main directory has four sub-directories:
```
doc/ -- documentation (this README)
src/ -- source files for the Lemmatizer as well as resources like WordNet-3.0
    and verb-lemDict.txt
config -- example config file
test/ -- input.txt and output.txt, used for making sure code is working as desired.
```

## 5. COMPILING AND RUNNING THE COGCOMP LEMMATIZER

We assume that people will run this as part of the Curator, or part of the
CogComp Preprocessor.  If you are a developer, you can use Maven to get
and compile this project.  


### 6. CONTACT INFORMATION

If you have any questions/issues use our issue tracker. 

## Citation

If you use this code in your research, please provide the URL for this github repository in the relevant publications.
Thank you for citing us if you use us in your work! 
More info at http://cogcomp.org/page/software_view/illinois-lemmatizer
