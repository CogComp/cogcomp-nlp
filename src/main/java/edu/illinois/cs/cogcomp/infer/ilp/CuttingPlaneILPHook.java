package edu.illinois.cs.cogcomp.infer.ilp;

import gurobi.GRB;
import gurobi.GRBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
@Deprecated
public class CuttingPlaneILPHook implements ILPSolver {

  private static final Logger logger = LoggerFactory
          .getLogger(CuttingPlaneILPHook.class);

  private static final int MAX_ITER = 100;
  private final ILPSolver baseSolver;
  // private Set<Constraint> constrs;

  private Set<List<ILPConstraint>> constrs;
  boolean isSolved = false;
  boolean hasUpdate = false;
  boolean timedOut = false;

  int numVariables = 0;

  public CuttingPlaneILPHook(ILPSolver baseSolver) {
    this.baseSolver = baseSolver;
    // constrs = new HashSet<Constraint>();
    constrs = new HashSet<List<ILPConstraint>>();
    hasUpdate = false;
    isSolved = false;
  }

  public int addBooleanVariable(double arg0) {
    isSolved = false;
    hasUpdate = true;

    numVariables++;

    return baseSolver.addBooleanVariable(arg0);

  }

  public int addIntegerVariable(double arg0) {
    isSolved = false;
    hasUpdate = true;

    numVariables++;

    return baseSolver.addIntegerVariable(arg0);

  }


  public int addRealVariable(double arg0) {
    isSolved = false;
    hasUpdate = true;

    numVariables++;

    return baseSolver.addRealVariable(arg0);

  }

  public int[] addDiscreteVariable(double[] arg0) {
    isSolved = false;
    hasUpdate = true;

    numVariables += arg0.length;

    return baseSolver.addDiscreteVariable(arg0);
  }

  // private void addCuttinPlaneConstraint(int[] ids, double[] coefs,
  // double rhs, char sense) {
  // constrs.add(new Constraint(ids, coefs, rhs, sense));
  // isSolved = false;
  // }

  public void addEqualityConstraint(int[] arg0, double[] arg1, double arg2) {
    this.baseSolver.addEqualityConstraint(arg0, arg1, arg2);
    isSolved = false;
  }

  public void addGreaterThanConstraint(int[] arg0, double[] arg1, double arg2) {
    // addConstraint(arg0, arg1, arg2, GRB.GREATER_EQUAL);
    this.baseSolver.addGreaterThanConstraint(arg0, arg1, arg2);
    isSolved = false;
  }

  public void addLessThanConstraint(int[] arg0, double[] arg1, double arg2) {
    // addConstraint(arg0, arg1, arg2, GRB.LESS_EQUAL);
    this.baseSolver.addLessThanConstraint(arg0, arg1, arg2);
    isSolved = false;
  }

  public void addCuttingPlaneConstraintCollection(
          List<ILPConstraint> constraints) {
    isSolved = false;
    this.constrs.add(constraints);
  }

  // public void addCuttingPlaneEqualityConstraint(int[] arg0, double[] arg1,
  // double arg2) {
  // addCuttinPlaneConstraint(arg0, arg1, arg2, GRB.EQUAL);
  // }
  //
  // public void addCuttingPlaneGreaterThanConstraint(int[] arg0, double[]
  // arg1,
  // double arg2) {
  // addCuttinPlaneConstraint(arg0, arg1, arg2, GRB.GREATER_EQUAL);
  // }
  //
  // public void addCuttingPlaneLessThanConstraint(int[] arg0, double[] arg1,
  // double arg2) {
  // addCuttinPlaneConstraint(arg0, arg1, arg2, GRB.LESS_EQUAL);
  // }

  public boolean getBooleanValue(int arg0) {
    return baseSolver.getBooleanValue(arg0);
  }

  public double getRealValue(int arg0) {
    return baseSolver.getRealValue(arg0);
  }

  public int getIntegerValue(int arg0) {
    return baseSolver.getIntegerValue(arg0);
  }

  public boolean isSolved() {
    return isSolved;
  }

  public double objectiveValue() {
    return baseSolver.objectiveValue();
  }

