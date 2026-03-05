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
package org.miaixz.bus.gitlab;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.miaixz.bus.gitlab.models.AccessLevel;
import org.miaixz.bus.gitlab.models.GitLabForm;
import org.miaixz.bus.gitlab.models.GitLabFormValue;
import org.miaixz.bus.gitlab.models.Variable;
import org.miaixz.bus.gitlab.support.ISO8601;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedHashMap;

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
        withParam(AbstractApi.PER_PAGE_PARAM, perPage);
    }

    public GitLabApiForm(GitLabForm form) {
        super();
        for (Entry<String, GitLabFormValue> e : form.getFormValues().entrySet()) {
            GitLabFormValue value = e.getValue();
            switch (value.getType()) {
                case ACCESS_LEVEL:
                    withParam(e.getKey(), (AccessLevel) value.getValue(), value.isRequired());
                    break;

                case DATE:
                    withParam(e.getKey(), (Date) value.getValue(), value.isRequired());
                    break;

                case LIST:
                    withParam(e.getKey(), (List<?>) value.getValue(), value.isRequired());
                    break;

                case MAP:
                    Map<String, ?> mapValue = (Map<String, ?>) value.getValue();
                    withParam(e.getKey(), mapValue, value.isRequired());
                    break;

                case OBJECT:
                default:
                    withParam(e.getKey(), value.getValue(), value.isRequired());
                    break;
            }
        }
    }

    /**
     * Fluent method for adding query and form parameters to a get() or post() call.
     *
     * @param name  the name of the field/attribute to add
     * @param value the value of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(String name, Object value) {
        return withParam(name, value, false);
    }

    /**
     * Fluent method for adding Date query and form parameters to a get() or post() call.
     *
     * @param name the name of the field/attribute to add
     * @param date the value of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(String name, Date date) {
        return withParam(name, date, false);
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
    public GitLabApiForm withParam(String name, Date date, boolean required) {
        return withParam(name, date == null ? null : ISO8601.toString(date), required);
    }

    /**
     * Fluent method for adding AccessLevel query and form parameters to a get() or post() call.
     *
     * @param name  the name of the field/attribute to add
     * @param level the value of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(String name, AccessLevel level) {
        return withParam(name, level, false);
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
    public GitLabApiForm withParam(String name, AccessLevel level, boolean required) {
        return withParam(name, level == null ? null : level.toValue(), required);
    }

    /**
     * Fluent method for adding a List type query and form parameters to a get() or post() call.
     *
     * @param name   the name of the field/attribute to add
     * @param values a List containing the values of the field/attribute to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(String name, List<?> values) {
        return withParam(name, values, false);
    }

    /**
     * Fluent method for adding a List type query and form parameters to a get() or post() call.
     *
     * @param name     the name of the field/attribute to add
     * @param values   a List containing the values of the field/attribute to add
     * @param required the field is required flag
     * @return this GitLabAPiForm instance
     * @throws IllegalArgumentException if a required parameter is null or empty
     */
    public GitLabApiForm withParam(String name, List<?> values, boolean required) {

        if (values == null || values.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(name + " cannot be empty or null");
            }

            return this;
        }

        for (Object value : values) {
            if (value != null) {
                this.param(name + "[]", value.toString());
            }
        }

        return this;
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
    public GitLabApiForm withParam(String name, Map<String, ?> variables, boolean required) {

        if (variables == null || variables.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(name + " cannot be empty or null");
            }

            return this;
        }

        for (Entry<String, ?> variable : variables.entrySet()) {
            Object value = variable.getValue();
            if (value != null) {
                this.param(name + "[" + variable.getKey() + "]", value.toString());
            }
        }

        return this;
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
    public GitLabApiForm withParam(String name, Object value, boolean required) {

        if (value == null) {
            if (required) {
                throw new IllegalArgumentException(name + " cannot be empty or null");
            }

            return this;
        }

        String stringValue = value.toString();
        if (required && stringValue.trim().length() == 0) {
            throw new IllegalArgumentException(name + " cannot be empty or null");
        }

        this.param(name.trim(), stringValue);
        return this;
    }

    /**
     * Fluent method for adding a List&lt;Variable&gt; type query and form parameters to a get(), post(), or put() call.
     *
     * @param variables the List of Variable to add
     * @return this GitLabAPiForm instance
     */
    public GitLabApiForm withParam(List<Variable> variables) {

        if (variables == null || variables.isEmpty()) {
            return this;
        }

        variables.forEach(v -> {
            String value = v.getValue();
            if (value != null) {
                this.param("variables[" + v.getKey() + "]", value);
            }
        });

        return this;
    }

}
