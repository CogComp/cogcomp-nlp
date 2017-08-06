$ cd tools 

> python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile
$ python TE3-evaluation.py data/gold data/system 
# runs with debug level 0 and only reports the performance; also creates a temporary folder to create normalized files  

> python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile debug_level 
$ python TE3-evaluation.py data/gold data/system 0.5
# with debug level 0.5, print DETAILED entity feature performance, e.g. class accuracy, precision, recall, etc.  
$ python TE3-evaluation.py data/gold data/system 1  
# based on the debug_level print debug information. also try debug level 2. 
# run with debug >= 1 to get normalization step errors. 

> python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile debug_level tmp_folder
$ python TE3-evaluation.py data/gold data/system 1 tmp-to-be-deleted
# additionally creates the temporary folder to put normalized files


> python TE3-evaluation.py gold_folder_or_file system_folder_or_filefile debug_level tmp_folder evaluation_method 
$ python TE3-evaluation.py data/gold data/system 0 tmp-to-be-deleted acl11
$ python TE3-evaluation.py data/gold data/system 0 tmp-to-be-deleted implicit_in_recall
# run with different evaluation methods. acl11 to run with ACL'11 short paper metric, not considering the reduced graph for relations. implicit_in_recall to reward the implicit relation in recall as well. 

## usage: 
## to check the performance of all files in a gold folder: 
##          python TE3-evaluation.py gold_folder_path system_folder_path debug_level 


ADDITIONAL INFO (optional):

TimeML-Normalizer

Description: Given different TimeML annotations in different folders or individual files, it normalizes the ids of the entities. For example, if one event is e1 in one annotation and the same event is referred as e251 in another annotations, they will obtain the same id after the normalization. 

Usage:
	java -jar path_to_tool_jar/TimeML-Normalizer.jar -a "annotation1;...;annotationN"
	NOTE: add -d option to see extra debug information

Example:
	java -jar TimeML-Normalizer/TimeML-Normalizer.jar -a "sample-data/test-fold1;sample-data/TIPSem-fold1;sample-data/TIPSemB-fold1;sample-data/trios-fold1"
java -jar TimeML-Normalizer/TimeML-Normalizer.jar -a "sample-data/test-fold1/APW19980219.0476.tml;sample-data/TIPSem-fold1/APW19980219.0476.tml;sample-data/TIPSemB-fold1/APW19980219.0476.tml;sample-data/trios-fold1/APW19980219.0476.tml"



## Allen's OVERLAP is missing in the annotation. It should map to DURING and DURING_INV, but from the annotation guidelines (http://www.timeml.org/site/publications/timeMLdocs/annguide_1.2.1.pdf pg number 45), it seems to mean SIMULTANEOUS. Hence, all the DURING and DURING_INV are converted to SIMULTANEOUS. To change this behavior, please change here: 
evaluation-relations/relation_to_timegraph.py
change the variable: 
consider_DURING_as_SIMULTANEOUS = True 
to 
consider_DURING_as_SIMULTANEOUS = False 

## last updated: April 13, 2013. 1:54PM EST. 