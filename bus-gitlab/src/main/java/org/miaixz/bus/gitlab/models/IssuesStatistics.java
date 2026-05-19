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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The issues statistics class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IssuesStatistics implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852258156052L;

    private Statistics statistics;

    /**
     * Returns the statistics.
     *
     * @return the result
     */

    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the statistics.
     *
     * @param statistics the statistics value
     */

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the counts.
     *
     * @return the result
     */

    @JsonIgnore
    public Counts getCounts() {
        return (statistics != null ? statistics.counts : null);
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
     * The statistics class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Statistics implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852258198568L;

        private Counts counts;

        /**
         * Returns the counts.
         *
         * @return the result
         */

        public Counts getCounts() {
            return counts;
        }

        /**
         * Sets the counts.
         *
         * @param counts the counts value
         */

        public void setCounts(Counts counts) {
            this.counts = counts;
        }

    }

    /**
     * The counts class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class Counts implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852258280077L;

        private Integer all;
        private Integer closed;
        private Integer opened;

        /**
         * Returns the all.
         *
         * @return the result
         */

        public Integer getAll() {
            return all;
        }

        /**
         * Sets the all.
         *
         * @param all the all value
         */

        public void setAll(Integer all) {
            this.all = all;
        }

        /**
         * Returns the closed.
         *
         * @return the result
         */

        public Integer getClosed() {
            return closed;
        }

        /**
         * Sets the closed.
         *
         * @param closed the closed value
         */

        public void setClosed(Integer closed) {
            this.closed = closed;
        }

        /**
         * Returns the opened.
         *
         * @return the result
         */

        public Integer getOpened() {
            return opened;
        }

        /**
         * Sets the opened.
         *
         * @param opened the opened value
         */

        public void setOpened(Integer opened) {
            this.opened = opened;
        }

    }

}
