/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.tracer.binding.spring.context.async;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor;
import org.springframework.scheduling.annotation.AsyncAnnotationAdvisor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
public class PostTpicAsyncBeanPostProcessor extends AbstractAdvisingBeanPostProcessor {

    @Autowired
    public PostTpicAsyncBeanPostProcessor(Executor executor, Backend backend) {
        advisor = new TpicPostAdvisor(executor, backend);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    static class TpicPostAdvisor extends AsyncAnnotationAdvisor {

        private final Executor executor;
        private final Backend backend;

        public TpicPostAdvisor(Executor executor, Backend backend) {
            super();
            this.executor = executor;
            this.backend = backend;
        }

        @Override
        public Advice getAdvice() {
            return new DelegateTpicToThreadInterceptor(executor, backend);
        }
    }

    static class DelegateTpicToThreadInterceptor extends AnnotationAsyncExecutionInterceptor {

        private final Backend backend;

        DelegateTpicToThreadInterceptor(Executor defaultExecutor, Backend backend) {
            super(defaultExecutor);
            this.backend = backend;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if (invocation instanceof ReflectiveMethodInvocation) {
                final ReflectiveMethodInvocation methodInvocation = (ReflectiveMethodInvocation) invocation;
                final Object tpicObj = methodInvocation.getUserAttribute(TraceConsts.TPIC_HEADER);
                if (tpicObj instanceof Map) {
                    final Map<? extends String, ? extends String> tpic = (Map<? extends String, ? extends String>) tpicObj;
                    backend.putAll(tpic);
                }
            }

            try {
                return invocation.proceed();
            } finally {
                backend.clear();
            }
        }
    }

}
