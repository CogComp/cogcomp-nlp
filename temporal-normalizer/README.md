# Illinois-time

Illinois-time is a temporal extraction and normalization framework, designed to detect the temporal phrase of given text, and normalize them according to [TIMEX3 standard](http://www.timeml.org/tempeval2/tempeval2-trial/guidelines/timex3guidelines-072009.pdf)

## Usage

If you want to use this in your project, you need to take two steps. First add the dependencies, and then call the functions
in your program.
Here is how you can add maven dependencies into your program:

```xml
    <repositories>
        <repository>
            <id>CogCompSoftware</id>
            <name>CogCompSoftware</name>
            <url>http://cogcomp.cs.illinois.edu/m2repo/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>edu.illinois.cs.cogcomp</groupId>
            <artifactId>illinois-time</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>
```

**Note:** Make sure to change the pom.xml parameter `VERSION` to the latest version of the project.

In general, the best way to use the illinois-time is through the [`TemporalChunkerAnnotator class`](src/main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/TemporalChunkerAnnotator.java). Like any other annotator, it is used by calling the `addView()` method on the `TextAnnotation` containing sentences to be tagged.

```java
	Properties rmProps = new TemporalChunkerConfigurator().getDefaultConfig().getProperties();
	// Whether you want to use HeidelTime or our normalizer for normalization
	rmProps.setProperty("useHeidelTime", useHeidelTime ? "True" : "False");
	TemporalChunkerAnnotator tca = new TemporalChunkerAnnotator(new ResourceManager(rmProps));
	tca.addView(ta);
```

For developers, after you make any changes to this module, run ` mvn install -pl temporal-normalizer -am -DskipTests` to recompile (make sure the current directory is under the main project -- `cogcomp-nlp`).
## Framework Overview
Illinois-time is divided into two parts: extractor and normalizer. Our extractor is a statistical model based on Illinois-chunker, a shallow parser. We train our chunker using [TimeBank](https://catalog.ldc.upenn.edu/LDC2006T08) and [AQUAINT](https://tac.nist.gov//data/data_desc.html#AQUAINT) newspaper dataset.

Our normalizer is a deterministic rule-based system. Another option is to use HeidelTime to normalize.

The whole module is tested on [TempEval3](https://www.cs.york.ac.uk/semeval-2013/task1/index.php%3Fid=data.html) dataset, evaluated using an evaluation tool provided by TempEval3. You can run this [benchmark script](scripts/benchmark.sh) by doing `sh scripts/benchmark.sh` (make sure your CWD is `cogcomp-nlp`) to see the performance. You should see the following results (some unnecessary printed information is neglected, we only show the result using our extractor + normalizer here):
###### Using our extractor + our normalizer:
=== Timex Performance ===
|Strict Match|	F1|	P|	R|
| --- | --- | --- | --- |
|	|	79.35|	89.91|	71.01|

|Relaxed Match|	F1|	P|	R|
|---|---|---|---|
|   |	83.4|	94.5|	74.64|

|Attribute F1|	Value|	Type|
|---|---|---|
| |70.45|	74.49|

## Models
When using`TemporalChunkerAnnotator`, the models are loaded automatically from the directory specified in the `Property` [`TemporalChunkerConfigurator.MODEL_PATH`](src/main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/TemporalChunkerConfigurator.java)

Thus, to use your own models, simply place them in this directory and they will be loaded; otherwise, the model version
specified in this project's `pom.xml` file will be loaded from the Maven repository and used.

Note : To use your own models, exclude the `illinois-time-models` artifact from the `illinois-time` dependency in your `pom.xml`


## Training
For the training of our extraction model, please refer to Chunker, with training data substituted by temporal related text.