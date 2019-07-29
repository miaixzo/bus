package org.aoju.bus.limiter.interceptor;

import org.aoju.bus.limiter.source.LimitedResourceSource;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;


/**
 * 实际的切面
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BeanFactoryLimitedResourceSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private LimitedResourceSource limitedResourceSource;

    /**
     * 切点
     */
    private final LimitedResourceSourcePointcut pointcut = new LimitedResourceSourcePointcut() {
        @Override
        protected LimitedResourceSource getLimitedResourceSource() {
            return BeanFactoryLimitedResourceSourceAdvisor.this.limitedResourceSource;
        }
    };

    public BeanFactoryLimitedResourceSourceAdvisor(LimitedResourceSource limitedResourceSource) {
        this.limitedResourceSource = limitedResourceSource;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
