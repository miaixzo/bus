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
package org.aoju.bus.metric;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ClassKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.annotation.MappingApi;
import org.aoju.bus.metric.annotation.Service;
import org.aoju.bus.metric.builtin.HeartBeatProcessor;
import org.aoju.bus.metric.consts.NettyMode;
import org.aoju.bus.metric.manual.ApiDefinition;
import org.aoju.bus.metric.manual.DefinitionHolder;
import org.aoju.bus.metric.manual.ErrorFactory;
import org.aoju.bus.metric.manual.docs.ApiDocBuilder;
import org.aoju.bus.metric.manual.docs.ApiDocHolder;
import org.aoju.bus.metric.manual.docs.ApiServiceDocCreator;
import org.aoju.bus.metric.manual.docs.DocFileCreator;
import org.aoju.bus.metric.register.RegistCallback;
import org.aoju.bus.metric.register.SingleParameterContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * @version 6.0.5
 * @since JDK 1.8++
 */
public class ApiRegister {

    private static final ApiMethodFilter API_METHOD_FILTER = new ApiMethodFilter();
    private static final String[] EMPTY_STRING_ARRAY = {};
    /**
     * 验证组列表
     */
    private static Map<Object, Object> API_PROCESS_CACHE = new ConcurrentHashMap<>();
    private static Map<String, Class<?>> classGenricTypeCache = new HashMap<>(16);
    private static Map<String, Field> genericTypeFieldCache = new HashMap<>();
    /**
     * 校验实例信息
     */
    private static ApiRegister instance;

    static {
        API_PROCESS_CACHE.put(NettyMode.HEART_BEAT.getCode(), new HeartBeatProcessor());
    }

    private int apiCount;
    private ApiConfig config;

    public ApiRegister() {

    }

    public ApiRegister(ApiConfig config) {
        this.config = config;
    }

    /**
     * 单例模型初始化
     *
     * @return the object
     */
    public static ApiRegister getInstance() {
        synchronized (ApiRegister.class) {
            if (ObjectKit.isEmpty(instance)) {
                instance = new ApiRegister();
            }
        }
        return instance;
    }

    /**
     * 注册组件
     *
     * @param name   组件名称
     * @param object 组件对象
     */
    public static void register(String name, Object object) {
        if (API_PROCESS_CACHE.containsKey(name)) {
            throw new InstrumentException("重复注册同名称的校验器：" + name);
        }
        Class<?> clazz = object.getClass();
        if (API_PROCESS_CACHE.containsKey(clazz.getSimpleName())) {
            throw new InstrumentException("重复注册同类型的校验器：" + clazz);
        }
        API_PROCESS_CACHE.putIfAbsent(name, object);
        API_PROCESS_CACHE.putIfAbsent(clazz.getSimpleName(), object);
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
    }

