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
    use the sentence slitter defined in the [Learning Based Java](https://github.com/IllinoisCogComp/lbjava) (LBJava)
    library to split the text into sentences and further apply the
    LBJ tokenizer to tokenize the sentence.
    
    ```java 
    String text1 = "Good afternoon, gentlemen. I am a HAL-9000 "
      + "computer. I was born in Urbana, Il. in 1992";
    
    String corpus = "2001_ODYSSEY";
    String textId = "001";
    
    // Create a TextAnnotation using the LBJ sentence splitter 
    // and tokenizers. 
    TextAnnotationBuilder tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());
    
    TextAnnotation ta1 = tab.createTextAnnotation(corpus, textId, text1); 
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
    String corpus = "2001_ODYSSEY"
    String textId2 = "002";
    
	String[] sentence1 = {"Good",  "afternoon", ",", "gentlemen", "."};
    String[] sentence2 = {"I", "am", "a", "HAL-9000", "computer", "."};
    
	List<String[]> tokenizedSentences = Arrays.asList(sentence1, sentence2);
    TextAnnotation ta2 = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(
    											corpus, textId2, tokenizedSentences);
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


### Creating `AnnotatorService`
`AnnotatorService` is our super-wrapper that provides access to different annotations and free caching. 
 Currently we have two classes implementing `AnnotatorService`: 
 
  1. illinois-curator 
  2. illinois-pipeline 


The image below describes the different ways of creating 
`TextAnnotation` objects from either tokenized or raw text. 

![schema 001](https://cloud.githubusercontent.com/assets/2441454/10808693/4132f746-7dbc-11e5-8d6a-b5fe1e8ed0b8.png)

Below is an example of how to use `IllinoisPipelineFactory` to create new annotations. 

```java 
AnnotatorService annotator = IllinoisPipelineFactory.buildPipeline();
// Or alternatively to use the curator: 
// AnnotatorService annotator = CuratorFactory.buildCuratorClient();
```

and then create a `TextAnnotation` component and add the `View`s you need:

```java 
TextAnnotation ta = annotator.createBasicTextAnnotation(corpusID, taID, "Some text that I want to process.");
```


Of course the real fun begins now! Using `AnnotatorService` you can add different annotation 
Views using their canonical name:

```java 
annotator.addView(ta, ViewNames.POS);
annotator.addView(ta, ViewNames.NER_CONLL);
```

These `View`s as well as the `TextAnnotation` object are now locally cached for faster future access.

You can later print the existing views: 

```java 
System.out.println(ta1.getAvailableViews());
```

or access the views them directly: 

```java 
TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);

for (int i = 0; i < ta.size(); i++) {
    System.out.println(i + ":" + posView.getLabel(i));
}
```

###Creating Annotators

The `AnnotatorService` class is based on stringing together classes that
extend the `Annotator` abstract class. This class is used within the 
project to wrap the Illinois POS, Chunker, Lemmatizer, and NER.

In your extension of `Annotator` you need to define the following methods:

`addView(TextAnnotation ta)`

This method is invoked by `Annotator.getView(TextAnnotation ta)`, and
does the actual work of creating a `View` object, populating it with
`Contituent` and `Relation` objects as appropriate, and adding it
to the `TextAnnotation` argument.  This method should first check that
the argument contains all the necessary prerequisite views (the names
of these views are also required by the `Annotator` constructor) and
throw an exception if they are not.

`initialize(ResourceManager rm)`

This method should read configuration parameters (including paths
from which to load resources) from the `ResourceManager` argument. 
Your class constructor will provide a ResourceManager object as an
argument to the `Annotator` constructor. This is used for lazy 
initialization, if active, in which case the first call to `getView()`
will call `initialize()` with this configuration. 


###Configurators

For ease of use of your own NLP software, especially classes that
extend `Annotator` or `AnnotatorService`, you are encouraged to create 
a `Configurator` class that specifies the relevant configuration flags 
and their default values. 

The POS, Chunker, Lemmatizer and NER modules all have their own 
extension of the `Configurator` class for this purpose.  This makes
it easy to specify a default constructor for your annotator, and
to specify only non-default configuration options when you instantiate
one or more classes that use `ResourceManager` and `Configurator`
to manage configuration options.


##Citation

If you use this code in your research, please provide the URL for this github repository in the relevant publications.  
If you use any of the NLP modules, please check their README files to see if there are relevant publications to cite. 
