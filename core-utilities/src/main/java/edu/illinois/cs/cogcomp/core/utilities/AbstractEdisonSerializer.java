package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class AbstractEdisonSerializer {
  protected static View createEmptyView(TextAnnotation ta, String viewClass,
                                        String viewName, String viewGenerator, double score)
      throws NoSuchMethodException, ClassNotFoundException,
      InstantiationException, IllegalAccessException,
      InvocationTargetException {
    Object[] args = { viewName, viewGenerator, ta, score };
    @SuppressWarnings("rawtypes")
    Class[] argsClass = { String.class, String.class, TextAnnotation.class,
        double.class };

    @SuppressWarnings("unchecked")
    Constructor<? extends View> constructor = (Constructor<? extends View>) Class
        .forName(viewClass).getConstructor(argsClass);
    return constructor.newInstance(args);
  }

}