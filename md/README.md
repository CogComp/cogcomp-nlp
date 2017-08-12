# Mention Detection

See the [parent package reademe](https://github.com/CogComp/cogcomp-nlp/blob/master/README.md) first

## Introduction

A mention is a reference or representation of an entity or an object that appeared in texts. Mentions can have different mention types and the entity a mention is referencing can have different entity types. For example, in the sentence "He is Obama, the president.", "He", "Obama" and "the president" are referencing to the same object (Barack Obama himself), so they are all mentions to the entity Barack Obama. 

This is a mention detection module that aims to annotate all mentions in given texts. It has an annotator that accepts a `TextAnnotation` and give it a new view which contains all the mentions as `Constituent`.

This package tags all the tokens in a text into either "B/I/O" or "B/I/O/L/U" schema, then train/test on each single token. After predicting all tokens, we interpret this representation back to mentions.

## Extent Detection

The mentions predicted from the previous step is only mention heads. After detecting all mention-heads first, this package also has extent classification for each heads. In the extent classifier training process, each token in the extent to both right and left of a given mention head is formed into pairs to train a binary classifier, indicating whether the token is in the extent or not. To add negative examples, one token to the left and right of the mention extent is added. In the evaluating process, given a mention head, the package forms pairs with tokens to the left and right, until finding a token that is predicted to be not in the extent.

## Results

Head Boundary

| Train\Eval (F1) | ACE  | ERE  |
|-----------------|------|------|
| ACE             | 89.6 | 83.8 |
| ERE             | 81.7 | 86.7 |
| ACE+ERE         | 88.4 | 86.5 |

Extent boundary accuracy given Head

ACE: 89.45

ERE: 88.74

Combined: 86.65

## Usage

### Install with Maven

If you want to use the illinois-md package independently, you can add a maven dependency in your pom.xml. Please replace the `VERSION` with the latest version of the parent package.

```xml
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>illinois-md</artifactId>
    <version>VERSION</version>
</dependency>
```

### Using Annotator

The recommended way to use this package is through the `MentionAnnotator`. `MentionAnnotator` can be initialized with or without an mode parameter.

The mode parameter has four options:

- `MentionAnnotator("ACE_NONTYPE")` loads the model trained on ACE without type.
- `MentionAnnotator("ACE_TYPE")` loads the model trained on ACE with type.
- `MentionAnnotator("ERE_NONTYPE")` loads the model trained on ERE without type.
- `MentionAnnotator("ERE_TYPE")` loads the model trained on ERE with type.

The initialization of `MentionAnnotator()` without parameter set mode to `"ACE_NONTYPE"`


```java
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import org.cogcomp.md.MentionAnnotator;

import java.io.IOException;
import java.util.List;

public class app
{
    public static void main( String[] args ) throws IOException, AnnotatorException
    {
        String text1 = "Good afternoon, gentlemen. I am a HAL-9000 "
                + "computer. I was born in Urbana, Il. in 1992";

        String corpus = "2001_ODYSSEY";
        String textId = "001";

        // Create a TextAnnotation using the LBJ sentence splitter
        // and tokenizers.
        TextAnnotationBuilder tab;
        // don't split on hyphens, as NER models are trained this way
        boolean splitOnHyphens = false;
        tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));

        TextAnnotation ta = tab.createTextAnnotation(corpus, textId, text1);
        
        MentionAnnotator mentionAnnotator = new MentionAnnotator();
        mentionAnnotator.addView(ta);

        View mentionView = ta.getView(ViewNames.MENTION);
        List<Constituent> predictedMentions = mentionView.getConstituents();
    }
}
```

### Using package to train/test

This package also supports custom training. Through this, the user can produce models that is not pre-loaded in the annotator.

To use this, put `data/` `models/` `tmp/` at the root of the cogcomp-nlp package, then modify/run tests from `BIOTester`, `ExtentTester` or `AnnotatorTester`

Please note that the user need to check the inner implementation of the tests to ensure data paths and output paths are correct.

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
If you use this tool, please cite the following works.
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
