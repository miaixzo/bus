/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.builder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.annotation.RegisterMapper;
import org.miaixz.bus.mapper.builder.resolve.EntityResolve;
import org.miaixz.bus.mapper.entity.EntityTable;
import org.miaixz.bus.mapper.entity.Property;
import org.miaixz.bus.mapper.provider.EmptyProvider;
import org.miaixz.bus.mapper.support.MetaObject;
import org.miaixz.bus.mapper.support.Reflector;

/**
 * 处理主要逻辑，最关键的一个类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperBuilder {

    /**
     * 注册的接口
     */
    private List<Class<?>> registerClass = new ArrayList<>();
    /**
     * 注册的通用Mapper接口
     */
    private Map<Class<?>, Collection<MapperTemplate>> registerMapper = new ConcurrentHashMap<>();
    /**
     * 通用Mapper配置
     */
    private Property property = new Property();

    /**
     * 默认构造方法
     */
    public MapperBuilder() {

    }

    /**
     * 带配置的构造方法
     *
     * @param properties 属性
     */
    public MapperBuilder(Properties properties) {
        this();
        setProperties(properties);
    }

    /**
     * 通过通用Mapper接口获取对应的MapperTemplate
     *
     * @param mapperClass mapper类
     * @return the object
     * @throws Exception 异常
     */
    private Collection<MapperTemplate> fromMapperClasses(Class<?> mapperClass) {
        Map<Class<?>, MapperTemplate> templateMap = new ConcurrentHashMap<>();
        Method[] methods = mapperClass.getDeclaredMethods();
        for (Method method : methods) {
            Class<?> templateClass = null;
            if (method.isAnnotationPresent(SelectProvider.class)) {
                SelectProvider provider = method.getAnnotation(SelectProvider.class);
                templateClass = provider.type();
            } else if (method.isAnnotationPresent(InsertProvider.class)) {
                InsertProvider provider = method.getAnnotation(InsertProvider.class);
                templateClass = provider.type();
            } else if (method.isAnnotationPresent(DeleteProvider.class)) {
                DeleteProvider provider = method.getAnnotation(DeleteProvider.class);
                templateClass = provider.type();
            } else if (method.isAnnotationPresent(UpdateProvider.class)) {
                UpdateProvider provider = method.getAnnotation(UpdateProvider.class);
                templateClass = provider.type();
            }
            if (templateClass == null || !MapperTemplate.class.isAssignableFrom(templateClass)) {
                templateClass = EmptyProvider.class;
            }
            MapperTemplate mapperTemplate;
            try {
                mapperTemplate = templateMap.getOrDefault(templateClass, (MapperTemplate) templateClass
                        .getConstructor(Class.class, MapperBuilder.class).newInstance(mapperClass, this));
                templateMap.put(templateClass, mapperTemplate);
            } catch (Exception e) {
                Logger.error("实例化MapperTemplate对象失败:" + e, e);
                throw new MapperException("实例化MapperTemplate对象失败:" + e.getMessage());
            }
            // 注册方法
            try {
                mapperTemplate.addMethodMap(method.getName(),
                        templateClass.getMethod(method.getName(), MappedStatement.class));
            } catch (NoSuchMethodException e) {
                Logger.error(templateClass.getName() + "中缺少" + method.getName() + "方法!", e);
                throw new MapperException(templateClass.getName() + "中缺少" + method.getName() + "方法!");
            }
        }
        return templateMap.values();
    }

    /**
     * 注册通用Mapper接口
     *
     * @param mapperClass mapper
     */
    public void registerMapper(Class<?> mapperClass) {
        if (!registerMapper.containsKey(mapperClass)) {
            registerClass.add(mapperClass);
            registerMapper.put(mapperClass, fromMapperClasses(mapperClass));
        }
        // 自动注册继承的接口
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
     * @param mapperClass mapper类
     */
    public void registerMapper(String mapperClass) {
        try {
            registerMapper(Class.forName(mapperClass));
        } catch (ClassNotFoundException e) {
            Logger.error("注册通用Mapper[" + mapperClass + "]失败，找不到该通用Mapper!", e);
            throw new MapperException("注册通用Mapper[" + mapperClass + "]失败，找不到该通用Mapper!");
        }
    }

    /**
     * 判断当前的接口方法是否需要进行拦截
     *
     * @param msId 方法信息
     * @return the object
     */
    public MapperTemplate isMapperMethod(String msId) {
        MapperTemplate mapperTemplate = getMapperTemplateByMsId(msId);
        if (mapperTemplate == null) {
            // 通过 @RegisterMapper 注解自动注册的功能
            try {
                Class<?> mapperClass = Reflector.getMapperClass(msId);
                if (mapperClass.isInterface() && hasRegisterMapper(mapperClass)) {
                    mapperTemplate = getMapperTemplateByMsId(msId);
                }
            } catch (Exception e) {
                Logger.warn("特殊情况: " + e);
            }
        }
        return mapperTemplate;
    }

    /**
     * 根据 msId 获取 MapperTemplate
     *
     * @param msId 方法信息
     * @return the object
     */
    public MapperTemplate getMapperTemplateByMsId(String msId) {
        for (Map.Entry<Class<?>, Collection<MapperTemplate>> entry : registerMapper.entrySet()) {
            for (MapperTemplate t : entry.getValue()) {
                if (t.supportMethod(msId)) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * 判断接口是否包含通用接口，
     *
     * @param mapperInterface 接口信息
     * @return the boolean
     */
    public boolean isExtendCommonMapper(Class<?> mapperInterface) {
        for (Class<?> mapperClass : registerClass) {
            if (mapperClass.isAssignableFrom(mapperInterface)) {
                return true;
            }
        }
        // 通过 @RegisterMapper 注解自动注册的功能
        return hasRegisterMapper(mapperInterface);
    }

    /**
     * 增加通过 @RegisterMapper 注解自动注册的功能
     *
     * @param mapperInterface 接口信息
     * @return the boolean
     */
    private boolean hasRegisterMapper(Class<?> mapperInterface) {
        // 如果一个都没匹配上，很可能是还没有注册 mappers，此时通过 @RegisterMapper 注解进行判断
        Class<?>[] interfaces = mapperInterface.getInterfaces();
        boolean hasRegisterMapper = false;
        if (interfaces != null && interfaces.length > 0) {
            for (Class<?> anInterface : interfaces) {
                // 自动注册标记了 @RegisterMapper 的接口
                if (anInterface.isAnnotationPresent(RegisterMapper.class)) {
                    hasRegisterMapper = true;
                    // 如果已经注册过，就避免在反复调用下面会迭代的方法
                    if (!registerMapper.containsKey(anInterface)) {
                        registerMapper(anInterface);
                    }
                }
                // 如果父接口的父接口存在注解，也可以注册
                else if (hasRegisterMapper(anInterface)) {
                    hasRegisterMapper = true;
                }
            }
        }
        return hasRegisterMapper;
    }

    /**
     * 配置完成后，执行下面的操作 处理configuration中全部的MappedStatement
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
            prefix = mapperInterface.getName();
        } else {
            prefix = Normal.EMPTY;
        }
        for (Object object : new ArrayList<Object>(configuration.getMappedStatements())) {
            if (object instanceof MappedStatement) {
                MappedStatement ms = (MappedStatement) object;
                if (ms.getId().startsWith(prefix)) {
                    processMappedStatement(ms);
                }
            }
        }
    }

    /**
     * 处理 MappedStatement
     *
     * @param ms MappedStatement
     */
    public void processMappedStatement(MappedStatement ms) {
        MapperTemplate mapperTemplate = isMapperMethod(ms.getId());

        if (mapperTemplate != null && ms.getSqlSource() instanceof ProviderSqlSource) {
            setSqlSource(ms, mapperTemplate);
        }

        // 如果是原生mybatisSqlSource的查询，添加ResultMap
        if (ms.getSqlSource() instanceof RawSqlSource && ms.getSqlCommandType() == SqlCommandType.SELECT) {
            if (ms.getResultMaps() != null && !ms.getResultMaps().isEmpty()) {
                setRawSqlSourceMapper(ms);
            }
        }
    }

    /**
     * 获取通用Mapper配置
     *
     * @return the object
     */
    public Property getConfig() {
        return property;
    }

    /**
     * 设置通用Mapper配置
     *
     * @param property 配置
     */
    public void setConfig(Property property) {
        this.property = property;
        if (property.getResolveClass() != null) {
            try {
                EntityBuilder.setResolve(property.getResolveClass().newInstance());
            } catch (Exception e) {
                Logger.error("创建 " + property.getResolveClass().getName() + " 实例失败，请保证该类有默认的构造方法!", e);
                throw new MapperException("创建 " + property.getResolveClass().getName() + " 实例失败，请保证该类有默认的构造方法!", e);
            }
        }
        if (property.getMappers() != null && property.getMappers().size() > 0) {
            for (Class mapperClass : property.getMappers()) {
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
        property.setProperties(properties);
        // 注册解析器
        if (properties != null) {
            String resolveClass = properties.getProperty("resolveClass");
            if (StringKit.isNotEmpty(resolveClass)) {
                try {
                    EntityBuilder.setResolve((EntityResolve) Class.forName(resolveClass).newInstance());
                } catch (Exception e) {
                    Logger.error("创建 " + resolveClass + " 实例失败!", e);
                    throw new MapperException("创建 " + resolveClass + " 实例失败!", e);
                }
            }
        }
        // 注册通用接口
        if (properties != null) {
            String mapper = properties.getProperty("mappers");
            if (StringKit.isNotEmpty(mapper)) {
                String[] mappers = mapper.split(Symbol.COMMA);
                for (String mapperClass : mappers) {
                    if (mapperClass.length() > 0) {
                        registerMapper(mapperClass);
                    }
                }
            }
        }
    }

    /**
     * 重新设置SqlSource 执行该方法前必须使用isMapperMethod判断，否则msIdCache会空
     *
     * @param ms             MappedStatement
     * @param mapperTemplate 模板信息
     */
    public void setSqlSource(MappedStatement ms, MapperTemplate mapperTemplate) {
        try {
            if (mapperTemplate != null) {
                mapperTemplate.setSqlSource(ms);
            }
        } catch (Exception e) {
            throw new MapperException(e);
        }
    }

    /**
     * 设置原生Mybatis查询的实体映射， JPA的注解优先级将高于mybatis自动映射
     */
    public void setRawSqlSourceMapper(MappedStatement ms) {

        EntityTable entityTable = EntityBuilder.getEntityTableOrNull(ms.getResultMaps().get(0).getType());
        if (entityTable != null) {
            List<ResultMap> resultMaps = new ArrayList<>();
            ResultMap resultMap = entityTable.getResultMap(ms.getConfiguration());
            if (resultMap != null) {
                resultMaps.add(resultMap);
                org.apache.ibatis.reflection.MetaObject metaObject = MetaObject.forObject(ms);
                metaObject.setValue("resultMaps", Collections.unmodifiableList(resultMaps));
            }
        }
    }

}