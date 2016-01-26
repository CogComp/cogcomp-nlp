Files included in this dataset:
- README.txt: this file
- annotation-guidelines.html: the annotation guidlines that the annotators followed while labeling the corpus
- comma-labeled-data.txt: the annotated data
- other.txt: a subset of the annotated data. It only contains the commas which were labeled as Other by Srikumar et al along with their refined labels.


This corpus is a refinement of Srikumar et al's 2008 dataset. The sentences are drawn from section 00 of the WSJ portion of the Penn Treebank. The oringal dataset included 4 labels: Substitute, Attribute, Locative, and List. The rest of the commas were labeled as Other. This dataset has been expanded to inlcude 4 new labels: Introductory, Complementary, Interrupter, Quotation. There are still some Other labels for commas that could not be refined. All the annotations were done by a single annotator.

Format of comma-labeled-data.txt:
	<wsj-sent-id>
	<tokenized sentence with comma labels embedded inside square brackets>
	<empty-line>

	Example:
	wsj/00/wsj_0049.mrg:39
	At his new job ,[Interrupter] as partner in charge of federal litigation in the Sacramento office of Orrick ,[List] Herrington & Sutcliffe ,[List,Interrupter] he will make out much better .
