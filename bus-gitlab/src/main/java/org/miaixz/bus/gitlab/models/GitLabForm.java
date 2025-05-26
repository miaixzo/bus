/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GitLabForm {

    private Map<String, GitLabFormValue> formValues = new LinkedHashMap<>();

    public GitLabForm() {
        super();
    }

    /**
     * Create a GitLabApiForm instance with the "page", and "per_page" parameters preset.
     *
     * @param page    the value for the "page" parameter
     * @param perPage the value for the "per_page" parameter
     */
    public GitLabForm(int page, int perPage) {
        super();
        withParam(Constants.PAGE_PARAM, page);
        withParam(Constants.PER_PAGE_PARAM, perPage);
    }

    /**
     * Fluent method for adding query and form parameters to a get() or post() call.
     *
     * @param name  the name of the field/attribute to add
     * @param value the value of the field/attribute to add
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, Object value) {
        return withParam(name, value, false);
    }

    /**
     * Fluent method for adding Date query and form parameters to a get() or post() call.
     *
     * @param name the name of the field/attribute to add
     * @param date the value of the field/attribute to add
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, Date date) {
        return withParam(name, date, false);
    }

    /**
     * Fluent method for adding Date query and form parameters to a get() or post() call.
     *
     * @param name     the name of the field/attribute to add
     * @param date     the value of the field/attribute to add
     * @param required the field is required flag
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, Date date, boolean required) {
        formValues.put(name, new GitLabFormValue(date, GitLabFormValueType.DATE, required));
        return this;
    }

    /**
     * Fluent method for adding AccessLevel query and form parameters to a get() or post() call.
     *
     * @param name  the name of the field/attribute to add
     * @param level the value of the field/attribute to add
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, AccessLevel level) {
        return withParam(name, level, false);
    }

    /**
     * Fluent method for adding AccessLevel query and form parameters to a get() or post() call.
     *
     * @param name     the name of the field/attribute to add
     * @param level    the value of the field/attribute to add
     * @param required the field is required flag
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, AccessLevel level, boolean required) {
        formValues.put(name, new GitLabFormValue(level, GitLabFormValueType.ACCESS_LEVEL, required));
        return this;
    }

    /**
     * Fluent method for adding a List type query and form parameters to a get() or post() call.
     *
     * @param name   the name of the field/attribute to add
     * @param values a List containing the values of the field/attribute to add
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, List<?> values) {
        return withParam(name, values, false);
    }

    /**
     * Fluent method for adding a List type query and form parameters to a get() or post() call.
     *
     * @param name     the name of the field/attribute to add
     * @param values   a List containing the values of the field/attribute to add
     * @param required the field is required flag
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, List<?> values, boolean required) {
        formValues.put(name, new GitLabFormValue(values, GitLabFormValueType.LIST, required));
        return this;
    }

    /**
     * Fluent method for adding an array of hash type query and form parameters to a get() or post() call.
     *
     * @param name      the name of the field/attribute to add
     * @param variables a Map containing array of hashes
     * @param required  the field is required flag
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, Map<String, ?> variables, boolean required) {
        formValues.put(name, new GitLabFormValue(variables, GitLabFormValueType.MAP, required));
        return this;
    }

    /**
     * Fluent method for adding query and form parameters to a get() or post() call.
     *
     * @param name     the name of the field/attribute to add
     * @param value    the value of the field/attribute to add
     * @param required the field is required flag
     * @return this {@link GitLabForm} instance
     */
    public GitLabForm withParam(String name, Object value, boolean required) {
        formValues.put(name, new GitLabFormValue(value, GitLabFormValueType.OBJECT, required));
        return this;
    }

    public Map<String, GitLabFormValue> getFormValues() {
        return formValues;
    }

}
