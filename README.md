# Mention Detection

A mention detection module that annoatates all mentions of the given TextAnnoatation

## Run Mention Head Tests

`mvn exec:java -Dexec.mainClass="org.cogcomp.md.BIOTester [METHOD]"`

Supported Methods:
 - "test_cv" Run a five fold cross validation on ACE
 - "test_ere" Run a test on ERE with the model trained on ACE
 - "test_ts" Run a test on ACE test set with the model trained on ACE
 - "calculateAvgMentionLength" Calculates the average mention head length, showed by type
 - "TrainACEModel" Train, generate and save a new model trained on ACE corpus
 - "TrainEREModel" Train, generate and save a new model trained on ERE corpus
 
## Run Mention Extent Tests

`mvn exec:java -Dexec.mainClass="org.cogcomp.md.ExtentTester [METHOD]"`

Supported Methods:
 - "testExtentOnGoldHead" Run a test of predicting extents with gold heads on ACE
 - "testExtentOnPredictedHead" Run a test of predicting extents with predicted heads on ACE
