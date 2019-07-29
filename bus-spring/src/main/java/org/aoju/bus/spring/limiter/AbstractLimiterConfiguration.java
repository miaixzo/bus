package org.aoju.bus.spring.limiter;

import org.aoju.bus.spring.annotation.EnableLimiter;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;


/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractLimiterConfiguration implements ImportAware {

    protected AnnotationAttributes enableLimiter;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLimiter = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLimiter.class.getName(), false));
        if (this.enableLimiter == null) {
            throw new IllegalArgumentException(
                    "@EnableLimiter is not present on importing class " + importMetadata.getClassName());
        }
    }

}
