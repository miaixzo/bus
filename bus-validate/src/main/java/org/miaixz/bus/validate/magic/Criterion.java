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
package org.miaixz.bus.validate.magic;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.text.replacer.HighMultiReplacer;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the common properties extracted from a validation annotation. This class acts as a data carrier for a
 * single validation rule, holding all necessary information for the validation process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Criterion {

    /**
     * Indicates if the validation should be applied to each element of an array or collection.
     */
    private boolean array = false;
    /**
     * The error code to be used when validation fails.
     */
    private String errcode;
    /**
     * The error message template.
     */
    private String errmsg;
    /**
     * The name of the field being validated.
     */
    private String field;

    /**
     * The name of the validator.
     */
    private String name;
    /**
     * The validation groups this rule belongs to.
     */
    private String[] group;
    /**
     * The actual annotation instance from which this criterion was built.
     */
    private Annotation annotation;
    /**
     * The class of the {@link Matcher} that will perform the validation.
     */
    private Class<?> clazz;
    /**
     * A custom exception class to be thrown on validation failure.
     */
    private Class<? extends ValidateException> exception;
    /**
     * Parameters for interpolating the error message.
     */
    private Map<String, Object> param;
    /**
     * A list of nested validation criterions, used for handling meta-annotations.
     */
    private List<Criterion> list;

    /**
     * Default constructor. Initializes the parameter map and criterion list.
     */
    public Criterion() {
        this.list = new ArrayList<>();
        this.param = new HashMap<>();
    }

    /**
     * Adds a parent validation annotation's properties. This is used for handling meta-annotations where one validation
     * annotation is composed of others.
     *
     * @param criterion The parent validation criterion to add.
     */
    public void addParentProperty(Criterion criterion) {
        if (CollKit.isEmpty(this.list)) {
            this.list = new ArrayList<>();
        }
        this.list.add(criterion);
    }

    /**
     * Adds a parameter for string interpolation in the error message.
     *
     * @param name  The name of the placeholder in the message template.
     * @param value The value to be substituted for the placeholder.
     * @throws ValidateException if a parameter with the same name already exists.
     */
    public void addParam(String name, Object value) {
        if (MapKit.isEmpty(this.param)) {
            this.param = new HashMap<>();
        }
        if (this.param.containsKey(name)) {
            throw new ValidateException("Error message already exists:" + name);
        }
        this.param.put(name, value);
    }

    /**
     * Gets the final validation message after string interpolation.
     *
     * @return The interpolated error message string.
     */
    public String getMessage() {
        StringBuilder text = new StringBuilder();
        HighMultiReplacer.of(this.param).replace(this.errmsg, 0, text);
        return text.toString();
    }

}
