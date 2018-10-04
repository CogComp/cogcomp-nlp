# CogComp Chunker (Shallow Parser)

Chunking (Shallow Parsing) is the identification of constituents (noun groups, verbs, verb groups etc.) in a sentence. 

## Usage

If you want to use this in your project, you need to take two steps. First add the dependencies, and then call the functions 
in your program. 
Here is how you can add maven dependencies into your program: 

```xml
    <repositories>
        <repository>
            <id>CogcompSoftware</id>
            <name>CogcompSoftware</name>
            <url>http://cogcomp.org/m2repo/</url>
        </repository>
    </repositories>
    
    <dependencies>
    <!--Remove this dependency if you want to train your own model-->
        <dependency>
            <groupId>edu.illinois.cs.cogcomp</groupId>
            <artifactId>illinois-chunker</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>
```

**Note:** Make sure to change the pom.xml parameter `VERSION` to the latest version of the project.

In general, the best way to use the Chunker is through the [`ChunkerAnnotator class`](src/main/java/edu/illinois/cs/cogcomp/chunker/main/ChunkerAnnotator.java). Like any other annotator, it is used by calling the `addView()` method on the `TextAnnotation` containing sentences to be tagged.

```java
	ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
	chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
	chunker.addView(ta);
```
Please also check [ChunkerDemo](src/main/java/edu/illinois/cs/cogcomp/chunker/main/ChunkerDemo.java) for more details.

## Models
When using`ChunkerAnnotator`, the models are loaded automatically from the directory specified in the `Property` [`ChunkerConfigurator.MODEL_DIR_PATH`](src/main/java/edu/illinois/cs/cogcomp/chunker/main/ChunkerConfigurator.java)

Thus, to use your own models (maybe you need to train your models first; please read the following section `Training`), simply place them in this directory and they will be loaded; otherwise, the model version
specified in this project's `pom.xml` file will be loaded from the Maven repository and used.

