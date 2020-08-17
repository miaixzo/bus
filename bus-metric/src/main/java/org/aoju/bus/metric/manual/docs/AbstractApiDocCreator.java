/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.metric.manual.docs;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.metric.ApiRegister;
import org.aoju.bus.metric.manual.Api;
import org.aoju.bus.metric.manual.docs.annotation.ApiDocMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 负责生成文档
 *
 * @param <ServiceAnnotation> 类上面的注解，如Controller,ApiService
 * @param <MethodAnnotation>  方法上面的注解，如Api,RequestMapping
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public abstract class AbstractApiDocCreator<ServiceAnnotation extends Annotation, MethodAnnotation extends Annotation> {

    /**
     * 默认版本号
     */
    private String defaultVersion;
    private ApplicationContext applicationContext;


    public AbstractApiDocCreator(String defaultVersion) {
        if (defaultVersion == null) {
            defaultVersion = Normal.EMPTY;
        }
        this.defaultVersion = defaultVersion;
    }

    /**
     * 返回service类上面的注解，如Service
     *
     * @return 返回注解class
     */
    protected abstract Class<ServiceAnnotation> getServiceAnnotationClass();

    /**
     * 返回方法上面的注解，如：RequestMapping
     *
     * @return 返回注解class
     */
    protected abstract Class<MethodAnnotation> getMethodAnnotationClass();

    /**
     * 根据方法注解获取api信息
     *
     * @param annotation 方法注解
     * @return 返回api信息
     */
    protected abstract Api getApi(MethodAnnotation annotation);

    public void create() {
        Assert.notNull(applicationContext, "ApplicationContext不能为空");

        String[] beans = ApiRegister.findBeanNamesByAnnotationClass(getServiceAnnotationClass());
        ApiDocBuilder apiDocBuilder = ApiDocHolder.createBuilder();
        for (String beanName : beans) {
            Object handler = applicationContext.getBean(beanName);
            Class<?> beanClass = handler.getClass();
            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                // 找方法上面的注解,如@Api,@RequestMapping
                MethodAnnotation methodAnnotation = AnnotationUtils.findAnnotation(method, getMethodAnnotationClass());
                ApiDocMethod apiDocMethod = AnnotationUtils.findAnnotation(method, ApiDocMethod.class);
                // 如果找到
                if (!method.isSynthetic() && methodAnnotation != null && apiDocMethod != null) {
                    final Api api = getApi(methodAnnotation);
                    if (api.getVersion() == null || Normal.EMPTY.equals(api.getVersion().trim())) {
                        api.setVersion(defaultVersion);
                    }
                    // 生成doc内容
                    apiDocBuilder.addDocItem(api, handler, method);
                }
            }
        }
    }

}
