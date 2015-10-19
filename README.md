There are 2 models:
1) Local Classifier : A classifier that classifies individual commas based on the sentence-parse, shallow parse, POS tags, and NER tags
2) Sequential Model : Uses structured prediction to label a sequence of commas that are siblings in the parse tree of the sentence

There are 2 sources for annotation data:
1) Comma Resolution Data(data/Original-data-by-Srikumar-et-al): A set of around a 1000 sentences from sectin 00 of the PTB in which all the commas have been labeled with their roles. The comma that were labeled as 'Other' have been refined and the annotations are in data/otherFile.txt
2) Comma-Syntax-Pattern annotations:  A mapping from a list of the most frequent syntax patterns, extracted from the context of a comma in the parse of the sentence, to comma labels

Run ClassifierComparison to get the performance of different models as evaluated over 5-fold cval.

Use CommaLabeler to obtain a comma View for a sentence represented as a TextAnnotation(must have the views required to extract features for the classifier).
