# CogComp-time

CogComp-time is a temporal extraction and normalization framework, designed to detect the temporal phrase of given text, and normalize them according to [TIMEX3 standard](http://www.timeml.org/tempeval2/tempeval2-trial/guidelines/timex3guidelines-072009.pdf)

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
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

import java.io.IOException;
import java.util.Properties;

public class demo {
    public static void main (String []args) throws IOException, AnnotatorException {
        Properties rmProps = new TemporalChunkerConfigurator().getDefaultConfig().getProperties();
        // Set to "True" if you want to use HeidelTime for normalization
        rmProps.setProperty("useHeidelTime", "False");
        TemporalChunkerAnnotator tca = new TemporalChunkerAnnotator(new ResourceManager(rmProps));

        Properties props = new Properties();
        props.setProperty( PipelineConfigurator.USE_POS.key, Configurator.TRUE );
        props.setProperty( PipelineConfigurator.USE_SENTENCE_PIPELINE.key, Configurator.TRUE );
        AnnotatorService pipeline = PipelineFactory.buildPipeline(new ResourceManager(props));
        String text = "Saturday morning";
        // If you don't specify DCT, we use today's date as default DCT
        tca.addDocumentCreationTime("2017-08-06");
        TextAnnotation ta = pipeline.createAnnotatedTextAnnotation("corpus", "id", text);
        tca.addView(ta);
        View temporalViews = ta.getView(ViewNames.TIMEX3);
        System.out.println(temporalViews);
    }
}
```

For developers, after you make any changes to this module, run ` mvn install -pl temporal-normalizer -am -DskipTests` to recompile (make sure the current directory is under the main project -- `cogcomp-nlp`).
## Framework Overview
Illinois-time is divided into two parts: extractor and normalizer. Our extractor is a statistical model based on Illinois-chunker, a shallow parser. We train our chunker using [TimeBank](https://catalog.ldc.upenn.edu/LDC2006T08) and [AQUAINT](https://tac.nist.gov//data/data_desc.html#AQUAINT) newspaper dataset.

Our normalizer is a deterministic rule-based system. Another option is to use HeidelTime to normalize.

The whole module is tested on [TempEval3](https://www.cs.york.ac.uk/semeval-2013/task1/index.php%3Fid=data.html) dataset, evaluated using an evaluation tool provided by TempEval3. You can run this [benchmark script](scripts/benchmark.sh) by doing `sh scripts/benchmark.sh` (make sure your CWD is `cogcomp-nlp`) to see the performance. You should see the following results (some unnecessary printed information is omitted, we only show the result using our extractor + normalizer here):
###### Using our extractor + our normalizer:

=== Timex Performance ===

|Strict Match|	F1|	P|	R|
|---|---|---|---|
|	|79.35|	89.91|	71.01|

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

## Performance Benchmark
Run:
```(shell)
cd temporal-normalizer
sh scripts/benchmark.sh
```
in terminal to get results printed on console.

We provide a [`TemporalNormalizerBenchmark`](src/main/java/edu/illinois/cs/cogcomp/temporal/normalizer/main/TemporalNormalizerBenchmark.java) for you to generated evaluated files. You can use the following options:
1. -verbose, this is optional
2. -useGoldChunk, optional, if not set, temporal chunker will be used
3. -inputFolder <filepath>, mandatory
4. -outputFolder <filepath>, mandatory

We use TempEval3 dataset for evaluation, you can download https://www.cs.york.ac.uk/semeval-2013/task1/index.php%3Fid=data.html.

## Citation
Please cite the following [paper](http://cogcomp.org/page/publication_view/691) if you used this package.

```
@inproceedings{ZhaoDoRo12,
    author = {Ran Zhao, Quang Do and Dan Roth},
    title = {A Robust Shallow Temporal Reasoning System},
    booktitle = {NAACL},
    month = {6},
    year = {2012},
    url = "http://cogcomp.org/papers/ZhaoDoRo12.pdf",
}
```