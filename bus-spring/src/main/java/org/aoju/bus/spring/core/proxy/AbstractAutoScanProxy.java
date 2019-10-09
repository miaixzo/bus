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
package org.aoju.bus.spring.core.proxy;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.spring.core.proxy.mode.ProxyMode;
import org.aoju.bus.spring.core.proxy.mode.ScanMode;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: 自动扫描代理</p>
 * <p>Description: </p>
 *
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public abstract class AbstractAutoScanProxy extends AbstractAutoProxyCreator {

    public static final String CGLIB = "Cglib";
    // JDK Proxy 类型
    public static final String PROXY_TYPE_REFLECTIVE = "Reflective Aop Proxy";
    // CGLIB Proxy 类型
    public static final String PROXY_TYPE_CGLIB = "Cglib Aop Proxy";
    // JDK Proxy 名称关键字
    public static final String JDK_PROXY_NAME_KEY = "com.sun.proxy";
    // CGLIB Proxy 名称关键字
    public static final String CGLIB_PROXY_NAME_KEY = "ByCGLIB";
    public static final String SEPARATOR = ";";
    private static final long serialVersionUID = 6827218905375993727L;
    // Bean名称和Bean对象关联
    private final Map<String, Object> beanMap = new HashMap<>();

    // Spring容器中哪些接口或者类需要被代理
    private final Map<String, Boolean> proxyMap = new HashMap<>();

    // Spring容器中哪些类是类代理，哪些类是通过它的接口做代理
    private final Map<String, Boolean> proxyTargetClassMap = new HashMap<String, Boolean>();

    // 扫描目录，如果不指定，则扫描全局。两种方式运行结果没区别，只是指定扫描目录加快扫描速度，同时可以减少缓存量
    private String[] scanPackages;

    // 通过注解确定代理
    private ProxyMode proxyMode;

    // 扫描注解后的处理
    private ScanMode scanMode;

    public AbstractAutoScanProxy() {
        this((String[]) null);
    }

    public AbstractAutoScanProxy(String scanPackages) {
        this(scanPackages, ProxyMode.BY_CLASS_OR_METHOD_ANNOTATION, ScanMode.FOR_CLASS_OR_METHOD_ANNOTATION);
    }

    public AbstractAutoScanProxy(String[] scanPackages) {
        this(scanPackages, ProxyMode.BY_CLASS_OR_METHOD_ANNOTATION, ScanMode.FOR_CLASS_OR_METHOD_ANNOTATION);
    }

    public AbstractAutoScanProxy(ProxyMode proxyMode, ScanMode scanMode) {
        this((String[]) null, proxyMode, scanMode);
    }

    public AbstractAutoScanProxy(String scanPackages, ProxyMode proxyMode, ScanMode scanMode) {
        this(scanPackages, proxyMode, scanMode, true);
    }

    public AbstractAutoScanProxy(String[] scanPackages, ProxyMode proxyMode, ScanMode scanMode) {
        this(scanPackages, proxyMode, scanMode, true);
    }

    public AbstractAutoScanProxy(ProxyMode proxyMode, ScanMode scanMode, boolean exposeProxy) {
        this((String[]) null, proxyMode, scanMode, exposeProxy);
    }

    public AbstractAutoScanProxy(String scanPackages, ProxyMode proxyMode, ScanMode scanMode, boolean exposeProxy) {
        this(StringUtils.isNotEmpty(scanPackages) ? scanPackages.trim().split(SEPARATOR) : null, proxyMode, scanMode, exposeProxy);
    }

    public AbstractAutoScanProxy(String[] scanPackages, ProxyMode proxyMode, ScanMode scanMode, boolean exposeProxy) {
        this.scanPackages = scanPackages;
        this.setExposeProxy(exposeProxy);
        this.proxyMode = proxyMode;
        this.scanMode = scanMode;

        StringBuilder builder = new StringBuilder();
        if (ArrayUtils.isNotEmpty(scanPackages)) {
            for (int i = 0; i < scanPackages.length; i++) {
                String scanPackage = scanPackages[i];
                builder.append(scanPackage);
                if (i < scanPackages.length - 1) {
                    builder.append(SEPARATOR);
                }
            }
        }
        Logger.info("------------- Aop Information ------------");
        Logger.info("Auto scan proxy class is {}", getClass().getCanonicalName());
        Logger.info("Scan packages is {}", ArrayUtils.isNotEmpty(scanPackages) ? builder.toString() : "not set");
        Logger.info("Proxy mode is {}", proxyMode);
        Logger.info("Scan mode is {}", scanMode);
        Logger.info("Expose proxy is {}", exposeProxy);
        Logger.info("-------------------------------------------------");

        // 设定全局拦截器，可以是多个
        // 如果同时设置了全局和额外的拦截器，那么它们都同时工作，全局拦截器先运行，额外拦截器后运行
        Class<? extends MethodInterceptor>[] commonInterceptorClasses = getCommonInterceptors();
        String[] commonInterceptorNames = getCommonInterceptorNames();

        String[] interceptorNames = ArrayUtils.addAll(commonInterceptorNames, convertInterceptorNames(commonInterceptorClasses));
        if (ArrayUtils.isNotEmpty(interceptorNames)) {
            setInterceptorNames(interceptorNames);
        }
    }

    private String[] convertInterceptorNames(Class<? extends MethodInterceptor>[] commonInterceptorClasses) {
        if (ArrayUtils.isNotEmpty(commonInterceptorClasses)) {
            String[] interceptorNames = new String[commonInterceptorClasses.length];
            for (int i = 0; i < commonInterceptorClasses.length; i++) {
                Class<? extends MethodInterceptor> interceptorClass = commonInterceptorClasses[i];
                interceptorNames[i] = interceptorClass.getAnnotation(Component.class).value();
            }
            return interceptorNames;
        }
        return null;
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
        boolean scanPackagesEnabled = scanPackagesEnabled();
        // scanPackagesEnabled=false，表示“只扫描指定目录”的方式未开启，则不会对扫描到的bean进行代理预先判断
        if (scanPackagesEnabled) {
            boolean scanPackagesContained = scanPackagesContained(beanClass);
            // 如果beanClass的类路径，未包含在扫描目录中，返回DO_NOT_PROXY
            if (!scanPackagesContained) {
                return DO_NOT_PROXY;
            }
        }

        // 根据Bean名称获取Bean对象
        Object bean = beanMap.get(beanName);

        // 获取最终目标类
        Class<?> targetClass = null;
        if (bean != null /* && AopUtils.isCglibProxy(bean) */) {
            targetClass = AopProxyUtils.ultimateTargetClass(bean);
        } else {
            targetClass = beanClass;
        }

        // Spring容器扫描实现类
        if (!targetClass.isInterface()) {
            // 扫描接口（从实现类找到它的所有接口）
            if (targetClass.getInterfaces() != null) {
                for (Class<?> targetInterface : targetClass.getInterfaces()) {
                    Object[] proxyInterceptors = scanAndProxyForTarget(targetInterface, beanName, false);
                    if (proxyInterceptors != DO_NOT_PROXY) {
                        return proxyInterceptors;
                    }
                }
            }

            // 扫描实现类（如果接口上没找到注解， 就找实现类的注解）
            Object[] proxyInterceptors = scanAndProxyForTarget(targetClass, beanName, true);
            if (proxyInterceptors != DO_NOT_PROXY) {
                return proxyInterceptors;
            }
        }
        return DO_NOT_PROXY;
    }

    protected Object[] scanAndProxyForTarget(Class<?> targetClass, String beanName, boolean proxyTargetClass) {
        String targetClassName = targetClass.getCanonicalName();
        Object[] interceptors = getInterceptors(targetClass);

        // 排除java开头的接口，例如java.io.Serializable，java.io.Closeable等，执行不被代理
        if (StringUtils.isNotEmpty(targetClassName) && !targetClassName.startsWith("java.")) {
            // 避免对同一个接口或者类扫描多次
            Boolean proxied = proxyMap.get(targetClassName);
            if (proxied != null) {
                if (proxied) {
                    return interceptors;
                }
            } else {
                Object[] proxyInterceptors = null;
                switch (proxyMode) {
                    // 只通过扫描到接口名或者类名上的注解后，来确定是否要代理
                    case BY_CLASS_ANNOTATION_ONLY:
                        proxyInterceptors = scanAndProxyForClass(targetClass, targetClassName, beanName, interceptors, proxyTargetClass);
                        break;
                    // 只通过扫描到接口或者类方法上的注解后，来确定是否要代理
                    case BY_METHOD_ANNOTATION_ONLY:
                        proxyInterceptors = scanAndProxyForMethod(targetClass, targetClassName, beanName, interceptors, proxyTargetClass);
                        break;
                    // 上述两者都可以
                    case BY_CLASS_OR_METHOD_ANNOTATION:
                        Object[] classProxyInterceptors = scanAndProxyForClass(targetClass, targetClassName, beanName, interceptors, proxyTargetClass);
                        // 没有接口或者类名上扫描到目标注解，那么扫描接口或者类的方法上的目标注解
                        Object[] methodProxyInterceptors = scanAndProxyForMethod(targetClass, targetClassName, beanName, interceptors, proxyTargetClass);
                        if (classProxyInterceptors != DO_NOT_PROXY || methodProxyInterceptors != DO_NOT_PROXY) {
                            proxyInterceptors = interceptors;
                        } else {
                            proxyInterceptors = DO_NOT_PROXY;
                        }
                        break;
                }

                // 是否需要代理
                proxyMap.put(targetClassName, Boolean.valueOf(proxyInterceptors != DO_NOT_PROXY));

                if (proxyInterceptors != DO_NOT_PROXY) {
                    // 是接口代理还是类代理
                    proxyTargetClassMap.put(beanName, proxyTargetClass);

                    Logger.info("------------ Proxy Information -----------");
                    Class<? extends MethodInterceptor>[] commonInterceptorClasses = getCommonInterceptors();
                    if (ArrayUtils.isNotEmpty(commonInterceptorClasses)) {
                        Logger.info("Class [{}] is proxied by core intercept classes [{}], proxyTargetClass={}", targetClassName, StringUtils.toString(commonInterceptorClasses), proxyTargetClass);
                    }

                    String[] commonInterceptorNames = getCommonInterceptorNames();
                    if (ArrayUtils.isNotEmpty(commonInterceptorNames)) {
                        Logger.info("Class [{}] is proxied by core intercept beans [{}], proxyTargetClass={}", targetClassName, StringUtils.toString(commonInterceptorNames), proxyTargetClass);
                    }

                    if (proxyInterceptors != PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS && ArrayUtils.isNotEmpty(proxyInterceptors)) {
                        Logger.info("Class [{}] is proxied by additional interceptors [{}], proxyTargetClass={}", targetClassName, proxyInterceptors, proxyTargetClass);
                    }
                    Logger.info("-------------------------------------------------");
                }
                return proxyInterceptors;
            }
        }
        return DO_NOT_PROXY;
    }

    protected Object[] scanAndProxyForClass(Class<?> targetClass, String targetClassName, String beanName, Object[] interceptors, boolean proxyTargetClass) {
        // 判断目标注解是否标注在接口名或者类名上
        boolean proxied = false;
        Class<? extends Annotation>[] classAnnotations = getClassAnnotations();
        if (ArrayUtils.isNotEmpty(classAnnotations)) {
            for (Class<? extends Annotation> classAnnotation : classAnnotations) {
                if (targetClass.isAnnotationPresent(classAnnotation)) {
                    // 是否执行“注解扫描后处理”
                    if (scanMode == ScanMode.FOR_CLASS_ANNOTATION_ONLY || scanMode == ScanMode.FOR_CLASS_OR_METHOD_ANNOTATION) {
                        classAnnotationScanned(targetClass, classAnnotation);
                    } else {
                        // 如果“注解扫描后处理”不开启，没必要再往下执行循环，直接返回
                        return interceptors;
                    }

                    // 目标注解被扫描到，proxied赋值为true，即认为该接口或者类被代理
                    if (!proxied) {
                        proxied = true;
                    }
                }
            }
        }
        return proxied ? interceptors : DO_NOT_PROXY;
    }

    protected Object[] scanAndProxyForMethod(Class<?> targetClass, String targetClassName, String beanName, Object[] interceptors, boolean proxyTargetClass) {
        // 判断目标注解是否标注在方法上
        boolean proxied = false;
        Class<? extends Annotation>[] methodAnnotations = getMethodAnnotations();
        if (ArrayUtils.isNotEmpty(methodAnnotations)) {
            for (Method method : targetClass.getDeclaredMethods()) {
                for (Class<? extends Annotation> methodAnnotation : methodAnnotations) {
                    if (method.isAnnotationPresent(methodAnnotation)) {
                        // 是否执行“注解扫描后处理”
                        if (scanMode == ScanMode.FOR_METHOD_ANNOTATION_ONLY || scanMode == ScanMode.FOR_CLASS_OR_METHOD_ANNOTATION) {
                            methodAnnotationScanned(targetClass, method, methodAnnotation);
                        } else {
                            // 如果“注解扫描后处理”不开启，没必要再往下执行循环，直接返回
                            return interceptors;
                        }

                        // 目标注解被扫描到，proxied赋值为true，即认为该接口或者类被代理
                        if (!proxied) {
                            proxied = true;
                        }
                    }
                }
            }
        }
        return proxied ? interceptors : DO_NOT_PROXY;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Object object = super.postProcessBeforeInitialization(bean, beanName);

        // 前置扫描，把Bean名称和Bean对象关联存入Map
        boolean scanPackagesEnabled = scanPackagesEnabled();
        if (scanPackagesEnabled) {
            boolean scanPackagesContained = scanPackagesContained(bean.getClass());
            if (scanPackagesContained) {
                // 如果beanClass的类路径，包含在扫描目录中，则加入beanMap
                beanMap.put(beanName, bean);
            }
        } else {
            // scanPackagesEnabled=false，表示“只扫描指定目录”的方式未开启，则所有扫描到的bean都加入beanMap
            beanMap.put(beanName, bean);
        }
        return object;
    }

    @Override
    protected boolean shouldProxyTargetClass(Class<?> beanClass, String beanName) {
        // 设置不同场景下的接口代理，还是类代理
        Boolean proxyTargetClass = proxyTargetClassMap.get(beanName);
        if (proxyTargetClass != null) {
            return proxyTargetClass;
        }
        return super.shouldProxyTargetClass(beanClass, beanName);
    }

    // 是否是“只扫描指定目录”的方式
    protected boolean scanPackagesEnabled() {
        return ArrayUtils.isNotEmpty(scanPackages);
    }

    // 是否指定的beanClass包含在扫描目录中
    protected boolean scanPackagesContained(Class<?> beanClass) {
        for (String scanPackage : scanPackages) {
            if (StringUtils.isNotEmpty(scanPackage)) {
                // beanClassName有时候会为null...
                String beanClassName = beanClass.getCanonicalName();
                if (StringUtils.isNotEmpty(beanClassName)) {
                    if (beanClassName.startsWith(scanPackage) || beanClassName.contains(JDK_PROXY_NAME_KEY) || beanClassName.contains(CGLIB_PROXY_NAME_KEY)) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    // 获取切面拦截类的方式
    // 1. 根据targetClass从额外拦截类列表中去取（接口代理targetClass是接口类，类代理targetClass是类）
    // 2. 如果从额外拦截类列表中没取到，就从全局拦截类列表中去取（PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS）
    // 3. 如果从全局拦截类列表中没取到，就不代理（DO_NOT_PROXY）
    protected Object[] getInterceptors(Class<?> targetClass) {
        Object[] interceptors = getAdditionalInterceptors(targetClass);
        if (ArrayUtils.isNotEmpty(interceptors)) {
            return interceptors;
        }

        Class<? extends MethodInterceptor>[] commonInterceptorClasses = getCommonInterceptors();
        String[] commonInterceptorNames = getCommonInterceptorNames();
        if (ArrayUtils.isNotEmpty(commonInterceptorClasses) || ArrayUtils.isNotEmpty(commonInterceptorNames)) {
            return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
        }
        return DO_NOT_PROXY;
    }

    // 返回具有调用拦截的全局切面实现类，拦截类必须实现MethodInterceptor接口, 可以多个，通过@Component("xxx")方式寻址
    // 如果返回null， 全局切面代理关闭
    protected abstract Class<? extends MethodInterceptor>[] getCommonInterceptors();

    // 返回具有调用拦截的全局切面实现类的Bean名，拦截类必须实现MethodInterceptor接口, 可以多个
    // 如果返回null， 全局切面代理关闭
    protected abstract String[] getCommonInterceptorNames();

    // 返回额外的拦截类实例列表，拦截类必须实现MethodInterceptor接口，分别对不同的接口或者类赋予不同的拦截类，可以多个
    // 如果返回null， 额外切面代理关闭
    protected abstract MethodInterceptor[] getAdditionalInterceptors(Class<?> targetClass);

    // 返回接口名或者类名上的注解列表，可以多个, 如果接口名或者类名上存在一个或者多个该列表中的注解，即认为该接口或者类需要被代理和扫描
    // 如果返回null，则对列表中的注解不做代理和扫描
    protected abstract Class<? extends Annotation>[] getClassAnnotations();

    // 返回接口或者类的方法名上的注解，可以多个，如果接口或者类中方法名上存在一个或者多个该列表中的注解，即认为该接口或者类需要被代理和扫描
    // 如果返回null，则对列表中的注解不做代理和扫描
    protected abstract Class<? extends Annotation>[] getMethodAnnotations();

    // 一旦指定的接口或者类名上的注解被扫描到，将会触发该方法
    protected abstract void classAnnotationScanned(Class<?> targetClass, Class<? extends Annotation> classAnnotation);

    // 一旦指定的接口或者类的方法名上的注解被扫描到，将会触发该方法
    protected abstract void methodAnnotationScanned(Class<?> targetClass, Method method, Class<? extends Annotation> methodAnnotation);

}
