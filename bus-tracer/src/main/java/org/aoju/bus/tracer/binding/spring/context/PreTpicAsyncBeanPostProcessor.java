package org.aoju.bus.tracer.binding.spring.context;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.AnnotationAsyncExecutionInterceptor;
import org.springframework.scheduling.annotation.AsyncAnnotationAdvisor;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;

import java.util.Map;
import java.util.concurrent.Executor;

public class PreTpicAsyncBeanPostProcessor extends AsyncAnnotationBeanPostProcessor {

    public PreTpicAsyncBeanPostProcessor(Executor executor, Backend backend) {
        advisor = new TpicPreAdvisor(executor, backend);
        setBeforeExistingAdvisors(true);
    }

    boolean isBeforeExistingAdvisors() {
        return beforeExistingAdvisors;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
    }

    static class DelegateTpicToAsyncInterceptor extends AnnotationAsyncExecutionInterceptor {

        private final Backend backend;

        DelegateTpicToAsyncInterceptor(Executor defaultExecutor, Backend backend) {
            super(defaultExecutor);
            this.backend = backend;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if (invocation instanceof ReflectiveMethodInvocation) {
                final Map<String, String> tpic = backend.copyToMap();
                ((ReflectiveMethodInvocation) invocation).setUserAttribute(Builder.TPIC_HEADER, tpic);
            }

            return invocation.proceed();
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }

    class TpicPreAdvisor extends AsyncAnnotationAdvisor {

        private final Backend backend;
        private final Executor executor;

        TpicPreAdvisor(Executor executor, Backend backend) {
            super();
            this.executor = executor;
            this.backend = backend;
            setExecutor(executor); // compatible with spring 4
        }

        // use getAdvice instead of buildAdvice to be compatible with Spring 4
        @Override
        public Advice getAdvice() {
            return new DelegateTpicToAsyncInterceptor(executor, backend);
        }
    }
}
