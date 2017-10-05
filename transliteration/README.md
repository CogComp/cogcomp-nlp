# Transliteration

This is a Java port of Jeff Pasternack's C# code from [Learning Better Transliterations](http://cogcomp.org/page/publication_view/205)

See examples in [TestTransliteration](src/test/java/edu/illinois/cs/cogcomp/transliteration/TestTransliteration.java)
or [Runner](src/main/java/edu/illinois/cs/cogcomp/transliteration/Runner.java).


## Training data

To train a model, you need pairs of names. A common source is Wikipedia interlanguage links. For example, 
see [this data](http://www.clsp.jhu.edu/~anni/data/wikipedia_names) 
from [Learning Transliterations from all languages](http://cis.upenn.edu/~ccb/publications/transliterating-from-all-languages.pdf)
by Anne Irvine et al.

The standard data format expected is:
```bash
foreign<tab>english
```

That said, the [Utils class](src/main/java/edu/illinois/cs/cogcomp/utils/Utils.java) has readers for many 
different datasets (including Anne Irvine's data).  

## Training a model
The standard class is the [SPModel](src/main/java/edu/illinois/cs/cogcomp/transliteration/SPModel.java). Use it 
as follows:

```java
List<Example> training = Utils.readWikiData(trainfile);
SPModel model = new SPModel(training);
model.Train(10);
model.WriteProbs(modelfile);

```

This will train a model, and write it to the path specified by `modelfile`.

`SPModel` has another useful function called `Probability(source, target)`, which will return the transliteration probability
of a given pair. 

## Annotating
A trained model can be used immediately after training, or you can initialize `SPModel` using a 
previously trained and saved `modelfile`.

```java
SPModel model = new SPModel(modelfile);
model.setMaxCandidates(10);
TopList<Double,String> predictions = model.Generate(testexample);
```  

We limited the max number of candidates to 10, so `predictions` will have at most 10 elements. These
are sorted by score, highest to lowest, where the first element is the best.

## Interactive

Once you have trained a model, it is often helpful to try interacting with it. Use [interactive.sh](scripts/interactive.sh)
for this:
```bash
$ ./scripts/interactive.sh models/modelfile
```
