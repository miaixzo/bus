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
package org.aoju.bus.spring.storage;

import org.aoju.bus.spring.annotation.EnableStorage;
import org.aoju.bus.storage.Provider;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

/**
 * @author Kimi Liu
 * @version 3.1.2
 * @since JDK 1.8
 */
public class StorageImportSelector implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata
                .getAnnotationAttributes(EnableStorage.class.getName(),
                        true));
        Assert.notNull(attributes, "No auto-configuration attributes found. Is "
                + metadata.getClassName()
                + " annotated with @EnableStorage?");

        Provider provider = attributes.getEnum("provider");
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(StorageConfiguration.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.addPropertyValue("provider", provider.getValue());
        propertyValues.addPropertyValue("endpoint", env.getProperty("oss.endpoint", ""));
        propertyValues.addPropertyValue("accessKey", env.getProperty("oss.accessKey", ""));
        propertyValues.addPropertyValue("secretKey", env.getProperty("oss.secretKey", ""));
        propertyValues.addPropertyValue("urlPrefix", env.getProperty("oss.url-prefix", ""));
        propertyValues.addPropertyValue("privated", env.getProperty("oss.privated", "false"));
        propertyValues.addPropertyValue("bucket", env.getProperty("oss.bucket", ""));
        propertyValues.addPropertyValue("internalUrl", env.getProperty("oss.internal-url", ""));
        beanDefinition.setPropertyValues(propertyValues);
        beanDefinitionRegistry.registerBeanDefinition("ossProviderSpingFacade", beanDefinition);
    }

}
