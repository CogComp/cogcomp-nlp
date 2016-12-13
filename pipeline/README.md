# Illinois NLP Pipeline

This module contains code that allows you to run various NLP tools
to annotate plain text input. 

## CONTENTS
0. Quickstart
1. Purpose
	1. Intended Use
	2. Pipeline NLP Components
	3. License
2. Prerequisites
	1.  General Requirements: System
	2.  Specific Requirements: SRL
3. Download contents 
4. Dependencies
5. Running the Illinois NLP Pipeline
	1.  Running a Simple Command-Line Test
6. Programmatic use
	1. Configuration Options
	2. Changing the logging settings
	3. Troubleshooting

## 0. Quickstart

Assuming you downloaded the pipeline package as a zip from the
Illinois Cognitive Computation Group's web site:
To process a set of plain text files in one directory and generate
a corresponding set of annotated files in json format in a second
directory, run the command:
```
scripts/runPipelineOnDataset.sh  <configFile> <inputDirectory> <outputDirectory>
```
The configuration file needs only to contain options to override defaults.


## 1. Purpose

This software bundles some basic preprocessing steps that a lot of NLP
applications need, with the goal of making them run locally. Some
components have significant memory requirements, but given a machine
with sufficient memory, you can instantiate a AnnotatorService
object that provides plain text tokenization, Part-of-Speech tagging,
chunking, Named Entity tagging, lemmatization, dependency and
constituency parsing, and (verb) semantic role labeling.  You can also
use just a subset of these by changing the configuration file
that the pipeline uses.

By default, the illinois-nlp-pipeline will cache its outputs in a local
directory, so that if you process overlapping data sets (or process
the same data set multiple times), it will use a cached copy of the
system outputs and run much faster.


### 1.1 Intended use

The illinois-nlp-pipeline package was designed to be used either
programmatically -- inline in your Java code -- or from the command line,
using only those components you need to use for a given task.

Currently, the pipeline works only for English plain text. You will need
to remove XML/HTML mark-up, as well as formatting like bulleted lists
if you want well-formed output. (The pipeline may generate
output for such texts, but it is not guaranteed that the different
tools will succeed in producing mutually consistent output.)

One important note: if you wish to use your own tokenization, you should
implement a class that follows the Tokenizer interface from
illinois-core-utilities, and use it as an argument to a
TokenizerTextAnnotationBuilder (also from illinois-core-utilities).


## 1.2 Pipeline NLP Components

