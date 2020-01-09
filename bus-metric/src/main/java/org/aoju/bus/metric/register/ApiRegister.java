/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.register;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.Config;
import org.aoju.bus.metric.annotation.Api;
import org.aoju.bus.metric.annotation.Service;
import org.aoju.bus.metric.builtin.DefinitionHolder;
import org.aoju.bus.metric.builtin.ErrorFactory;
import org.aoju.bus.metric.builtin.doc.ApiDocBuilder;
import org.aoju.bus.metric.builtin.doc.ApiDocHolder;
import org.aoju.bus.metric.builtin.doc.ApiServiceDocCreator;
import org.aoju.bus.metric.builtin.doc.DocFileCreator;
import org.aoju.bus.metric.magic.ApiDefinition;
import org.aoju.bus.metric.support.ReflectUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * api注册类,在spring启动完成时进行注册
 *
 * <pre>
 * 原理:
 * 1. 在spring容器中找到被@ApiService注解的类
 * 2. 在类中找到被@Api注解的方法
 * 3. 保存方法信息以及类对象,方便后期进行invoke
 * </pre>
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public class ApiRegister {

    private static final ApiMethodFilter API_METHOD_FILTER = new ApiMethodFilter();

    private int apiCount;

    private Config config;
    private ApplicationContext applicationContext;

    public ApiRegister(Config config, ApplicationContext applicationContext) {
        this.config = config;
        this.applicationContext = applicationContext;
    }

    /**
     * 改造ReflectionUtils.doWithMethods().
     * BUG描述：如果Service类有@Transactional注解（作用在类上或方法上），那么spring会使用cglib动态
     * 生成一个子类并且继承原有的类public ClassCGLIB extends Service {}，然后重写
     * Service的方法，实现动态代理。这时使用ReflectionUtils.doWithMethods()获取方法会拿到所有的method,
     * 其实只应该拿到cglib类中的方法即可。
     *
     * @param clazz the class
     * @param mc    calback
     * @param mf    filter
     */
    private static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) {
        // Keep backing up the inheritance hierarchy.
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (mf != null && !mf.matches(method)) {
                continue;
            }
            try {
                mc.doWith(method);
            } catch (IllegalAccessException ex) {
                Logger.error("注册API失败", ex);
                System.exit(0);
            }
        }
        // ！！注意：下面这两句【必须注释掉】，如果clazz对象被CGLIB代理，那么父类就是原生类。因为CGLIB是以子类的方式生成。
        // 这里只需要获取子类的方法即可。
