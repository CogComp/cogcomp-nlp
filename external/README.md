# External Annotators 

Here we maintain lightweight wrappers to external (non-CogcComp) annotators. 
We sometimes use them or compare against these external tools. 
Here is what we currently have: 

| Annotator  | Description                                                                          | Link                                |
|------------|--------------------------------------------------------------------------------------|-------------------------------------|
| PathLSTM   | Semantic Role Labeling system introduced in [here](https://arxiv.org/abs/1605.07515) | https://github.com/microth/PathLSTM | 
| Stanford Relations |  Details [here](https://nlp.stanford.edu/software/relationExtractor.shtml)   |  https://stanfordnlp.github.io/CoreNLP/  |
| Stanford OpenIE |  Details [here](https://nlp.stanford.edu/software/openie.shtml)   |  https://stanfordnlp.github.io/CoreNLP/  |
| Stanford Co-reference |  Details [here](https://stanfordnlp.github.io/CoreNLP/coref.html)   |  https://stanfordnlp.github.io/CoreNLP/  |
| ClausIE | An OpenIE-like sentence simplification system |

Each module can be run as an independent webserver, and can be accessed by the pipeline-client. 


## License 
This module contains external dependencies, and each are subject to their own licenses. 
Beware of their license details before using any of these.  