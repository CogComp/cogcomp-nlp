# illinois-core-utilities

   illinois-core-utilities is a Java library that is designed to help programming NLP
   applications by providing a uniform representation of various NLP
   annotations of text (like parse trees, parts of speech, semantic
   roles, coreference, etc.) 

## Overview

  This library provides basic useful functionality in Java. The
  goals of this library are:
  
  1. Augment the Java library with useful data structures and
     algorithms that can be used across many NLP projects.
  2. Add support for recurring experiment-related tasks like
     cross-validation and inter-annotator agreement.
  3. Provide other utility classes for reading files, interface to the
     shell, etc.
   
## Functionality
  * Data structures
    - [`TextAnnotation`](src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/TextAnnotation.java). 
     The library also provides an easy way to access the [Curator](../curator/README.md).
    - `Record` (internal datastructure for [Curator](../curator/README.md))
    - LBJava data structures
    - `Pair` and `Triple` classes
    - `Tree`s, where the nodes can be arbitrary objects and a utility
      class to read trees from the bracket notation (like the Penn
      Treebank notation.)
    - `Queryable` list to support SQL like operations on the elements of
      the list
    - Bounded priority queue, to help with beam search
  * Experiment utilities
    - P/R/F1 reporting (see *EvaluationRecord*)
    - Statistical significance testing
    - Cross-validation helper
    - Android notification sender
  * Algorithms
    - Matching arbitrary lists with patterns
    - Levenstein distance
    - Longest common subsequence
    - Searching for patterns in trees
    - Replacing parts of trees that match a pattern
    - Graph search algorithms -- breadth first, depth first, uniform
      cost and beam.
  * IO
    - Corpus readers (CoNLL, PTB, Ontonotes, etc.)
    - Reading a file, one line at a time
    - Utility functions like mkdir, ls, etc
  * Transformers 
    - A transformer defines a general purpose interface that
      transforms one object into another. This is used extensively in
      the project Edison. For example, any annotation that is
      performed on text can be thought of as the result of a
      transformer.
    - A special transformer is a Predicate, which transforms an object
      into a Boolean.
  * Search
    - Beam search
    - Breadth/Depth first search
    - Graph search
  * Miscellaneous utilities
    - ArgMax
    - Counter
    - A command line interface that uses Java reflection to expose
      static functions of a pre-defined class to the shell
    - And much more...


## Examples and Clarification 
This set of examples goes over the basics of the data
structures. Recollect that different annotations over text are
called `Views`, each of which is a graph of `Constituents` and
`Relations`. The object that manages views corresponding to a
single piece of text is called a `TextAnnotation`.
   