//        if (clazz.getSuperclass() != null) {
//            doWithMethods(clazz.getSuperclass(), mc, mf);
//        }
//        else if (clazz.isInterface()) {
//            for (Class<?> superIfc : clazz.getInterfaces()) {
//                doWithMethods(superIfc, mc, mf);
//            }
//        }
    }

    /**
     * 接口注册操作
     *
     * @param registCallback 成功后回调
     */
    public void regist(RegistCallback registCallback) {
        Logger.info("******** 开始注册操作 ********");
        this.initMessage();
        this.registApi();
        this.createDoc();
        this.afterRegist(registCallback);
        Logger.info("******** 注册操作结束 ********");
    }

    /**
     * 初始化国际化消息
     */
    private void initMessage() {
        ErrorFactory.initMessageSource(this.config.getIsvModules());
    }

    /**
     * 注册接口
     */
    private void registApi() {
        Logger.info("开始注册Api接口...");
        ApplicationContext ctx = this.getApplicationContext();
        Assert.notNull(ctx, "ApplicationContext不能为空");
        Assert.notNull(config, "ApiConfig不能为空");

        DefinitionHolder.clear();
        // 找到所有ApiService的类名
        String[] beans = ReflectUtil.findApiServiceNames(ctx);

        for (String beanName : beans) {
            // 被@ApiService标记的类
            Object handler = ctx.getBean(beanName);
            // 处理beanClass类中被@Api标记的方法
            doWithMethods(handler.getClass(), new ApiMethodProcessor(handler), API_METHOD_FILTER);
        }
        Logger.info("注册Api接口完毕，共{}个接口", apiCount);
    }

    /**
     * 注册完成后回调
     *
     * @param registCallback callback
     */
    private void afterRegist(RegistCallback registCallback) {
        if (registCallback != null) {
            Logger.info("执行Api注册回调");
            registCallback.onRegistFinished(config);
            Logger.info("执行Api注册回调完毕");
        }
    }

    /**
     * 生成doc文档
     */
    private void createDoc() {
        if (this.config.isShowDoc()) {
            Logger.info("生成接口文档");
            new ApiServiceDocCreator(this.config.getVersion(), this.applicationContext).create();
        }
        DocFileCreator docFileCreator = config.getDocFileCreator();
        if (docFileCreator != null) {
            try {
                ApiDocBuilder apiDocBuilder = ApiDocHolder.getApiDocBuilder();
                docFileCreator.createMarkdownDoc(apiDocBuilder.getApiModules());
            } catch (IOException e) {
                Logger.error("生成文档文件出错", e);
            }
        }
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 过滤出被@Api标记的方法
     */
    private static class ApiMethodFilter implements ReflectionUtils.MethodFilter {
        @Override
        public boolean matches(Method method) {
            return !method.isSynthetic() && AnnotationUtils.findAnnotation(method, Api.class) != null;
        }
    }

    private class ApiMethodProcessor implements ReflectionUtils.MethodCallback {
        private Object handler;
        private Service serviceAnno;

        public ApiMethodProcessor(Object handler) {
            super();
            this.handler = handler;
            this.serviceAnno = AnnotationUtils.findAnnotation(handler.getClass(), Service.class);
        }

        @Override
        public void doWith(Method method) throws IllegalArgumentException {
            ReflectionUtils.makeAccessible(method);
            Api api = AnnotationUtils.findAnnotation(method, Api.class);
            boolean ignoreSign = api.ignoreSign() ? true : this.serviceAnno.sign();
            boolean ignoreValidate = api.ignoreValidate() ? true : this.serviceAnno.validate();

            boolean isWrapResult = this.serviceAnno.wrap() ? api.wrapResult() : false;

            ApiDefinition apiDefinition = new ApiDefinition();
            apiDefinition.setIgnoreSign(ignoreSign);
            apiDefinition.setIgnoreValidate(ignoreValidate);
            apiDefinition.setWrapResult(isWrapResult);
            apiDefinition.setHandler(handler);
            apiDefinition.setMethod(method);
            apiDefinition.setName(api.name());
            apiDefinition.setNoReturn(api.noReturn());
            apiDefinition.setIgnoreJWT(api.ignoreJWT());
            apiDefinition.setIgnoreToken(api.isIgnoreToken());
            String version = api.version();
            if ("".equals(version.trim())) {
                version = config.getVersion();
            }
            apiDefinition.setVersion(version);

            Parameter[] parameters = method.getParameters();
            Class<?> paramClass = null;
            if (parameters != null && parameters.length > 0) {
                Parameter parameter = parameters[0];
                paramClass = parameter.getType();
                boolean isNumberOrStringType = ClassUtils.isNumberOrStringType(paramClass);
                apiDefinition.setSingleParameter(isNumberOrStringType);
                apiDefinition.setMethodArguClass(paramClass);
                if (isNumberOrStringType) {
                    SingleParameterContext.add(handler, method, parameter);
                }
            }

            Logger.debug("注册接口name={},version={},method={} {}({})", api.name(), api.version(),
                    method.getReturnType().getName(), method.getName(), paramClass == null ? "" : paramClass.getName());

            try {
                DefinitionHolder.addApiDefinition(apiDefinition);
                config.getApiRegistEvent().onSuccess(apiDefinition);
            } catch (InstrumentException e) {
                Logger.error(e.getMessage(), e);
                System.exit(0);
            }

            apiCount++;
        }

    }

}
