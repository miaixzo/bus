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

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.sensitive.metric.ConditionProvider;
import org.miaixz.bus.sensitive.metric.DafaultProvider;
import org.miaixz.bus.sensitive.metric.StrategyProvider;

/**
 * Marks a field for desensitization. This annotation specifies the strategy and conditions for masking sensitive
 * information. It can also be used within {@link NShield} to define rules for keys in a JSON string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Shield {

    /**
     * The key name within a JSON string to which this rule applies. This is only used when this annotation is part of
     * an {@link NShield} annotation.
     *
     * @return The JSON key name.
     */
    String key() default Normal.EMPTY;

    /**
     * The built-in type of desensitization to apply (e.g., CHINESE_NAME, ID_CARD). Different types have different
     * masking rules.
     *
     * @return The desensitization type.
     */
    EnumValue.Masking type() default EnumValue.Masking.NONE;

    /**
     * The masking mode, which determines which part of the string to mask.
     *
     * @return The desensitization mode.
     */
    EnumValue.Mode mode() default EnumValue.Mode.MIDDLE;

    /**
     * The name of the field from which to get the value for desensitization. This is useful if the value to be
     * desensitized is in a different field than the one being annotated.
     *
     * @return The source field name.
     */
    String field() default Normal.EMPTY;

    /**
     * The character to use for masking sensitive information.
     *
     * @return The masking character.
     */
    String shadow() default Symbol.STAR;

    /**
     * The fixed number of characters to keep unmasked at the beginning of the string.
     *
     * @return The size of the unmasked header.
     */
    int fixedHeaderSize() default 0;

    /**
     * The fixed number of characters to keep unmasked at the end of the string.
     *
     * @return The size of the unmasked trailer.
     */
    int fixedTailorSize() default 3;

    /**
     * Whether to automatically determine the unmasked parts based on the specified {@link #type()}. If true,
     * {@link #fixedHeaderSize()} and {@link #fixedTailorSize()} may be ignored.
     *
     * @return {@code true} to enable auto-fixing, {@code false} otherwise.
     */
    boolean autoFixedPart() default true;

    /**
     * A custom condition class that determines if this annotation should be applied. The desensitization will only
     * occur if the condition is met.
     *
     * @return The condition provider class.
     */
    Class<? extends ConditionProvider> condition() default ConditionProvider.class;

    /**
     * A custom strategy class that defines the desensitization logic. This overrides the built-in {@link #type()}.
     *
     * @return The strategy provider class.
     */
    Class<? extends StrategyProvider> strategy() default DafaultProvider.class;

}
