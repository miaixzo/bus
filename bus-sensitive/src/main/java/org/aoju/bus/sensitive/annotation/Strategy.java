package org.aoju.bus.sensitive.annotation;

import org.aoju.bus.sensitive.provider.StrategyProvider;

import java.lang.annotation.*;

/**
 * 脱敏策略注解
 * <p>
 * 1.自定义的策略默认生效。
 * 2.如果有多个策略, 则优先执行一次满足条件的策略。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Strategy {

    /**
     * 自定义脱敏的策略实现
     *
     * @return 策略实现类信息
     */
    Class<? extends StrategyProvider> value();

}
