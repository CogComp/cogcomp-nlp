package edu.illinois.cs.cogcomp.comma.lbj;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.learn.TestingMetric;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class PrintMetrics implements TestingMetric {
	int iteration = 0;
	int total_iterations;
	EvaluateDiscrete performanceRecord = new EvaluateDiscrete();


    public PrintMetrics(int total_iterations){
        super();
        this.total_iterations = total_iterations;
    }
	
	public String getName() {
		return "Accuracy. In addition to reporting accuracy, this class will also print the result of lbjava.classify.TestDiscrete";
	}
	
	@Override
	public double test(Classifier classifier, Classifier oracle, Parser parser) {
		iteration++;
		
		//Comma.setGold(false);
		EvaluateDiscrete currentPerformance = EvaluateDiscrete.evaluateDiscrete(classifier, oracle, parser);
		//Comma.setGold(true);
		
		performanceRecord.reportAll(currentPerformance);
		
		if(iteration == total_iterations){
			performanceRecord.printPerformance(System.out);
			performanceRecord.printConfusion(System.out);
		}
		
		return currentPerformance.getOverallStats()[0];
	}
}
