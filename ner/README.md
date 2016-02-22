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
java -classpath  "dist/*:lib/*:models/*" -Xmx8g edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NerTagger -annotate input output config/ner.properties"
```

This will annotate with 4 NER categories: PER, LOC, ORG, and MISC.  If you change the `modelName` parameter
of `config/ner.properties` to **NER_ONTONOTES**, your input text will be annotated with 18 labels. In both cases, 
each input file will be annotated in bracket format and the result written to a file with the same name 
under the directory `output/`.


### PROGRAMMATIC USE

If you want to use the NER tagger programmatically, we recommend
using the class/method [edu.illinois.cs.cogcomp.ner.NERAnnotator.addView(TextAnnotation ta)]
(src/main/java/edu/illinois/cs/cogcomp/ner/NERAnnotator.java).

You can easily incorporate the Illinois Named Entity Recognizer into
your Maven project by adding the following dependencies to your pom.xml file:
```xml
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>illinois-ner</artifactId>
    <version>VERSION</version>
</dependency>
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>illinois-ner</artifactId>
    <version>VERSION</version>
    <classifier>models-conll</classifier>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>illinois-ner</artifactId>
    <version>VERSION</version>
    <classifier>models-ontonotes</classifier>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>edu.illinois.cs.cogcomp</groupId>
    <artifactId>LBJava</artifactId>
    <version>1.2.10</version>
</dependency>
```
