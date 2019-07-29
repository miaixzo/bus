package org.aoju.bus.cache.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spel表达式的计算功能(@Cached内的condition、@CacheKey内的spel只是作为一个增值服务, 并不作为核心功能, 只是作为key拼装的一个亮点, 并不是必须功能)
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SpelCalculator {

    private static final ExpressionParser parser = new SpelExpressionParser();

    public static Object calcSpelValueWithContext(String spel, String[] argNames, Object[] argValues, Object defaultValue) {
        if (Strings.isNullOrEmpty(spel)) {
            return defaultValue;
        }

        // 将[参数名->参数值]导入spel环境
        EvaluationContext context = new StandardEvaluationContext();

        Preconditions.checkState(argNames.length == argValues.length);
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
        if (Strings.isNullOrEmpty(spel)) {
            return defaultValue;
        }

        return parser.parseExpression(spel).getValue(defaultValue);
    }
}
