/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import gurobi.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class GurobiTest {

    @Test
    public void testGurobi() throws GRBException {
        if(System.getenv().containsKey("CI") && System.getenv().get("CI").equals("true") &&
                System.getenv().containsKey("SEMAPHORE") && System.getenv().get("SEMAPHORE").equals("true")
                && System.getenv().containsKey("CIRCLECI") && System.getenv().get("CIRCLECI").equals("true")) {
            System.out.println("Running the test on Semaphore. Skipping this test  . . . ");
        }
        else {
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

                assertEquals("x", x.get(GRB.StringAttr.VarName));
                assertEquals(1.0, x.get(GRB.DoubleAttr.X), 0.0);
                assertEquals(0.0, y.get(GRB.DoubleAttr.X), 0.0);
                assertEquals(1.0, z.get(GRB.DoubleAttr.X), 0.0);
                assertEquals(-3.0, model.get(GRB.DoubleAttr.ObjVal), 0.0);
            } catch (UnsatisfiedLinkError e) {
                System.out.println("\n\n**** GUROBI LICENSE NOT FOUND! SKIPPING THE TEST ****\n\n");
            }
        }
    }
}
