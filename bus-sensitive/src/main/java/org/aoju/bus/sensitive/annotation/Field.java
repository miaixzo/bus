package org.aoju.bus.sensitive.annotation;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.sensitive.Builder;
import org.aoju.bus.sensitive.Condition;
import org.aoju.bus.sensitive.provider.ConditionProvider;
import org.aoju.bus.sensitive.provider.StrategyProvider;
import org.aoju.bus.sensitive.strategy.DafaultStrategy;

import java.lang.annotation.*;


/**
 * 标注在字段上，用以说明字段上那些类型需要脱敏
 * 脱敏后，插件在写请求后对数据脱敏后存在数据库，对读请求不拦截
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

    /**
     * json中的key的信息
     */
    String key() default "";

    /**
     * 脱敏类型
     * 不同的脱敏类型置换*的方式不同
     */
    Builder.Type type() default Builder.Type.NONE;

    /**
     * 脱敏模型
     * 不同的脱敏类型脱敏模型可自定义模型
     */
    Builder.Mode mode() default Builder.Mode.MIDDLE;

    /**
     * 该属性从哪个字段取得
     */
    String field() default "";

    /**
     * 设置遮挡字符
     */
    char maskChar() default Symbol.C_STAR;

    /**
     * 固定的头部字符数量
     */
    int fixedHeaderSize() default 0;

    /**
     * 固定的尾部字符数量
     */
    int fixedTailorSize() default 3;

    /**
     * 自动头尾固定部分
     */
    boolean autoFixedPart() default true;

    /**
     * 注解生效的条件
     */
    Class<? extends ConditionProvider> condition() default Condition.class;

    /**
     * 脱敏策略
     */
    Class<? extends StrategyProvider> strategy() default DafaultStrategy.class;

    /**
     * 是否加密
     */
    boolean encrypt() default false;

    /**
     * 是否解密
     */
    boolean decrypt() default false;

}
