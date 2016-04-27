# Illinois Chunker (Shallow Parser)

Chunking(Shallow Parsing) is the identification of constituents (noun groups, verbs, verb groups etc.) in a sentence. The system implemented here is based of the following paper: 

```
@inproceedings{PunyakanokRo01,
    author = {V. Punyakanok and D. Roth},
    title = {The Use of Classifiers in Sequential Inference},
    booktitle = {NIPS},
    pages = {995--1001},
    year = {2001},
    publisher = {MIT Press},
    acceptance = {25/514 (4.8\%) Oral Presentations; 152/514 (29%) overall},
    url = " http://cogcomp.cs.illinois.edu/papers/nips01.pdf",
    funding = {NSF98 CAREER},
    projects = {LnI,SI,IE,NE,NLP,CCM},
    comment = {Structured, sequential output; Sequence Prediction: HMM with classifiers, Conditional Models, Constraint Satisfaction},
}
```

## Usage

If you want to use this in your project, you need to take two steps. First add the dependencies, and then call the functions 
in your program. 
Here is how you can add maven dependencies into your program: 

```xml
    <repositories>
        <repository>
            <id>CogcompSoftware</id>
            <name>CogcompSoftware</name>
            <url>http://cogcomp.cs.illinois.edu/m2repo/</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>edu.illinois.cs.cogcomp</groupId>
            <artifactId>illinois-chunker</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>
```

**Note:** Make sure to change the pom.xml parameter `VERSION` to the latest version of the project.

In general, the best way to use the Chunker is through the [`ChunkerAnnotator class`](src/main/java/edu/illinois/cs/cogcomp/chunker/main/ChunkerAnnotator.java). Like any other annotator, it is used by calling the `addView()` method on the `TextAnnotation` containing sentences to be tagged.

```
	ChunkerAnnotator chunker  = new ChunkerAnnotator();
	chunker.addView(ta);
```

## Models
When using`ChunkerAnnotator`, the models are loaded automatically from the directory specified in the `Property` [`ChunkerConfigurator.MODEL_DIR_PATH`](src/main/java/edu/illinois/cs/cogcomp/chunker/main/ChunkerConfigurator.java)

Thus, to use your own models, simply place them in this directory and they will be loaded; otherwise, the model version 
specified in this project's `pom.xml` file will be loaded from the Maven repository and used.

Note : To use your own models, exclude the `illinois-chunker-model` artifact from the `illinois-chunker` dependency in your `pom.xml`.

## Training
The class [`ChunkerTrain`](src/main/java/edu/illinois/cs/cogcomp/chunker/main/ChunkerTrain.java) contains a main method that can be used to 
train the models for a Chunker provided you have access to the necessary training data. It can be called from the top-level 
of the Chunker sub-project using the following command.

    mvn exec:java -Dexec.mainClass="edu.illinois.cs.cogcomp.chunker.main.ChunkerTrain"
