# CogComp NER Tagger

This is a state of the art NER tagger that tags plain text with named entities. 
The newest version tags entities with either the "classic" 4-label type set 
(people / organizations / locations / miscellaneous), while the most recent can also tag entities with a larger 
18-label type set (based on the OntoNotes corpus). It uses gazetteers extracted from Wikipedia, word class models 
derived from unlabeled text, and expressive non-local features.

As of model version 3.3, the CoNLL classifiers are trained using a data set augmented with email data. Overall, 
this slightly improves performance on the CoNLL data and significantly improves performance on email data.
This is the model that is used by default; you can specify other models as indicated in the table below by 
setting the configuration parameter "modelName" to the value under "Model classifier" (OntoNotes has its
own NerOntonotesConfigurator class). 


| Corpus | F1 on held-out data | Model classifier | Model version | View Name |
| :--- | :--- | :--- | :--- | :--- |
| CoNLL (trained on CoNLL only)| 90.88 | CoNLL | 3.1 | ViewNames.NER_CONLL |
| CoNLL (trained on CoNLL + enron) | 91.12 | CoNLL_enron | 4.0 | ViewNames.NER_CONLL |
| OntoNotes | 84.61 | OntoNotes | 4.0| ViewNames.NER_ONTONOTES |
| Enron email | 77.68 | ConLL_enron | 3.3 | ViewNames.NER_CONLL |
| MUC | 88.37 | CoNLL_enron | 3.3 | ViewNames.NER_CONLL |



## Quickstart

