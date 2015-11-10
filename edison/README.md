# illinois-edison

`Edison` is a feature extraction framework that uses the data structures of [illinois-core-utilities](..`core-utilities`README.md)
to extract features used in NLP applications.

This library has been successfully used to facilitate the feature extraction for several higher level
NLP applications like semantic role labeling, coreference
resolution, textual entailment, paraphrasing and relation
extraction which use information across several views over text to
make a decision.



## Concepts
   - Feature extractors
   - Feature input transformers
   - Operations

## Feature manifests and `.fex` definitions

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
  

| *fex name*           | *Feature Extractor*                 | *Description*                                                           |
|:--------------------:|:-----------------------------------:|-------------------------------------------------------------------------|
| `capitalization`     | `capitalization`                    | Adds the following two features: One with the word in its actual case, and the second, an indicator for whether the word is captitalized   |
| `conflated-pos`      | `conflatedPOS`                      | The coarse POS tag (one of Noun, Verb, Adjective,                       |
|                      |                                     | Adverb, Punctuation, Pronoun and Other)                                 |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `de-adj-nouns`       | `deAdjectivalAbstractNounsSuffixes` | An indicator for whether the word ends with a de-                       |
|                      |                                     | adjectival suffix. The list of such suffixes is in                      |
|                      |                                     | `WordLists.DE_ADJ_SUFFIXES`.                                            |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `de-nom-nouns`       | `deNominalNounProducingSuffixes`    | An indicator for whether the word ends with a de-                       |
|                      |                                     | nominal noun producing suffix. The list of such suffixes                |
|                      |                                     | is in `WordLists.DENOM_SUFFIXES`.                                       |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `de-verbal-suffixes` | `deVerbalSuffix`                    | An indicator for whether the word ends with a de-                       |
|                      |                                     | verbal producing suffix. The list of such suffixes                      |
|                      |                                     | is in `WordLists.DE_VERB_SUFFIXES`.                                     |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `gerunds`            | `gerundMarker`                      | An indicator for whether the word ends with an `-ing`.                  |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `known-prefixes`     | `knownPrefixes`                     | An indicator for whether the word starts with one of                    |
|                      |                                     | the following: `poly`, `ultra`, `post`, `multi`, `pre`, `fore`, `ante`, |
|                      |                                     | `pro`, `meta` or `out`                                                  |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `lemma`              | `lemma`                             | The lemma of the word, taken from the LEMMA view                        |
|                      |                                     | (that is, `ViewNames.LEMMA`)                                            |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `nom`                | `nominalizationMarker`              | An indicator for whether the word is a nominalization                   |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `numbers`            | `numberNormalizer`                  | An indicator for whether the word is a number                           |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `pos`                | `pos`                               | The part of speech tag of the word (taken                               |
|                      |                                     | from `ViewNames.POS`)                                                   |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `prefix-suffix`      | `prefixSuffixes`                    | The first and last two, three characters in the lower                   |
|                      |                                     | cased word                                                              |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `word`               | `word`                              | The word, lower cased                                                   |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `wordCase`           | `wordCase`                          | The word, without changing the case                                     |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|
| `date`               | `dateMarker`                        | An indicator for whether the token is a valid date                      |
|----------------------|-------------------------------------|-------------------------------------------------------------------------|


