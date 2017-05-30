# Comma-SRL: Comma Role Labeler

This software extracts relations commas participate in, expanding on previous work in this area. 
Commas and the surrounding sentence structure often express relations that are essential to understanding the
meaning of the sentence. 

## Structure 

There are 2 models:

1. *Local Classifier* : A classifier that classifies individual commas based on the sentence-parse, 
   shallow parse, POS tags, and NER tags
2. *Sequential Model* : Uses structured prediction to label a sequence of commas that are siblings 
   in the parse tree of the sentence

There are 2 sources for annotation data:

1. Comma Resolution Data (`data/corpus`):
   A set of around a 1000 sentences from section 00 of the PTB in which all the commas have been 
   labeled with their roles. The comma that were labeled as **Other** have been refined and the annotations are 
   in `data/otherFile.txt`
2. Comma-Syntax-Pattern annotations (`data/Bayraktar-SyntaxToLabel`):  
   A mapping from a list of the most frequent syntax patterns, extracted 
   from the context of a comma in the parse of the sentence, to comma labels

Execute `./scripts/annotate.sh` in the project directory to annotate the commas in the `data/infile.txt` and receive output in the `data/outfile.txt`. 
You can edit the `infile` to add more sentences. Each sentence must be on a different line.
**NB:** This script requires [Maven](https://maven.apache.org/download.cgi) to be installed.

Run `ClassifierComparison` to get the performance of different models as evaluated over 5-fold cval.

Use `CommaLabeler` to obtain a comma `View` for a sentence represented as a `TextAnnotation`
(must have the views required to extract features for the classifier).



If you use this software please cite our work: 
```
@inproceedings{arivazhagan2016labeling,
  title={Labeling the Semantic Roles of Commas.},
  author={Arivazhagan, Naveen and Christodoulopoulos, Christos and Roth, Dan},
  booktitle={AAAI},
  pages={2885--2891},
  year={2016}
}
```