  public void reset() {

    baseSolver.reset();
    this.constrs.clear();
    this.isSolved = false;
    this.hasUpdate = false;
    this.timedOut = false;
  }

  public void setMaximize(boolean arg0) {
    baseSolver.setMaximize(arg0);
  }

  public boolean isTimedOut() {
    return timedOut;
  }

  public boolean solve() throws Exception {

    assert numVariables > 0 : "No variables added!";

    long start = System.currentTimeMillis();
    int iteration = 0;
    int numConstraints = 0;

    logger.debug("Number of constraints to add: {}", this.constrs.size());

    boolean solved = baseSolver.solve();

    isSolved = false;

    if (baseSolver instanceof GurobiHook) {
      GurobiHook s = (GurobiHook) baseSolver;
      timedOut = s.isTimedOut();
      // s.noPresolveTimelimit = 0;
    }

    if (!solved) {
      isSolved = false;

    } else {

      List<List<ILPConstraint>> violated = new ArrayList<List<ILPConstraint>>();
      getViolatedConstraints(violated);

      boolean constriantsSatisfied = violated.size() == 0;

      while (!isSolved && !constriantsSatisfied && iteration < MAX_ITER) {
        for (List<ILPConstraint> cList : violated) {
          for (ILPConstraint c : cList) {
            addConstraint(c);
            numConstraints++;
          }
        }

        logger.debug("Added {} constraints", violated.size());

        solved = baseSolver.solve();

        violated.clear();
        getViolatedConstraints(violated);

        constriantsSatisfied = violated.size() == 0;

        if (!solved) {
          isSolved = false;
          if (baseSolver instanceof GurobiHook) {
            GurobiHook s = (GurobiHook) baseSolver;
            timedOut = s.isTimedOut();

            System.out.println("Status from gurobi model: ");
            s.printModelStatus();

            if (timedOut) {
              logger.error("Timed out!");
            }

            if (s.unsat()) {
              logger.error("Unsat!");
              throw new RuntimeException("Unsat");
            }
          }

          break;
        }

        iteration++;
      }

      if (!constriantsSatisfied)
        isSolved = false;
      else
        isSolved = true;
    }

    long end = System.currentTimeMillis();

    logger.debug("Took {} ms and {} iterations. Added " + numConstraints
            + " constraints", (end - start), iteration);

    return isSolved;

  }

  public void printModelStatus() throws GRBException {
    if (baseSolver instanceof GurobiHook) {
      ((GurobiHook) baseSolver).printModelStatus();
    }
  }

  private void addConstraint(ILPConstraint c) {
    if (c.sense == GRB.EQUAL)
      baseSolver.addEqualityConstraint(c.vars, c.coeffs, c.rhs);
    else if (c.sense == GRB.GREATER_EQUAL)
      baseSolver.addGreaterThanConstraint(c.vars, c.coeffs, c.rhs);
    else if (c.sense == GRB.LESS_EQUAL)
      baseSolver.addLessThanConstraint(c.vars, c.coeffs, c.rhs);
    else
      assert false : c.sense;
  }

  private void getViolatedConstraints(List<List<ILPConstraint>> violated) {

    violated.clear();
    for (List<ILPConstraint> cList : this.constrs) {
      boolean invalid = false;
      for (ILPConstraint c : cList) {

        double lhs = 0;
        int index = 0;
        for (int id : c.vars) {
          if (baseSolver.getBooleanValue(id))
            lhs += c.coeffs[index];
          index++;
        }

        if (c.sense == GRB.EQUAL && lhs != c.rhs)
          invalid = true;
        else if (c.sense == GRB.LESS_EQUAL && lhs > c.rhs)
          invalid = true;
        else if (c.sense == GRB.GREATER_EQUAL && lhs < c.rhs)
          invalid = true;

        if (invalid)
          break;
      }

      if (invalid) {
        violated.add(cList);
        // System.out.println(c + "\t" + lhs + "\t" + c.rhs);
      }

    }

    for (List<ILPConstraint> c : violated)
      this.constrs.remove(c);
  }

  public void write(StringBuffer arg0) {
    baseSolver.write(arg0);
  }

}
