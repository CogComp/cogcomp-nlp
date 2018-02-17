# CogComp Question Type Classifer 

The goal is to categorize questions into different semantic classes based on the possible semantic types of the answers. We develop a hierarchical classifier guided by a layered semantic hierarchy of answer types that makes use of a sequential model for multi-class classification Question classification would benefit question answering process further if it has the capacity to distinguish between a large and complex set of finer classes.

We define a two-layered taxonomy, which represents a natural semantic classification for typical answers. The hierarchy contains 6 coarse classes: (ABBREVIATION, DESCRIPTION, ENTITY, HUMAN, LOCATION and NUMERIC VALUE) and 50 fine classes.
 

## Usage
A recommended way to use this system us through the pipeline. Please take a look into [the pipeline readme](pipeline/README.md) for more details. 


## Training
The dataset used for training the models is provided in [this publication](http://cogcomp.org/page/publication_view/94).
Also, you have to uncomment the lines in the lbjava definitions that are specify training and testing. 
After that, you can simply run and `mvn compile -pl question-typer` to train the model. 


## Citation

If you use this tool, please cite the following paper: 

```
@inproceedings{LiRo02,
    author = {X. Li and D. Roth},
    title = {Learning Question Classifiers},
    booktitle = {COLING},
    pages = {556--562},
    year = {2002},
    acceptance = {198-435 (45\%)},
    url = "http://cogcomp.org/papers/qc-coling02.pdf",
    funding = {NSF98,MURI,ITR-MIT},
    projects = {CCR,QA,TE},
    comment = {Classifying Answer Type for Question Answering. Sequential Classification. Multiclass Classification.},
}
```

