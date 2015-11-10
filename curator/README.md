# illinois-curator
The Curator acts as a central server that can annotate text using
several annotators. With this package, we can connect to the Curator to
get those annotations and build our own NLP-driven
application. This package contains code for interacting with Curator, using the data structures from illinois-core-utilities.


![schema 001](https://cloud.githubusercontent.com/assets/2441454/10808693/4132f746-7dbc-11e5-8d6a-b5fe1e8ed0b8.png)
The image above describes the different ways of creating 
`TextAnnotation` objects from either tokenized or raw text. 

Below is an example of how the process works when using an 
`AnnotatorService` (our super-wrapper that provides access to different annotations and free caching). illinois-curator implements the `AnnotatorService` interface, making it compatible with illinois-nlp-pipeline.

To use, first create a `CuratorAnnotatorService` to use the pipeline: 

```java 
ResourceManager rm = new ResourceManager("config/project.properties")
AnnotatorService annotator = IllinoisPipelineFactory.buildPipeline(rm);
```

Or alternatively to use the curator: 
```java 
AnnotatorService annotator = CuratorPipeline.buildCurator(rm);
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
