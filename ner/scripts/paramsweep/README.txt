This process describes the process of doing a parameter sweep to get the
optimal runtime arguments for Illinois CogComp NER system. This is a
highly technical process, and should not be attempted without first 
evaluating the effectiveness of the machine to do the computation.

1. Check Out the Source Code
Check out the version of the source code you want to use, or download the 
appropriate zip file. There should a a directory named "cogcomp-nlp"
containing the source codes. The scripts can be found in the 
cogcomp-nlp/ner/scripts/paramsweep directory after the download. Copy these
scripts (and in particular, "generatesweep.sh" and "run.sh") up to the same
directory containing the source, with a command like this one:

cp cogcomp-nlp/ner/scripts/paramsweep/*.sh .

This will copy the scripts up to where they need to be. Also remove the benchmark
directory from the distribution with this command:

rm -rf cogcomp-nlp/ner/benchmark

This directory will be replaced with a copy of the the benchmark directory you 
will provide in a later step. Now you should have a directory containing
"cogcomp-nlp" with all the source, and the "generatesweep.sh" and "run.sh" 
command line tools, all in the top level directory.

2. Collect Training Data

The user will be responsible for finding and setting up the original training
and testing data as described in the benchmark.sh script file. The user will 
not run the "benchmark.sh" script themselves, that will be done automatically
by a tool that allows us to run several benchmarks simultaneously. The 
benchmark directory should be placed in the top level directory where "cogcomp-nlp"
is located. Each configuration file must contain the 4 entries as described in 
the next section.

3. Generate the Sweep

For NER, the learning rate and thickness for new training is codified in the 
configuration file. Each configuration file MUST define the learning rate and thickness
for each of the models explicitly like so:

# parameter sweep reveals these to be the best params, L1 model is best.
learningRatePredictionsLevel1 .001
thicknessPredictionsLevel1 100
learningRatePredictionsLevel2 .02
thicknessPredictionsLevel2 200

If these configuration parameters do not exist in the config files, they must be added,
although it does not matter what values they have intially. These configuration parameters
are replaced by the "generatesweep.sh" script in order to set the learning rates and 
thicknesses for that run. For example, L1r.01-t10L2r.02-t20 indicates a directory 
containing a run where the SparseAveragedPerceptron was run with default parameters 
of learning rate = .01 and thickness = 10 for the L1 model, and lr = .02 and thickness 
of 20 for the L2 model. The generatesweep.sh file contains the ranges of learning rates
and thicknesses to sweep for each model, a directory is created for each combination of
these arguments.

The "generatesweep.sh" command line tool will compile the software, copy the compiled
software into a number of directories reflecting the learner parameters as described
above. With this done, it will create the benchmarks directory in each of the copied 
directories. From the example above, it would create the 
"L1r.01-t10L2r.02-t20/ner/benchmark/" and populate it approriately. It will copy the
configuration files (since they will be changed in a later step), but it will link
the training data to avoid unnecessary disk usage. When all the benchmark directories are
created, it will go through and modify the learning rates and thicknesses. The range of the
parameter sweep is specified in variable in the "generatesweep.sh" script as shown here:

# learning rates and thickness range
learnrateL1=(.01 .02 .04 .06 .08)
thicknessL1=(20 40 60 80)
learnrateL2=(.01 .02 .04 .06 .08)
thicknessL2=(20 40 60 80)

You can modify the ranges with any number of values in each of the arrays. Remember, this
is a very time consuming computation, so keep the list of parameters as small as you can, maybe
doing some preliminary smaller sweeps so you can focus in on a good range of values.

Once the range of the sweep has been established, and the configuration files in the top level benchmark
directory have been checked for the appropriate configuration parameters as outlines 
above, you are ready to run the script like so:

./generatesweep.sh

This process may take a few minutes, there will be some feedback as it proceeds.

4. Training and Testing the Models

There is a benchmark.sh in each of the resulting ner/scripts directories to run the
benchmarks. There is one stand-alone java class named edu.illinois.cs.cogcomp.ner.ParameterSweep 
that will execute these "benchmark.sh" scripts when run from the outermost directory.
This executable will take one optional numeric argument specifying the number of 
concurrent processes to support. If no argument is supplied, it will assume one process
per each processor core. Each process takes considerable memory, approaching 10g in some 
cases, so don't overburden your machine, or you will find the system killing off your
processes. 

To run the sweep, simply execute the run command from the top level directory:

./run.sh

When this execution completes, there should be a results.out file in each of the generated
ner directories containing results. Errors will be logged in errors.out in the generated
ner directories.
