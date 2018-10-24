# CogComp NLP Pipeline

This software bundles some basic preprocessing steps that a lot of NLP
applications need, with the goal of making them run locally. Some
components have significant memory requirements, but given a machine
with sufficient memory, you can instantiate an `AnnotatorService`
object that provides plain text tokenization, Part-of-Speech tagging,
chunking, Named Entity tagging, lemmatization, dependency and
constituency parsing, and (verb) semantic role labeling.  You can also
use just a subset of these by changing the configuration file
that the pipeline uses.

By default, the cogcomp-nlp-pipeline will cache its outputs in a local
directory, so that if you process overlapping data sets (or process
the same data set multiple times), it will use a cached copy of the
system outputs and run much faster.

### Components and requirements 

The pipeline has the following annotators. To understand the annotations,
please refer to the descriptions of the individual packages at the URLs
provided. These annotations are stored as `View`s in a single `TextAnnotation`
data structure.

The CogComp NLP Pipeline provides a suite of state-of-the-art
Natural Language Processing tools of varying complexity.  Some have
specific prerequisites that must be present if you want to run them.
The memory is expected MAXIMUM run-time memory required for the component
by itself. Note that the pipeline runs only one copy of each active component
so that, for example, a single Chunker component fulfils the needs
of several other components for which it is a dependency.


