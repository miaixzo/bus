package org.aoju.bus.spring.core.registrar;

import org.aoju.bus.spring.core.proxy.AbstractInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.MutablePropertyValues;

/**
 * <p>Title:注解信息 </p>
 * <p>Description: </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractRegistrarInterceptor extends AbstractInterceptor {

    protected MutablePropertyValues annotationValues;

    public AbstractRegistrarInterceptor(MutablePropertyValues annotationValues) {
        this.annotationValues = annotationValues;
    }

    public MutablePropertyValues getAnnotationValues() {
        return annotationValues;
    }

    public String getInterface(MethodInvocation invocation) {
        return getMethod(invocation).getDeclaringClass().getCanonicalName();
    }

}