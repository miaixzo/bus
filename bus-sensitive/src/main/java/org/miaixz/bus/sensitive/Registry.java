/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.sensitive;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.sensitive.magic.annotation.Strategy;
import org.miaixz.bus.sensitive.metric.*;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统中内置的策略映射 注解和实现之间映射
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Registry {

    /**
     * 策略组列表
     */
    private static Map<Builder.Type, StrategyProvider> STRATEGY_CACHE = new ConcurrentHashMap<>();

    static {
        register(Builder.Type.ADDRESS, new AddressProvider());
        register(Builder.Type.BANK_CARD, new BandCardProvider());
        register(Builder.Type.CNAPS_CODE, new CnapsProvider());
        register(Builder.Type.DEFAUL, new DafaultProvider());
        register(Builder.Type.EMAIL, new EmailProvider());
        register(Builder.Type.CITIZENID, new CitizenIdProvider());
        register(Builder.Type.MOBILE, new MobileProvider());
        register(Builder.Type.NAME, new NameProvider());
        register(Builder.Type.NONE, new NoneProvider());
        register(Builder.Type.PASSWORD, new PasswordProvider());
        register(Builder.Type.PAY_SIGN_NO, new CardProvider());
        register(Builder.Type.PHONE, new PhoneProvider());
    }

    /**
     * 注册组件
     *
     * @param name   组件名称
     * @param object 组件对象
     */
    public static void register(Builder.Type name, StrategyProvider object) {
        if (STRATEGY_CACHE.containsKey(name)) {
            throw new InternalException("重复注册同名称的组件：" + name);
        }
        Class<?> clazz = object.getClass();
        if (STRATEGY_CACHE.containsKey(clazz.getSimpleName())) {
            throw new InternalException("重复注册同类型的组件：" + clazz);
        }
        STRATEGY_CACHE.putIfAbsent(name, object);
    }

    /**
     * 生成脱敏工具
     *
     * @param name 模型
     * @return the object
     */
    public static StrategyProvider require(Builder.Type name) {
        StrategyProvider sensitiveProvider = STRATEGY_CACHE.get(name);
        if (ObjectKit.isEmpty(sensitiveProvider)) {
            throw new IllegalArgumentException("none sensitiveProvider be found!, type:" + name);
        }
        return sensitiveProvider;
    }

    /**
     * 获取对应的系统内置实现
     *
     * @param annotationClass 注解实现类
     * @return 对应的实现方式
     */
    public static StrategyProvider require(final Class<? extends Annotation> annotationClass) {
        StrategyProvider strategy = STRATEGY_CACHE.get(annotationClass);
        if (ObjectKit.isEmpty(strategy)) {
            throw new InternalException("不支持的系统内置方法,用户请勿在自定义注解中使用[BuiltInStrategy]!");
        }
        return strategy;
    }

    /**
     * 获取策略
     *
     * @param annotations 字段对应注解
     * @return 策略
     */
    public static StrategyProvider require(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Strategy sensitiveStrategy = annotation.annotationType().getAnnotation(Strategy.class);
            if (ObjectKit.isNotEmpty(sensitiveStrategy)) {
                Class<? extends StrategyProvider> clazz = sensitiveStrategy.value();
                StrategyProvider strategy;
                if (BuiltInProvider.class.equals(clazz)) {
                    strategy = Registry.require(annotation.annotationType());
                } else {
                    strategy = ReflectKit.newInstance(clazz);
                }
                return strategy;
            }
        }
        return null;
    }

    /**
     * 是否包含指定名称策略
     *
     * @param name 策略名称
     * @return true：包含, false：不包含
     */
    public boolean contains(String name) {
        return STRATEGY_CACHE.containsKey(name);
    }

}
