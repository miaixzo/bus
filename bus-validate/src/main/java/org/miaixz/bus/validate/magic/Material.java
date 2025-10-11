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
public class Material {

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
     * The actual annotation instance from which this material was built.
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
     * A list of nested validation materials, used for handling meta-annotations.
     */
    private List<Material> list;

    /**
     * Default constructor. Initializes the parameter map and material list.
     */
    public Material() {
        this.list = new ArrayList<>();
        this.param = new HashMap<>();
    }

    /**
     * Adds a parent validation annotation's properties. This is used for handling meta-annotations where one validation
     * annotation is composed of others.
     *
     * @param material The parent validation material to add.
     */
    public void addParentProperty(Material material) {
        if (CollKit.isEmpty(this.list)) {
            this.list = new ArrayList<>();
        }
        this.list.add(material);
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
