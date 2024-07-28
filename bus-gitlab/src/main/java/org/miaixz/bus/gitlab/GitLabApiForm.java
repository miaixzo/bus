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
package org.miaixz.bus.gitlab;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.miaixz.bus.gitlab.models.AccessLevel;
import org.miaixz.bus.gitlab.models.Variable;
import org.miaixz.bus.gitlab.support.ISO8601;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class extends the standard JAX-RS Form class to make it fluent.
 */
public class GitLabApiForm extends Form {

    public GitLabApiForm() {
        super();
    }

    public GitLabApiForm(MultivaluedHashMap<String, String> map) {
        super(map);
    }

    /**
     * Create a GitLabApiForm instance with the "page", and "per_page" parameters preset.
     *
     * @param page    the value for the "page" parameter
     * @param perPage the value for the "per_page" parameter
     */
    public GitLabApiForm(int page, int perPage) {
        super();
        withParam(AbstractApi.PAGE_PARAM, page);
        withParam(AbstractApi.PER_PAGE_PARAM, (Integer) perPage);
    }

    /**
     * Fluent method for adding query and form parameters to a get() or post() call.
     *
     * @param name  the name of the field/attribute to add
     * @param value the value of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(String name, Object value) throws IllegalArgumentException {
        return (withParam(name, value, false));
    }

    /**
     * Fluent method for adding Date query and form parameters to a get() or post() call.
     *
     * @param name the name of the field/attribute to add
     * @param date the value of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(String name, Date date) throws IllegalArgumentException {
        return (withParam(name, date, false));
    }

    /**
     * Fluent method for adding Date query and form parameters to a get() or post() call.
     *
     * @param name     the name of the field/attribute to add
     * @param date     the value of the field/attribute to add
     * @param required the field is required flag
     * @return this GitLabAPiForm instance
     * @throws IllegalArgumentException if a required parameter is null or empty
     */
    public GitLabApiForm withParam(String name, Date date, boolean required) throws IllegalArgumentException {
        return (withParam(name, (date == null ? null : ISO8601.toString(date)), required));
    }

    /**
     * Fluent method for adding AccessLevel query and form parameters to a get() or post() call.
     *
     * @param name  the name of the field/attribute to add
     * @param level the value of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(String name, AccessLevel level) throws IllegalArgumentException {
        return (withParam(name, level, false));
    }

    /**
     * Fluent method for adding AccessLevel query and form parameters to a get() or post() call.
     *
     * @param name     the name of the field/attribute to add
     * @param level    the value of the field/attribute to add
     * @param required the field is required flag
     * @return this GitLabAPiForm instance
     * @throws IllegalArgumentException if a required parameter is null or empty
     */
    public GitLabApiForm withParam(String name, AccessLevel level, boolean required) throws IllegalArgumentException {
        return (withParam(name, (level == null ? null : level.toValue()), required));
    }

    /**
     * Fluent method for adding a List type query and form parameters to a get() or post() call.
     *
     * @param <T>    the type contained by the List
     * @param name   the name of the field/attribute to add
     * @param values a List containing the values of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public <T> GitLabApiForm withParam(String name, List<T> values) {
        return (withParam(name, values, false));
    }

    /**
     * Fluent method for adding a List type query and form parameters to a get() or post() call.
     *
     * @param <T>      the type contained by the List
     * @param name     the name of the field/attribute to add
     * @param values   a List containing the values of the field/attribute to add
     * @param required the field is required flag
     * @return this GitLabAPiForm instance
     * @throws IllegalArgumentException if a required parameter is null or empty
     */
    public <T> GitLabApiForm withParam(String name, List<T> values, boolean required) throws IllegalArgumentException {

        if (values == null || values.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(name + " cannot be empty or null");
            }

            return (this);
        }

        for (T value : values) {
            if (value != null) {
                this.param(name + "[]", value.toString());
            }
        }

        return (this);
    }

    /**
     * Fluent method for adding an array of hash type query and form parameters to a get() or post() call.
     *
     * @param name      the name of the field/attribute to add
     * @param variables a Map containing array of hashes
     * @param required  the field is required flag
     * @return this GitLabAPiForm instance
     * @throws IllegalArgumentException if a required parameter is null or empty
     */
    public GitLabApiForm withParam(String name, Map<String, ?> variables, boolean required)
            throws IllegalArgumentException {

        if (variables == null || variables.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(name + " cannot be empty or null");
            }

            return (this);
        }

        for (Entry<String, ?> variable : variables.entrySet()) {
            Object value = variable.getValue();
            if (value != null) {
                this.param(name + "[" + variable.getKey() + "]", value.toString());
            }
        }

        return (this);
    }

    /**
     * Fluent method for adding query and form parameters to a get() or post() call. If required is true and value is
     * null, will throw an IllegalArgumentException.
     *
     * @param name     the name of the field/attribute to add
     * @param value    the value of the field/attribute to add
     * @param required the field is required flag
     * @return this GitLabAPiForm instance
     * @throws IllegalArgumentException if a required parameter is null or empty
     */
    public GitLabApiForm withParam(String name, Object value, boolean required) throws IllegalArgumentException {

        if (value == null) {
            if (required) {
                throw new IllegalArgumentException(name + " cannot be empty or null");
            }

            return (this);
        }

        String stringValue = value.toString();
        if (required && stringValue.trim().length() == 0) {
            throw new IllegalArgumentException(name + " cannot be empty or null");
        }

        this.param(name.trim(), stringValue);
        return (this);
    }

    /**
     * Fluent method for adding a List&lt;Variable&gt; type query and form parameters to a get(), post(), or put() call.
     *
     * @param variables the List of Variable to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(List<Variable> variables) {

        if (variables == null || variables.isEmpty()) {
            return (this);
        }

        variables.forEach(v -> {
            String value = v.getValue();
            if (value != null) {
                this.param("variables[" + v.getKey() + "]", value);
            }
        });

        return (this);
    }
}
