Illinois NER Tagger
====================

This is a state of the art NER tagger that tags plain text with named entities. 
The newest version tags entities with either the "classic" 4-label type set 
(people / organizations / locations / miscellaneous), while the most recent can also tag entities with a larger 
18-label type set (based on the OntoNotes corpus). It uses gazetteers extracted from Wikipedia, word class models 
derived from unlabeled text, and expressive non-local features.

## Licensing
To see the full license for this software, see [LICENSE](../master/LICENSE) or visit the [download page](http://cogcomp.cs.illinois.edu/page/software_view/NETagger) for this software
and press 'Download'. The next screen displays the license. 


## Quickstart

This assumes you have downloaded the package from the [Cogcomp download page](http://cogcomp.cs.illinois.edu/page/software_view/NETagger). If instead, you have cloned the github repo, then see the [Compilation section](#how-to-compile-the-software).

### Using the Menu Driven Command Line Application

IllinoisNER now includes a powerful menu driven command line application. This application provides users a flexible environment
supporting applications ranging from simple evaluation to complex bulk tagging. The configuration file must be passed in on the command
line, although there is the option to modify the confiruation during at runtime.

The top level menu is as follows:

```bash
1 - select input [<input file or directory>]
2 - change output [<output file or directory>]
3 - annotate <input file or directory>, storing <output file or directory>
4 - show and modify configuration parameters.
q - exit the application.
Choose from above options:
```
The first option(enter "1" on the keyboard) allows users to enter the name of an input file or directory. The second likewise
for an output file or directory. Once these parameters have been set it is possible to do multiple runs, potentially changing
configuration parameters. If no input is specified, standard input is assumed. If output is not specified, it is delivered on 
standard out. When the user selects option 3, all inputs will be processed and delivered as separate files if an output directory
is selected, or in a single file, or if nothing is selected to standard out. If you want to change configuration parameters, enter
"4" and follow the instructions on the following page.

To run this application run the runNER.sh bash script:

```bash
$ ./scripts/runNER.sh configFilename
```
This script requires the configuration file name.

### Simple COMMAND LINE

To annotate plain text files, navigate to the root directory (`illinois-ner/`), and run the
following commands (plain text files are included in `test/SampleInputs/`).

```bash
$ mkdir output
$ java -Xmx3g -classpath "dist/*:lib/*:models/*" edu.illinois.cs.cogcomp.ner.NerTagger -annotate test/SampleInputs/ output/ config/ner.properties
```

This will annotate each file in the input directory with 4 NER categories: PER, LOC, ORG, and MISC. This may be slow. If you change the `modelName` parameter
of `config/ner.properties` to **NER_ONTONOTES**, your input text will be annotated with 18 labels. In both cases, 
each input file will be annotated in bracket format and the result written to a file with the same name 
under the directory `output/`.

### PROGRAMMATIC USE

If you want to use the NER tagger programmatically, we recommend
using the class [`NERAnnotator`](src/main/java/edu/illinois/cs/cogcomp/ner/NERAnnotator.java) class. Like any other annotator,
it is used by calling the `addView()` method on the `TextAnnotation` containing sentences to be tagged.

To annotate the text in the CoNLL/Ontonotes format, instantiate the NERAnnotator object with the appropriate ViewName, `ViewNames.NER_CONLL`/`ViewNames.NER_ONTONOTES`. (CoNLL Format NER used in example below)

A complete example follows.

```java
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import java.io.IOException;

import java.util.Properties;

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
        tab = new TokenizerTextAnnotationBuilder(new IllinoisTokenizer());

        TextAnnotation ta = tab.createTextAnnotation(corpus, textId, text1);

        NERAnnotator co = new NERAnnotator(ViewNames.NER_CONLL);
        co.doInitialize();

        co.addView(ta);

        System.out.println(ta.getView(ViewNames.NER_CONLL));
    }
}
```

Note that you will need to include all the included jars on the classpath, as before.

```bash
$ javac -cp "dist/*:lib/*:models/*" App.java
$ java -cp "dist/*:lib/*:models/*:." App
```

If you have Maven installed,  you can easily incorporate the Illinois Named Entity Recognizer into
your Maven project by adding the following dependencies to your pom.xml file:

```xml
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>illinois-ner</artifactId>
    <version>VERSION</version>
</dependency>
```

## How to compile the software

### PREREQUISITES

- Java 1.7+ (see [here](https://www.java.com/en/download/help/download_options.xml)).
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
$ java -Xmx4g -cp target/classes:target/dependency/* edu.illinois.cs.cogcomp.ner.NerTagger -train <training-file> <development-set-file> <files-format> <config-file>
```

Where the parameters are:

- training-file 
    - the training file
- development-set-file 
    - this file is used for parameter tuning of the training, use the training file if you don't have a development set (use the same file both for training and for development)
- files-format can be either:
    - -c (for column format) or
    - -r (for brackets format.
    - See below for more information on the formats). Both the training and the development files have to be in the same format.

Complete, working example. Before running this, open [`config/ner.properties`](config/ner.properties) and change the `pathToModelFile` to
something else (for example, `ner/mymodels`). This will prevent it from attempting to overwrite the jar.

```bash
$ java -Xmx4g -cp target/classes:target/dependency/* edu.illinois.cs.cogcomp.ner.NerTagger -train test/Test/0224.txt test/Test/0228.txt -c config/ner.properties
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

##Citation

L. Ratinov and D. Roth, Design Challenges and Misconceptions in Named Entity Recognition. CoNLL (2009) pp.

Thank you for citing us if you use us in your work! http://cogcomp.cs.illinois.edu/page/software_view/NETagger

```
@inproceedings{RatinovRo09,
    author = {L. Ratinov and D. Roth},
    title = {Design Challenges and Misconceptions in Named Entity Recognition},
    booktitle = {CoNLL},
    month = {6},
    year = {2009},
    url = " http://cogcomp.cs.illinois.edu/papers/RatinovRo09.pdf",
    funding = {MIAS, SoD, Library},
    projects = {IE},
    comment = {Named entity recognition; information extraction; knowledge resources; word class models; gazetteers; non-local features; global features; inference methods; BIO vs. BIOLU; text chunk representation},
}
```
