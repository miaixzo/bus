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
package org.miaixz.bus.image.builtin.hp;

import java.util.Arrays;

/**
 * Hanging Protocol filter operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum HangingProtocolFilter {

    /**
     * Constant for the member of value.
     */
    MEMBER_OF,
    /**
     * Constant for the not member of value.
     */
    NOT_MEMBER_OF,
    /**
     * Constant for the range incl value.
     */
    RANGE_INCL,
    /**
     * Constant for the range excl value.
     */
    RANGE_EXCL,
    /**
     * Constant for the greater or equal value.
     */
    GREATER_OR_EQUAL,
    /**
     * Constant for the less or equal value.
     */
    LESS_OR_EQUAL,
    /**
     * Constant for the greater than value.
     */
    GREATER_THAN,
    /**
     * Constant for the less than value.
     */
    LESS_THAN;

    /**
     * The epsilon value.
     */
    private static final double EPSILON = 1e-8;

    /**
     * Executes the matches operation.
     *
     * @param values      the values.
     * @param valueNumber the value number.
     * @param params      the params.
     * @return true if the condition is met; otherwise false.
     */
    public boolean matches(String[] values, int valueNumber, String... params) {
        return switch (this) {
            case MEMBER_OF -> selected(values, valueNumber).anyMatch(value -> contains(params, value));
            case NOT_MEMBER_OF -> selected(values, valueNumber).noneMatch(value -> contains(params, value));
            default -> throw new UnsupportedOperationException(name() + " is numeric");
        };
    }

    /**
     * Executes the matches operation.
     *
     * @param values      the values.
     * @param valueNumber the value number.
     * @param params      the params.
     * @return true if the condition is met; otherwise false.
     */
    public boolean matches(double[] values, int valueNumber, double... params) {
        return switch (this) {
            case MEMBER_OF -> selected(values, valueNumber).anyMatch(value -> contains(params, value));
            case NOT_MEMBER_OF -> selected(values, valueNumber).noneMatch(value -> contains(params, value));
            case RANGE_INCL -> selected(values, valueNumber)
                    .anyMatch(value -> value >= params[0] && value <= params[1]);
            case RANGE_EXCL -> selected(values, valueNumber)
                    .noneMatch(value -> value >= params[0] && value <= params[1]);
            case GREATER_OR_EQUAL -> selected(values, valueNumber).anyMatch(value -> value >= params[0]);
            case LESS_OR_EQUAL -> selected(values, valueNumber).anyMatch(value -> value <= params[0]);
            case GREATER_THAN -> selected(values, valueNumber).anyMatch(value -> value > params[0]);
            case LESS_THAN -> selected(values, valueNumber).anyMatch(value -> value < params[0]);
        };
    }

    /**
     * Executes the matches operation.
     *
     * @param values      the values.
     * @param valueNumber the value number.
     * @param params      the params.
     * @return true if the condition is met; otherwise false.
     */
    public boolean matches(int[] values, int valueNumber, int... params) {
        return matches(
                Arrays.stream(values).asDoubleStream().toArray(),
                valueNumber,
                Arrays.stream(params).asDoubleStream().toArray());
    }

    /**
     * Executes the value of code operation.
     *
     * @param codeString the code string.
     * @return the operation result.
     */
    public static HangingProtocolFilter valueOfCode(String codeString) {
        return HangingProtocolFilter.valueOf(codeString);
    }

    /**
     * Gets the code string.
     *
     * @return the code string.
     */
    public String getCodeString() {
        return name();
    }

    /**
     * Executes the contains operation.
     *
     * @param params the params.
     * @param value  the value.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean contains(String[] params, String value) {
        return Arrays.asList(params).contains(value);
    }

    /**
     * Executes the contains operation.
     *
     * @param params the params.
     * @param value  the value.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean contains(double[] params, double value) {
        return Arrays.stream(params).anyMatch(param -> Math.abs(param - value) <= EPSILON);
    }

    /**
     * Executes the selected operation.
     *
     * @param values      the values.
     * @param valueNumber the value number.
     * @return the operation result.
     */
    private static java.util.stream.Stream<String> selected(String[] values, int valueNumber) {
        if (valueNumber > 0) {
            return java.util.stream.Stream.of(values[valueNumber - 1]);
        }
        return Arrays.stream(values);
    }

    /**
     * Executes the selected operation.
     *
     * @param values      the values.
     * @param valueNumber the value number.
     * @return the operation result.
     */
    private static java.util.stream.DoubleStream selected(double[] values, int valueNumber) {
        if (valueNumber > 0) {
            return java.util.stream.DoubleStream.of(values[valueNumber - 1]);
        }
        return Arrays.stream(values);
    }

}
