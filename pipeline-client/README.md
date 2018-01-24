# CogComp Pipeline Client 

A client is a light-weight system to access the pipeline through network. For more on the pipeline server, look [here](pipeline/README.md).  

#### Java Client 

After setting up the server on a remote machine, we can create a java client to make calls to the server. 
Here in the snnippet we show how it is done: 

```java 
import edu.illinois.cs.cogcomp.pipeline.server.ServerClientAnnotator; 
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

ServerClientAnnotator annotator = new ServerClientAnnotator();
annotator.setUrl("localhost", "8080"); // set the url and port name of your server here 
annotator.setViews(ViewNames.POS, ViewNames.LEMMA); // specify the views that you want 
TextAnnotation ta = annotator.annotate("This is the best sentence ever."); 
System.out.println(ta.getAvailableViews()); // here you should see that the required views are added  
```

#### Python Client

[CogComp-NLPy](https://github.com/CogComp/cogcomp-nlpy) is our library for accessing our pipeline from Java.   

