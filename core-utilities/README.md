# illinois-core-utilities

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
    - **TextAnnotation** (previously in *Edison*)
    - **Record** support (used in *Curator*)
    - **LBJava** data structures
    - Pair and Triple classes
    - Trees, where the nodes can be arbitrary objects and a utility
      class to read trees from the bracket notation (like the Penn
      Treebank notation.)
    - Queryable list to support SQL like operations on the elements of
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
    - **Corpus** readers (CoNLL, PTB, Ontonotes, etc.)
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

## More information
  1. The (possibly incomplete) javadoc of this library is in the doc
     directory, which is under the main project directory.

  2. The Wiki page for this library will contain example code. (This
     does not exist as of now, but will be added soon.)

  3. If that doesn't work, contact me.

