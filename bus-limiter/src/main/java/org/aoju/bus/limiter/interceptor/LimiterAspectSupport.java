package org.aoju.bus.limiter.interceptor;

import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.limiter.execute.LimitContextsValueWrapper;
import org.aoju.bus.limiter.execute.LimiterExecutionContext;
import org.aoju.bus.limiter.metadata.LimitedResourceMetadata;
import org.aoju.bus.limiter.metadata.LimitedResourceMetadataCache;
import org.aoju.bus.limiter.resource.LimitedResource;
import org.aoju.bus.limiter.source.LimitedResourceSource;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;


/**
 * 实际的advisor
 * 使用beanfactory的一些基础设施
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class LimiterAspectSupport implements BeanFactoryAware, InitializingBean, SmartInitializingSingleton {

    private BeanFactory beanFactory;

    private boolean initialized = false;

    private LimitedResourceSource limitedResourceSource;

    private LimitedResourceMetadataCache limitedResourceMetadataCache;

    /**
     * @param invocation
     * @param target
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    protected Object execute(final MethodInvocation invocation, Object target, Method method, Object[] args) throws Throwable {

        if (this.initialized) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
            LimitedResourceSource limitedResourceSource = getLimitedResourceSource();
            if (limitedResourceSource != null) {
                Collection<LimitedResource> limitedResources = limitedResourceSource.getLimitedResource(targetClass, method);
                if (!CollUtils.isEmpty(limitedResources)) {
                    Collection<LimiterExecutionContext> contexts = getLimiterOperationContexts(limitedResources, method, args, target, targetClass);
                    LimitContextsValueWrapper limitContextsValueWrapper = limitContexts(contexts);
                    if (limitContextsValueWrapper.value()) {
                        try {
                            return invocation.proceed();
                        } catch (Throwable e) {
                            throw e;
                        } finally {
                            releaseContexts(contexts);
                        }
                    } else {
                        return limitContextsValueWrapper.getLimiterFailResolveResult();
                    }

                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.initialized = true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        this.limitedResourceMetadataCache = new LimitedResourceMetadataCache(beanFactory);
    }

    protected LimitContextsValueWrapper limitContexts(Collection<LimiterExecutionContext> contexts) {
        Collection<LimiterExecutionContext> limited = new ArrayList<>();
        for (LimiterExecutionContext context : contexts) {
            if (context.limit() && context.getThrowable() == null) {
                limited.add(context);
            } else {
                releaseContexts(limited);
                Object result = context.getFallbackResult();
                return new LimitContextsValueWrapper(false, result);
            }

        }
        return new LimitContextsValueWrapper(true, null);
    }

    protected void releaseContexts(Collection<LimiterExecutionContext> contexts) {
        if (contexts != null && !contexts.isEmpty()) {
            for (LimiterExecutionContext context : contexts) {
                context.release();
            }
        }
    }


    protected Collection<LimiterExecutionContext> getLimiterOperationContexts(Collection<LimitedResource> limitedResources, Method method, Object[] args, Object target, Class<?> targetClass) {
        Collection<LimiterExecutionContext> retVal = new ArrayList<>();
        for (LimitedResource limitedResource : limitedResources) {
            LimitedResourceMetadata metadata = limitedResourceMetadataCache.getLimitedResourceMetadata(limitedResource, method, targetClass);
            retVal.add(new LimiterExecutionContext(metadata, args, target, this.beanFactory));
        }
        return retVal;
    }


    public LimitedResourceSource getLimitedResourceSource() {
        return limitedResourceSource;
    }

    public void setLimitedResourceSource(LimitedResourceSource limitedResourceSource) {
        this.limitedResourceSource = limitedResourceSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
