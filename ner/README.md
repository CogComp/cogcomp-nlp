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

### FROM THE COMMAND LINE

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
using the class [`NERAnnotator class`](src/main/java/edu/illinois/cs/cogcomp/ner/NERAnnotator.java). Like any other annotator,
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

We strongly recommend the use of Maven. You can easily incorporate the Illinois Named Entity Recognizer into
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

- To run the Named entity tagger, you will need Java installed on your
system (see [here](https://www.java.com/en/download/help/download_options.xml)).
- If you are running it on Windows, you may need to set path variables 
(see [here](http://docs.oracle.com/javase/tutorial/essential/environment/paths.html)).
- To compile the code you will need the Maven project management tool. 
(see [here](http://maven.apache.org/download.cgi))

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

NB: Please make sure that no files exist in the models directory (or the classpath)
    before training new models. If you have included the pre-trained models jar in
    the pom.xml as a dependency please remove it and delete the corresponding file in
    target/dependency.

Scripts are provided by way of example: [train.sh](scripts/train.sh). There is no file in the folder test/Train for the moment and the user should copy her/his files there to use the script with the default path.

The script [train.sh](scripts/train.sh) runs a model like this below:

```bash
java -Xmx4g -cp target/classes:target/dependency/* edu.illinois.cs.cogcomp.ner.NerTagger -train <training-file> -test  <development-set-file> <files-format> <force-sentence-splitting-on-newlines> <config-file>
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
- force-sentence-splitting-on-newlines
    - can be either true or false.

Sample training command:
```bash
java -Xmx4g -cp target/classes:target/dependency/* edu.illinois.cs.cogcomp.ner.NerTagger -train Data/GoldData/Reuters/train.brackets.gold -test  Data/GoldData/Reuters/test.brackets.gold -r true Config/allLayer1.config
```
Where do you get the data from? Unfortunately, the data that was used to train 
the system is copyrighted. So you need to obtain your own data.

There are two trained models packaged with this softare: CoNLL and Ontonotes. The 
CoNLL data came from the CoNLL03 shared task, which is a subset of the Reuters
1996 news corpus. This is annotated with 4 entity types: PER, ORG, LOC, MISC.

The Ontonotes data can be obtained from the Linguistic Data Consortium, provided
you have an appropriate license. This data is annotated with 18 different labels.

A note on the training procedure:
The config file specifies where to put the models. (To understand the structure 
of the config files, take a look at the code in [LbjTagger.Parameters.java](src/main/java/edu/illinois/cs/cogcomp/ner/LbjTagger/Parameters.java)). Here are sample lines from a config file that 
specify the paths for the models:

```bash
configFileName                myNewNERModel
pathToModelFile                ./data/Models/MyNERModel/
```

This means that 2 files will be created in the folder './data/Models/MyNERModel':

```bash
./data/Models/MyNERModel/myNewNERModel.model.level1
./data/Models/MyNERModel/myNewNERModel.model.level2
```
    
If you copy-paste one of the config files trying to make up your own configuration, 
make sure that you specify the new path for saving the models, otherwise, the 
old good models will be overwritten.

Sample bracket format (the spaces before the close brackets (]) are not important):

    Now the [ORG National Weather Service  ] is calling for above-normal temperatures in more than half of the [LOC U.S.  ] 

The column format used here is a little different from CoNLL03
annotation format. Note that there is shallow parse and POS info there, 
but it is not used, so these values can be replaced by dummy values. The 
importance of the column format is that sentence boundaries are clearly 
marked, which is not the case for "brackets format".

Sample column format:
```
    O        0    0    O       -X-    -DOCSTART-    x    x    0
    B-LOC    0    0    I-NP    NNP    Portugal      x    x    0
    O        0    1    I-VP    VBD    called        x    x    0
    O        0    2    I-PP    IN     up            x    x    0
    B-ORG    0    3    I-NP    NNP    Porto         x    x    0
    O        0    4    I-NP    JJ     central       x    x    0
    O        0    5    I-NP    NN     defender      x    x    0
    B-PER    0    6    I-NP    NNP    Joao          x    x    0
    I-PER    0    7    I-NP    NNP    Manuel        x    x    0
    I-PER    0    8    I-VP    NNP    Pinto         x    x    0
    O        0    9    I-PP    IN     on            x    x    0
    O        0    10   I-NP    NNP    Friday        x    x    0
```

