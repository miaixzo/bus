package org.aoju.bus.sensitive;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.lang.exception.ValidateException;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.sensitive.annotation.Strategy;
import org.aoju.bus.sensitive.provider.StrategyProvider;
import org.aoju.bus.sensitive.strategy.*;
import org.aoju.bus.sensitive.strategy.*;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统中内置的策略映射
 * 1. 注解和实现之间映射
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Registry {

    /**
     * 验证组列表
     */
    private static Map<Builder.Type, StrategyProvider> STRATEGY_CACHE = new ConcurrentHashMap<>();

    static {
        register(Builder.Type.ADDRESS, new AddressStrategy());
        register(Builder.Type.BANK_CARD, new BandCardStrategy());
        register(Builder.Type.CNAPS_CODE, new CnapsStrategy());
        register(Builder.Type.DEFAUL, new DafaultStrategy());
        register(Builder.Type.EMAIL, new EmailStrategy());
        register(Builder.Type.ID_CARD, new IDCardStrategy());
        register(Builder.Type.MOBILE, new MobileStrategy());
        register(Builder.Type.NAME, new NameStrategy());
        register(Builder.Type.NONE, new NoneStrategy());
        register(Builder.Type.PASSWORD, new PasswordStrategy());
        register(Builder.Type.PAY_SIGN_NO, new PayStrategy());
        register(Builder.Type.PHONE, new PhoneStrategy());
    }

    /**
     * 注册组件
     *
     * @param type  组件名称
     * @param objet 组件对象
     */
    public static void register(Builder.Type type, StrategyProvider objet) {
        if (STRATEGY_CACHE.containsKey(type)) {
            throw new ValidateException("重复注册同名称的组件：" + type);
        }
        Class<?> clazz = objet.getClass();
        if (STRATEGY_CACHE.containsKey(clazz.getSimpleName())) {
            throw new ValidateException("重复注册同类型的组件：" + clazz);
        }
        STRATEGY_CACHE.putIfAbsent(type, objet);
    }

    /**
     * 生成脱敏工具
     */
    public static StrategyProvider require(Builder.Type type) {
        StrategyProvider sensitiveProvider = STRATEGY_CACHE.get(type);
        if (sensitiveProvider == null) {
            throw new IllegalArgumentException("none sensitiveProvider be found!, type:" + type.name());
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
        if (ObjectUtils.isNull(strategy)) {
            throw new InstrumentException("不支持的系统内置方法，用户请勿在自定义注解中使用[BuiltInStrategy]!");
        }
        return strategy;
    }

    /**
     * 获取策略
     *
     * @param annotations 字段对应注解
     * @return 策略
     * @since 0.0.6
     */
    public static StrategyProvider require(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Strategy sensitiveStrategy = annotation.annotationType().getAnnotation(Strategy.class);
            if (ObjectUtils.isNotNull(sensitiveStrategy)) {
                Class<? extends StrategyProvider> clazz = sensitiveStrategy.value();
                StrategyProvider strategy;
                if (BuiltInStrategy.class.equals(clazz)) {
                    strategy = Registry.require(annotation.annotationType());
                } else {
                    strategy = ClassUtils.newInstance(clazz);
                }
                return strategy;
            }
        }
        return null;
    }

}
