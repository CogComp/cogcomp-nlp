# Verb-sense Classifier 

This system addresses the verb sense disambiguation (VSD) problem, a sub-problem of word sense 
disambiguation (WSD), for English. It predicts the sense that a verb features in a given
sentence, from among other, potential different meanings.

This system was developed as a part of Semantic Role Labeling system. 

## Performance 

Here is the performance of this system after the most recent training: 

| Label | Correct | Excess | Missed | Precision | Recall | F1 | 
|---|---|---|---|---|---|---| 
| 01 | 4230 | 205 | 113 | 95.38 | 97.4 | 96.38 | 
| 02 | 378 | 88 | 147 | 81.12 | 72 | 76.29 | 
| 03 | 178 | 30 | 60 | 85.58 | 74.79 | 79.82 | 
| 04 | 51 | 16 | 14 | 76.12 | 78.46 | 77.27 | 
| 05 | 16 | 10 | 16 | 61.54 | 50 | 55.17 | 
| 06 | 10 | 8 | 10 | 55.56 | 50 | 52.63 | 
| 07 | 3 | 2 | 3 | 60 | 50 | 54.55 | 
| 08 | 6 | 4 | 0 | 60 | 100 | 75 | 
| 09 | 3 | 2 | 3 | 60 | 50 | 54.55 | 
| 10 | 0 | 3 | 3 | 0 | 0 | 0 | 
| 11 | 10 | 1 | 0 | 90.91 | 100 | 95.24 | 
| 12 | 3 | 1 | 3 | 75 | 50 | 60 | 
| 13 | 1 | 2 | 1 | 33.33 | 50 | 40 | 
| 14 | 5 | 1 | 3 | 83.33 | 62.5 | 71.43 | 
| 15 | 0 | 2 | 0 | 0 | 0 | 0 | 
| 16 | 0 | 1 | 0 | 0 | 0 | 0 | 
| 17 | 1 | 1 | 0 | 50 | 100 | 66.67 | 
| 18 | 0 | 1 | 0 | 0 | 0 | 0 | 
| 19 | 0 | 0 | 1 | 0 | 0 | 0 | 
| 20 | 0 | 0 | 1 | 0 | 0 | 0 | 
| All | 4895 | 378 | 378 | 92.83 | 92.83 | 92.83 | 


## Usage
While you can dig through the code to use it directly, we suggest you use it through our pipeline. More details in [pipeline](../pipeline)'s instructions. 

## Citation 
If you use this system, and want to give credits to our system, please cite the following work: 
```
@inproceedings{PRYZT04,
    author = {V. Punyakanok and D. Roth and W. Yih and D. Zimak and Y. Tu},
    title = {Semantic Role Labeling via Generalized Inference over Classifiers Shared Task Paper},
    booktitle = {CoNLL},
    pages = {130--133},
    year = {2004},
    comment = {Semantic Parsing; Structure Learning with Expressive Constraints; Constraint Optimization; Integer Linear Programming},
}
```