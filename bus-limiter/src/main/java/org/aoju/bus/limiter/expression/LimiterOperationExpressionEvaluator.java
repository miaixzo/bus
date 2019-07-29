package org.aoju.bus.limiter.expression;

import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.limiter.Limiter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class LimiterOperationExpressionEvaluator {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();


    private final Map<ExpressionKey, Expression> keyCache = new ConcurrentHashMap<>(64);


    public EvaluationContext createEvaluationContext(Limiter limiter, Method method, Object[] args, Object target, Class<?> targetClass, Method targetMethod,
                                                     Map<String, Object> injectArgs, BeanFactory beanFactory) {

        LimiterExpressionRootObject rootObject = new LimiterExpressionRootObject(limiter, method, args, target, targetClass);
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, targetMethod, args, this.parameterNameDiscoverer);
        for (String key : injectArgs.keySet()) {
            evaluationContext.setVariable(key, injectArgs.get(key));
        }

        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    public Object key(String keyExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return getExpression(methodKey, keyExpression).getValue(evalContext);
    }

    protected Expression getExpression(AnnotatedElementKey elementKey, String expression) {

        ExpressionKey expressionKey = new ExpressionKey(elementKey, expression);
        Expression expr = keyCache.get(expressionKey);
        if (expr == null) {
            expr = this.parser.parseExpression(expression);
            keyCache.put(expressionKey, expr);
        }
        return expr;
    }


    protected static class ExpressionKey implements Comparable<ExpressionKey> {

        private final AnnotatedElementKey element;

        private final String expression;

        protected ExpressionKey(AnnotatedElementKey element, String expression) {

            this.element = element;
            this.expression = expression;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ExpressionKey)) {
                return false;
            }
            ExpressionKey otherKey = (ExpressionKey) other;
            return (this.element.equals(otherKey.element) &&
                    ObjectUtils.nullSafeEquals(this.expression, otherKey.expression));
        }

        @Override
        public int hashCode() {
            return this.element.hashCode() * 29 + this.expression.hashCode();
        }

        @Override
        public String toString() {
            return this.element + " with expression \"" + this.expression + "\"";
        }

        @Override
        public int compareTo(ExpressionKey other) {
            int result = this.element.toString().compareTo(other.element.toString());
            if (result == 0) {
                result = this.expression.compareTo(other.expression);
            }
            return result;
        }
    }


}
