/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
