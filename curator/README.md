# CogComp Curator

The Curator acts as a central server that can annotate text using
several annotators. With this package, we can connect to the Curator to
get those annotations and build our own NLP-driven
application. This package contains code for interacting with Curator, using the data structures from `cogcomo-core-utilities`.


To use, first create a `CuratorAnnotatorService` to use the pipeline: 

```java 
using edu.illinois.cs.cogcomp.curator.CuratorFactory;

AnnotatorService annotator = CuratorFactory.buildCuratorClient();

// Or alternatively to use the pipeline:
// using edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;
// AnnotatorService annotator = PipelineFactory.buildPipeline();
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

## Frequently Asked Questions 

 - **Why curator tests take so much time/fail?** We have some unit tests in this module which need connection to a remote curator system. Since it is often inaccessble to CIs, we skip them on Semaphore. If you're running locally and seeing failures (or unexpected long delays on its tests) it must be that you don't have connection to Curator (in which case you can just ignore it). 
 

## Citation

J. Clarke and V. Srikumar and M. Sammons and D. Roth, An NLP Curator (or: How I Learned to Stop Worrying and Love NLP Pipelines). LREC (2012) pp.

Thank you for citing us if you use us in your work! http://cogcomp.org/page/software_view/Curator

```
@inproceedings{ClarkeSrSaRo2012,
    author = {J. Clarke and V. Srikumar and M. Sammons and D. Roth},
    title = {An NLP Curator (or: How I Learned to Stop Worrying and Love NLP Pipelines)},
    booktitle = {LREC},
    month = {5},
    year = {2012},
    url = "http://cogcomp.org/papers/ClarkeSrSaRo2012.pdf",
}
```
