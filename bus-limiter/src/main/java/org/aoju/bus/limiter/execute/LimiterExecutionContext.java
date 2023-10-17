/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.limiter.execute;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.limiter.Injector;
import org.aoju.bus.limiter.expression.LimiterOperationExpressionEvaluator;
import org.aoju.bus.limiter.metadata.LimitedResourceMetadata;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * limiter 上下文信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LimiterExecutionContext {

    private static final HashMap<String, Object> emptyMap = new HashMap<>();

    private LimitedResourceMetadata metadata;

    private Object[] args;

    private Object target;

    private Map<String, Object> injectArgs;

    private LimiterOperationExpressionEvaluator evaluator;

    private BeanFactory beanFactory;

    private Object key;

    private Object fallbackResult;

    private Throwable throwable;

    public LimiterExecutionContext(LimitedResourceMetadata metadata, Object[] args, Object target, BeanFactory beanFactory) {
        this.metadata = metadata;
        this.args = extractArgs(metadata.getTargetMethod(), args);
        this.target = target;
        this.injectArgs = generateInjectArgs();
        this.beanFactory = beanFactory;
        this.evaluator = new LimiterOperationExpressionEvaluator();
        this.key = generateKey();
    }

    public static HashMap<String, Object> getEmptyMap() {
        return emptyMap;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean limit() {
        boolean ret;
        try {
            ret = this.metadata.getLimiter().limit(this.key, this.metadata.getLimiterParameters());
        } catch (Throwable throwable) {
            this.throwable = throwable;
            ret = this.metadata.getErrorHandler().resolve(throwable, this);
        }

        if (!ret) {
            this.fallbackResult = this.metadata.getFallback().resolve(this.metadata.getTargetMethod(), this.metadata.getTargetClass(), this.args, this.metadata.getLimitedResource(), this.target);
        }
        return ret;
    }

    public void release() {
        this.metadata.getLimiter().release(this.key, this.metadata.getLimiterParameters());
    }

    public Object getFallbackResult() {
        return fallbackResult;
    }

    private Object[] extractArgs(Method method, Object[] args) {
        if (!method.isVarArgs()) {
            return args;
        } else {
            Object[] varArgs = ObjectKit.toObjectArray(args[args.length - 1]);
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
            System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
            return combinedArgs;
        }
    }

    private Object generateKey() {
        if (StringKit.hasText(this.metadata.getLimitedResource().getKey())) {
            EvaluationContext evaluationContext = evaluator.createEvaluationContext(this.metadata.getLimiter(), this.metadata.getTargetMethod(), this.args,
                    this.target, this.metadata.getTargetClass(), this.metadata.getTargetMethod(), injectArgs, beanFactory);
            Object evalKey = evaluator.key(this.metadata.getLimitedResource().getKey(), new AnnotatedElementKey(this.metadata.getTargetMethod(), this.metadata.getTargetClass()), evaluationContext);
            Assert.notNull(evalKey, "key值计算为null!");
            return evalKey;
        }
        return this.metadata.getTargetClass().getName() + Symbol.SHAPE +
                this.metadata.getTargetMethod().getName();

    }

    private Map<String, Object> generateInjectArgs() {

        if (CollKit.isEmpty(this.metadata.getArgumentInjectors())) {
            return emptyMap;
        }
        Map<String, Object> retVal = new HashMap<>();
        Collection<Injector> argumentInjectors = this.metadata.getArgumentInjectors();
        for (Injector argumentInjector : argumentInjectors) {
            Map<String, Object> tempMap = argumentInjector.inject(this.args);
            if (!tempMap.isEmpty()) {
                retVal.putAll(tempMap);
            }
        }
        return retVal;
    }

    public LimitedResourceMetadata getMetadata() {
        return metadata;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getTarget() {
        return target;
    }

    public Map<String, Object> getInjectArgs() {
        return injectArgs;
    }

    public LimiterOperationExpressionEvaluator getEvaluator() {
        return evaluator;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Object getKey() {
        return key;
    }

}
