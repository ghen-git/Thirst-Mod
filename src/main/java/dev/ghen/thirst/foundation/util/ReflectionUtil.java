package dev.ghen.thirst.foundation.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil
{
    /**
     * HAHA fuck you, reflections.
     * */
    public static Object fuckYouReflections(Method method, Object obj, Object... args)
    {
        try
        {
            return method.invoke(obj, args);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}
