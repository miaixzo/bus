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
package org.miaixz.bus.logger;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.lang.Normal;

/**
 * Thread-safe executor that applies neutral log operators in registration order.
 *
 * @author Kimi Liu
 */
public class Executor {

    /**
     * Identity-based registration counts shared by the static logger facade.
     */
    private static final Map<Operator, Integer> REGISTRATIONS = new IdentityHashMap<>();

    /**
     * Immutable operator snapshot read without locking on the logging hot path.
     */
    private static volatile List<Operator> OPERATORS = List.of();

    /**
     * Exposes the stateless executor facade; operator registrations remain shared by the static logging pipeline.
     */
    public Executor() {
        // No initialization required.
    }

    /**
     * Registers an operator by instance identity and retains repeated context registrations.
     *
     * @param operator operator to register
     */
    public static synchronized void register(Operator operator) {
        Operator required = Objects.requireNonNull(operator, "operator");
        Integer count = REGISTRATIONS.get(required);
        REGISTRATIONS.put(required, count == null ? 1 : count + 1);
        if (count == null) {
            List<Operator> updated = new ArrayList<>(OPERATORS);
            updated.add(required);
            OPERATORS = List.copyOf(updated);
        }
    }

    /**
     * Unregisters a previously registered operator.
     *
     * @param operator operator to unregister
     */
    public static synchronized void unregister(Operator operator) {
        Integer count = operator == null ? null : REGISTRATIONS.get(operator);
        if (count == null) {
            return;
        }
        if (count > 1) {
            REGISTRATIONS.put(operator, count - 1);
        } else {
            REGISTRATIONS.remove(operator);
            OPERATORS = OPERATORS.stream().filter(candidate -> candidate != operator).toList();
        }
    }

    /**
     * Applies every registered operator while preserving logging availability when an extension fails.
     *
     * @param loggable original loggable data
     * @return processed loggable data
     */
    public static Loggable process(Loggable loggable) {
        Loggable current = Objects.requireNonNull(loggable, "loggable");
        for (Operator operator : OPERATORS) {
            try {
                Loggable processed = operator.apply(current);
                if (processed != null) {
                    current = processed;
                }
            } catch (RuntimeException ignored) {
                return new Loggable(current.level(), current.throwable(), "[LOG PROCESSING FAILED]",
                        Normal.EMPTY_OBJECT_ARRAY);
            }
        }
        return current;
    }

    /**
     * Applies registered operators to one named value used in structured diagnostic output.
     *
     * @param key   diagnostic field name
     * @param value diagnostic field value
     * @return processed diagnostic value
     */
    public static Object processValue(String key, Object value) {
        Object current = value;
        for (Operator operator : OPERATORS) {
            try {
                current = operator.applyValue(key, current);
            } catch (RuntimeException ignored) {
                return "[LOG PROCESSING FAILED]";
            }
        }
        return current;
    }

}
