This directory contains several scripts which can be used to tune the parameters
of the NER system for a particular purpose. The two parameters that can be tuned
are the learning rate and thickness of SparseAveragedPerceptron learner. 

Following are required procedural steps to complete this process.

1. Collect Training Data

The user will be responsible for finding and setting up the original training
and testing data as described in the benchmark.sh script file. The user will 
not run the benchmark.sh script themselves, that will be done automatically
by a tool that allows us to run several benchmarks simultaneously. The 
benchmarkData directory should be placed in the same directory as the scripts.

2. Check Out the Source Code

Check out the version of the source code you want to use, or download the 
appropriate zip file. There should a a directory named "illinois-cogcomp-nlp"
containing the source code.

3. Generate the Sweep

For NER, the default learning rate and thickness are hard-coded in the 
LbjTagger.lbj file. The code source is copied by the “generatesweep.sh” 
script into separate directories with names indicating the learning rate 
and the thickness. For example, r.01-t10 indicates a directory containing 
a run where the SparseAveragedPerceptron was run with default parameters of 
learning rate = .01 and thickness = 10. Once the copy of the source directory
is made, the LbjTagger.lbj file is programmatically modified to change the
learning rate and thickness appropriately. The generatesweep.sh file contains
the ranges of learning rates and thicknesses to sweep, a directory is created
for each combination of these two arguments.

4. Compiling the Resulting Sources

The generatesweep.sh file will generate the directories containing instances 
of the code base to do the various computations for later comparison. The 
“compile.sh” script will do a maven compile in the resulting directories. This
script also contains a range of learning rate and thickness parameters; these 
must correspond exactly to what is in the compile.sh file. Running compile.sh
will compile all the codes generated in the previous step.

5. Linking Training and Testing Data

“link.sh” creates a link in each of the directories where benchmark will run to 
the benchmark directory containing the data to run. The benchmark directory
where the symbolic links original is hard coded in the link.sh file, but can be 
easily changed. The user will have to supply the data for the benchmark 
directories, configured exactly as document in benchmark.sh script that actually 
executes training and testing. Users must supply training data in "train", training 
evaluation data in "dev" and final  testing data in "test" for each of these 
datasets.  

6. Training and Testing the Models

There is a benchmark.sh in each of the resulting ner/scripts directories to run the
benchmarks, however, the run.jar jar file is an executable jar file that will run all 
of them for you. This jar fill will execute benchmark.sh scripts in directory that 
starts with "r.". To run this jar enter:

nohup java -jar run.jar >& run.out &

From the command line. It will do the rest. When this execution completes, there 
should be a training.out file in each of the generated ner directories containing 
results.
