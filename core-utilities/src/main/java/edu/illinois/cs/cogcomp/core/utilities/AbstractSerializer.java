/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class AbstractSerializer {
    protected static View createEmptyView(TextAnnotation ta, String viewClass, String viewName,
            String viewGenerator, double score) throws NoSuchMethodException,
            ClassNotFoundException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        Object[] args = {viewName, viewGenerator, ta, score};
        @SuppressWarnings("rawtypes")
        Class[] argsClass = {String.class, String.class, TextAnnotation.class, double.class};

        @SuppressWarnings("unchecked")
        Constructor<? extends View> constructor =
                (Constructor<? extends View>) Class.forName(viewClass).getConstructor(argsClass);
        return constructor.newInstance(args);
    }

}
