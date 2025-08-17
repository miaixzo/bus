/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
 * SpEL表达式计算器
 * <p>
 * 提供SpEL(Spring Expression Language)表达式的计算功能，用于解析和执行SpEL表达式。 支持带上下文和不带上下文的表达式计算，用于缓存注解中的条件判断和键值生成。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpelCalculator {

    /**
     * SpEL表达式解析器
     */
    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 计算带上下文的SpEL表达式值
     * <p>
     * 在给定的上下文中计算SpEL表达式的值，上下文包含方法参数名和参数值的映射关系。 支持原始参数名和生成的xArg格式的参数名。
     * </p>
     *
     * @param spel         SpEL表达式字符串
     * @param argNames     参数名数组
     * @param argValues    参数值数组
     * @param defaultValue 默认值，当表达式为空时返回
     * @return 表达式计算结果
     */
    public static Object calcSpelValueWithContext(String spel, String[] argNames, Object[] argValues,
            Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }

        // 将[参数名->参数值]导入spel环境
        EvaluationContext context = new StandardEvaluationContext();
        Assert.isTrue(argNames.length == argValues.length);

        // 设置原始参数名到参数值的映射
        for (int i = 0; i < argValues.length; ++i) {
            context.setVariable(argNames[i], argValues[i]);
        }

        // 设置生成的xArg格式参数名到参数值的映射
        String[] xArgNames = Builder.getXArgNames(argValues.length);
        for (int i = 0; i < argValues.length; ++i) {
            context.setVariable(xArgNames[i], argValues[i]);
        }

        return parser.parseExpression(spel).getValue(context);
    }

    /**
     * 计算不带上下文的SpEL表达式值
     * <p>
     * 在给定的默认对象上下文中计算SpEL表达式的值，主要用于处理对象属性访问。
     * </p>
     *
     * @param spel         SpEL表达式字符串
     * @param defaultValue 默认值，当表达式为空时返回
     * @return 表达式计算结果
     */
    public static Object calcSpelWithNoContext(String spel, Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }
        return parser.parseExpression(spel).getValue(defaultValue);
    }

}