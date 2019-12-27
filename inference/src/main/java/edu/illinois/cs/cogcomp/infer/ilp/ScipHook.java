/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import de.zib.jscip.nativ.NativeScipException;
import de.zib.jscip.nativ.jni.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a generic interface to the SCIP ILP solver providing a number of common initialization
 * steps and access to the SCIP environment. This class is NOT guaranteed to be thread-safe!
 */
public class ScipHook implements ILPSolver {

    // default parameters
    static double timeLimit = 180d;
    static int threads = 1;
    static String logFile = "scip.log";
    static boolean messagehdlrQuiet = false;
    static int printVersion = 0;

    // Min and max values to use when defining the model
    // TODO(ashish33) check how to access SCIP's built-in SCIP_REAL_MAX, etc.
    private double scipMin = -1e+20;
    private double scipMax = 1e+20;

    private final static Logger logger = LoggerFactory.getLogger(ScipHook.class);

    // the SCIP environment
    private JniScip env;

    // the SCIP variable environment
    private JniScipVar envVar;

    // SCIP set packing constraint environment
    private JniScipConsSetppc envConsSetppc;

    // the SCIP linear constraint environment
    private JniScipConsLinear envConsLinear;

    // a SCIP instance
    private Long scip;

    Map<Integer, Long> intToLongIndex;
    Map<Long, Integer> longToIntIndex;

