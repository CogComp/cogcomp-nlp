This directory contains several scripts. The user will need to check out
the cogcomp-nlp project into this directory for the scripts to work
properly. 

1. Collect Training Data

The user will be responsible for finding and setting up the original training
and testing data as described in the benchmark.sh script file. The user will 
not run the benchmark.sh script themselves, that will be done automatically
by a tool that allows us to run several benchmarks simultaneously. The 
benchmarkData directory should be placed in the same directory as the scripts.

2. Check Out the Source Code
Check out the version of the source code you want to use, or download the 
appropriate zip file. There should a a directory named "cogcomp-nlp"
containing the source code.

3. Generate the Sweep

For NER, the learning rate and thickness are hard-coded in the 
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
the benchmarkData directory containing the data to run. The benchmarkData directory
where the symbolic links original is hard coded in the link.sh file, but can be 
easily changed. The user will have to supply the data for the benchmarkData 
directories, configured exactly as document in benchmark.sh script that actually 
executes training and testing. Users must supply training data in "train", training 
evaluation data in "dev" and final  testing data in "test" for each of these 
datasets.  

6. Training and Testing the Models

There is a benchmark.sh in each of the resulting ner/scripts directories to run the
benchmarks. There is one stand-alone java class named edu.illinois.cs.cogcomp.ner.ParameterSweep 
that will execute these benchmark.sh scripts when run from the outtermost directory.
This executable will take one optional numberic argument specifying the number of 
concurrent processes to support. If no argument is supplied, it will assume one process
per each processor core. A script has also been provided for this purpose, it is named "run.sh".

It will do the rest. When this execution completes, there should be a results.out 
file in each of the generated ner directories containing results. Errors will be logged
in errors.out in the generated ner directories.
