# CogComp Core-Utilities

   cogcomp-core-utilities is a Java library that is designed to help programming NLP
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

 1. Using the LBJ sentence splitter and tokenizer: 
    
    The simplest way to define a `TextAnnotation` is to just give the
    text to the constructor. Note that in the following example,
    `text1` consists of three sentences. The corresponding `ta1` will
    use the sentence slitter defined in the [Learning Based Java](https://github.com/CogComp/lbjava) (LBJava)
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

 2. Using pre-tokenized text:  
    
    Quite often, our data source could specify the tokenization for
    text. We can use this to create a `TextAnnotation` by specifying
    the sentences and tokens manually. In this case, the input to the
    constructor consists of the corpus, text identifier and a `List`
    of strings. Each element in the list will be treated as a
    sentence. This constructor assumes that the sentences in the list
    are white-space tokenized.
    
    ```java 
    String corpus = "2001_ODYSSEY";
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
 
  1. [cogcomp-curator](../curator/README.md)
  2. [nlp-pipeline](../pipeline/README.md)


The image below describes the different ways of creating 
`TextAnnotation` objects from either tokenized or raw text. 

![schema 001](https://cloud.githubusercontent.com/assets/2441454/10808693/4132f746-7dbc-11e5-8d6a-b5fe1e8ed0b8.png)

Below is an example of how to use `PipelineFactory` to create new annotations. 

```java 
using edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

AnnotatorService annotator = PipelineFactory.buildPipeline();

// Or alternatively to use the curator:
// using edu.illinois.cs.cogcomp.curator.CuratorFactory;
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

### Creating Annotators

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

One key configuration default, `AnnotatorConfigurator.IS_SENTENCE_LEVEL`
indicates whether an annotator requires context beyond a single sentence.
For example, when the POS tagger predicts a label for a word, it uses
only information from the same sentence. This means that a document
can be split up into sentences, which can then be processed one at a 
time by the POS annotator, without degrading accuracy. NER,
on the other hand, has features that depend on a context that may 
extend into previous sentences: if the sentence-by-sentence approach
is used for NER, some degradation is to be expected. 

### Configurators

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

A configurator must inherit from `edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator.java`
and override the `getDefaultConfig()` method. Specify any configuration parameters as public static
`edu.illinois.cs.cogcomp.core.utilities.configuration.Property` fields in the Configurator subclass you
create. This specifies configuration parameter keys and default values, and makes the names available
to clients. 

The `getDefaultConfig()` method should simply populate a `ResourceManager` object with all default values --
there are convenience methods in `Configurator` to make this easier. 

When implementing your Annotator's simplest constructor, instantiate your configurator subclass and
call its `getDefaultConfig()` method. Implement a second Constructor that takes a `ResourceManager`
as its argument, instantiate your configurator, and call its `getConfig(ResourceManager)` method.

Here's the constructor code from ChunkAnnotator:
```
    public ChunkerAnnotator(boolean lazilyInitialize) {
        this(lazilyInitialize, new ChunkerConfigurator().getDefaultConfig());
    }

    public ChunkerAnnotator(boolean lazilyInitialize, ResourceManager rm) {
        super(ViewNames.SHALLOW_PARSE, new String[] {ViewNames.POS}, lazilyInitialize, new ChunkerConfigurator().getConfig(rm));
    }
``` 

This allows you to only specify configuration parameters you wish to modify from their default values, using the keys specified
in the relevant Configurator's Property fields. You can
either directly populate a java `Properties` object with these key/value pairs, then instantiate a `ResourceManager` and call
your Configurator's `getConfig()` method with that; or you can write these non-default values into a text file with each 
key/value pair on a new line, with the key and value separated by a tab character. 

For the Chunker example, suppose you want to change the model path. In `ChunkerConfigurator` this is specified as:
```
    public static final Property MODEL_PATH = new Property("modelPath", MODEL_DIR_PATH.value
            + MODEL_NAME.value + ".lc");
```

To directly populate a Properties object:
```
    import edu.illinois.cs.cogcomp.core.resources.ResourceManager;
    import java.util.Properties;
    import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;

    Properties props = new Properties;
    props.setProperty(ChunkerConfigurator.MODEL_PATH.key, "/some/other/path");
    ChunkerAnnotator ca = new ChunkerAnnotator(new ResourceManager(props));
```

To use a text file instead, create a text file (for this example, "config/altChunker.config") with the single line:
```
modelPath	/some/other/path
```

...and use it to instantiate a ResourceManager object:

```
    ChunkerAnnotator ca = new ChunkerAnnotator(new ResourceManager("config/altChunker.config"));
```

### Serialization and Deserialization

To store `TextAnnotation` objects on disk, serialization/deserialization is supported in the following formats:

- Binary Serialization: Binary serialization using Apache Common's `SerializationUtils` to serialize the `TextAnnotation` class. 
```java
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;

// Serialize and save to file
SerializationHelper.serializeTextAnnotationToFile(ta, "text_annotation.bin", true);

// Read file from disk and deserialize
TextAnnotation ta = SerializationHelper.deserializeTextAnnotationFromFile("text_annotation.bin")
```

- JSON: Lightweight human-readable data-interchange format.
```java
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;

String jsonString = SerializationHelper.serializeToJson(ta);

TextAnnotation ta = SerializationHelper.deserializeFromJson(jsonString);
```

- [Protocol Buffer (Version 2)](https://developers.google.com/protocol-buffers/): Protocol Buffers are Google' language-neutral, platform-neutral mechanism for serializing structured data. Structure definition for TextAnnotation is defined at [TextAnnotation.proto](src/main/proto/TextAnnotation.proto).
```java
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;

// Serialize and save to file
SerializationHelper.serializeTextAnnotationToProtobuf(ta, "text_annotation.buf", true);

// Read file from disk and deserialize
TextAnnotation ta = SerializationHelper.deserializeFromProtobuf("text_annotation.buf");
```

More usage information in the `SerializationHelper` class.

### Generating Protocol Buffer Java Code

**Note:** If you make any change to TextAnnotation class which involves adding/removing data items, make sure to update the
protocol buffer schema and the corresponding serialization code accordingly.

Install the [Protocol Buffer compiler](https://github.com/google/protobuf#protocol-compiler-installation) locally.

On macOS, you can install the compiler using Homebrew: `brew install protobuf`.

Run the following commands from the repository root:

```bash
protoc --java_out=core-utilities/src/main/java core-utilities/src/main/proto/TextAnnotation.proto

mvn license:format
```

### StringTransformation

The `StringTransformation` class helps users processing noisy or marked-up text to track changes
in character offsets, and to map any annotations developed on the cleaned up text back to the original
source.  `StringTransformation` keeps a copy of an **original** text, a **current** text,
and a set of pending edits.  It accepts edits specified as a span of the **current** text and a 
String to replace that span (which may be the empty string in the case of deletion). Pending edits
can be applied on demand, and are automatially applied when the current text is retrieved.
Clients can specify offsets in the original String and find their counterparts in the modified
text, or vice versa.  This allows users to clean up text, process it with NLP compoments, then
map annotations back to the original text. Such functionality is useful in NLP tasks where evaluation
scripts require annotations to be specified in terms of the original source text. 
Corpus reader components such as `XmlDocumentProcessor` use `StringTransformation` to
track changes made when xml elements are stripped; `StringTransformationCleanup` applies
common text cleanup (removing non-standard characters, punctuation errors, etc.) but
allows users to map any annotations acquired from the cleaned-up text to the original text.
See `StringTransformationText` for some examples. 


### TextAnnotationUtilities

The `TextAnnotationUtilities` class provides helper functions for copying constituents and views
between TextAnnotations.  In particular, it supports excerpting annotations for sentences or other
subspans of some `TextAnnotation`'s underlying text into a shorter, self-contained TextAnnotation,
or mapping from a sentence `TextAnnotation` to one for a longer span containing that sentence.

There is also support for `StringTransformation`, mapping a TextAnnotation associated with a 
transformed text to an equivalent TextAnnotation over the original text.


## Citation

If you use this code in your research, please provide the URL for this github repository in the relevant publications.  
If you use any of the NLP modules, please check their README files to see if there are relevant publications to cite. 