    public ScipHook(String probName, double timeLimit, int threads, String logFile,
            boolean messagehdlrQuiet, int printVersion) {

        intToLongIndex = new HashMap<>();
        longToIntIndex = new HashMap<>();

        // initialization: load JNI library
        logger.debug("Java library path = " + System.getProperty("java.library.path"));
        JniScipLibraryLoader.loadLibrary();

        // initialization: create various handlers in the SCIP environment
        // create the SCIP environment
        env = new JniScip();

        // create the SCIP variable environment
        envVar = new JniScipVar();

        // create SCIP set packing constraint environment
        envConsSetppc = new JniScipConsSetppc();

        // create the SCIP linear constraint environment
        envConsLinear = new JniScipConsLinear();

        // initialization: create a SCIP instance
        try {
            scip = env.create();
        } catch (NativeScipException e) {
            e.printStackTrace();
        }

        // initialization: set various parameters
        try {
            env.setMessagehdlrQuiet(scip, messagehdlrQuiet);
            if (logFile.length() > 0) {
                env.setMessagehdlrLogfile(scip, logFile);
            }
            env.includeDefaultPlugins(scip); // include default plugins of SCIP
            env.setRealParam(scip, "limits/time", timeLimit); // set SCIP's overall time limit
            env.setIntParam(scip, "lp/threads", threads); // number of threads used for LP

            // initialization: create empty problem tied to the given problem name
            env.createProbBasic(scip, probName);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
    }

    public ScipHook(String probName) {
        this(probName, timeLimit, threads, logFile, messagehdlrQuiet, printVersion);
    }

    @Override
    public void setMaximize(boolean d) {
        try {
            if (d)
                env.setObjsense(scip, JniScipObjsense.SCIP_OBJSENSE_MAXIMIZE);
            else
                env.setObjsense(scip, JniScipObjsense.SCIP_OBJSENSE_MINIMIZE);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int addBooleanVariable(double c) {
        long variableId = -1;
        try {
            variableId = env.createVarBasic(scip, "", 0, 1, c, JniScipVartype.SCIP_VARTYPE_BINARY);
            env.addVar(scip, variableId);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
        int newIntIdx = intToLongIndex.size();
        intToLongIndex.put(newIntIdx, variableId);
        longToIntIndex.put(variableId, newIntIdx);


        return newIntIdx;
    }

    @Override
    public int addRealVariable(double c) {
        long variableId = -1;
        try {
            variableId =
                    env.createVarBasic(scip, "", -Double.MAX_VALUE, Double.MAX_VALUE, c,
                            JniScipVartype.SCIP_VARTYPE_CONTINUOUS);
            env.addVar(scip, variableId);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }

        int newIntIdx = intToLongIndex.size();
        intToLongIndex.put(newIntIdx, variableId);
        longToIntIndex.put(variableId, newIntIdx);

        return newIntIdx;
    }

    @Override
    public int addIntegerVariable(double c) {
        long variableId = -1;
        try {
            variableId =
                    env.createVarBasic(scip, "", -Double.MAX_VALUE, Double.MAX_VALUE, c,
                            JniScipVartype.SCIP_VARTYPE_INTEGER);
            env.addVar(scip, variableId);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }

        int newIntIdx = intToLongIndex.size();
        intToLongIndex.put(newIntIdx, variableId);
        longToIntIndex.put(variableId, newIntIdx);

        return newIntIdx;
    }

    @Override
    public int[] addDiscreteVariable(double[] c) {
        int[] indices = new int[c.length];
        for (int idx = 0; idx < c.length; idx++) {
            indices[idx] = addBooleanVariable(c[idx]);
        }
        double[] allOnesCoeffs = new double[c.length];
        Arrays.fill(allOnesCoeffs, 1.0);
        addLessThanConstraint(indices, allOnesCoeffs, 1);
        return indices;
    }

    @Override
    public void addEqualityConstraint(int[] i, double[] a, double b) {
        addReleaseCons(createConsBasicLinear("", i, a, b, b));
    }

    @Override
    public void addGreaterThanConstraint(int[] i, double[] a, double b) {
        addReleaseCons(createConsBasicLinear("", i, a, b, null));
    }

    @Override
    public void addLessThanConstraint(int[] i, double[] a, double b) {
        addReleaseCons(createConsBasicLinear("", i, a, null, b));
    }

    /** Adds a constraint to SCIP and "release" it */
    private void addReleaseCons(long cons) {
        try {
            env.addCons(scip, cons);
            env.releaseCons(scip, cons);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and captures a linear constraint in its most basic version; all constraint flags are
     * set to their basic value as explained for the method SCIPcreateConsLinear(); all flags can be
     * set via SCIPsetConsFLAGNAME methods in scip.h
     *
     * @param name name of constraint
     * @param vars seq with variables of constraint entries
     * @param coeffs seq with coefficients of constraint entries
     * @param lhs left hand side of constraint, optional
     * @param rhs right hand side of constraint, optional
     */
    private long createConsBasicLinear(String name, int[] vars, double[] coeffs, Double lhs,
            Double rhs) {
        long constraintPt = Long.MIN_VALUE;
        try {
            Double lhsCorrected = (lhs == null) ? scipMin : lhs;
            Double rhsCorrected = (rhs == null) ? scipMax : rhs;
            long[] longIndices = new long[vars.length];
            for (int i = 0; i < vars.length; i++) {
                longIndices[i] = intToLongIndex.get(vars[i]);
            }

            constraintPt =
                    envConsLinear.createConsBasicLinear(scip, name, vars.length, longIndices,
                            coeffs, lhsCorrected, rhsCorrected);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
        return constraintPt;
    }

    @Override
    public boolean solve() throws Exception {
        // although solve() could have been directly called here, first call presolve() so that
        // simplified problem stats can be stored for future reference
        env.presolve(scip);

        // now do branch-and-bound search using solve()
        env.solve(scip);

        logger.info("Solution status: " + env.getStatus(scip));
        logger.info("Objective value: " + env.getPrimalbound(scip));

        return env.getStatus(scip) == JniScipStatus.SCIP_STATUS_OPTIMAL;
    }

    @Override
    public boolean isSolved() {
        try {
            return env.getStatus(scip) >= JniScipStatus.SCIP_STATUS_BESTSOLLIMIT;
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean getBooleanValue(int index) {
        double x = getRealValue(index);
        return x > 0.5;
    }

    @Override
    public int getIntegerValue(int index) {
        double x = getRealValue(index);
        return (int) Math.round(x);
    }

    @Override
    public double getRealValue(int index) {
        try {
            return env.getSolVal(scip, getBestSol(), intToLongIndex.get(index));
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** get pointer to the best solution found */
    private Long getBestSol() throws NativeScipException {
        return env.getBestSol(scip);
    }

    @Override
    public double objectiveValue() {
        try {
            return env.getPrimalbound(scip);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public double objectiveCoeff(int index) {
        try {
            return envVar.varGetObj(index);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void reset() {
        long origScipStage = 0;
        long newScipStage = 0;
        // reset SCIP to pre-presolve stage
        try {
            origScipStage = env.getStage(scip);
            env.freeTransform(scip);
            newScipStage = env.getStage(scip);
        } catch (NativeScipException e) {
            e.printStackTrace();
        }
        logger.debug("SCIP solver stage changed from " + origScipStage + " to " + newScipStage);
    }

    @Override
    public void write(StringBuffer buffer) {

    }
}
