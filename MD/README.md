# Mention Detection

A mention detection module that annoatates all mentions of the given TextAnnoatation

## Introduction

### Mention Detection

A mention detection module aims to annotate all the mentions of different entities in the given texts. In this package, we took the approach of detection all mention heads first, then predict mention extents for all the predicted mention heads.

### Head Detection

This package tagged all the tokens in a text into either "B/I/O" or "B/I/O/L/U" schema, then train/test on each single token. After evaluating all tokens, we interpret this representation back to mentions.

### Extent Detection

This package does extent classification after detecting mention heads. In the training process, each token in the extent to both right and left of a given mention head is formed into pairs to train a binary classifier, saying whether the token is in the extent or not. To add negative examples, one token to the left and right of the mention extent is added. In the evaluating process, given a mention head, the package forms pairs with tokens to the left and right, until finding a token that is predicted to be not in the extent.

## Results

Head Boundary

| Train\Eval (F1) | ACE  | ERE  |
|-----------------|------|------|
| ACE             | 89.6 | 83.8 |
| ERE             | 81.7 | 86.7 |
| ACE+ERE         | 88.4 | 86.5 |

Extent boundary accuracy given Head

ACE: 89.45

## Using Annotator

```java
TextAnnoatation to_be_annoatated;
MentionAnnotator mentionAnnotator = new MentionAnnotator();
mentionAnnotator.addView(to_be_annotated);
List predictedMentions = to_be_annotated.getView("MENTION").getConstituents();
```

## Run Tests

### Run Mention Head Tests

`mvn exec:java -Dexec.mainClass="org.cogcomp.md.BIOTester [METHOD]"`

Supported Methods:
 - "test_cv" Run a five fold cross validation on ACE
 - "test_ere" Run a test on ERE with the model trained on ACE
 - "test_ts" Run a test on ACE test set with the model trained on ACE
 - "calculateAvgMentionLength" Calculates the average mention head length, showed by type
 - "TrainACEModel" Train, generate and save a new model trained on ACE corpus
 - "TrainEREModel" Train, generate and save a new model trained on ERE corpus
 
### Run Mention Extent Tests

`mvn exec:java -Dexec.mainClass="org.cogcomp.md.ExtentTester [METHOD]"`

Supported Methods:
 - "testExtentOnGoldHead" Run a test of predicting extents with gold heads on ACE
 - "testExtentOnPredictedHead" Run a test of predicting extents with predicted heads on ACE

## Citation

```
@inproceedings{PengChRo15,
    author = {Haoruo Peng, Kai-Wei Chang Dan Roth},
    title = {A Joint Framework for Coreference Resolution and Mention Head Detection},
    booktitle = {CoNLL},
    pages = {10},
    month = {7},
    year = {2015},
    address = {University of Illinois, Urbana-Champaign, Urbana, IL, 61801},
    publisher = {ACL},
    url = "http://cogcomp.org/papers/MentionDetection.pdf",
}

@inproceedings{RatinovRo09,
    author = {L. Ratinov and D. Roth},
    title = {Design Challenges and Misconceptions in Named Entity Recognition},
    booktitle = {CoNLL},
    month = {6},
    year = {2009},
    url = "http://cogcomp.org/papers/RatinovRo09.pdf",
    funding = {MIAS, SoD, Library},
    projects = {IE},
    comment = {Named entity recognition; information extraction; knowledge resources; word class models; gazetteers; non-local features; global features; inference methods; BIO vs. BIOLU; text chunk representation},
}
```
