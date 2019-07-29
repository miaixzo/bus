package org.aoju.bus.limiter;

import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractLimiterAnnotationParser<T extends Limiter<?>, V extends Annotation> implements LimiterAnnotationParser<T> {

    private Class<Annotation> supportAnnotation;

    // 不需要同步
    private synchronized Class<Annotation> computeSupportAnnotation() {
        if (supportAnnotation != null) {
            return supportAnnotation;
        }
        supportAnnotation = (Class<Annotation>) ((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[1];
        return supportAnnotation;
    }

    @Override
    public Class<Annotation> getSupportAnnotation() {
        if (supportAnnotation != null) return supportAnnotation;
        return computeSupportAnnotation();
    }

    public String getLimiter(AnnotationAttributes attributes) {
        return attributes.getString("limiter");
    }

    public String getKey(AnnotationAttributes attributes) {
        return attributes.getString("key");
    }

    public String getFallback(AnnotationAttributes attributes) {
        return attributes.getString("fallback");
    }

    public String getErrorHandler(AnnotationAttributes attributes) {
        return attributes.getString("errorHandler");
    }

    public Collection<String> getArgumentInjectors(AnnotationAttributes attributes) {
        return Arrays.asList(attributes.getStringArray("argumentInjectors"));
    }

}
