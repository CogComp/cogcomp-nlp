Illinois NER Tagger
====================
This is a state of the art NER tagger that tags plain text with named entities. 
The newest version tags entities with either the "classic" 4-label type set 
(people / organizations / locations / miscellaneous), while the most recent can also tag entities with a larger 
18-label type set (based on the OntoNotes corpus). It uses gazetteers extracted from Wikipedia, word class models 
derived from unlabeled text, and expressive non-local features.

## Licensing
To see the full license for this software, see [LICENSE](LICENSE) or visit the download page for this software
and press 'Download'. The next screen displays the license. 


## Quickstart

### FROM THE COMMAND LINE

Assuming you have plain text files you want to process in directory `input/`, you can generate the annotated versions
in a new directory (suppose you create `output/`) by navigating to the root directory and running the command:

```bash
java -Xmx3g -classpath "dist/*:lib/*:models/*" edu.illinois.cs.cogcomp.ner.NerTagger -annotate input output config/ner.properties"
```

This will annotate with 4 NER categories: PER, LOC, ORG, and MISC. This may be slow. If you change the `modelName` parameter
of `config/ner.properties` to **NER_ONTONOTES**, your input text will be annotated with 18 labels. In both cases, 
each input file will be annotated in bracket format and the result written to a file with the same name 
under the directory `output/`.

Alternatively, you can run the `runNER.sh` script, which allows you to tag input files, or to tag text typed into the console.

```bash
$ ./scripts/runNER.sh
```

### PROGRAMMATIC USE

If you want to use the NER tagger programmatically, we recommend
using the class [`NERAnnotator class`](src/main/java/edu/illinois/cs/cogcomp/ner/NERAnnotator.java). Like any other annotator, it is used by calling the `addView()` method on the `TextAnnotation` containing sentences to be tagged.

To annotate the text in the CoNLL/Ontonotes format, instantiate the NERAnnotator object with the appropriate ViewName, `ViewNames.NER_CONLL`/`ViewNames.NER_ONTONOTES`. (CoNLL Format NER used in example below)

```java
NERAnnotator CoNLLannotator = new NERAnnotator(new ResourceManager(new Properties()), ViewNames.NER_CONLL);

CoNLLannotator.addView(ta);											
```

You can easily incorporate the Illinois Named Entity Recognizer into
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

The code comes pre-compiled, so there is no need for compilation. The jar
can be found in [./dist](./dist).

However, if you so desire, it is easy to recompile. cd to the main directory
and run: 
```bash
$ mvn lbjava:clean lbjava:compile compile 
$ mvn dependency:copy-dependencies
```

This will completely compile the whole project. However, it will *NOT*
train the models: see below for details. You can add pre-existing
models as maven dependencies if you wish.

Note that you cannot install the software in the usual way: you will
need to run the command
```bash
$ mvn -DskipTests install
```
as you must train the models separately from the install process, and the
junit tests will fail without models already being present.  


## Compiling the Source
Assuming you have models, you will still need source data to run some tests.
The appropriate values are set in ```src/test/resources/ner-test.properties```
(note that this is distinct from the config file for the ner itself). 



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

What if you have some data, and you want to incrementally train the model? 
Unfortunately, you'll have to obtain the original copyrighted data, append the 
new data to the original data, and then to retrain the model. It's best if you mix
up the datasets --- that is, the merged dataset will be a mixture of documents 
from the old and new datasets. The perceptron learning algorithm needs to 
be presented with examples in "random" order. There is currently no support 
for this.

There are two trained models packaged with this softare: CoNLL and Ontonotes. The 
CoNLL data came from the CoNLL03 shared task, which is a subset of the Reuters
1996 news corpus. This is annotated with 4 entity types: PER, ORG, LOC, MISC.

The Ontonotes data can be obtained from the Linguistic Data Consortium, provided
you have an appropriate license. This data is annotated with 18 different labels.

A note on the training procedure:
The config file specifies where to put the models. (To understand the structure 
of the config files, take a look at the code in [LbjTagger.Parameters.java](src/main/java/edu/illinois/cs/cogcomp/ner/LbjTagger/Parameters.java), or
look at [README-CONFIG](config/README-CONFIG)). Here are sample lines from a config file that 
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
Now the [ORG National Weather Service  ] is calling for above-normal temperatures in more than 
half of the [LOC U.S.  ] 

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

