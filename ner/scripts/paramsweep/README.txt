This directory contains several scripts. The user will need to check out
the cogcomp-nlp project into this directory for the scripts to work
properly. 

1. Check Out the Source Code
Check out the version of the source code you want to use, or download the 
appropriate zip file. There should a a directory named "cogcomp-nlp"
containing the source code, also in the same directory as the scripts.

2. Collect Training Data

The user will be responsible for finding and setting up the original training
and testing data as described in the benchmark.sh script file. The user will 
not run the benchmark.sh script themselves, that will be done automatically
by a tool that allows us to run several benchmarks simultaneously. The 
benchmark directory should be placed in the cogcomp-nlp/ner/ directory where the
code was checked out in step 1. Each configuration file must contain the 4 entries
as described in the next section.

3. Generate the Sweep

For NER, the learning rate and thickness for new training are codified in the 
configuration file. Each configuration file MUST define the learning rate and thickness
for each of the models explicitly like so:

# parameter sweep reveals these to be the best params, L1 model is best.
learningRatePredictionsLevel1 .001
thicknessPredictionsLevel1 100
learningRatePredictionsLevel2 .02
thicknessPredictionsLevel2 200

These configuration parameters are replaced by the generatesweep.sh script in order
to set the learning rates and thicknesses for that run. For example, L1r.01-t10L2r.02-t20 
indicates a directory containing a run where the SparseAveragedPerceptron was run 
with default parameters of learning rate = .01 and thickness = 10 for the L1 model, and
lr = .02 and thickness of 20 for the L2 model. The generatesweep.sh file contains
the ranges of learning rates and thicknesses to sweep for each model, a directory is 
created for each combination of these arguments.

4. Compiling the Resulting Sources

The generatesweep.sh file will generate the directories containing instances 
of the code base to do the various computations for later comparison. The 
“compile.sh” script will do a maven compile in the resulting directories. This
script also contains a range of learning rate and thickness parameters; these 
must correspond exactly to what is in the generatesweep.sh file(and all other 
scripts for that matter). Running compile.sh will compile all the codes generated
in the previous step.

5. Training and Testing the Models

There is a benchmark.sh in each of the resulting ner/scripts directories to run the
benchmarks. There is one stand-alone java class named edu.illinois.cs.cogcomp.ner.ParameterSweep 
that will execute these benchmark.sh scripts when run from the outermost directory.
This executable will take one optional numeric argument specifying the number of 
concurrent processes to support. If no argument is supplied, it will assume one process
per each processor core. A script has also been provided for this purpose, it is named "run.sh".

It will do the rest. When this execution completes, there should be a results.out 
file in each of the generated ner directories containing results. Errors will be logged
in errors.out in the generated ner directories.
