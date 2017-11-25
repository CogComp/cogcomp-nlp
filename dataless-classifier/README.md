# CogComp-DatalessClassifier
Given a label ontology, and textual descriptions of those labels, Dataless-Classifier is capable of classifying arbitrary text into that ontology.

It is particularly useful in those scenarios where it is difficult/expensive to gather enough training data to train a supervised text classifier. Dataless-Classifier utilizes the semantic meaning of the labels to bypass the need for explicit supervision. For more information, please visit our main project [page](http://cogcomp.org/page/project_view/6).    


Some key points:
- The Main classes for the Dataless Annotators are:
  * **ESADatalessAnnotator** for the ESA-based Dataless Annotator
  * **W2VDatalessAnnotator** for the Word2Vec-based Dataless Annotator
- Dataless Annotators add the **DATALESS_ESA** and **DATALESS_W2V** views to the input `TextAnnotation` respectively, and it requires the presence of a **TOKENS** view with the end-user's desired Tokenization.
- Since Labels/Topics are inferred at the Document-Level, all topic annotations span the entire document.
- Sample invocation has been provided in the main functions of each annotator.
- Both annotators load up embeddings in memory, and thus can easily consume upto **10GB RAM**.


## Label Hierarchy
Dataless Classification requires the end-user to specifcy a Label hierarchy (with label descriptions), which it classifies into. The Label hierarchy needs to be provided using a very specific format:
* **labelNamePath**: Specify your label id to label name mapping here in the `labelID \t labelName` format 
  (label id can be any ID specific to your system, however we use the label name itself as ID in our sample hierachy for readibility)
* **labelHierarchyPath**: The first line of this file should contain tab-separated list of Top-Level nodes in the hierarchy (i.e. the ones directly connected to the root). Then, every following line should specify the connections in the hierachy in the `parentLabelID \t childLabelID1 \t childLabelID2 \t ...` format.
* **labelDescPath**: Dataless' performance hinges on good label descriptions, which you specify in this file in the `labelID \t labelDescription` format.

We provide a sample 20newsgroups hierarchy with label descriptions inside data/hierarchy/20newsgroups, where:
* idToLabelNameMap.txt should be used as labelNamePath
* parentChildIdMap.txt should be used as labelHierarchyPath
* labelDesc\_Kws\_simple.txt should be used as labelDescPath

We also provide improved 20newsgroups label descriptions in *labelDesc\_Kws\_embellished.txt* which corresponds to the label descriptions used in [2], whereas the *labelDesc\_Kws\_simple.txt* corresponds to the label descriptions used in [1].

## Embeddings
ESA and Word2Vec Embeddings are fetched from the DataStore on demand.

## Config
A sample config file with the default values has been provided in the config folder .. *config/project.properties*

To check whether you are properly set to use the project or not, run:
* `mvn -Dtest=ESADatalessTest#testPredictions test` to test the ESADatalessAnnotator.
* `mvn -Dtest=W2VDatalessTest#testPredictions test` to test the W2VDatalessAnnotator.

If you use this software for research, please cite the following papers:

[1] Chang, Ming-Wei, et al. "Importance of Semantic Representation: Dataless Classification." AAAI. Vol. 2. 2008.

[2] Song, Yangqiu, and Dan Roth. "On Dataless Hierarchical Text Classification." AAAI. Vol. 7. 2014.
