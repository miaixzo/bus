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
package org.aoju.bus.mapper.builder;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.entity.Config;
import org.aoju.bus.mapper.provider.EmptyProvider;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理主要逻辑,最关键的一个类
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
public class MapperBuilder {

    /**
     * 缓存skip结果
     */
    private final Map<String, Boolean> msIdSkip = new ConcurrentHashMap<>();

    /**
     * 注册的接口
     */
    private List<Class<?>> registerClass = new ArrayList<>();

    /**
     * 注册的通用Mapper接口
     */
    private Map<Class<?>, MapperTemplate> registerMapper = new ConcurrentHashMap<>();

    /**
     * 缓存msid和MapperTemplate
     */
    private Map<String, MapperTemplate> msIdCache = new ConcurrentHashMap<>();

    /**
     * 通用Mapper配置
     */
    private Config config = new Config();

    /**
     * 默认构造方法
     */
    public MapperBuilder() {
    }

    public MapperBuilder(Properties properties) {
        this();
        setProperties(properties);
    }

    /**
     * 通过通用Mapper接口获取对应的MapperTemplate
     *
     * @param mapperClass 对象
     * @return 结果
     */
    private MapperTemplate fromMapperClass(Class<?> mapperClass) {
        Method[] methods = mapperClass.getDeclaredMethods();
        Class<?> templateClass = null;
        Class<?> tempClass = null;
        Set<String> methodSet = new HashSet<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(SelectProvider.class)) {
                SelectProvider provider = method.getAnnotation(SelectProvider.class);
                tempClass = provider.type();
                methodSet.add(method.getName());
            } else if (method.isAnnotationPresent(InsertProvider.class)) {
                InsertProvider provider = method.getAnnotation(InsertProvider.class);
                tempClass = provider.type();
                methodSet.add(method.getName());
            } else if (method.isAnnotationPresent(DeleteProvider.class)) {
                DeleteProvider provider = method.getAnnotation(DeleteProvider.class);
                tempClass = provider.type();
                methodSet.add(method.getName());
            } else if (method.isAnnotationPresent(UpdateProvider.class)) {
                UpdateProvider provider = method.getAnnotation(UpdateProvider.class);
                tempClass = provider.type();
                methodSet.add(method.getName());
            }
            if (templateClass == null) {
                templateClass = tempClass;
            } else if (templateClass != tempClass) {
                throw new InstrumentException("一个通用Mapper中只允许存在一个MapperTemplate子类!");
            }
        }
        if (templateClass == null || !MapperTemplate.class.isAssignableFrom(templateClass)) {
            templateClass = EmptyProvider.class;
        }
        MapperTemplate mapperTemplate;
        try {
            mapperTemplate = (MapperTemplate) templateClass.getConstructor(Class.class, MapperBuilder.class).newInstance(mapperClass, this);
        } catch (Exception e) {
            throw new InstrumentException("实例化MapperTemplate对象失败:" + e.getMessage());
        }
        //注册方法
        for (String methodName : methodSet) {
            try {
                mapperTemplate.addMethodMap(methodName, templateClass.getMethod(methodName, MappedStatement.class));
            } catch (NoSuchMethodException e) {
                throw new InstrumentException(templateClass.getCanonicalName() + "中缺少" + methodName + "方法!");
            }
        }
        return mapperTemplate;
    }

    /**
     * 注册通用Mapper接口
     *
     * @param mapperClass 对象
     */
    public void registerMapper(Class<?> mapperClass) {
        if (!registerMapper.containsKey(mapperClass)) {
            registerClass.add(mapperClass);
            registerMapper.put(mapperClass, fromMapperClass(mapperClass));
        }
        //自动注册继承的接口
        Class<?>[] interfaces = mapperClass.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                registerMapper(anInterface);
            }
        }
    }

    /**
     * 注册通用Mapper接口
     *
     * @param mapperClass 对象
     */
    public void registerMapper(String mapperClass) {
        try {
            registerMapper(Class.forName(mapperClass));
        } catch (ClassNotFoundException e) {
            throw new InstrumentException("注册通用Mapper[" + mapperClass + "]失败,找不到该通用Mapper!");
        }
    }

    /**
     * 判断当前的接口方法是否需要进行拦截
     *
     * @param msId 方法id
     * @return the boolean
     */
    public boolean isMapperMethod(String msId) {
        if (msIdSkip.get(msId) != null) {
            return msIdSkip.get(msId);
        }
        for (Map.Entry<Class<?>, MapperTemplate> entry : registerMapper.entrySet()) {
            if (entry.getValue().supportMethod(msId)) {
                msIdSkip.put(msId, true);
                msIdCache.put(msId, entry.getValue());
                return true;
            }
        }
        msIdSkip.put(msId, false);
        return false;
    }

    /**
     * 判断接口是否包含通用接口
     *
     * @param mapperInterface 接口
     * @return the boolean
     */
    public boolean isExtendCommonMapper(Class<?> mapperInterface) {
        for (Class<?> mapperClass : registerClass) {
            if (mapperClass.isAssignableFrom(mapperInterface)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果当前注册的接口为空,自动注册默认接口
     */
    public void ifEmptyRegisterDefaultInterface() {
        if (registerClass.size() == 0) {
            registerMapper("org.aoju.bus.mapper.common.Mapper");
        }
    }

    /**
     * 配置完成后,执行下面的操作
     * 处理configuration中全部的MappedStatement
     *
     * @param configuration 配置
     */
    public void processConfiguration(Configuration configuration) {
        processConfiguration(configuration, null);
    }

    /**
     * 配置指定的接口
     *
     * @param configuration   配置
     * @param mapperInterface 接口
     */
    public void processConfiguration(Configuration configuration, Class<?> mapperInterface) {
        String prefix;
        if (mapperInterface != null) {
            prefix = mapperInterface.getCanonicalName();
        } else {
            prefix = Normal.EMPTY;
        }
        for (Object object : new ArrayList<Object>(configuration.getMappedStatements())) {
            if (object instanceof MappedStatement) {
                MappedStatement ms = (MappedStatement) object;
                if (ms.getId().startsWith(prefix) && isMapperMethod(ms.getId())) {
                    if (ms.getSqlSource() instanceof ProviderSqlSource) {
                        setSqlSource(ms);
                    }
                }
            }
        }
    }

    /**
     * 获取通用Mapper配置
     *
     * @return 配置信息
     */
    public Config getConfig() {
        return config;
    }

    /**
     * 设置通用Mapper配置
     *
     * @param config 配置信息
     */
    public void setConfig(Config config) {
        this.config = config;
        if (config.getMappers() != null && config.getMappers().size() > 0) {
            for (Class mapperClass : config.getMappers()) {
                registerMapper(mapperClass);
            }
        }
    }

    /**
     * 配置属性
     *
     * @param properties 属性
     */
    public void setProperties(Properties properties) {
        config.setProperties(properties);
        String mapper = null;
        if (properties != null) {
            mapper = properties.getProperty("mappers");
        }
        if (Assert.isNotEmpty(mapper)) {
            String[] mappers = mapper.split(Symbol.COMMA);
            for (String mapperClass : mappers) {
                if (mapperClass.length() > 0) {
                    registerMapper(mapperClass);
                }
            }
        }
    }

    /**
     * 重新设置SqlSource
     * 执行该方法前必须使用isMapperMethod判断,否则msIdCache会空
     *
     * @param ms MappedStatement
     */
    public void setSqlSource(MappedStatement ms) {
        MapperTemplate mapperTemplate = msIdCache.get(ms.getId());
        try {
            if (mapperTemplate != null) {
                mapperTemplate.setSqlSource(ms);
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

}
