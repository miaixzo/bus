/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.limiter.source;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.limiter.LimiterAnnotationParser;
import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Kimi Liu
 * @version 3.6.2
 * @since JDK 1.8
 */
public class LimitedResourceScanner implements LimitedResourceSource {

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    Map<String, LimitedResource> limitedResourceMap = new HashMap<>();
    Map<String, Collection<LimitedResource>> limitedResourceRegistry = new HashMap<>();
    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
    private ResourcePatternResolver resourcePatternResolver;
    private MetadataReaderFactory metadataReaderFactory;
    private String basePackage;
    private Collection<LimiterAnnotationParser> limiterAnnotationParsers;


    public LimitedResourceScanner(String basePackage, Collection<LimiterAnnotationParser> limiterAnnotationParsers, ResourceLoader resourceLoader) {
        this.basePackage = basePackage;
        this.limiterAnnotationParsers = limiterAnnotationParsers;
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    public void scanLimitedResource() {
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(basePackage) + '/' + this.resourcePattern;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadataReadingVisitor classVisitor = (AnnotationMetadataReadingVisitor) metadataReader.getClassMetadata();
                    if (classVisitor.isInterface() || classVisitor.isAbstract()) {
                        continue;
                    }
                    for (LimiterAnnotationParser parser : limiterAnnotationParsers) {
                        Set<MethodMetadata> methodMetadata = classVisitor.getAnnotatedMethods(parser.getSupportAnnotation().getName());
                        if (CollUtils.isEmpty(methodMetadata)) {
                            continue;
                        }
                        for (MethodMetadata metadata : methodMetadata) {
                            MethodMetadataReadingVisitor methodVisitor = (MethodMetadataReadingVisitor) metadata;
                            AnnotationAttributes attributes = methodVisitor.getAnnotationAttributes(parser.getSupportAnnotation().getName());
                            if (attributes != null) {
                                Annotation annotation;
                                LimitedResource limitedResource = parser.parseLimiterAnnotation(attributes);
                                if (limitedResource != null) {
                                    String key = methodVisitor.getDeclaringClassName() + "#" +
                                            methodVisitor.getMethodName() + "@"
                                            + parser.getSupportAnnotation().getSimpleName();
                                    limitedResourceMap.put(key, limitedResource);
                                    // add to registry
                                    String classMethod = methodVisitor.getDeclaringClassName()
                                            + "#" + methodVisitor.getMethodName();
                                    if (!limitedResourceRegistry.containsKey(classMethod)) {
                                        List<LimitedResource> tempList = new ArrayList<>();
                                        tempList.add(limitedResource);
                                        limitedResourceRegistry.put(classMethod, tempList);
                                    } else {
                                        Collection<LimitedResource> tempList = limitedResourceRegistry.get(classMethod);
                                        tempList.add(limitedResource);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    @Override
    public Collection<LimitedResource> getLimitedResource(Class<?> targetClass, Method method) {
        String key = targetClass.getName() + "#" + method.getName();
        return limitedResourceRegistry.get(key);
    }

}
