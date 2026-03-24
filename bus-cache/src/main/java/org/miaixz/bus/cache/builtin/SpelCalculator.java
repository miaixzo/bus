/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cache.builtin;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StringKit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A utility class for evaluating cache expressions used in key generation and conditional caching.
 * <p>
 * Provides a pure-Java implementation that handles the expression patterns used by cache annotations ({@code @Cached},
 * {@code @CachedGet}, {@code @CacheKey}). No external expression language dependency is required.
 * </p>
 *
 * <p>
 * Supported patterns:
 * </p>
 * <ul>
 * <li>Variable reference: {@code #param}</li>
 * <li>Property navigation: {@code #param.field}, {@code #param.field.nested}</li>
 * <li>Method call: {@code #param.method()}</li>
 * <li>Null checks: {@code #param != null}, {@code #param == null}</li>
 * <li>String concatenation: {@code #a.id + '_' + #b.name}</li>
 * <li>Positional arguments: {@code #args0}, {@code #args1}</li>
 * <li>Root-object property (no-context form): {@code fieldName}, {@code method()}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpelCalculator {

    private SpelCalculator() {
    }

    /**
     * Evaluates an expression within a context populated from method arguments.
     * <p>
     * Variables are accessible by their original parameter names (e.g., {@code #userId}) and by positional synthetic
     * names (e.g., {@code #args0}). Used for evaluating {@code condition} and composite {@code key} expressions in
     * {@code @Cached} / {@code @CachedGet}.
     * </p>
     *
     * @param spel         the expression string to evaluate
     * @param argNames     parameter names declared on the intercepted method
     * @param argValues    argument values passed to the intercepted method
     * @param defaultValue value to return when {@code spel} is null or empty
     * @return the evaluation result
     */
    public static Object calcSpelValueWithContext(
            String spel,
            String[] argNames,
            Object[] argValues,
            Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }
        Assert.isTrue(
                argNames.length == argValues.length,
                "Argument names and values arrays must have the same length");

        Map<String, Object> vars = new HashMap<>(argNames.length * 2 + 1);
        for (int i = 0; i < argNames.length; i++) {
            vars.put(argNames[i], argValues[i]);
        }
        String[] xArgNames = Builder.getXArgNames(argValues.length);
        for (int i = 0; i < argValues.length; i++) {
            vars.put(xArgNames[i], argValues[i]);
        }
        return evaluate(spel.trim(), vars);
    }

    /**
     * Evaluates an expression against a root object, without a named variable context.
     * <p>
     * Used for extracting property values from a single argument via {@code @CacheKey(value = "...")}.
     * </p>
     *
     * @param spel         the expression string (e.g., {@code "id"}, {@code "user.name"})
     * @param defaultValue the root object to evaluate the expression against
     * @return the evaluation result
     */
    public static Object calcSpelWithNoContext(String spel, Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }
        return navigatePath(defaultValue, spel.trim());
    }

    // ---- internal evaluation ------------------------------------------------

    private static Object evaluate(String expr, Map<String, Object> vars) {
        List<String> parts = splitOnPlus(expr);
        if (parts.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                Object val = evalSingle(part.trim(), vars);
                sb.append(val == null ? "" : val);
            }
            return sb.toString();
        }
        return evalSingle(expr, vars);
    }

    private static Object evalSingle(String expr, Map<String, Object> vars) {
        int neqIdx = indexOfOperator(expr, "!=");
        if (neqIdx >= 0) {
            Object left = evalToken(expr.substring(0, neqIdx).trim(), vars);
            Object right = evalToken(expr.substring(neqIdx + 2).trim(), vars);
            return !Objects.equals(left, right);
        }
        int eqIdx = indexOfOperator(expr, "==");
        if (eqIdx >= 0) {
            Object left = evalToken(expr.substring(0, eqIdx).trim(), vars);
            Object right = evalToken(expr.substring(eqIdx + 2).trim(), vars);
            return Objects.equals(left, right);
        }
        return evalToken(expr, vars);
    }

    private static Object evalToken(String token, Map<String, Object> vars) {
        if ("null".equals(token)) {
            return null;
        }
        if ("true".equals(token)) {
            return Boolean.TRUE;
        }
        if ("false".equals(token)) {
            return Boolean.FALSE;
        }
        int len = token.length();
        if (len >= 2 && ((token.charAt(0) == '\'' && token.charAt(len - 1) == '\'')
                || (token.charAt(0) == '"' && token.charAt(len - 1) == '"'))) {
            return token.substring(1, len - 1);
        }
        if (token.startsWith("#")) {
            String rest = token.substring(1);
            int dot = rest.indexOf('.');
            if (dot < 0) {
                return vars.get(rest);
            }
            Object val = vars.get(rest.substring(0, dot));
            return navigatePath(val, rest.substring(dot + 1));
        }
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException ignored) {
        }
        return token;
    }

    private static Object navigatePath(Object obj, String path) {
        if (obj == null || path.isEmpty()) {
            return obj;
        }
        int dot = path.indexOf('.');
        String segment = dot < 0 ? path : path.substring(0, dot);
        String remainder = dot < 0 ? "" : path.substring(dot + 1);
        Object next = accessSegment(obj, segment);
        return remainder.isEmpty() ? next : navigatePath(next, remainder);
    }

    private static Object accessSegment(Object obj, String segment) {
        if (obj == null) {
            return null;
        }
        if (segment.endsWith("()")) {
            return invokeMethod(obj, segment.substring(0, segment.length() - 2));
        }
        Object val = getField(obj, segment);
        if (val != null) {
            return val;
        }
        String getter = "get" + Character.toUpperCase(segment.charAt(0)) + segment.substring(1);
        return invokeMethod(obj, getter);
    }

    private static Object getField(Object obj, String name) {
        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) {
            try {
                Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            } catch (IllegalAccessException e) {
                // Should not happen after setAccessible(true); log and abort.
                throw new RuntimeException("Cannot access field '" + name + "' on " + obj.getClass().getName(), e);
            }
        }
        return null;
    }

    private static Object invokeMethod(Object obj, String methodName) {
        Class<?> cls = obj.getClass();
        while (cls != null && cls != Object.class) {
            try {
                Method m = cls.getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m.invoke(obj);
            } catch (NoSuchMethodException e) {
                cls = cls.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException("Cannot invoke method '" + methodName + "' on " + obj.getClass().getName(),
                        e);
            }
        }
        return null;
    }

    /** Locate an operator string outside string literals. Returns -1 if not found. */
    private static int indexOfOperator(String expr, String op) {
        boolean inStr = false;
        char strChar = 0;
        int end = expr.length() - op.length() + 1;
        for (int i = 0; i < end; i++) {
            char c = expr.charAt(i);
            if (!inStr && (c == '\'' || c == '"')) {
                inStr = true;
                strChar = c;
            } else if (inStr && c == strChar) {
                inStr = false;
            } else if (!inStr && expr.startsWith(op, i)) {
                return i;
            }
        }
        return -1;
    }

    /** Split on {@code +} outside string literals. */
    private static List<String> splitOnPlus(String expr) {
        List<String> parts = new ArrayList<>();
        boolean inStr = false;
        char strChar = 0;
        int start = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (!inStr && (c == '\'' || c == '"')) {
                inStr = true;
                strChar = c;
            } else if (inStr && c == strChar) {
                inStr = false;
            } else if (!inStr && c == '+') {
                parts.add(expr.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(expr.substring(start));
        return parts;
    }

}
