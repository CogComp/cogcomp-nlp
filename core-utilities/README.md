# illinois-core-utilities

   /illinois-core-utilities/ is a Java library that is designed to help programming NLP
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


## Brief Explanations 
### `TextAnnotation`
A `TextAnnotation` can be thought of as a container that stores different layers 
of annotations over some text.
      
### Views 

The library stores all information about a specific annotation over text
in an object called `View`. In its most general sense, a `View` is a
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
