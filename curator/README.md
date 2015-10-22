# illinois-curator

This package contains code for interacting with Curator, using the data structures from illinois-core-utilities.

illinois-curator implements the `AnnotatorService` interface, making it compatible with illinois-nlp-pipeline.

To use, first create a `CuratorAnnotatorService`:

```
ResourceManager rm = new CuratorConfigurator().getDefaultConfig(); 
AnnotatorService curator = new CuratorAnnotatorService(rm);
```

and then create a `TextAnnotation` component and add the `View`s you need:

```
TextAnnotation ta = curator.createBasicTextAnnotation("corpus", "id0", text);
curator.addView(ta, ViewNames.POS);
curator.addView(ta, ViewNames.NER);
```

Alternatively, you can generate a fully annotated `TextAnnotation` with all the `View`s available in Curator:

```
TextAnnotation ta = curator.createAnnotatedTextAnnotation("corpus", "id0", text);
```

or with a specific set of views you need:

```
Set<String> viewNames = new HashSet<String>(Arrays.asList({ViewNames.POS, ViewNames.NER}));
TextAnnotation ta = curator.createBasicTextAnnotation("corpus", "id0", text, viewNames);
```