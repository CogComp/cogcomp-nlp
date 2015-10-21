# illinois-srl: Semantic Role Labeler

### Running
You can use the **illinois-srl** system in either  *interactive* or *annotator* mode.
#### Interactive mode
In *interactive mode* the user can input a single piece of text and get back the feedback from either 
the **Nom**inal or **Verb**al SRL systems in plain text. 
 
To run the system in *interactive mode* see the class `edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler`
or simply run the script: 

```
scripts/run-interactive.sh <config> <Verb|Nom>
```

#### As an `Annotator` component
**illinois-srl** can also be used programmatically through the 
[Annotator interface](http://cogcomp.cs.illinois.edu/software/doc/illinois-core-utilities/apidocs/edu/illinois/cs/cogcomp/core/datastructures/textannotation/Annotator.html).

The main method is `getView(TextAnnotation)` inside `SemanticRoleLabeler`. This will add a new 
[`PredicateArgumentView`](http://cogcomp.cs.illinois.edu/software/doc/illinois-core-utilities/apidocs/edu/illinois/cs/cogcomp/core/datastructures/textannotation/PredicateArgumentView.html)
for either **Nom**inal or **Verb**al SRL. 

### Training
To train the SRL system you will require access to the [Propbank](https://verbs.colorado.edu/~mpalmer/projects/ace.html)
or [Nombank](http://nlp.cs.nyu.edu/meyers/NomBank.html) corpora. You need to set pointers to these in the 
`config/srl-config.properties` file.
(To train the system with a non-Prop/Nombank corpus, you need to extend 
[`AbstractSRLAnnotationReader`](http://cogcomp.cs.illinois.edu/software/doc/illinois-core-utilities/apidocs/edu/illinois/cs/cogcomp/nlp/corpusreaders/AbstractSRLAnnotationReader.html))

To perform the whole training/testing suite, run the `Main` class with parameters `<config-file> expt Verb|Nom true`.
This will:

1. Read and cache the datasets (train/test)
2. Annotate each `TextAnnotation` with the required views
   (here you can set the `useCurator` flag to false to use the CogComp's standalone NLP pipeline) 
3. Pre-extract and cache the features for the classifiers
4. Train the classifiers
5. Evaluate on the (cached) test corpus

**IMPORTANT** After training, make sure you comment-out the pre-trained SRL model dependencies inside 
`pom.xml` (lines 27-38). 