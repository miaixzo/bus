package org.aoju.bus.spring.core.selector;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.logger.Logger;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * <p>Title: Selector导入注册</p>
 * <p>Description: </p>
 * 参考@EnableCircuitBreaker的用法
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractImportSelector<T> implements DeferredImportSelector, BeanClassLoaderAware, EnvironmentAware {

    private ClassLoader beanClassLoader;
    private Class<T> annotationClass;
    private Environment environment;

    protected AbstractImportSelector() {
        this.annotationClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(this.getClass(), AbstractImportSelector.class);
    }

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        if (!isEnabled()) {
            return new String[0];
        }

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(this.annotationClass.getName(), true));

        Assert.notNull(attributes, "No " + getSimpleName() + " attributes found. Is " + metadata.getClassName() + " annotated with @" + getSimpleName() + "?");

        // Find all possible auto configuration classes, filtering duplicates
        List<String> factories = new ArrayList<>(new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(this.annotationClass, this.beanClassLoader)));

        if (factories.isEmpty() && !hasDefaultFactory()) {
            throw new IllegalStateException("Annotation @" + getSimpleName() + " found, but there are no implementations. Did you forget to include a starter?");
        }

        if (factories.size() > 1) {
            // there should only ever be first DiscoveryClient, but there might be more than first factory
            Logger.warn("More than first implementation " + "of @" + getSimpleName() + " (now relying on @Conditionals to pick first): " + factories);
        }

        return factories.toArray(new String[factories.size()]);
    }

    protected boolean hasDefaultFactory() {
        return false;
    }

    protected String getSimpleName() {
        return this.annotationClass.getSimpleName();
    }

    protected Class<T> getAnnotationClass() {
        return this.annotationClass;
    }

    protected Environment getEnvironment() {
        return this.environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    protected abstract boolean isEnabled();

}