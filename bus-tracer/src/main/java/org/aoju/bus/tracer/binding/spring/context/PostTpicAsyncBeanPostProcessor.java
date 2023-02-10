package org.aoju.bus.tracer.binding.spring.context;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor;
import org.springframework.scheduling.annotation.AsyncAnnotationAdvisor;

import java.util.Map;
import java.util.concurrent.Executor;

public class PostTpicAsyncBeanPostProcessor extends AbstractAdvisingBeanPostProcessor {

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
            // setTaskExecutor(executor); // compatible with spring 4
        }

        // use getAdvice instead of buildAdvice to be compatible with Spring 4
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
                final Object tpicObj = methodInvocation.getUserAttribute(Builder.TPIC_HEADER);
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
