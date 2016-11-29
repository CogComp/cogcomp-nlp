# Illinois Inference 

This project is a suite of unified wrappers to a set optimization libraries, as well as some basic approximate solvers: 

 - Gurobi: one of the fastest commercial toolkits. You need to have a license for using this.  
 - ojAlgo: Open Source optimization tool. 
 - Beam search: a heuristic search algorithm that explores a graph by expanding the most promising node in a limited set.  


## Setting up Gurobi  

To download and install Gurobi visit [http://www.gurobi.com/](http://www.gurobi.com/)

Make sure to include Gurobi in your PATH and LD_LIBRARY variables
```
export GUROBI_HOME="PATH-TO-GUROBI/linux64"
export PATH="${PATH}:${GUROBI_HOME}/bin"
export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${GUROBI_HOME}/lib"
```

## Frequently Asked Questions 

 - **Why the inference modules tests keep failing?** We have have a unit test for Gurobi which works only when the license is provided in environment. For this reason we skip on Semaphore. If you're running locally and seeing failures it must be that you don't have the license installed on your computer (which you can ignore, if you don't need it).  