**Note** : To use your own models (either you're retraining it or using it), **exclude** the `illinois-chunker-model` artifact from the `cogcomp-chunker` dependency in your `pom.xml` to avoid potential conflicts.

## Training
1. If you just want to retrain the chunker models using your data, i.e., without modifying the features of the chunker model, then please read the following:
    
    The class [`ChunkerTrain`](src/main/java/edu/illinois/cs/cogcomp/chunker/main/ChunkerTrain.java) contains a main method that can be used to train the models for a Chunker provided you have access to the necessary training data. It can be called from the top-level
of the Chunker sub-project using the following command (`[DEV_RATIO]` means optional). `MODELDIR` and `MODELNAME` are the dir and name of the model you want to save, respectively. `ROUND` is the number of iterations. `DEV_RATIO` is the portion of training set you want to use as development set and it should be between 0 and 1. When `DEV_RATIO` is specified, `ROUND` is explained as the maximum number of iterations and the chunker will select the iter number (from 1 to `ROUND`) based on its performance on development set.
    ```
    mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkerTrain -Dexec.args="$TRAINFILE $MODELDIR $MODELNAME $ROUND [$DEV_RATIO]"
    ```
    Please also refer to [mvn_train.sh](scripts/mvn_train.sh) for an example of usage.
2. If you want to modify the features used in chunker models, then you need to modify [chunk.lbj](src/main/lbj/chunk.lbj) first, and then `mvn lbjava:clean && mvn lbjava:generate`, such that the [lbjava](src/main/java/edu/illinois/cs/cogcomp/chunker/main/lbjava) files can be re-generated. However, please be noted that the current files in [lbjava](src/main/java/edu/illinois/cs/cogcomp/chunker/main/lbjava) have been **MANUALLY** modified after automatic `mvn lbjava:generate` (e.g., see [this manual change](https://github.com/CogComp/cogcomp-nlp/blob/15faa528ecda2679b8e631140b1116c84c2710a3/chunker/src/main/java/edu/illinois/cs/cogcomp/chunker/main/lbjava/PreviousTags.java#L26-L29) and [this manual change](https://github.com/CogComp/cogcomp-nlp/blob/467c324da318b48010d60b825eafc097da1d62f7/chunker/src/main/java/edu/illinois/cs/cogcomp/chunker/main/lbjava/Chunker.java#L39-L43)). The manual modifications to these automatically generated lbjava files include additional functionalities and if it's regenerated, these functionalities will be gone. Therefore, it is strongly recommended that the user not change the `.lbj` files in this repository unless the user is very familiar with `lbjava`.

## Off-the-shelf scripts
There are a bunch of scripts provided with this package in [scripts](scripts/). Please make sure [apache maven](http://maven.apache.org/install.html) is installed before running these scripts. They should be run from the module root directory, i.e., illinois-cogcomp-nlp/chunker/. For example,
```
cd cogcomp-nlp/chunker/
sh scripts/mvn_demo.sh
```
1. `mvn_compile.sh`: to compile the illinois-chunker module.
2. `mvn_validate.sh`: to validate if the illinois-chunker is functioning properly. Specifically, a test run is performed and we compare its output with a provided reference output.
3. `mvn_benchmark.sh`: to run benchmark tests, access to cogcomp server is required. If the user wants to run his/her own benchmark tests, please change the two properties, `TEST_GOLDPOS_DATA` and `TEST_NOPOS_DATA` in `ChunkerConfigurator.java` and re-compile. Make sure the provided test sets are in the CoNLL2000 format. For example,
```
Old - B-NP
Environmentalism - I-NP
involved - B-VP
microbe - B-NP
hunters - I-NP
and - O
sanitationists - B-NP
. - O
```
4. `mvn_demo.sh`: to process a test file (raw text) and output the chunking results (along with the POS annotation). Change the variable `TESTFILE` in it to process your own files. The demo script uses `ChunkerAnnotator`, which loads the model specified in `ChunkerConfigurator` (the two properties therein, `MODEL_DIR_PATH` and `MODEL_NAME`). The current configuration loads the `illinois-chunker-model` from the dependency list. Therefore, if a new model is needed, please change the two properties correspondingly to your own dir.
5. `mvn_train.sh`: to retrain the chunker model, provided access to proper training sets. Change the `MODELDIR` and `MODELNAME` in it if you need.
6. `mvn_test_conll.sh`: to test the chunker on a test file (must be in the CoNLL2000 format). If no arguments (for `MODELDIR` and `MODELNAME`) are specified, the chunker will load the default chunker model. To specify `MODELDIR` and `MODELNAME`, use as
```
sh scripts/mvn_test_conll.sh (model directory) (model name)
```
## Tips for IntelliJ users
Command line use can be found in [scripts](scripts/). But if you want to use IDEs like IntelliJ, please check the following tips before running.
### Import project
Please import the `cogcomp-nlp` project from `Existing Sources` using the external model `Maven`.
### Setup
Select the `Run` tab. From the drop-down menu, select `Edit Configurations`. In the `Configuration` tab therein, specify the `Working directory` to be `$MODULE_DIR$`. Also, don't forget to set up corresponding `Program arguments`.

## Further Reading 

The system implemented here is based of the following paper: 

```
@inproceedings{PunyakanokRo01,
    author = {V. Punyakanok and D. Roth},
    title = {The Use of Classifiers in Sequential Inference},
    booktitle = {NIPS},
    pages = {995--1001},
    year = {2001},
    publisher = {MIT Press},
    acceptance = {25/514 (4.8\%) Oral Presentations; 152/514 (29%) overall},
    url = " http://cogcomp.org/papers/nips01.pdf",
    funding = {NSF98 CAREER},
    projects = {LnI,SI,IE,NE,NLP,CCM},
    comment = {Structured, sequential output; Sequence Prediction: HMM with classifiers, Conditional Models, Constraint Satisfaction},
}
```