### Creating a `TextAnnotation`
A `TextAnnotation` can be thought of as a container that stores different layers 
of annotations over some text.

  1. Using the LBJ sentence splitter and tokenizer
 
    The simplest way to define a `TextAnnotation` is to just give the
    text to the constructor. Note that in the following example,
    `text1` consists of three sentences. The corresponding `ta1` will
    use the sentence slitter defined in the [Learning based Java](http://cogcomp.cs.illinois.edu/page/software_view/11) (LBJava)
    library to split the text into sentences and further apply the
    LBJ tokenizer to tokenize the sentence.
    
    ```java 
    String text1 = "Good afternoon, gentlemen. I am a HAL-9000 "
      + "computer. I was born in Urbana, Il. in 1992";
    
    String corpus = "2001_ODYSSEY";
    String textId = "001";
    
    // Create a TextAnnotation using the LBJ sentence 
    // splitter and tokenizers.
    TextAnnotation ta1 = new TextAnnotation(corpus, textId, text1); 
    ```

  2. Using pre-tokenized text

    Quite often, our data source could specify the tokenization for
    text. We can use this to create a `TextAnnotation` by specifying
    the sentences and tokens manually. In this case, the input to the
    constructor consists of the corpus, text identifier and a `List`
    of strings. Each element in the list will be treated as a
    sentence. This constructor assumes that the sentences in the list
    are white-space tokenized.
    
    ```java 
    String textId2 = "002";
    
    List<String> tokenizedSentences = Arrays.asList(
                 "Good afternoon , gentlemen .", 
                     "I am a HAL-9000 computer .",
                 "I was born in Urbana , Il. in 1992 .");
    TextAnnotation ta2 = new TextAnnotation(corpus, textId2, tokenizedSentences);
    ```
      
### Views 

The library stores all information about a specific annotation over text
in an object called `View`. 
A `View` is a graph, where the nodes are `Constituents` and the edges are `Relations`.
In its most general sense, a `View` is a
graph whose nodes are labeled spans of text. The edges between the
nodes represent the relationships between them. A `TextAnnotation` can
be thought of as a container of views, indexed by their names.

The tokens are not stored in a `View`. The `TextAnnotation` knows the
tokens of the text and each `Constituent` of every view is defined in
terms of the tokens. A constituent can represent zero tokens or spans.

Sentences are stored as a view. In the terminology above, the
`Constituents` will correspond to the sentences. There are no
`Relations` between them. (The ordering between the sentences is not
explicitly represented because this can be inferred from the
`Constituents` which refer to the tokens.) So the graph that this View
represents is a degenerate graph, with only nodes and no edges.


This example shows how to use the `View` datastructure to create an arbitrary view. 

```java
String corpus = "2001_ODYSSEY";
String textId = "001";
String text1 = "Good afternoon, gentlemen. I am a HAL-9000 computer.";
	
TextAnnotation ta1 = new TextAnnotation(corpus, textId, text1);

View myView = new View("MyViewName", "MyViewGenerator", ta1, 0.121);
ta1.addView("MyViewName", myView);

Constituent m1 = new Constituent("M1", "MyViewName", ta1, 5, 6);
myView.addConstituent(m1);

Constituent m2 = new Constituent("M2", "MyViewName", ta1, 7, 10);
myView.addConstituent(m2);

Constituent m3 = new Constituent("M1", "MyViewName", ta1, 8, 9);
myView.addConstituent(m3);

Constituent m4 = new Constituent("M2", "MyViewName", ta1, 9, 10);
myView.addConstituent(m4);

Relation r1 = new Relation("Subject-Object", m1, m2, 0.001);
myView.addRelation(r1);

Relation r2 = new Relation("NameOf", m3, m4, 0.12);
myView.addRelation(r2);

System.out.println(myView.getConstituents());
System.out.println(myView.getRelations());

System.out.println(r1.getSource());
System.out.println(r2.getTarget());
```


### Accessing the text and tokens
Edison keeps track of the raw text along with the tokens it
contains. So, we can get the original text using the function
`getText()` and also the tokenized text using the function
`getTokenizedText()`. The function `getToken(int tokenId)` gives
us the tokens in the text.

```java 
// Print the text. This prints the raw text that was used to
// create the TextAnnotation object. In the case where the
// second constructor is used, the text is printed whitespace
// tokenized.
System.out.println(ta1.getText());
System.out.println(ta2.getText());
 
// Print the tokenized text. The tokenization scheme is
// specified by the constructor, which in the first example
// defaults to the LBJ tokenizer, and in the second one is
// specified manually.
System.out.println(ta1.getTokenizedText());
System.out.println(ta2.getTokenizedText());
 
// Print the tokens
for (int i = 0; i < ta.size(); i++) {
    System.out.print(i + ":" + ta.getToken(i) + "\t");
}
System.out.println();
```
    
### Accessing sentences
Each `TextAnnotation` knows the views it contains. To get these,
use the function `getAvailableViews()`, which returns a set of
strings representing the names of the views it contains.

The following code prints all the available views in the
`TextAnnotation` ta1 defined above. It then goes over each
sentence and prints them.
    
```java 
System.out.println(ta1.getAvailableViews());
 
// Print the sentences. The Sentence class has many of the same
// methods as a TextAnnotation.
List<Sentence> sentences = ta1.sentences();
 
System.out.println(sentences.size() + " sentences found.");
 
for (int i = 0; i < sentences.size(); i++) {
    Sentence sentence = sentences.get(i);
    System.out.println(sentence);
}
```

### Accessing Constituents    

This example gets all the shallow parse constituents. 
In the shallow parse constituent, each chunk will have one constituent. 
There are no relations between the chunks.

```java     
SpanLabelView shallowParseView = (SpanLabelView) ta
                .getView(ViewNames.SHALLOW_PARSE);
                
List<Constituent> shallowParseConstituents = shallowParseView
                .getConstituents();
for (Constituent c : shallowParseConstituents) {
    System.out.println(c.getStartSpan() + "-" + c.getEndSpan() + ":"
            + c.getLabel() + " " + c.getSurfaceString());
}
```

### Creating complex features 

One can combine the simple datastructures introduced so far and create relatively complex features. 
Here we create features by combination of edge labels for dependency parsing and named-entity recognition. 

```java 
SpanLabelView ne = (SpanLabelView) ta.getView(ViewNames.NER);
TreeView dependency = (TreeView) ta.getView(ViewNames.DEPENDENCY);

System.out.println(dependency);
System.out.println(ne);

for (Constituent neConstituent : ne.getConstituents()) {
    List<Constituent> depConstituents = (List<Constituent>) dependency
            .where(Queries.containedInConstituent(neConstituent));

    for (Constituent depConstituent : depConstituents) {
        System.out.println("Outgoing relations");

        for (Relation depRel : depConstituent.getOutgoingRelations()) {
            System.out.println("\t" + neConstituent + "--"
                    + depRel.getRelationName() + "--> "
                    + depRel.getTarget());
        }

        System.out.println("Incoming relations");

        for (Relation depRel : depConstituent.getIncomingRelations()) {
            System.out
                    .println("\t" + depRel.getSource() + "--"
                            + depRel.getRelationName() + "--> "
                            + neConstituent);
        }
    }
}
```