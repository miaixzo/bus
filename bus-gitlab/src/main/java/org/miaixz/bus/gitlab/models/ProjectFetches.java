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

import org.miaixz.bus.gitlab.support.JacksonJson;

import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The project fetches class.
 *
 * @author Kimi Liu
 */
public class ProjectFetches implements Serializable {

    /**
     * Constructs a new {@code ProjectFetches} instance.
     */
    public ProjectFetches() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852271365267L;
    /**
     * The fetches value.
     */
    private Fetches fetches;

    /**
     * Returns the fetches.
     *
     * @return the result
     */

    public Fetches getFetches() {
        return fetches;
    }

    /**
     * Sets the fetches.
     *
     * @param fetches the fetches value
     */

    public void setFetches(Fetches fetches) {
        this.fetches = fetches;
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
     * The date count class.
     *
     * @author Kimi Liu
     */
    public static class DateCount implements Serializable {

        /**
         * Constructs a new {@code DateCount} instance.
         */
        public DateCount() {
            // No initialization required.
        }

        /**
         * The serial version uid value.
         */
        @Serial
        private static final long serialVersionUID = 2852271507886L;

        /**
         * The count value.
         */
        private Integer count;

        /**
         * The date value.
         */
        @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
        private Date date;

        /**
         * Returns the count.
         *
         * @return the result
         */

        public Integer getCount() {
            return count;
        }

        /**
         * Sets the count.
         *
         * @param count the count value
         */

        public void setCount(Integer count) {
            this.count = count;
        }

        /**
         * Returns the date.
         *
         * @return the result
         */

        public Date getDate() {
            return date;
        }

        /**
         * Sets the date.
         *
         * @param date the date value
         */

        public void setDate(Date date) {
            this.date = date;
        }

    }

    /**
     * The fetches class.
     *
     * @author Kimi Liu
     */
    public static class Fetches implements Serializable {

        /**
         * Constructs a new {@code Fetches} instance.
         */
        public Fetches() {
            // No initialization required.
        }

        @Serial
        private static final long serialVersionUID = 2852271590601L;

        /**
         * The total value.
         */
        private Integer total;
        /**
         * The days value.
         */
        private List<DateCount> days;

        /**
         * Returns the total.
         *
         * @return the result
         */

        public Integer getTotal() {
            return total;
        }

        /**
         * Sets the total.
         *
         * @param total the total value
         */

        public void setTotal(Integer total) {
            this.total = total;
        }

        /**
         * Returns the days.
         *
         * @return the result
         */

        public List<DateCount> getDays() {
            return days;
        }

        /**
         * Sets the days.
         *
         * @param days the days value
         */

        public void setDays(List<DateCount> days) {
            this.days = days;
        }

    }

}
