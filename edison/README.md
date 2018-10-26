# CogComp Edison

*Edison* is a feature extraction framework that uses the data structures of [core-utilities](../core-utilities/README.md)
to extract features used in NLP applications.
We can define functions for feature extraction that use the tokens and one or more views. 
This enables us to not only develop feature sets like words, n-grams, paths in parse trees, which work with a single view, 
but also more complex features that combine information from several views.

This library has been successfully used to facilitate the feature extraction for several higher level
NLP applications like semantic role labeling, coreference
resolution, textual entailment, paraphrasing and relation
extraction which use information across several views over text to
make a decision.

The library is using a Feature Extraction Language (FEX) as a declarative way of defining features. 
that can be applied to text to generate features. 

## Maven dependency
To use Edison as a Maven dependency please add the following lines to your `pom.xml` file:
```xml
<repositories>
    <repository>
        <id>CogcompSoftware</id>
        <name>CogcompSoftware</name>
        <url>http://cogcomp.org/m2repo/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>edu.illinois.cs.cogcomp</groupId>
        <artifactId>illinois-edison</artifactId>
        <version>#VERSION</version>
    </dependency>
</dependencies>
```

or if you are using SBT:
```
resolvers += "CogcompSoftware" at "http://cogcomp.org/m2repo/"

libraryDependencies += "edu.illinois.cs.cogcomp" % "illinois-edison" % "#VERSION"
```

where `#VERSION` is the version included in the `pom.xml` file. 

## The `FeatureExtractor` interface
Edison comes with several built-in feature extractors. 
All feature extractors implement the interface `FeatureExtractor`, which provides two functions:

   1. `getFeatures(Constituent)`: This function converts the input constituent into a set of Features.
   2. `getName()`: Returns the name of the feature extractor.

Feature extractors can be combined with each via operators to build newer ones. 
For example, a valid operators to conjoin feature extractors with each other to 
produce a new `FeatureExtractor`. Use the `FeatureUtilities.conjoin` to do this.

### Output features to a file
You can output the set of `Feature`s returned by any `FeatureExtractor` to an [SVMLite-format](http://svmlight.joachims.org) 
file in order to be used by any learning software that supports it using the method 
`WriteSVMLightFormat.writeFeatureExample`.

## List of pre-defined feature extractors
To see the set of pre-defined feature extractors along
with their description and the `FeatureExtractor` that implements
them. 

### Bias feature
The keyword `bias` in a `.fex` specification includes a feature
that will always be present. This is useful to add a bias feature
for binary classification.

## FEX Functionality

In addition to the programmatic generation using feature extractors, we allow specification of
feature extractors through a special file that defines feature extractors. This file is the .fex file.

### What happens
`FeatureManifest` loads the file and sends it to `ManifestParser`. Contains logic for converting
    definitions, variables, names into `FeatureExtractor`s.
`ManifestParser` actually parses the file, and stores definitions, variables, names, etc.


### Structure of the .fex file

The file has a lisp-like syntax. As in lisp, a semicolon (;) is the comment symbol. This may come anywhere
in the line.
```
file := (define name definition* featureslist)
definition := NULL | (define name body) | (defvar name value)
body := something parsable by FeatureManifest.createFex
featureslist := leaf | (list leaf*) | (conjoin leaf leaf)
keywordbody := (conjoin ??) | (list leaf*) | (conjoin-and-include) | (bigram) | (trigram) | (transform-input) | (if)
name := <string>
leaf := must be in KnownFexes.fexes
```

## Citation

Mark Sammons, Christos Christodoulopoulos, Parisa Kordjamshidi, Daniel Khashabi, Vivek Srikumar, Paul Vijayakumar, Mazin Bokhari, Xinbo Wu, Dan Roth, EDISON: Feature Extraction for NLP, Simplified. Proceedings of the Tenth International Conference on Language Resources and Evaluation (LREC 2016) (2016) pp.

Thank you for citing us if you use us in your work! http://cogcomp.org/page/software_view/Edison

```
@inproceedings{SCKKSVBWR16,
    author = {Mark Sammons, Christos Christodoulopoulos, Parisa Kordjamshidi, Daniel Khashabi, Vivek Srikumar, Paul Vijayakumar, Mazin Bokhari, Xinbo Wu, Dan Roth},
    title = {EDISON: Feature Extraction for NLP, Simplified},
    booktitle = {Proceedings of the Tenth International Conference on Language Resources and Evaluation (LREC 2016)},
    year = {2016},
    publisher = {European Language Resources Association (ELRA)},
    editor = {Nicoletta Calzolari (Conference Chair) and Khalid Choukri and Thierry Declerck and Marko Grobelnik and Bente Maegaard and Joseph Mariani and Asuncion Moreno and Jan Odijk and Stelios Piperidis},
    url = "http://cogcomp.org/papers/SCKKSVBWR16.pdf",
}
```
