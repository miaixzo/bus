/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.validate.magic.Criterion;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation result collector.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Collector {

    /**
     * The object being validated.
     */
    private Verified target;

    /**
     * The list of validation results.
     */
    private List<Collector> result;

    /**
     * The validation criterion associated with this result.
     */
    private Criterion criterion;

    /**
     * The pass/fail status of this validation result.
     */
    private boolean pass;

    /**
     * Constructs a new Collector for a validation target.
     *
     * @param target the object being validated.
     */
    public Collector(Verified target) {
        this.target = target;
        this.result = new ArrayList<>();
    }

    /**
     * Constructs a new Collector that wraps another collector.
     *
     * @param collector the existing collector.
     */
    public Collector(Collector collector) {
        this.target = collector.getTarget();
        this.result = new ArrayList<>();
        this.result.add(collector);
    }

    /**
     * Constructs a new Collector representing a single validation result.
     *
     * @param target    the object being validated.
     * @param criterion the validation criterion.
     * @param pass      whether the validation passed.
     */
    public Collector(Verified target, Criterion criterion, boolean pass) {
        this.target = target;
        this.criterion = criterion;
        this.pass = pass;
    }

    /**
     * Adds a validation result to this collector.
     *
     * @param collector the validation result to collect.
     */
    public void collect(Collector collector) {
        this.result.add(collector);
    }

    /**
     * Recursively retrieves all validation results from this collector and its sub-collectors.
     *
     * @return a flattened list of all validation results.
     */
    public List<Collector> getResult() {
        List<Collector> list = new ArrayList<>(Normal._16);
        for (Collector collector : this.result) {
            if (collector instanceof Collector) {
                list.addAll(collector.getResult());
            } else {
                throw new IllegalArgumentException("Unsupported collector type for validation results: " + collector);
            }
        }
        return list;
    }

    /**
     * Gets the target object of this validation.
     *
     * @return the {@link Verified} target object.
     */
    public Verified getTarget() {
        return target;
    }

    /**
     * Checks if this validation and all its sub-validations passed.
     *
     * @return {@code true} if all validations passed, {@code false} otherwise.
     */
    public boolean isPass() {
        return this.result.stream().allMatch(Collector::isPass);
    }

}