    /**
     * 设置某个字段的值
     *
     * @param target    实体类，必须有字段的set方法
     * @param fieldName 字段名
     * @param val       值
     */
    public static void invokeFieldValue(Object target, String fieldName, Object val) {
        String setMethodName = getSetMethodName(fieldName);
        Method[] methods = target.getClass().getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            Class<?>[] methodParams = method.getParameterTypes();

            if (setMethodName.equals(methodName)) {
                // 能否拷贝
                boolean canCopy =
                        // 并且只有一个参数
                        methodParams.length == 1
                                // val是methodParams[0]或他的子类
                                && methodParams[0].isInstance(val) || Number.class.isInstance(val);

                if (canCopy) {
                    try {
                        if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                            method.setAccessible(true);
                        }
                        method.invoke(target, val);
                        break;
                    } catch (Throwable ex) {
                        throw new InstrumentException("Could not set property '" + fieldName + "' value to target", ex);
                    }
                }
            }
        }
    }

    /**
     * 返回实体类中具有指定泛型的字段
     *
     * @param obj          实体类
     * @param genericClass 指定泛型
     * @return 没有返回null
     */
    public static Field getListFieldWithGeneric(Object obj, Class<?> genericClass) {
        Class<?> objClass = obj.getClass();
        String key = objClass.getName() + genericClass.getName();
        Field value = genericTypeFieldCache.get(key);
        if (value != null) {
            return value;
        }
        Field[] fields = objClass.getDeclaredFields();
        for (Field field : fields) {
            Type genericType = getListGenericType(field);
            if (genericType == genericClass) {
                genericTypeFieldCache.put(key, field);
                return field;
            }
        }
        return null;
    }

    /**
     * 返回集合字段的泛型类型
     * 如：List&lt;User&gt; list;返回User.class
     *
     * @param field 类中的一个属性
     * @return 返回类型
     */
    public static Type getListGenericType(Field field) {
        if (isListType(field.getType())) {
            Type genericType = field.getGenericType();

            if (genericType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genericType).getActualTypeArguments();
                if (params.length == 1) {
                    return params[0];
                }
            }
        }
        return Object.class;
    }

    public static boolean isListType(Type type) {
        return type == List.class;
    }

    /**
     * 返回set方法名 name - setName
     *
     * @param fieldName 属性名称
     * @return 返回方法名
     */
    public static String getSetMethodName(String fieldName) {
        return Normal.SET + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * 返回定义类时的泛型参数的类型
     * 如:定义一个BookManager类
     * <code>{@literal public BookManager extends GenricManager<Book,Address>}{...} </code>
     * 调用getSuperClassGenricType(getClass(),0)将返回Book的Class类型
     * 调用getSuperClassGenricType(getClass(),1)将返回Address的Class类型
     *
     * @param clazz 从哪个类中获取
     * @param index 泛型参数索引,从0开始
     * @return 返回泛型参数类型
     */
    public static Class<?> getSuperClassGenricType(Class<?> clazz, int index) throws IndexOutOfBoundsException {
        String cacheKey = clazz.getName() + index;
        Class<?> cachedClass = classGenricTypeCache.get(cacheKey);
        if (cachedClass != null) {
            return cachedClass;
        }

        Type genType = clazz.getGenericSuperclass();

        // 没有泛型参数
        if (!(genType instanceof ParameterizedType)) {
            throw new RuntimeException("class " + clazz.getName() + " 没有指定父类泛型");
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

            if (index >= params.length || index < 0) {
                throw new RuntimeException("泛型索引不正确，index:" + index);
            }
            if (!(params[index] instanceof Class)) {
                throw new RuntimeException(params[index] + "不是Class类型");
            }

            Class<?> retClass = (Class<?>) params[index];
            // 缓存起来
            classGenricTypeCache.put(cacheKey, retClass);

            return retClass;
        }
    }

    /**
     * 找到所有ApiService的类名
     *
     * @return 返回类名称数组
     */
    public static String[] findApiServiceNames() {
        return findBeanNamesByAnnotationClass(Service.class);
    }

    /**
     * 找到所有被注解标记的类名
     *
     * @param annotationClass 注解class
     * @return 返回类名称数组，没有返回空数组
     */
    public static String[] findBeanNamesByAnnotationClass(Class<? extends Annotation> annotationClass) {
        String[] beans = ApiAware.getApplicationContext().getBeanNamesForAnnotation(annotationClass);
        // 如果没找到，去父容器找
        if (beans == null || beans.length == 0) {
            ApplicationContext parentCtx = ApiAware.getApplicationContext().getParent();
            if (parentCtx != null) {
                beans = parentCtx.getBeanNamesForAnnotation(annotationClass);
            }
        }
        if (beans == null) {
            beans = EMPTY_STRING_ARRAY;
        }
        return beans;
    }

    /**
     * 是否包含指定名称内容
     *
     * @param name 内容名称
     * @return true：包含, false：不包含
     */
    public boolean contains(String name) {
        return API_PROCESS_CACHE.containsKey(name);
    }

    /**
     * 根据校验器名称获取内容
     *
     * @param name 内容名称
     * @return 内容对象, 找不到时返回null
     */
    public Object require(String name) {
        return API_PROCESS_CACHE.get(name);
    }

    /**
     * 优先根据内容名称获取内容,找不到时,根据类型获取内容对象
     *
     * @param name  内容名称
     * @param clazz 内容类型
     * @return 内容对象, 找不到时返回null
     */
    public Object require(String name, Class<?> clazz) {
        Object object = this.require(name);
        if (ObjectKit.isEmpty(object)) {
            object = this.require(clazz.getSimpleName());
        }
        return object;
    }

    /**
     * 当前注册对象
     *
     * @return 内容对象
     */
    public Object get() {
        return this.API_PROCESS_CACHE;
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
        Assert.notNull(config, "ApiConfig不能为空");

        DefinitionHolder.clear();
        // 找到所有ApiService的类名
        String[] beans = findApiServiceNames();

        for (String beanName : beans) {
            // 被@ApiService标记的类
            Object handler = ApiAware.getApplicationContext().getBean(beanName);
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
            new ApiServiceDocCreator(this.config.getVersion()).create();
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

    public ApiConfig getConfig() {
        return config;
    }

    public void setConfig(ApiConfig config) {
        this.config = config;
    }

    /**
     * 过滤出被@Api标记的方法
     */
    private static class ApiMethodFilter implements ReflectionUtils.MethodFilter {
        @Override
        public boolean matches(Method method) {
            return !method.isSynthetic() && AnnotationUtils.findAnnotation(method, MappingApi.class) != null;
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
            MappingApi mappingApi = AnnotationUtils.findAnnotation(method, MappingApi.class);
            boolean ignoreSign = mappingApi.ignoreSign() ? true : this.serviceAnno.sign();
            boolean ignoreValidate = mappingApi.ignoreValidate() ? true : this.serviceAnno.validate();

            boolean isWrapResult = this.serviceAnno.wrap() ? mappingApi.wrapResult() : false;

            ApiDefinition apiDefinition = new ApiDefinition();
            apiDefinition.setIgnoreSign(ignoreSign);
            apiDefinition.setIgnoreValidate(ignoreValidate);
            apiDefinition.setWrapResult(isWrapResult);
            apiDefinition.setHandler(handler);
            apiDefinition.setMethod(method);
            apiDefinition.setName(mappingApi.name());
            apiDefinition.setNoReturn(mappingApi.noReturn());
            apiDefinition.setIgnoreJWT(mappingApi.ignoreJWT());
            apiDefinition.setIgnoreToken(mappingApi.isIgnoreToken());
            String version = mappingApi.version();
            if (Normal.EMPTY.equals(version.trim())) {
                version = config.getVersion();
            }
            apiDefinition.setVersion(version);

            Parameter[] parameters = method.getParameters();
            Class<?> paramClass = null;
            if (parameters != null && parameters.length > 0) {
                Parameter parameter = parameters[0];
                paramClass = parameter.getType();
                boolean isNumberOrStringType = ClassKit.isNumberOrStringType(paramClass);
                apiDefinition.setSingleParameter(isNumberOrStringType);
                apiDefinition.setMethodArguClass(paramClass);
                if (isNumberOrStringType) {
                    SingleParameterContext.add(handler, method, parameter);
                }
            }

            Logger.debug("注册接口name={},version={},method={} {}({})", mappingApi.name(), mappingApi.version(),
                    method.getReturnType().getName(), method.getName(), paramClass == null ? Normal.EMPTY : paramClass.getName());

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