The pipeline has the following annotators. To understand the annotations,
please refer to the descriptions of the individual packages at the URLs
provided. These annotations are stored as Views in a single TextAnnotation
data structure -- see README_DEVELOPER and the [illinois-cogcomp-nlp](https://github.com/IllinoisCogComp/illinois-cogcomp-nlp) library.
The memory is expected MAXIMUM run-time memory required for the component
by itself. Note that the pipeline runs only one copy of each active component
so that, for example, a single Chunker component fulfils the needs
of several other components for which it is a dependency.

1. Lemmatizer: <1G memory, no dependencies.
2. Part-of-Speech tagger: <1G, no dependencies.
3. Chunker: <1G, requires Part-of-Speech tagger.
4. [Named Entity Recognizer](http://cogcomp.cs.illinois.edu/page/software_view/NETagger) (CoNLL): 2G, no dependencies.
5. [Named Entity Recognizer](http://cogcomp.cs.illinois.edu/page/software_view/NETagger) (OntoNotes) 4G, no dependencies.
6. [Constituency Parser](http://nlp.stanford.edu/software/lex-parser.shtml) (Stanford): 1G, no dependencies.
5. [Dependency Parser](http://nlp.stanford.edu/software/lex-parser.shtml) (Stanford): shares resources of Constituency parser so no individual footprint; no dependencies.
7. Verb Semantic Role Labeler: 4G, requires Lemmatizer, Part-of-Speech, Named Entity Recognizer (CoNLL),
   Constituency Parser.
8. Noun Semantic Role Labeler: 1G, requires Lemmatizer, Part-of-Speech, Named Entity Recognizer (CoNLL),
   Constituency Parser.


### 1.3 LICENSE

## Licensing
To see the full license for this software, see [LICENSE](../master/LICENSE) or visit the [download page](http://cogcomp.cs.illinois.edu/page/software_view/NETagger) for this software
and press 'Download'. The next screen displays the license.


## 2 PREREQUISITES

The Illinois NLP Pipeline provides a suite of state-of-the-art
Natural Language Processing tools of varying complexity.  Some have
specific prerequisites that must be present if you want to run them.


### 2.1 GENERAL REQUIREMENTS: SYSTEM

This software was developed on the following platform:

Ubuntu Linux (2.6.32-279.5.2.el6.x86_64)
Java 1.8

The memory required depends on the options you set in the config
file. 2G should be plenty to run the Tokenizer, POS tagger,
lemmatizer, and Shallow Parser (a.k.a. Chunker).  NER will require
an additional 2G (CoNLL model) or 4G (OntoNotes model). The Stanford
Syntactic and Dependency Parsers require approximately 2G. The
Ilinois Verb Semantic Role Labeler (SRL) requires 4G, and the Noun SRL
also requires 4G.

Note that individual Illinois NLP tools may depend on other tools
for inputs, and will not work unless those components are also active.
If you try to run the system with an invalid configuration, it will
print a warning about the missing components.


### 2.2 SPECIFIC REQUIREMENTS: SRL

To run the Semantic Role Labeler you must have an instance of the
Gurobi license on your machine, and set the relevant environment
variables (see [this page](http://www.gurobi.com/products/licensing-pricing/licensing-overview) -- note that there is a free academic use license).


## 3. DOWNLOAD CONTENTS

If you have downloaded the Illinois NLP Pipeline as a stand-alone package,
it will come with all the libraries and other files it requires. (If you
want to use the Semantic Role Labelers, you will need to install a
Gurobi license -- see the section 2.2.)

The download package is organized thus:
```
config/ : configuration files
dist/ : the Illinois Preprocessor jar
lib/ : dependencies
scripts/ : scripts to allow command-line test of the Illinois NLP Pipeline
src/ : source code for the Illinois NLP Pipeline
test/ : test files used for the command line test of the Illinois NLP Pipeline
```
See the section "Running the Illinois NLP Pipeline" for details on running the pipeline.

This distribution contains all the dependencies needed to run the
Illinois NLP Pipeline. This includes configuration files for some
individual components; scripts to process plain text files from the
command line; and .jar files for the libraries used by the pipeline and
its components.

The Illinois NLP Pipeline package sets default configuration options for
all its components.  If you want to experiment with different settings,
we recommend checking out the project from [github](https://github.com/IllinoisCogComp/illinois-cogcomp-nlp) -- see the section on Programmatic Use.

## 4. Dependencies
If this package is used in maven, please add the following dependencies with proper reepositories.
```
<dependencies>
    <dependency>
        <groupId>edu.illinois.cs.cogcomp</groupId>
        <artifactId>illinois-nlp-pipeline</artifactId>
        <version>3.0.86</version>
    </dependency>
    <dependency>
        <groupId>edu.stanford.nlp</groupId>
        <artifactId>stanford-corenlp</artifactId>
        <version>3.3.1</version>
    </dependency>
</dependencies>
<repositories>
    <repository>
        <id>CogcompSoftware</id>
        <name>CogcompSoftware</name>
        <url>http://cogcomp.cs.illinois.edu/m2repo/</url>
    </repository>
    <repository>
        <id>StanfordNLP</id>
        <name>StanfordNLP</name>
        <url>http://central.maven.org/maven2/</url>
    </repository>
</repositories>
```

## 5. RUNNING THE ILLINOIS NLP PIPELINE

This software has been developed to allow some of our more complex tools
to be run completely within a single JVM, either programmatically or
from the command line,  instead of in tandem with the [CCG NLP Curator](http://cogcomp.cs.illinois.edu/page/software_view/Curator).

These instructions assume you have downloaded the pipeline as a
single package from the Cognitive Computation Group web site.

The standard distribution for this package puts dependencies in lib/;
the parser model in data/; and the config file in config/. There are
two sample scripts that are provided to test that the
pipeline works after you have downloaded
it. scripts/runPreprocessor.sh takes as arguments a configuration file
and a text file; it processes the text file according to the
properties set in the config file, and writes output to STDOUT.
scripts/testPreprocessor.sh is a self-contained script that calls
runPreprocessor.sh with fixed arguments and compares the output to
some reference output. If the new output and reference output are
different, the script prints an error message and indicates the
differences.

### 5.1 Running a Simple Command-Line Test

Running the test:
```
scripts/testPreprocessor.sh
```

Running your own text to get a visual sense of what IllinoisPreprocessor is doing:
```
scripts/runPreprocessor.sh  config/pipelineConfig.txt [yourTextFile] > [yourOutputFile]
```

## 6. PROGRAMMATIC USE

You can check the javadoc for detailed information about the
IllinoisPreprocessor API.

The main class is PipelineFactory, in the package
edu.illinois.cs.cogcomp.pipeline.main under src/main/java. For an
example showing how the PipelineFactory and BasicAnnotatorService (the
class it instantiates, which is the pipeline itself) can be used, look at
CachingPipelineTest class under src/test/resources/, in
edu.illinois.cs.cogcomp.pipeline.main.

To process text input, use the '()' method:
```java
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

String docId = "APW-20140101.3018"; // arbitrary string identifier
String textId = "body"; // arbitrary string identifier
String text = ...; // contains plain text to be annotated

ResourceManager rm = new ResourceManager( "config/pipeline-config.properties" );
AnnotatorService pipeline = PipelineFactory.buildPipeline( rm );
TextAnnotation ta = pipeline.createAnnotatedTextAnnotation( docId, textId, text );
```
This method takes as its argument a String variable containing the
text you want to process. This String should not be too long --
depending on the annotators you plan to use, a reasonable upper limit
is 1,000 words (fewer if you use resource-intensive annotators like
Verb or Noun SRL).

The method returns a TextAnnotation data structure (see the
illinois-core-utilities package for details), which contains
a View corresponding to each annotation source. Each View contains
a set of Constituents and Relations representing the annotator output.
Access views and constituents via:
```java
String viewName = ViewNames.POS; // example using ViewNames class constants
View view = ta.getView(viewName);
List<Constituent> constituents = view.getConstituents();
```
See the documentation for individual components (links in section 1 above) for
more information about the annotations and their representation as Constituents and
Relations.

### 6.1 Configuration Options

The default configuration options are specified in the class
edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator.
Each property has a String as a key and a value.  If you want to change specific behaviors,
such as activating or deactivating specific components, you can write non-default entries
in a configuration file and use a ResourceManager (see illinois-core-utilities)
to instantiate an instance of the pipeline (any entries
that duplicate default values will have no effect and are not required).
The default keys and values are specified below; comments provide more information where the
values themselves are not self-explanatory.  Note that the key/value pairs each appear
on a separate line and are themselves separated by a tab key. If you have limited memory or wish
to save on processing time, you should set the values for unnecessary annotations to 'false'
-- in particular, SRL components require more time and memory than most other components,
and the parsers can take a relatively long time on long sentences.

```
// in milliseconds
stanfordMaxTimePerSentence  1000

// in tokens
stanfordParseMaxSentenceLength  60

// directory in which cached annotations will be written.
simpleCacheDir simple-annotation-cache

// flags indicating which NLP components will be used
usePos  true
useLemma    true
useShallowParse true

// "standard" NER: see http://cogcomp.cs.illinois.edu/page/demo_view/NER
useNerConll true

// "extended" NER -- see http://cogcomp.cs.illinois.edu/page/demo_view/NERextended
useNerOntonotes true

useStanfordParse    true
useStanfordDep  true

// semantic role labelers
useSrlVerb  true
useSrlNom   true
```

Note that individuals have their own configuration options -- see the documentation
for individual components for details.


### 6.2 Changing the logging settings

This project uses slf4j's log4j libraries.  You can change the
settings by creating a log4j.properties file and adding the directory
containing that file to the classpath.

### 6.3 Troubleshooting
Please be noted that there should not be two active `PipelineFactory` in a single run. For example, the following code yields run-time errors:
```
public class testpipeline {
    public static TextAnnotation getTA(String id, String text) throws Exception{
        ResourceManager rm = new PipelineConfigurator().getConfig(new ResourceManager( "pipeline-config.properties" ));
        AnnotatorService prep = PipelineFactory.buildPipeline(rm);//pipeline is instantiated everytime this function is called.
        TextAnnotation rec = prep.createAnnotatedTextAnnotation(id, "", text);
        return rec;
    }
    public static void main(String[] args_) throws Exception {
        String text = "Houston, Monday, July 21 -- Men have landed and walked on the moon.";
        TextAnnotation rec1 = testpipeline.getTA("1",text);
        text = "Here's another sentence to process.";
        TextAnnotation rec2 = testpipeline.getTA("2",text);
    }
}
```
```
Exception in thread "main" org.mapdb.DBException$FileLocked: File is already opened and is locked: annotation-cache
	at org.mapdb.volume.Volume.lockFile(Volume.java:446)
	at org.mapdb.volume.RandomAccessFileVol.<init>(RandomAccessFileVol.java:52)
	at org.mapdb.volume.RandomAccessFileVol$1.makeVolume(RandomAccessFileVol.java:26)
	...
Caused by: java.nio.channels.OverlappingFileLockException
	at sun.nio.ch.SharedFileLockTable.checkList(FileLockTable.java:255)
	at sun.nio.ch.SharedFileLockTable.add(FileLockTable.java:152)
	at sun.nio.ch.FileChannelImpl.lock(FileChannelImpl.java:1030)
	...
```
To fix this problem, consider changing it to:
```
public class testpipeline {
    public static TextAnnotation getTA(String id, String text, AnnotatorService prep) throws Exception{
        TextAnnotation rec = prep.createAnnotatedTextAnnotation(id, "", text);
        return rec;
    }
    public static void main(String[] args_) throws Exception {
        ResourceManager rm = new PipelineConfigurator().getConfig(new ResourceManager( "pipeline-config.properties" ));
        AnnotatorService prep = PipelineFactory.buildPipeline(rm);//pipeline is only instantiated once.
        String text = "Houston, Monday, July 21 -- Men have landed and walked on the moon.";
        TextAnnotation rec1 = testpipeline.getTA("1",text);
        text = "Here's another sentence to process.";
        TextAnnotation rec2 = testpipeline.getTA("2",text);
    }
}
```
