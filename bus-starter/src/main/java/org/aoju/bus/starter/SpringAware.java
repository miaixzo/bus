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
package org.aoju.bus.starter;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 以静态变量保存Spring ApplicationContext,
 * 可在任何代码任何地方任何时候中取出ApplicaitonContext.
 *
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
@Component
public class SpringAware implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 检查上下文信息.
     *
     * @return true/false
     */
    private static void isApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("请配置注解扫描,或者定义SpringContextAware");
        }
    }

    /**
     * 取得存储在静态变量中的ApplicationContext.
     *
     * @return 上下文信息
     */
    public static ApplicationContext getApplicationContext() {
        isApplicationContext();
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        System.setProperty("user.timezone", "Asia/Shanghai");
        SpringAware.applicationContext = applicationContext;
    }

    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     *
     * @param <T>  对象
     * @param name 名称
     * @return the object
     */
    public static <T> T getBean(String name) {
        isApplicationContext();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     * 如果有多个Bean符合Class, 取出第一个.
     *
     * @param <T>   对象
     * @param clazz 对象
     * @return the object
     */
    public static <T> T getBean(Class<T> clazz) {
        isApplicationContext();
        Map beanMaps = applicationContext.getBeansOfType(clazz);
        if (beanMaps != null && !beanMaps.isEmpty()) {
            return (T) beanMaps.values().iterator().next();
        } else {
            return null;
        }
    }

    /**
     * 依据类型获取所有子类(key为spring的id,value为对象实例)
     *
     * @param <T>          对象
     * @param requiredType 类型
     * @return the object
     * @throws BeansException 异常
     */
    public static <T> Map<String, T> getBeanOfType(Class<T> requiredType) throws BeansException {
        isApplicationContext();
        return applicationContext.getBeansOfType(requiredType);
    }

    /**
     * <pre>
     *     获取指定注解的Bean
     *
     * @param  annType 指定注解类型
     * @return 结果map
     * </pre>
     */
    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annType) {
        isApplicationContext();
        return applicationContext.getBeansWithAnnotation(annType);
    }

    /**
     * <pre>
     *     获取当前profile
     *     默认获取第一个
     *
     * @return profile
     * </pre>
     */
    public static String getActiveProfile() {
        isApplicationContext();
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }


    /**
     * 当前是否开发/测试模式
     *
     * @return boolean true|false
     */
    public static boolean isDemoMode() {
        return isTestMode() || isDevMode();
    }

    /**
     * 当前是否开发环境
     *
     * @return boolean
     */
    public static boolean isDevMode() {
        return "dev".equalsIgnoreCase(getActiveProfile());
    }

    /**
     * 当前是否测试环境
     *
     * @return boolean
     */
    public static boolean isTestMode() {
        return "test".equalsIgnoreCase(getActiveProfile());
    }

}
