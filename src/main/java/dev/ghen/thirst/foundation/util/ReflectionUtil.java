package dev.ghen.thirst.foundation.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil
{
    public static Object MethodReflection(Method method, Object obj, Object... args)
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