This assumes you have downloaded the package from the [CogComp download page](http://cogcomp.org/page/software_view/NETagger). If instead, you have cloned the github repo, then see the [Compilation section](#how-to-compile-the-software).

### Using the Menu Driven Command Line Application

CogCompNER now includes a powerful menu driven command line application. This application provides users a flexible environment
supporting applications ranging from simple evaluation to complex bulk tagging. The configuration file must be passed in on the command
line, although there is the option to modify the confiruation during at runtime.

The top level menu is as follows:

```bash
1 - select input [standard in]
2 - change output [standard out]
3 - annotate text entered from the command line, presenting results to the terminal.
4 - show and modify configuration parameters.
q - exit the application.
Choose from above options:
```
The first option(enter "1" on the keyboard) allows users to enter the name of an input file or directory. The second likewise
for an output file or directory. Once these parameters have been set it is possible to do multiple runs, potentially changing
configuration parameters. If no input is specified, standard input is assumed. If output is not specified, it is delivered on 
standard out. When the user selects option 3, all inputs will be processed and delivered as separate files if an output directory
is selected, or in a single file, or if nothing is selected to standard out. If you want to change configuration parameters, enter
"4".

To run this application run the runNER.sh bash script:

```bash
$ ./scripts/runNER.sh configFilename
```
The configuration parameter is optional. If no config file is specified, default parameters are used.

#### Interactive mode
In the afore mentoned menue, you can choose the interactive mode, where you can annotate a text entered from the command line, and get the results back to the terminal. Here is a sample output: 

```
./scripts/runNER.sh

[some logging here]

1 - select input [standard in]
2 - change output [standard out]
3 - annotate text entered from the command line, presenting results to the terminal.
4 - show and modify configuration parameters.
q - exit the application.
Choose from above options: 
3
Enter the text to process, or blank line to return to the menu.

[some logging here]

: 
Obama just landed in Urbana-Champaign, after his trip to Europe last week. 
-----------------
Obama just landed in [LOC Urbana ] -[LOC Champaign ] , after his trip to [LOC Europe ]  last week. 
: 

Down below, bomb-sniffing dogs will patrol the trains and buses that are expected to take approximately 30,000 of the 80,000-plus spectators to Sunday's Super Bowl between the Denver Broncos and Seattle Seahawks.
-----------------
Down below, bomb-sniffing dogs will patrol the trains and buses that are expected to take approximately 30,000 of the 80,000-plus spectators to Sunday's [MISC Super Bowl ]  between the [ORG Denver Broncos ]  and [ORG Seattle Seahawks ] .
: 
```


### Java COMMAND LINE

To annotate plain text files, navigate to the root directory (`illinois-ner/`), and run the
following commands (a plain text file is included in `test/`).
NOTE: These commands assume you ran `mvn install` and `mvn dependency:copy-dependencies`,
which create the ner binary in `target/` and copies all dependency jars into `target/dependency`.

```bash
$ mkdir output
$ java -Xmx6g -classpath "target/*:target/dependency/*" edu.illinois.cs.cogcomp.ner.NerTagger -annotate test/SampleInputs/ output/ config/ner.properties
```

This will annotate each file in the input directory with 4 NER categories: PER, LOC, ORG, and MISC. This may be slow. If you 
change the `modelName` parameter of `config/ner.properties` to **NER_ONTONOTES**, your input text will be annotated with 18 
labels. In both cases, each input file will be annotated in bracket format and the result written to a file with the same name 
under the directory `output/`.

### Additional commands

Additional command scripts are provided to simplify the creation of models, testing of models and bulk annotation as well. 
Each of these take a configuration file as their last parameter. The configuration file specifies potentially many parameters, 
but most importantly, these files specify the location of the model.

Use the annotation.sh script as follows:

```bash
scripts/annotate.sh INPUT_DIRECTORY OUTPUT_DIRECTORY CONFIG_FILE
```

The input directory must be a directory containing files to annotate. The output directory must exist, the labeled 
data will be stored in this directory on completion. The last parameter (as is the case for all these scripts) is the 
configuration file. This functionality is also implemented in the menu driven command line interface.

The train.sh script is used to train a new model. The training directory, testing directory and config file are included 
on the command line.

```bash
scripts/train.sh TRAINING_DATA_DIRECTORY TESTING_DATA_DIRECTORY CONFIGURATION_FILE
```

The test.sh script tests a model against the labeled data in a test directory provided on the command line:

```bash
scripts/train.sh TRAINING_DATA_DIRECTORY TESTING_DATA_DIRECTORY CONFIGURATION_FILE
```

The benchmark.sh script is used to do complex parameter tuning and to train multiple models for evaluation quickly
and easily. This script relies on a directory structure to provide many datasets and configurations to train and 
test against. See the script file for more details.

### PROGRAMMATIC USE

If you want to use the NER tagger programmatically, we recommend
using the class [`NERAnnotator`](src/main/java/edu/illinois/cs/cogcomp/ner/NERAnnotator.java) class. Like any other annotator,
it is used by calling the `addView()` method on the `TextAnnotation` containing sentences to be tagged.

To annotate the text in the CoNLL/Ontonotes format, instantiate the NERAnnotator object with the appropriate ViewName, `ViewNames.NER_CONLL`/`ViewNames.NER_ONTONOTES`. (CoNLL Format NER used in example below)

A complete example follows.

```java
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import java.io.IOException;

// Filename: App.java
public class App
{
    public static void main( String[] args ) throws IOException
    {
        String text1 = "Good afternoon, gentlemen. I am a HAL-9000 "
            + "computer. I was born in Urbana, Il. in 1992";

        String corpus = "2001_ODYSSEY";
        String textId = "001";

        // Create a TextAnnotation using the LBJ sentence splitter
        // and tokenizers.
        TextAnnotationBuilder tab;
        // don't split on hyphens, as NER models are trained this way
        boolean splitOnHyphens = false;
        tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens, false));

        TextAnnotation ta = tab.createTextAnnotation(corpus, textId, text1);

        NERAnnotator co = new NERAnnotator(ViewNames.NER_CONLL);
        try {
            co.getView(ta);
        } catch (AnnotatorException e) {
            e.printStackTrace();
        }

        System.out.println(ta.getView(ViewNames.NER_CONLL));
    }
}
```

Note that you will need to include all the included jars on the classpath, as before.
The following commands assume you ran `mvn install` and `mvn dependency:copy-dependencies`,
which create the ner binary in `target/` and copies all dependency jars into `target/dependency`.

```bash
$ javac -cp "target/*.jar:target/dependency/*" App.java
$ java -cp "target/*.jar:target/dependency/*:." App
```

If you have Maven installed,  you can easily incorporate the CogComp Named Entity Recognizer into
your Maven project by adding the following dependencies to your pom.xml file:

```xml
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>illinois-ner</artifactId>
    <version>VERSION</version>
</dependency>
```

### Configuration
NER has numerous parameters that can be tuned during training and/or which 
affect memory footprint and performance at runtime. These flags and default
values can be found in the classes in package `edu.illinois.cs.cogcomp.ner.config`.

By default, NER components use contextual features in a fairly large
context window, and so its "isSentenceLevel" parameter is set to "false".
If set to "true", this may potentially add some robustness if you add
sophisticated features that depend on deeper levels of NLP processing
(currently, very few are used/required). 

## How to compile the software

### PREREQUISITES

- Java 1.8+ (see [here](https://www.java.com/en/download/help/download_options.xml)).
- Maven 3 (see [here](http://maven.apache.org/download.cgi))
- If you are running it on Windows, you may need to set path variables
(see [here](http://docs.oracle.com/javase/tutorial/essential/environment/paths.html)).


### COMPILATION

It is not hard to compile. cd to the main directory (`ner/`)
and run: 
```bash
$ mvn compile
$ mvn dependency:copy-dependencies
```

This will completely compile the whole project. However, it will *NOT*
train the models: see below for details. You can add pre-existing
models as maven dependencies if you wish.

Note that you cannot install the software in the usual way: you will
need to run the command
```bash
$ mvn -DskipTests=true -Dlicense.skip=true install
```
as you must train the models separately from the install process, and the
junit tests will fail without models already being present.  


## How to train the tagger on new data

For this section, we will assume you use Maven, and have compiled the code, and copied dependencies. If you
 are using the downloaded package, you can just replace the classpath in each commend with `dist/*:lib/*`.

The example script [train.sh](scripts/train.sh) trains a model like this:

```bash
$ java -Xmx8g -cp target/classes:target/dependency/* edu.illinois.cs.cogcomp.ner.NerTagger -train <training-file> <development-set-file> <files-format> <config-file>
```

Where the parameters are:

- training-file 
    - the training file
- development-set-file 
    - this file is used for parameter tuning of the training, use the training file if you don't have a development set (use the same file both for training and for development)
- files-format can be either:
    - -c (for column format) or
    - -r (for brackets format)
    - -json (for JSON-Serialized [TextAnnotation](https://github.com/CogComp/cogcomp-nlp/blob/master/core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/datastructures/textannotation/TextAnnotation.java) format (see [SerializationHelper](https://github.com/CogComp/cogcomp-nlp/blob/master/core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/utilities/SerializationHelper.java) for more details)
    - See below for more information on the formats. Both the training and the development files have to be in the same format.

Complete, working example. Before running this, open [`config/ner.properties`](config/ner.properties) and change the `pathToModelFile` to
something else (for example, `ner/mymodels`). This will prevent it from attempting to overwrite the jar.

```bash
$ java -Xmx8g -cp target/classes:target/dependency/* edu.illinois.cs.cogcomp.ner.NerTagger -train test/Test/0224.txt test/Test/0228.txt config/ner.properties
```

(This is is just dummy data, so it gives a score of about 12 F1).

Where do you get the data from? Unfortunately, the data that was used to train 
the system is copyrighted. So you need to obtain your own data.

There are two trained models packaged with this software: CoNLL and Ontonotes. The
CoNLL data came from the CoNLL03 shared task, which is a subset of the Reuters
1996 news corpus. This is annotated with 4 entity types: PER, ORG, LOC, MISC.

The Ontonotes data can be obtained from the Linguistic Data Consortium, provided
you have an appropriate license. This data is annotated with 18 different labels.

A note on the training procedure:
The config file specifies where to put the models. (To understand the structure 
of the config files, take a look at the code in [LbjTagger/Parameters.java](src/main/java/edu/illinois/cs/cogcomp/ner/LbjTagger/Parameters.java)).
Here are sample lines from a config file that
specify the paths for the models:

```config
modelName = myNewNERModel
pathToModelFile = ./data/Models/MyNERModel/
```

This means that 2 files will be created in the folder './data/Models/MyNERModel':

```config
./data/Models/MyNERModel/myNewNERModel.model.level1
./data/Models/MyNERModel/myNewNERModel.model.level2
```

Sample bracket format (the spaces before the close brackets (]) are not important):

    Now the [ORG National Weather Service  ] is calling for above-normal temperatures in more than half of the [LOC U.S.  ] 

The column format used here is a little different from CoNLL03
annotation format. The files are tab separated, and have the tag in column 0,
and the word in column 5. Note that shallow parse and POS can also be included, but these values
can also simply be replaced by dummy values. The importance of the column format is that sentence boundaries are clearly
marked with an empty line, which is not the case for "brackets format".

See the files in [test/Test/](test/Test/) for sample column format.

## Licensing
To see the full license for this software, see [LICENSE](../master/LICENSE) or visit the [download page](http://cogcomp.org/page/software_view/NETagger) for this software
and press 'Download'. The next screen displays the license. 

## Citation

L. Ratinov and D. Roth, Design Challenges and Misconceptions in Named Entity Recognition. CoNLL (2009) pp.

Thank you for citing us if you use us in your work! http://cogcomp.org/page/software_view/NETagger

```
@inproceedings{RatinovRo09,
    author = {L. Ratinov and D. Roth},
    title = {Design Challenges and Misconceptions in Named Entity Recognition},
    booktitle = {CoNLL},
    month = {6},
    year = {2009},
    url = " http://cogcomp.org/papers/RatinovRo09.pdf",
    funding = {MIAS, SoD, Library},
    projects = {IE},
    comment = {Named entity recognition; information extraction; knowledge resources; word class models; gazetteers; non-local features; global features; inference methods; BIO vs. BIOLU; text chunk representation},
}
```
