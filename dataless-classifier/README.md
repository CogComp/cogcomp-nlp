# CogComp-DatalessClassifier

Some key points:
- The Main classes for the Dataless Annotators are:
  * **ESADatalessAnnotator** for the ESA-based Dataless Annotator
  * **W2VDatalessAnnotator** for the Word2Vec-based Dataless Annotator
- Dataless Annotators add the **ESA_DATALESS** and **W2V_DATALESS** views to the input `TextAnnotation` respectively, and it requires the presence of a **TOKENS** view with the end-user's desired Tokenization.
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
Dataless Annotators also require the corresponding embeddings, i.e. ESA embeddings for the ESADatalessAnnotator, and Word2Vec embeddings for the W2VDatalessAnnotator. If you are using the project as a dependency, you'll have to add the following dependencies too:

	<dependency>
		<groupId>edu.illinois.cs.cogcomp</groupId>
		<artifactId>esaEmbedding</artifactId>
		<version>1.0</version>
	</dependency>
	<dependency>
		<groupId>edu.illinois.cs.cogcomp</groupId>
		<artifactId>w2vEmbedding-100</artifactId>
		<version>1.0</version>
	</dependency>

Then, you can use the default annotator configs, which have been initialized to make everything work with the aforementioned dependencies.

Alternatively, you can just download the aforementioned dependencies, unzip and put the corresponding resources in your desired path, and specify the path in the project config:
* **esaPath**: path to the esa embeddings file *(esaEmbedding/esa_vectors.txt)*
* **esaMapPath**: path to the esa conceptID to conceptName file *(esaEmbedding/idToConceptMap.txt)*
* **w2vPath**: path to the word2vec embeddings file *(w2vEmbedding-100/w2v\_vectors.txt)*

A sample config file with the default values has been provided in the config folder .. *config/project.properties*

To check whether you are properly set to use the project or not, run:
* `mvn -Dtest=ESADatalessTest#test test` to test the ESADatalessAnnotator.
* `mvn -Dtest=W2VDatalessTest#test test` to test the W2VDatalessAnnotator.


References:

[1] Chang, Ming-Wei, et al. "Importance of Semantic Representation: Dataless Classification." AAAI. Vol. 2. 2008.

[2] Song, Yangqiu, and Dan Roth. "On Dataless Hierarchical Text Classification." AAAI. Vol. 7. 2014.