1. Lemmatizer: <1G memory, no dependencies.
2. Part-of-Speech tagger: <1G, no dependencies.
3. Chunker: <1G, requires Part-of-Speech tagger.
4. [Named Entity Recognizer](http://cogcomp.org/page/software_view/NETagger) (CoNLL): 4G, no dependencies.
5. [Named Entity Recognizer](http://cogcomp.org/page/software_view/NETagger) (OntoNotes) 6G, no dependencies.
6. [Constituency Parser](http://nlp.stanford.edu/software/lex-parser.shtml) (Stanford): 1G, no dependencies.
6. [Dependency Parser](http://nlp.stanford.edu/software/lex-parser.shtml) (Stanford): shares resources of Constituency parser so no individual footprint; no dependencies.
7. Dependency Parser (CogComp): <1G requires Part-of-Speech tagger, Chunker.
8. Verb Semantic Role Labeler: ~40G (see [issue656](https://github.com/CogComp/cogcomp-nlp/issues/656)), requires Lemmatizer, Part-of-Speech, Shallow Parsing, Named Entity Recognizer (CoNLL),
   Constituency Parser.
9. Noun Semantic Role Labeler: 1G, requires Lemmatizer, Part-of-Speech, Named Entity Recognizer (CoNLL),
   Constituency Parser.
10. Quantifier: <2G, requires Part-of-Speech.
11. Preposition SRL: <2GB
12. Comma-SRL: <1GB, requires POS, Lemmatizer, Part-of-Speech, Named Entity Recognizer (CoNLL), Constituency Parser. 

Note that individual CogComp NLP tools may depend on other tools
for inputs, and will not work unless those components are also active.
If you try to run the system with an invalid configuration, it will
print a warning about the missing components.

## Contents 

The pipeline module is organized thus:
```
config/ : configuration files
scripts/ : scripts to allow command-line test of the CogComp NLP Pipeline
src/ : source code for the CogComp NLP Pipeline
test/ : test files used for the command line test of the CogComp NLP Pipeline
```
See the section "Running the CogComp NLP Pipeline" for details on running the pipeline.

This distribution contains all the dependencies needed to run the
CogComp NLP Pipeline. This includes configuration files for some
individual components; scripts to process plain text files from the
command line; and .jar files for the libraries used by the pipeline and
its components.

 
## Usage 

This software has been developed to allow some of our more complex tools
to be run completely within a single JVM, either programmatically or
from the command line,  instead of in tandem with the [CCG NLP Curator](http://cogcomp.org/page/software_view/Curator).

The `cogcomp-nlp-pipeline` package was designed to be used either
programmatically -- inline in your Java code -- or from the command line,
using only those components you need to use for a given task.

Currently, the pipeline works only for English plain text. You will need
to remove XML/HTML mark-up, as well as formatting like bulleted lists
if you want well-formed output. (The pipeline may generate
output for such texts, but it is not guaranteed that the different
tools will succeed in producing mutually consistent output.)

One important note: if you wish to use your own tokenization, you should
implement a class that follows the `Tokenizer` interface from
`illinois-core-utilities`, and use it as an argument to a
`TokenizerTextAnnotationBuilder` (also from `cogcomp-core-utilities`).


### Running a Simple Command-Line Test
NOTE: These commands assume you ran `mvn install` and `mvn dependency:copy-dependencies`,
which create the pipeline binary in `target/` and copies all dependency jars into 
`target/dependency`.  
Two sample scripts are provided to test that the pipeline works after you have downloaded
it. `scripts/runPipelineOnDataset.sh` takes as arguments a configuration file
and a text file; it processes the text file according to the
properties set in the config file, and writes output to STDOUT.
`scripts/testPreprocessor.sh` is a self-contained script that calls
`runPipelineOnDataset.sh` with fixed arguments and compares the output to
some reference output. If the new output and reference output are
different, the script prints an error message and indicates the
differences.

To process a set of plain text files in one directory and generate
a corresponding set of annotated files in json format in a second
directory, run the command:
Running the test:

```sh
scripts/testPipeline.sh
```

Running your own text to get a visual sense of what IllinoisPreprocessor is doing:
```sh
scripts/runPipelineOnDataset.sh  config/pipelineConfig.txt [yourInputFile] [yourOutputFile]
```

### Programmatic Use

First you have to add it as a dependency to your project. 
If this package is used in maven, please add the following dependencies with proper repositories.
```xml
<dependencies>
    <dependency>
        <groupId>edu.illinois.cs.cogcomp</groupId>
        <artifactId>illinois-nlp-pipeline</artifactId>
        <version>#VERSION</version>
    </dependency>
</dependencies>
<repositories>
    <repository>
        <id>CogcompSoftware</id>
        <name>CogcompSoftware</name>
        <url>http://cogcomp.org/m2repo/</url>
    </repository>
</repositories>
```

where `#VERSION` is the version included in the `pom.xml` file.

The main class is `PipelineFactory`, in the package
`edu.illinois.cs.cogcomp.pipeline.main` under `src/main/java`. For an
example showing how the `PipelineFactory` and `BasicAnnotatorService` (the
class it instantiates, which is the pipeline itself) can be used, look at
`CachingPipelineTest` class under `src/test/resources/`, in
`edu.illinois.cs.cogcomp.pipeline.main`.

To process text input, use the 'createAnnotatedTextAnnotation()' method:
```java
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

String docId = "APW-20140101.3018"; // arbitrary string identifier
String textId = "body"; // arbitrary string identifier
String text = ...; // contains plain text to be annotated

AnnotatorService pipeline = PipelineFactory.buildPipeline();
TextAnnotation ta = pipeline.createAnnotatedTextAnnotation( docId, textId, text );
```

The output of this will be a tokenized document. If you want to add more views, 
you have to specify them either as arguments of `buildPipeline`, or 
in a configurator. (both explained in the next sub-sections)


This method takes as its argument a String variable containing the
text you want to process. This `String` should not be too long --
depending on the annotators you plan to use, a reasonable upper limit
is 1,000 words (fewer if you use resource-intensive annotators like
Verb or Noun SRL).

The method returns a `TextAnnotation` data structure (see the
`cogcomp-core-utilities` package for details), which contains
a View corresponding to each annotation source. Each View contains
a set of `Constituents` and `Relations` representing the annotator output.
Access views and constituents via:
```java
String viewName = ViewNames.POS; // example using ViewNames class constants
View view = ta.getView(viewName);
List<Constituent> constituents = view.getConstituents();
```
See the documentation for individual components (links in section 1 above) for
more information about the annotations and their representation as Constituents and
Relations.

### Setting the view names programmatically 

The previous usage will add only the basic annotations (e.g. tokenization, sentences, etc). 
To add more high-level annotations you have to specify them in the definition of the `buildPipeline` funtion: 

```java
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

String docId = "APW-20140101.3018"; // arbitrary string identifier
String textId = "body"; // arbitrary string identifier
String text = ...; // contains plain text to be annotated

AnnotatorService pipeline = PipelineFactory.buildPipeline(ViewNames.POS, ViewNames.SRL_VERB);
TextAnnotation ta = pipeline.createAnnotatedTextAnnotation( docId, textId, text );
```

This will include the two views `ViewNames.POS` and `ViewNames.SRL_VERB` in the output, in addition to the default views. 


### Configuration Options

If you want to change specific behaviors,
such as activating or deactivating specific components, you need to write a custom config file and use
it as the example below. 

This mostly happens when you have limited resourcesΩ. For example, SRL and parsers tend to take more time and memory,
 so you can turn them off if you don't need them.

```java
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

// An example of "[PATH_TO_YOUR_CONFIG_FILE]" is "config/pipeline-config.properties"
ResourceManager userConfig = new ResourceManager("[PATH_TO_YOUR_CONFIG_FILE]");
AnnotatorService pipeline = PipelineFactory.buildPipeline(userConfig);
```

The config file is composed with lines of `[KEY]\t[VAL]` pairs, 
 where each pair specifies a property name and its value.
 `[KEY]`s are property names specified in 
 [PipelineConfigurator](https://github.com/CogComp/cogcomp-nlp/blob/master/pipeline/src/main/java/edu/illinois/cs/cogcomp/pipeline/common/PipelineConfigurator.java).
The mechanism behind is that the config file will be parsed by our 
 [ResourceManager](https://github.com/CogComp/cogcomp-nlp/blob/master/core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/utilities/configuration/ResourceManager.java).
Please see the documentation of
 [core-utilities](https://github.com/CogComp/cogcomp-nlp/blob/master/core-utilities/README.md)
 to learn more.
 
You can refer to [the default config file](https://github.com/CogComp/cogcomp-nlp/blob/master/pipeline/config/pipeline-config.properties)
 if you need an example. Most property names are self-explanatory, please see specific usages if some are not.

Note that individual annotators have their own configuration options -- see the documentation
for individual components for details.

### Changing the logging settings

This project uses slf4j's log4j libraries.  You can change the
settings by creating a log4j.properties file and adding the directory
containing that file to the classpath.

## Using pipeline webserver 

Often a convenient model of using the pipeline server is, running the server (which includes all the annotators) on a 
big machine (=big memory) and sending calls to the server with clients. Here we first introduce the details of 
the server and later we will delineate the clients.
 
The server supports post and get requests to obtain  annotation for a requested text, with desired views. 
In order to run the webserver with default settings (port = 8080), do: 

```shell
pipeline/scripts/runWebserver.sh
```

The following arguments are supported:
```shell
usage: pipeline/scripts/runWebserver.sh [-h] [--port PORT] [--rate HOURS]

optional arguments:
  -h, --help             show this help message and exit
  --port PORT, -P PORT   Port to run the webserver.
  --rate HOUR, -L HOUR   Max number of queries per day. If empty, there won't be any limit. 
```

Here are the available APIs: 

| API                    | Address      | Supported request type | Parameters                                                                   | Example                                                          |
|------------------------|--------------|------------------------|------------------------------------------------------------------------------|------------------------------------------------------------------|
| Annotating text        | `/annotate`  | POST/GET               | `text`: the target raw text ; `views`: views to be added, separated by comma | `/annotate?text="This is sample text"&views=POS,NER_CONLL` |
| Getting existing views | `/viewNames` | POST/GET               | N/A                                                                          | `/viewNames`                                                     |


Note that the current web server is very basic. It does not support parallel processing within a single request, nor across multiple requests.


## Frequently Asked Questions (FAQs)

- While running the Pipeline if you see an error regarding insufficient Java heap space, you will need to set the `JAVA_OPTIONS` or `MAVEN_OPTIONS` to include "-Xmx20g": 
```
export MAVEN_OPTS="-Xmx10g"
```

- Between different runs of the Pipeline, if you see the following exception, you should remove the temporary cache folders created by MapDB.
```
Caused by: org.mapdb.DBException$DataCorruption: Header checksum broken. Store was not closed correctly, or is corrupted
```

- Initializing multiple instances of `PipelineFactory` in a single run will lead to an exception in MapDB. For example, the code below:
```java
public class TestPipeline {
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
would lead to the following exception:
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
```java
public class TestPipeline {
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


## LICENSE

To see the full license for this software, see [LICENSE](../master/LICENSE) or visit the [download page](http://cogcomp.org/page/software_view/NETagger) for this software
and press 'Download'. The next screen displays the license.
