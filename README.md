# Illinois Inference 

This project is a suite of unified wrappers to a set optimization libraries, as well as some basic approximate solvers: 

 - Gurobi: one of the fastest commercial toolkits. You need to have a license for using this.  
 - ojAlgo: Open Source optimization tool. 
 - Beam search: a heuristic search algorithm that explores a graph by expanding the most promising node in a limited set.  


## Using Gurobi  

To download and install Gurobi visit [http://www.gurobi.com/](http://www.gurobi.com/)

Make sure to include Gurobi in your PATH and LD_LIBRARY variables
```
    export GUROBI_HOME="PATH-TO-GUROBI/linux64"
    export PATH="${PATH}:${GUROBI_HOME}/bin"
    export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:${GUROBI_HOME}/lib"
```
