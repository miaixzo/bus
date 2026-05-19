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
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The abstract epic class.
 *
 * @param <E> the concrete epic model type
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class AbstractEpic<E extends AbstractEpic<E>> extends AbstractMinimalEpic<E> implements Serializable {

    /**
     * Constructs a new AbstractEpic instance.
     */
    public AbstractEpic() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852233623053L;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date startDate;

    private Long parentIid;
    private String description;
    private EpicState state;
    private String webUrl;
    private References references;
    private Author author;
    private List<String> labels;
    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date dueDate;
    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date endDate;

    /**
     * Sets the description and returns this epic model.
     *
     * @param description the epic description
     * @return this epic model
     */
    public E withDescription(String description) {
        this.description = description;
        return (E) (this);
    }

    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
    private Integer downvotes;
    private Integer upvotes;
    private String color;

    @JsonProperty("_links")
    private Map<String, String> links;

    /**
     * Sets the author and returns this epic model.
     *
     * @param author the epic author
     * @return this epic model
     */
    public E withAuthor(Author author) {
        this.author = author;
        return (E) (this);
    }

    /**
     * Sets the labels and returns this epic model.
     *
     * @param labels the epic labels
     * @return this epic model
     */
    public E withLabels(List<String> labels) {
        this.labels = labels;
        return (E) (this);
    }

    /**
     * Sets the start date and returns this epic model.
     *
     * @param startDate the epic start date
     * @return this epic model
     */
    public E withStartDate(Date startDate) {
        this.startDate = startDate;
        return (E) (this);
    }

    /**
     * Sets the end date and returns this epic model.
     *
     * @param endDate the epic end date
     * @return this epic model
     */
    public E withEndDate(Date endDate) {
        this.endDate = endDate;
        return (E) (this);
    }

    /**
     * The epic state enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum EpicState {

        /**
         * The opened epic state.
         */
        OPENED,
        /**
         * The closed epic state.
         */
        CLOSED,
        /**
         * The all epic state.
         */
        ALL;

        private static JacksonJsonEnumHelper<EpicState> enumHelper = new JacksonJsonEnumHelper<>(EpicState.class);

        /**
         * Resolves the epic state from the API value.
         *
         * @param value the API value
         * @return the epic state
         */
        @JsonCreator
        public static EpicState forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the API value for this epic state.
         *
         * @return the API value
         */
        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    /**
     * Returns a link by its API link name.
     *
     * @param name the link name
     * @return the matching link or {@code null}
     */
    @JsonIgnore
    public String getLinkByName(String name) {
        if (links == null || links.isEmpty()) {
            return (null);
        }

        return (links.get(name));
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

}
