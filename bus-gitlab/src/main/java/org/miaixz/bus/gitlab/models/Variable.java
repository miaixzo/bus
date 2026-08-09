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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumCodec;

/**
 * The variable class.
 *
 * @author Kimi Liu
 */
public class Variable implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852283021997L;

    /**
     * Create a List of Variable from the provided Map.
     *
     * @param variables the Map to convert to a List of Variable
     * @return the List of Variable containing the keys and values from the Map, or null if the Map is null
     */
    public static final List<Variable> convertMapToList(Map<String, String> variables) {

        if (variables == null) {
            return null;
        }

        List<Variable> varList = new ArrayList<>(variables.size());
        variables.forEach((k, v) -> varList.add(new Variable(k, v)));
        return varList;
    }

    /**
     * The key value.
     */
    private String key;
    /**
     * The value value.
     */
    private String value;
    /**
     * The variable type value.
     */
    private Type variableType;

    /**
     * The is protected value.
     */
    @JsonProperty("protected")
    private Boolean isProtected;

    /**
     * The is masked value.
     */
    @JsonProperty("masked")
    private Boolean isMasked;

    /**
     * The environment scope value.
     */
    private String environmentScope;

    /**
     * Constructs a new {@code Variable} instance.
     */

    public Variable() {
        // No initialization required.
    }

    /**
     * Constructs a new {@code Variable} instance.
     *
     * @param key   the key value
     * @param value the value value
     */

    public Variable(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key.
     *
     * @return the result
     */

    public String getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key the key value
     */

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the value.
     *
     * @return the result
     */

    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the value value
     */

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the variable type.
     *
     * @return the result
     */

    public Type getVariableType() {
        return variableType;
    }

    /**
     * Sets the variable type.
     *
     * @param variableType the variable type value
     */

    public void setVariableType(Type variableType) {
        this.variableType = variableType;
    }

    /**
     * Returns the protected.
     *
     * @return the result
     */

    public Boolean getProtected() {
        return isProtected;
    }

    /**
     * Sets the protected.
     *
     * @param isProtected the is protected value
     */

    public void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }

    /**
     * Returns the masked.
     *
     * @return the result
     */

    public Boolean getMasked() {
        return isMasked;
    }

    /**
     * Sets the masked.
     *
     * @param masked the masked value
     */

    public void setMasked(Boolean masked) {
        this.isMasked = masked;
    }

    /**
     * Returns the environment scope.
     *
     * @return the result
     */

    public String getEnvironmentScope() {
        return environmentScope;
    }

    /**
     * Sets the environment scope.
     *
     * @param environmentScope the environment scope value
     */

    public void setEnvironmentScope(String environmentScope) {
        this.environmentScope = environmentScope;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * Enum for the various Commit build status values.
     *
     * @author Kimi Liu
     */
    public enum Type {

        /**
         * The env var type.
         */
        ENV_VAR,
        /**
         * The file type.
         */
        FILE;

        /**
         * The enum codec value.
         */
        private static JacksonJsonEnumCodec<Type> enumCodec = new JacksonJsonEnumCodec<>(Type.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static Type forValue(String value) {
            return enumCodec.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumCodec.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumCodec.toString(this));
        }

    }

}
