/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities.commands;

import edu.illinois.cs.cogcomp.core.algorithms.LevensteinDistance;
import edu.illinois.cs.cogcomp.core.math.ArgMax;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * This class provides an easy way provide command line interface to the program. It exposes all the
 * static methods of a class (that is the template parameter) to the command line interface.
 * <p>
 * Additionally, it also prints documentation if necessary.
 * <p>
 * Usage: Create a class called AllCommands
 * <p>
 * 
 * <pre>
 * class AllCommands {
 * <code>
 *   &#064;CommandDescription(description = &quot;Does foo to bar&quot;);
 *      public static Foo(String bar) {
 *          ...
 *      }
 *  }
 *  </code>
 * </pre>
 * <p>
 * Create the main function somewhere
 * 
 * <pre>
 * public static void main(String[] args) throws Exception {
 *     InteractiveShell&lt;AllCommands&gt; tester = new InteractiveShell&lt;AllCommands&gt;(AllCommands.class);
 * 
 *     if (args.length == 0)
 *         tester.showDocumentation();
 *     else
 *         tester.runCommand(args);
 * }
 * </pre>
 * <p>
 * <p>
 * Now, command line options can be added to the program by adding static functions to AllCommands.
 *
 * @param <T> The class that contains all the commands.
 * @author vivek
 */
public class InteractiveShell<T> {
    Class<T> type;

    /**
     * Create a new interactive shell for type T
     *
     * @param className The class of type T
     */
    public InteractiveShell(Class<T> className) {
        type = className;
    }

    /**
     * Displays the available static commands along with their documentation. The documentation is
     * drawn from the function's CommandDescription.
     */
    public void showDocumentation() {

        System.out.println("\nAvailable Commands:\n\n");
        int i = 1;
        for (Method elem : type.getMethods()) {
            if (Modifier.isStatic(elem.getModifiers())) {
                if (elem.isAnnotationPresent(CommandIgnore.class))
                    continue;
                printMethodSignature(i, elem);

                i++;
            }
        }
    }

    static String[] splitBuffer(String input, int maxLength) {
        int elements = (input.length() + maxLength - 1) / maxLength;
        String[] ret = new String[elements];
        int start = 0;
        for (int i = 0; i < elements; i++) {

            int end = Math.min(start + maxLength, input.length());

            while (end > start && end < input.length()) {

                if (input.charAt(end - 1) == ' ' || input.charAt(end - 1) == '\t')
                    break;
                end--;
            }

            boolean hyphen = false;
            if (end == start) {
                end = start + maxLength - 1;
                hyphen = true;
            }

            end = Math.min(end, input.length());

            ret[i] = input.substring(start, end);
            if (hyphen)
                ret[i] += "-";

            ret[i] = ret[i].trim();

            start = end;
        }
        return ret;
    }

    /**
     * Runs a command.
     * <p>
     * The command is specified by the first element of the argument and its parameters are the rest
     * of the elements.
     * <p>
     * Note: This demands that all the parameters should be strings.
     *
     * @param args An array of strings. The name of the command should be the first element and its
     *        parameters should follow.
     */
    public void runCommand(String[] args) throws Exception {
        if (args.length == 0) {
            this.showDocumentation();
            return;
        }

        Object[] ss = new String[args.length - 1];
        System.arraycopy(args, 1, ss, 0, args.length - 1);

        boolean foundMethod = false;
        boolean incorrectParams = false;

        Method[] mList = type.getMethods();
        for (Method m : mList) {
            if (Modifier.isStatic(m.getModifiers())) {
                if (m.getName().equals(args[0])) {
                    foundMethod = true;

                    if (ss.length != m.getParameterTypes().length) {
                        incorrectParams = true;
                    } else {
                        incorrectParams = false;
                        Object o = m.invoke(null, ss);
                        if (o != null) {
                            System.out.println(o.toString());
                        }
                    }
                }
                if (foundMethod && !incorrectParams)
                    break;
            }
        }

        if (!foundMethod) {
            System.out.println("Unable to find " + args[0]);
            this.showDocumentation();

            String nearest = getNearestCommand(mList, args[0]);
            System.out.println("Did you mean: " + nearest);
        } else if (incorrectParams) {
            System.out.println("Invalid usage of " + args[0]);

            int i = 1;
            for (Method elem : type.getMethods()) {
                if (Modifier.isStatic(elem.getModifiers())) {
                    if (elem.isAnnotationPresent(CommandIgnore.class))
                        continue;
                    if (elem.getName().equals(args[0])) {

                        printMethodSignature(i, elem);
                        i++;
                    }
                }
            }

        }
    }

    private void printMethodSignature(int i, Method elem) {
        System.out.print(i + ". " + elem.getName() + " ");
        System.out.println(" [" + elem.getParameterTypes().length + " parameters]");

        if (elem.isAnnotationPresent(CommandDescription.class)) {
            CommandDescription annotation = elem.getAnnotation(CommandDescription.class);

            String usage = annotation.usage();

            System.out.println("   " + usage);

            String description = annotation.description();

            for (String s : splitBuffer(description, 75)) {
                System.out.println("     " + s);
            }
        } else {
            System.out.println("No documentation available");
        }

        System.out.println();
    }

    /**
     * Gets the closest command to a the second parameter. Closeness is defined by Levnstein
     * Distance.
     */
    private String getNearestCommand(Method[] list, String string) {
        ArgMax<String, Integer> closest = new ArgMax<>("", Integer.MIN_VALUE);

        for (Method m : list) {
            if (Modifier.isStatic(m.getModifiers())) {
                if (m.isAnnotationPresent(CommandIgnore.class))
                    continue;

                closest.update(m.getName(),
                        -LevensteinDistance.getLevensteinDistance(m.getName(), string));
            }
        }

        return closest.getArgmax();
    }
}
