package org.gradle.api.plugins.clirr;

import net.sf.clirr.core.ApiDifference;
import net.sf.clirr.core.DiffListenerAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BufferedListener extends DiffListenerAdapter {

    public static final Pattern METHOD_PATTERN = Pattern.compile(
            "((public|private|protected|static|final|native|synchronized|abstract)\\s)*(.+\\s)?([\\$_\\w]+)\\((.*)\\)"
    );
    private static final Map<String, Class<?>> primitiveClasses = new HashMap<String, Class<?>>();

    static {
        primitiveClasses.put("byte", byte.class);
        primitiveClasses.put("short", short.class);
        primitiveClasses.put("char", char.class);
        primitiveClasses.put("int", int.class);
        primitiveClasses.put("long", long.class);
        primitiveClasses.put("float", float.class);
        primitiveClasses.put("boolean", boolean.class);
        primitiveClasses.put("double", double.class);
        primitiveClasses.put("byte[]", byte[].class);
        primitiveClasses.put("short[]", short[].class);
        primitiveClasses.put("char[]", char[].class);
        primitiveClasses.put("int[]", int[].class);
        primitiveClasses.put("long[]", long[].class);
        primitiveClasses.put("float[]", float[].class);
        primitiveClasses.put("double[]", double[].class);
        primitiveClasses.put("boolean[]", boolean[].class);
        // Use the wrapper variant if necessary, like Integer.class,
        // so that you can instantiate it.
    }

    private final Map<String, List<ApiDifference>> differences;
    private final List<Integer> ignoredDifferenceTypes;
    private final Boolean ignoreDeprecated;
    private final List<String> ignoredPackages;
    private final ClassLoader origClassLoader;
    private final ClassLoader newClassLoader;


    public BufferedListener(final List<Integer> ignoredDifferenceTypes,
                            final List<String> ignoredPackages,
                            final Boolean ignoreDeprecated,
                            final ClassLoader origClassLoader,
                            final ClassLoader newClassLoader) {
        this.ignoredDifferenceTypes = ignoredDifferenceTypes;
        this.ignoredPackages = ignoredPackages;
        this.ignoreDeprecated = ignoreDeprecated;
        this.differences = new HashMap<String, List<ApiDifference>>();
        this.origClassLoader = origClassLoader;
        this.newClassLoader = newClassLoader;
    }


    @Override
    public void reportDiff(final ApiDifference difference) {
        if (ignoredDifferenceTypes.contains(difference.getMessage().getId())) {
            return;
        }

        if (ignoreDeprecated && checkForDeprecations(difference)) {
            return;
        }

        final String affectedClass = difference.getAffectedClass();

        for (String pkg : ignoredPackages) {
            if (affectedClass.startsWith(pkg)) {
                return;
            }
        }


        if (!differences.containsKey(affectedClass)) {
            differences.put(affectedClass, new ArrayList<ApiDifference>());
        }
        differences.get(affectedClass).add(difference);
    }

    @SuppressWarnings("unchecked")
    private boolean checkForDeprecations(final ApiDifference difference) {
        try {
            final Class<?> cls = origClassLoader.loadClass(difference.getAffectedClass());
            if (cls.getAnnotation(Deprecated.class) != null) {
                return true;
            }


            if (difference.getAffectedMethod() != null) {
                final Matcher matcher = METHOD_PATTERN.matcher(difference.getAffectedMethod());
                if (matcher.matches()) {
                    final String stripped = matcher.group(5).replaceAll("\\s+", "");
                    final String[] types = stripped.isEmpty() ? new String[0] : stripped.split(",");
                    final Class<?>[] parameters = new Class[types.length];
                    for (int i = 0; i < types.length; i++) {
                        if (primitiveClasses.containsKey(types[i])) {
                            parameters[i] = primitiveClasses.get(types[i]);
                        } else {
                            parameters[i] = origClassLoader.loadClass(types[i]);
                        }
                    }
                    if (matcher.group(3) != null) {
                        final Method method = cls.getDeclaredMethod(matcher.group(4), parameters);
                        if (method.getAnnotation(Deprecated.class) != null) {
                            return true;
                        }
                    } else {
                        final Constructor constructor = cls.getDeclaredConstructor(parameters);
                        if (constructor.getAnnotation(Deprecated.class) != null) {
                            return true;
                        }
                    }
                }
            }

            if (difference.getAffectedField() != null) {
                final Field field = cls.getDeclaredField(difference.getAffectedField());
                if (field.getAnnotation(Deprecated.class) != null) {
                    return true;
                }
            }

        } catch (Exception e) {
            //no-op
        }
        return false;
    }

    public Map<String, List<ApiDifference>> getDifferences() {
        return differences;
    }
}
