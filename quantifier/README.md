# CogComp Quantifier

This tool takes plain, unannotated text as input and detects mentions
of quantities in the text, as well as normalizes it to a standard
form. The program has two compenents connected in a pipeline:

  1. An LBJava classifier which detects spans of text containing quantities.
  2. A rule based normalizer which coverts quantities to a standard form.

This distribution contains the LBJava and Java source code for the
quantifier and also allows for training the model using your own
training data.

## Requirements
Compiling the CogComp Quantifier *requires* Java 1.6 or higher. If you use it a maven (sbt, etc) dependency, you need Java 1.8. 

## Installation
CogComp Quantifier can be downloaded from http://cogcomp.org/page/software_view/Quantifier.


## Running the CogComp Quantifier

### Generating Quantities for plain text file

The CogComp Quantifier comes bundled with a program that takes a
plain, unannotated text file as input and produces that same text with
standardized quantity annotations as output. To invoke this program,
type:

```
java -cp target/classes/:target/dependency/*
edu.illinois.cs.cogcomp.quant.driver.Quantifier <plainTextFile>
<doesNormalize(Y/N)>
```

When `doesNormalize` is set to `N`, no normalization is done, only spans
of text with quantities is detected. When set to Y, complete
normalization is done.


### PROGRAMMATIC USE

To include in a Maven project, use the following dependency and repository
declarations in your project's `pom.xml` file:

```xml 
<dependencies>
    ...
    <dependency>
        <groupId>edu.illinois.cs.cogcomp</groupId>
        <artifactId>illinois-quantifier</artifactId>
        <version>VERSION</version>
    </dependency>
    ...
</dependencies>
<repositories>
    <repository>
        <id>CogCompSoftware</id>
        <name>CogCompSoftware</name>
        <url>http://cogcomp.org/m2repo/</url>
    </repository>
</repositories>
```

and replace `VERSION` with our current version.  If you do not have Maven, you can add the jar file
`illinois-quantifier-2.0.1.jar` in `target/` folder to your classpath.


#### Using Locally
 
```java     
Quantifier quantifier = new Quantifier();
List<QuantSpan> quantSpans = quantifier.getSpans(<text>, true);
for(QuantSpan qs : quantSpans) {
    System.out.println("Quantity : "+qs.toString());		  
}
```


## Compiling the Code and Retraining

Note: To compile the code and lbjava files you need to have Maven
installed on your system. To download Maven, visit
http://maven.apache.org/download.cgi

From the main directory, run : 

```
  sh scripts/train.sh
```

This downloads all dependencies, trains the model, and compiles all
the source files. The training data is provided in data/trainData.txt


#### Training Details

The provided model is trained on annotated data, which is provided
with this distribution. The data is in data/train.txt, and follows the
CoNLL 2000 format. The annotations required are BIO tags for each token
of sentences, indicating the span of text. The valid tags are as follows :

```
  Tag       Explanation: "The chunker predicts the word ..."
  B-DATE      begins a date mention or daterange mention.
  I-DATE      is inside a date mention or daterange mention.
  B-RATIO     begins a ratio mention.
  I-RATIO     is inside a ratio mention.
  B-RANGE     begins a range mention (representing a range of (non-date)values).
  I-RANGE     is inside a range mention (representing a range of (non-date)values).
  B-NUM       begins a quantity mention, not falling in the above categories.
  I-NUM       is inside a quantity mention, not falling in the above categories.
  O           is outside of any quantity mention.
```

#### 3.3.2 Training on new data

You will need to generate the data in CoNLL format (same as `data/train.txt`). 
Add the location in `src/main/java/edu/illinois/cs/cogcomp/quant/lbj/Constants.java`, and recompile.


## Citation

If this software is used, please cite the following paper:

```
Reasoning about Quantities in Natural Language
   Subhro Roy, Tim Vieira and Dan Roth
   TACL 2015
```

## Troubleshooting and Reporting Problems

Simply open an issue in this repository. 


## Credits 
Copyright (C) 2012, Subhro Roy and Dan Roth
Cognitive Computation Group
Department of Computer Science, University of Illinois at Urbana-Champaign
http://cogcomp.org/page/software_view/Chunker