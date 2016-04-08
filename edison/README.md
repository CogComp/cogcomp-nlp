# illinois-edison

*Edison* is a feature extraction framework that uses the data structures of [illinois-core-utilities](..`core-utilities`README.md)
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

## Concepts
   - Feature extractors
   - Feature input transformers
   - Operations

## The `FeatureExtractor` interface
Edison comes with several built-in feature extractors. 
All feature extractors implement the interface `FeatureExtractor`, which provides two functions:

   1. `getFeatures(Constituent)`: This function converts the input constituent into a set of Features.
   2. `getName()`: Returns the name of the feature extractor.

Feature extractors can be combined with each via operators to build newer ones. 
For example, a valid operators to conjoin feature extractors with each other to 
produce a new `FeatureExtractor`. Use the `FeatureUtilities.conjoin` to do this.

## Defining Feature Extractors 
You can build feature extractors in two ways:

   1. You can manually build a feature extractor by implementing the FeatureExtractor interface.
   2. You can use a pre-defined

## List of pre-defined features

This section lists the set of pre-defined feature extractors along
with their description and the `FeatureExtractor` that implements
them.

### Bias feature
The keyword `bias` in a `.fex` specification includes a feature
that will always be present. This is useful to add a bias feature
for binary classification.

### Word features
The following list of feature extractors are operate on the last
word of the input constituent. They are all defined as static
members of the class `WordFeatureExtractorFactory`.
  

| *Feature Extractor*                 | *Description*                                                           |
|:-----------------------------------:|-------------------------------------------------------------------------|
| `capitalization`                    | Adds the following two features: One with the word in its actual case, and the second, an indicator for whether the word is captitalized   |
| `conflatedPOS`                      | The coarse POS tag (one of Noun, Verb, Adjective, Adverb, Punctuation, Pronoun and Other) |
| `deAdjectivalAbstractNounsSuffixes` | An indicator for whether the word ends with a de- adjectival suffix. The list of such suffixes is in `WordLists.DE_ADJ_SUFFIXES`. |
| `deNominalNounProducingSuffixes`    | An indicator for whether the word ends with a de- nominal noun producing suffix. The list of such suffixes is in `WordLists.DENOM_SUFFIXES`.                       |
| `deVerbalSuffix`                    | An indicator for whether the word ends with a de- verbal producing suffix. The list of such suffixes is in `WordLists.DE_VERB_SUFFIXES`.  |
| `gerundMarker`                      | An indicator for whether the word ends with an `-ing`.                  |
| `knownPrefixes`                     | An indicator for whether the word starts with one of the following: `poly`, `ultra`, `post`, `multi`, `pre`, `fore`, `ante`, `pro`, `meta` or `out`                    |
| `lemma`                             | The lemma of the word, taken from the LEMMA view (that is, `ViewNames.LEMMA`)  |
| `nominalizationMarker`              | An indicator for whether the word is a nominalization                   |
| `numberNormalizer`                  | An indicator for whether the word is a number                           |
| `pos`                               | The part of speech tag of the word (taken  from `ViewNames.POS`)  |
| `prefixSuffixes`                    | The first and last two, three characters in the lower cased word |
| `word`                              | The word, lower cased                                                   |
| `wordCase`                          | The word, without changing the case                                     |
| `dateMarker`                        | An indicator for whether the token is a valid date                      |

