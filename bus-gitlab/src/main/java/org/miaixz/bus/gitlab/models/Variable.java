/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
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
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class Variable implements Serializable {
    private static final long serialVersionUID = -1L;
    private String key;
    private String value;
    private Type variableType;
    @JsonProperty("protected")
    private Boolean isProtected;
    @JsonProperty("masked")
    private Boolean isMasked;
    private String environmentScope;
    public Variable() {
    }

    public Variable(String key, String value) {
        this.key = key;
        this.value = value;
    }

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getVariableType() {
        return variableType;
    }

    public void setVariableType(Type variableType) {
        this.variableType = variableType;
    }

    public Boolean getProtected() {
        return isProtected;
    }

    public void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }

    public Boolean getMasked() {
        return isMasked;
    }

    public void setMasked(Boolean masked) {
        this.isMasked = masked;
    }

    public String getEnvironmentScope() {
        return environmentScope;
    }

    public void setEnvironmentScope(String environmentScope) {
        this.environmentScope = environmentScope;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * Enum for the various Commit build status values.
     */
    public enum Type {

        ENV_VAR, FILE;

        private static JacksonJsonEnumHelper<Type> enumHelper = new JacksonJsonEnumHelper<>(Type.class);

        @JsonCreator
        public static Type forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }
}
