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
package org.miaixz.bus.validate;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.magic.Checker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the context of the current validation operation. This class holds configuration and state for a validation
 * process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Context {

    /**
     * The global error code. If a validation annotation uses -1 as its error code, it will be replaced by this global
     * error code by default.
     */
    private String errcode = Builder.DEFAULT_ERRCODE;

    /**
     * The currently active validation groups.
     */
    private List<String> group = new ArrayList<>();

    /**
     * The global validation exception. When a validation fails, if this global exception is defined, it will be thrown.
     * Otherwise, it checks for a field-specific exception, and then a validator-specific exception. If none are
     * defined, a {@link ValidateException} is thrown.
     */
    private Class<? extends ValidateException> exception;

    /**
     * The fields that are currently activated for validation.
     */
    private String[] field;

    /**
     * The fields to be skipped during the current validation.
     */
    private String[] skip;

    /**
     * The validation checker responsible for performing the validation logic.
     */
    private Checker checker;

    /**
     * Fast-fail mode. Default is {@code true}. If {@code true}, the validation process will throw an exception
     * immediately upon the first failure. If {@code false}, all validators for a field will be executed even if one
     * fails, and the exception will be thrown at the end.
     */
    private boolean fast = true;

    /**
     * Whether to validate the internal fields of an object. Default is {@code false}.
     */
    private boolean inside = false;

    /**
     * Default constructor for creating a new validation context.
     */
    public Context() {
    }

    /**
     * Creates a new validation context instance with a default checker registry.
     *
     * @return a new {@code Context} object.
     */
    public static Context newInstance() {
        Context context = new Context();
        context.setChecker(new Checker());
        return context;
    }

    /**
     * Adds validation groups to the current context.
     *
     * @param groups The validation groups to add.
     */
    public void addGroups(String... groups) {
        if (ObjectKit.isEmpty(groups) || groups.length == 0) {
            return;
        }
        if (CollKit.isEmpty(this.group)) {
            this.group = new ArrayList<>();
        }
        this.group.addAll(Arrays.asList(groups));
    }

}
