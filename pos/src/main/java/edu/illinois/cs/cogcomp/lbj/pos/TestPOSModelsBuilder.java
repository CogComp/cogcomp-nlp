package edu.illinois.cs.cogcomp.lbj.pos;

/**
 * Builder class for TestPOSModels class. Ideal to handle multiple parameters for constructor
 * in a clean manner.
 *
 * @author James Chen
 */
public class TestPOSModelsBuilder {

  private String modelPath;
  private String labeledTestFile;
  private String unlabeledTestFile;

  public TestPOSModelsBuilder() { }

  public TestPOSModels buildTestPOSModels() {
    return new TestPOSModels(modelPath, labeledTestFile, unlabeledTestFile);
  }

  public TestPOSModelsBuilder modelPath(String modelPath) {
    this.modelPath = modelPath;
    return this;
  }

  public TestPOSModelsBuilder labeledTestFile(String labeledTestFile) {
    this.labeledTestFile = labeledTestFile;
    return this;
  }

  public TestPOSModelsBuilder unlabeledTestFile(String unlabeledTestFile) {
    this.unlabeledTestFile = unlabeledTestFile;
    return this;
  }


}
