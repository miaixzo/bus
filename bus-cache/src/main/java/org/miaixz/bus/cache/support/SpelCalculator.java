/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cache.support;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StringKit;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * A utility class for calculating Spring Expression Language (SpEL) expressions.
 * <p>
 * This class provides methods to parse and execute SpEL expressions, which are used for conditional caching and dynamic
 * key generation within the cache annotations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpelCalculator {

    /**
     * The shared SpEL expression parser instance.
     */
    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Calculates the value of a SpEL expression within a given evaluation context.
     * <p>
     * The context is populated with the method's argument names and their corresponding values. It supports both the
     * original parameter names (if available) and synthetic names (e.g., `args0`, `args1`).
     * </p>
     *
     * @param spel         The SpEL expression string to evaluate.
     * @param argNames     An array of the method's parameter names.
     * @param argValues    An array of the method's argument values.
     * @param defaultValue The value to return if the SpEL expression is null or empty.
     * @return The result of the expression evaluation.
     */
    public static Object calcSpelValueWithContext(
            String spel,
            String[] argNames,
            Object[] argValues,
            Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }

        // Create the evaluation context and populate it with argument names and values.
        EvaluationContext context = new StandardEvaluationContext();
        Assert.isTrue(
                argNames.length == argValues.length,
                "Argument names and values arrays must have the same length");

        // Map original argument names to their values.
        for (int i = 0; i < argValues.length; ++i) {
            context.setVariable(argNames[i], argValues[i]);
        }

        // Also map synthetic argument names (e.g., "args0", "args1") to their values.
        String[] xArgNames = Builder.getXArgNames(argValues.length);
        for (int i = 0; i < argValues.length; ++i) {
            context.setVariable(xArgNames[i], argValues[i]);
        }

        return parser.parseExpression(spel).getValue(context);
    }

    /**
     * Calculates the value of a SpEL expression against a root object, without a full context.
     * <p>
     * This is primarily used for accessing properties of a given object.
     * </p>
     *
     * @param spel         The SpEL expression string to evaluate.
     * @param defaultValue The object to use as the root for the expression evaluation.
     * @return The result of the expression evaluation.
     */
    public static Object calcSpelWithNoContext(String spel, Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }
        return parser.parseExpression(spel).getValue(defaultValue);
    }

}
