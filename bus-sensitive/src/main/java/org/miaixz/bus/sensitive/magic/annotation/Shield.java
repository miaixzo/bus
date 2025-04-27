/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.sensitive.magic.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.sensitive.Builder;
import org.miaixz.bus.sensitive.magic.Condition;
import org.miaixz.bus.sensitive.metric.ConditionProvider;
import org.miaixz.bus.sensitive.metric.DafaultProvider;
import org.miaixz.bus.sensitive.metric.StrategyProvider;

/**
 * 标注在字段上,用以说明字段上那些类型需要脱敏 脱敏后,插件在写请求后对数据脱敏后存在数据库,对读请求不拦截 设计的考虑： 本来想过将生效条件单独抽离为一个注解,这样可以达到条件注解的复用
 * 但是有一个缺点,当指定多个策略时,条件的注解就会太宽泛,无法保证精细到每一个策略生效的场景 平衡的方式： 在 Strategy 注解中,可以指定策略 默认是全部,如果指定,则只针对其中的某个策略生效
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Shield {

    /**
     * json中的key的信息
     *
     * @return the string
     */
    String key() default Normal.EMPTY;

    /**
     * 脱敏类型 不同的脱敏类型置换*的方式不同
     *
     * @return the object
     */
    Builder.Type type() default Builder.Type.NONE;

    /**
     * 脱敏模型 不同的脱敏类型脱敏模型可自定义模型
     *
     * @return the object
     */
    Builder.Mode mode() default Builder.Mode.MIDDLE;

    /**
     * 该属性从哪个字段取得
     *
     * @return the string
     */
    String field() default Normal.EMPTY;

    /**
     * 设置遮挡字符
     *
     * @return the string
     */
    String shadow() default Symbol.STAR;

    /**
     * 固定的头部字符数量
     *
     * @return the int
     */
    int fixedHeaderSize() default 0;

    /**
     * 固定的尾部字符数量
     *
     * @return the int
     */
    int fixedTailorSize() default 3;

    /**
     * 自动头尾固定部分
     *
     * @return the boolean
     */
    boolean autoFixedPart() default true;

    /**
     * 注解生效的条件
     *
     * @return the object
     */
    Class<? extends ConditionProvider> condition() default Condition.class;

    /**
     * 脱敏策略
     *
     * @return the object
     */
    Class<? extends StrategyProvider> strategy() default DafaultProvider.class;

}
