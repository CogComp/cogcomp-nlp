# Cogcomp Dependency Parser

A dependency parser built using the [Illinois Structured Learning](https://github.com/CogComp/illinois-sl) framework.

## Quickstart

### PREREQUISITES

- Java 1.8+ (see [here](https://www.java.com/en/download/help/download_options.xml)).
- Maven 3 (see [here](http://maven.apache.org/download.cgi))
- If you are running it on Windows, you may need to set path variables
(see [here](http://docs.oracle.com/javase/tutorial/essential/environment/paths.html)).

### COMPILATION

It is not hard to compile. `cd` to the main directory (`edu.illinois.cs.cogcomp.depparse/`)
 and run:
```bash
 $ mvn compile
 $ mvn dependency:copy-dependencies
 ```

### USE

The parser is meant to be used as part of the [cogcomp-nlp-pipeline](../pipeline/README.md),
but it can also be used programmatically either from the `DepAnnotator`
class (sentence-by-sentence processing based on `TextAnnotation`) or via the `MainClass`
class for batch file processing mode.

## Licensing
To see the full license for this software, see [LICENSE](../master/LICENSE).
