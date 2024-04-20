/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.cache.support;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.StringKit;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spel表达式的计算功能(@Cached内的condition、@CacheKey内的spel只是作为一个增值服务, 并不作为核心功能, 只是作为key拼装的一个亮点, 并不是必须功能)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpelCalculator {

    private static final ExpressionParser parser = new SpelExpressionParser();

    public static Object calcSpelValueWithContext(String spel, String[] argNames, Object[] argValues, Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }

        // 将[参数名->参数值]导入spel环境
        EvaluationContext context = new StandardEvaluationContext();

        Assert.isTrue(argNames.length == argValues.length);
        for (int i = 0; i < argValues.length; ++i) {
            context.setVariable(argNames[i], argValues[i]);
        }

        // todo: 先将xArg放到这儿, 后面可以再想下可以放到哪儿?
        String[] xArgNames = ArgNameGenerator.getXArgNames(argValues.length);
        for (int i = 0; i < argValues.length; ++i) {
            context.setVariable(xArgNames[i], argValues[i]);
        }

        return parser.parseExpression(spel).getValue(context);
    }

    public static Object calcSpelWithNoContext(String spel, Object defaultValue) {
        if (StringKit.isEmpty(spel)) {
            return defaultValue;
        }

        return parser.parseExpression(spel).getValue(defaultValue);
    }

}
