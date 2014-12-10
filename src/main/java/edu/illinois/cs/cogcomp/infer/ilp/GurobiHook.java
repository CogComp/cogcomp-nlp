package edu.illinois.cs.cogcomp.infer.ilp;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GurobiHook implements ILPSolver {

    private static int timeoutLimit = 200;

    public static void setTimeoutLimit(int limit)  {
        timeoutLimit = limit;
    }
                                                 
    
	private static final Logger logger = LoggerFactory
			.getLogger(GurobiHook.class);

	private static GRBEnv ENV;
	static {
		try {
			ENV = new GRBEnv();
			ENV.set(GRB.IntParam.OutputFlag, 0); // no output

			// how many threads can we use?
			ENV.set(GRB.IntParam.Threads,
					Math.min(8, Runtime.getRuntime().availableProcessors()));

			// dump big stuff to filespace
			ENV.set(GRB.DoubleParam.NodefileStart, 0.5);

			ENV.set(GRB.DoubleParam.MIPGap, 1e-10);

			ENV.set(GRB.IntParam.Presolve, 0);

		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

        private final static int INTEGER = 0;
        private final static int BOOLEAN = 1;
        private final static int REAL = 2;
            

	private static class VariableInfo {
		public double coeff;
		public int variableType;

		public VariableInfo(double c, int type) {
			this.coeff = c;
			this.variableType = type;
		}
	}

	private GRBModel model;
	private GRBEnv env;
	private List<VariableInfo> obj;
	private List<ILPConstraint> constrs;
	private GRBVar[] vars;
	private boolean hasUpdate;
	private boolean isSolved;
	protected int noPresolveTimelimit;
	private int timelimit;
	private boolean isTimedOut;
	private boolean unsat;

	private boolean maximize;

	public GurobiHook() {
		try {
			// when solving without presolve how long should we take before
			// timeout
			noPresolveTimelimit = 30;
			// when solving with presolve how long should we take before timeout
			timelimit = timeoutLimit;
			model = new GRBModel(ENV);
			env = model.getEnv();
			obj = new ArrayList<VariableInfo>();

			constrs = new ArrayList<ILPConstraint>();
			hasUpdate = false;
			isSolved = false;
		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public int addBooleanVariable(double arg0) {
		int id = obj.size();
		obj.add(new VariableInfo(arg0, BOOLEAN));
		hasUpdate = true;
		isSolved = false;
		return id;
	}

    	public int addIntegerVariable(double arg0) {
		int id = obj.size();
		obj.add(new VariableInfo(arg0, INTEGER));
		hasUpdate = true;
		isSolved = false;
		return id;
	}


	public int addRealVariable(double arg0) {
		int id = obj.size();
		obj.add(new VariableInfo(arg0, REAL));
		hasUpdate = true;
		isSolved = false;
		return id;
	}

	public int[] addDiscreteVariable(double[] arg0) {
		int[] ids = new int[arg0.length];
		double[] objs = new double[arg0.length];

		for (int i = 0; i < ids.length; i++) {
			ids[i] = addBooleanVariable(arg0[i]);
			objs[i] = 1;
		}
		addEqualityConstraint(ids, objs, 1);
		return ids;
	}

	private GRBVar[] idsToVars(int[] ids) {
		GRBVar[] result = new GRBVar[ids.length];
		for (int i = 0; i < ids.length; i++) {
			result[i] = vars[ids[i]];
		}
		return result;
	}

	private GRBLinExpr makeLinExpr(int[] ids, double[] coefs) {
		try {
			GRBLinExpr expr = new GRBLinExpr();
			expr.addTerms(coefs, idsToVars(ids));
			isSolved = false;
			return expr;
		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private void addConstraint(int[] ids, double[] coefs, double rhs, char sense) {

		constrs.add(new ILPConstraint(ids, coefs, rhs, sense));
		isSolved = false;
	}

	public void addEqualityConstraint(int[] arg0, double[] arg1, double arg2) {
		addConstraint(arg0, arg1, arg2, ILPConstraint.EQUAL);
	}

	public void addGreaterThanConstraint(int[] arg0, double[] arg1, double arg2) {
		addConstraint(arg0, arg1, arg2, ILPConstraint.GREATER_THAN);
	}

	public void addLessThanConstraint(int[] arg0, double[] arg1, double arg2) {
		addConstraint(arg0, arg1, arg2, ILPConstraint.LESS_THAN);
	}

	public void printSolution() throws GRBException {
		for (int i = 0; i < vars.length; i++) {
			GRBVar var = vars[i];
			System.out.println("x_" + i + "\t" + var.get(StringAttr.VarName)
					+ "\t" + var.get(DoubleAttr.Obj) + "\t"
					+ var.get(DoubleAttr.X));
		}
	}

	public boolean getBooleanValue(int arg0) {
		try {
			if (!isSolved)
				return false;
			double x = vars[arg0].get(GRB.DoubleAttr.X);

			return x > 0.5;
		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}

	public double getRealValue(int arg0) {
		try {
			if (!isSolved)
				return Double.NaN;
			double x = vars[arg0].get(GRB.DoubleAttr.X);

			return x;
		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return Double.NaN;
	}

    
	public int getIntegerValue(int arg0) {
		try {
			if (!isSolved)
                            return Integer.MIN_VALUE;
			int x = (int) (vars[arg0].get(GRB.DoubleAttr.X));

			return x;
		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(-1);
		}
                return Integer.MIN_VALUE;
	}


	public boolean isSolved() {
		return isSolved;
	}

	public void reset() {
		try {
			model.reset();
			obj.clear();
			constrs.clear();
			hasUpdate = false;
			isSolved = false;
			this.noPresolveTimelimit = 100;
		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public void setMaximize(boolean arg0) {
		this.maximize = arg0;
		try {
			if (arg0) {
				model.set(GRB.IntAttr.ModelSense, -1);
			} else {
				model.set(GRB.IntAttr.ModelSense, +1);
			}
		} catch (GRBException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public boolean solve() throws Exception {

		long start = System.currentTimeMillis();

		// make the arrays to create the objective function and vars
		if (obj.size() > 0) {
			double[] coef = new double[obj.size()];
			double[] lb = new double[obj.size()];
			double[] ub = new double[obj.size()];
			char[] type = new char[obj.size()];
			String[] names = new String[obj.size()];
			for (int i = 0; i < obj.size(); i++) {
				VariableInfo info = obj.get(i);
				coef[i] = info.coeff;

				if (info.variableType == BOOLEAN) {
					type[i] = GRB.BINARY;
                                        lb[i] = 0.0;
                                        ub[i] = 1.0;
                                }
				else if(info.variableType == REAL) {
					type[i] = GRB.CONTINUOUS;
                                        lb[i] = -GRB.INFINITY;
                                        ub[i] = GRB.INFINITY;
                                }
                                else {
                                    lb[i] = -GRB.INFINITY;
                                    ub[i] = GRB.INFINITY;                                    
                                    type[i] = GRB.INTEGER;
                                }
				names[i] = "x" + i;
			}
			hasUpdate = true;
			vars = model.addVars(lb, ub, coef, type, names);
			obj.clear(); // clear temp objective rep

		}

		// force model update if new vars were added
		if (hasUpdate) {
			model.update();
			hasUpdate = false;
		}

		assert model.getVars().length > 0 : "Model has no variables!";

		// add constraints
		for (ILPConstraint c : constrs) {
			model.addConstr(makeLinExpr(c.vars, c.coeffs), c.sense, c.rhs,
					c.name);
		}
		// clear temporary constraint rep
		constrs.clear();

		long end = System.currentTimeMillis();
		logger.debug("Took {} ms to update and set the constraints",
				(end - start));

		// here we first attempt to presolve (some models can be solved quicker
		// directly than the time it takes to presolve them)

		isTimedOut = false;
		boolean more = true; // should we perform more solving?
		start = System.currentTimeMillis();
		// XXX: if you always see your solve time > noPresolveTimelimit then you
		// probably want to set noPresolveTimelimit to 0 as you are always
		// getting into the second solving loop.
		if (noPresolveTimelimit > 0) {
			env.set(GRB.IntParam.Presolve, 0);
			env.set(GRB.DoubleParam.TimeLimit, noPresolveTimelimit);
			model.optimize();
			more = model.get(GRB.IntAttr.Status) == GRB.TIME_LIMIT;
		}

		// we may have timed out because noPresolveTimelimit is usually small
		// we can solve big hard models quicker with presolve
		if (more) {
			logger.debug("Trying presolve version of problem");
			env.set(GRB.IntParam.Presolve, -1); // auto presolve settings
			env.set(GRB.DoubleParam.TimeLimit, timelimit);
			model.optimize();
		}

		boolean result = model.get(GRB.IntAttr.Status) == GRB.OPTIMAL;

		end = System.currentTimeMillis();
		logger.debug("Gurobi took {} ms and reported {} s", (end - start),
				model.get(GRB.DoubleAttr.Runtime));

		unsat = false;
		if (!result) {
			int status = model.get(GRB.IntAttr.Status);
			logger.info("Gurobi returned with status code: {}", status);

			if (status == GRB.TIME_LIMIT) {
				isTimedOut = true;
			}

			if (status == GRB.INFEASIBLE) {
				logger.info("Infeasable constraint set!");

				model.computeIIS();
				model.write("gurobi.ilp");

				model.write("gurobi.lp");

				logger.info("ILP information written to gurobi.ilp. Full LP written to gurobi.lp");
				result = false;
				unsat = true;

			}
		}

		isSolved = result;
		return result;
	}

	public void printModelStatus() throws GRBException {
		int status = model.get(GRB.IntAttr.Status);
		System.out.println("Model status: " + status);

		if (status == GRB.INFEASIBLE) {
			System.out.println("INFEASIBLE");
		}

	}

	public boolean isTimedOut() {
		return isTimedOut;
	}

	public void write(StringBuffer buffer) {

		if (maximize)
			buffer.append("max");
		else
			buffer.append("min");

		int variables = this.obj.size();
		for (int i = 0; i < variables; ++i) {
			double c = obj.get(i).coeff;
			buffer.append(" ");
			if (c >= 0)
				buffer.append("+");
			buffer.append(c);
			buffer.append(" x_");
			buffer.append(i);
		}
		buffer.append("\n");
		buffer.append("Binary variables");
		for (int i = 0; i < variables; ++i) {
			if (obj.get(i).variableType == BOOLEAN) {
				buffer.append("x_" + i + " ");
			}
		}
		buffer.append("\n");

		buffer.append("Integer variables");
		for (int i = 0; i < variables; ++i) {
			if (obj.get(i).variableType == INTEGER) {
				buffer.append("x_" + i + " ");
			}
		}
		buffer.append("\n");

		buffer.append("Real variables");
		for (int i = 0; i < variables; ++i) {
			if (obj.get(i).variableType == REAL) {
				buffer.append("x_" + i + " ");
			}
		}
		buffer.append("\n");





		for (ILPConstraint c : this.constrs) {
			buffer.append(c.toString() + "\n");
		}
	}

	public double objectiveValue() {
		if (this.isSolved()) {
			try {
				return model.get(GRB.DoubleAttr.ObjVal);
			} catch (GRBException e) {
				throw new RuntimeException(e);
			}
		}
		throw new RuntimeException("ILP not solved! Cannot get objective yet.");
	}

	public static void main(String[] args) {
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);

			// Create variables

			GRBVar x = model.addVar(0.0, 1.0, -1.0, GRB.BINARY, "x");
			GRBVar y = model.addVar(0.0, 1.0, -1.0, GRB.BINARY, "y");
			GRBVar z = model.addVar(0.0, 1.0, -2.0, GRB.BINARY, "z");

			// Integrate new variables

			model.update();

			// Add constraint: x + 2 y + 3 z <= 4

			GRBLinExpr expr = new GRBLinExpr();
			expr.addTerm(1.0, x);
			expr.addTerm(2.0, y);
			expr.addTerm(3, z);
			model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");

			// Add constraint: x + y >= 1

			expr = new GRBLinExpr();
			expr.addTerm(1.0, x);
			expr.addTerm(1.0, y);
			model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "c1");

			// Optimize model

			model.optimize();

			System.out.println(x.get(GRB.StringAttr.VarName) + " "
					+ x.get(GRB.DoubleAttr.X));
			System.out.println(y.get(GRB.StringAttr.VarName) + " "
					+ y.get(GRB.DoubleAttr.X));
			System.out.println(z.get(GRB.StringAttr.VarName) + " "
					+ z.get(GRB.DoubleAttr.X));

			System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". "
					+ e.getMessage());
		}
	}

	public boolean unsat() {
		return this.unsat;
	}

}
