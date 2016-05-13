# illinois-curator
The Curator acts as a central server that can annotate text using
several annotators. With this package, we can connect to the Curator to
get those annotations and build our own NLP-driven
application. This package contains code for interacting with Curator, using the data structures from illinois-core-utilities.


To use, first create a `CuratorAnnotatorService` to use the pipeline: 

```java 
AnnotatorService annotator = CuratorFactory.buildCuratorClient();
// Or alternatively to use the pipeline:
// AnnotatorService annotator = IllinoisPipelineFactory.buildPipeline();
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