# Illinois Part-of-Speech Tagger

D. Roth and D. Zelenko, Part of Speech Tagging Using a Network of Linear Separators. Coling-Acl, The 17th International Conference on Computational Linguistics (1998) pp.1136--1142

Thank you for citing us if you use us in your work! http://cogcomp.cs.illinois.edu/page/software_view/POS

Part-of-Speech Tagging is the identification of words as nouns, verbs, adjectives, adverbs, etc. The system implemented 
here is based of the following paper: 

```
@inproceedings{Even-ZoharRo01,
    author = {Y. Even-Zohar and D. Roth},
    title = {A Sequential Model for Multi Class Classification},
    booktitle = {EMNLP},
    pages = {10-19},
    year = {2001},
    url = " http://cogcomp.cs.illinois.edu/papers/emnlp01.pdf"
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
            <artifactId>illinois-pos</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>
```

**Note:** Make sure to change the pom.xml parameter `VERSION` to the latest version of the project.

In general, the best way to use the POS Tagger is through the [`POSAnnotator class`](src/main/java/edu/illinois/cs/cogcomp/pos/POSAnnotator.java). Like any other annotator, it is used by calling the `addView()` method on the `TextAnnotation` containing sentences to be tagged.

```java
	POSAnnotator posannotator = new POSAnnotator();
	posannotator.addView(ta);
```

## Models
When using`POSAnnotator`, the models are loaded automatically from the directory specified in the `Property` [`POSConfigurator.MODEL_PATH`](src/main/java/edu/illinois/cs/cogcomp/pos/POSConfigurator.java)

Thus, to use your own models, simply place them in this directory and they will be loaded; otherwise, the model version 
specified in this project's `pom.xml` file will be loaded from the Maven repository and used.

Note : To use your own models, exclude the `illinois-pos-models` artifact from the `illinois-pos` dependency in your `pom.xml`.

## Training
The class [`POSTrain`](src/main/java/edu/illinois/cs/cogcomp/pos/POSTrain.java) contains a main method that can be used to 
train the models for a POS tagger provided you have access to the necessary training data. It can be called from the top-level 
of the POS sub-project using the following command.

    mvn exec:java -Dexec.mainClass="edu.illinois.cs.cogcomp.pos.POSTrain"


