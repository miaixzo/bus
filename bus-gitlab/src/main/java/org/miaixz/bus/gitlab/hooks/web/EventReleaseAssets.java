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
package org.miaixz.bus.gitlab.hooks.web;

import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event release assets class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventReleaseAssets {

    private Integer count;
    private List<EventReleaseLink> links;
    private List<EventReleaseSource> sources;

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
     * Returns the links.
     *
     * @return the result
     */

    public List<EventReleaseLink> getLinks() {
        return links;
    }

    /**
     * Sets the links.
     *
     * @param links the links value
     */

    public void setLinks(List<EventReleaseLink> links) {
        this.links = links;
    }

    /**
     * Returns the sources.
     *
     * @return the result
     */

    public List<EventReleaseSource> getSources() {
        return sources;
    }

    /**
     * Sets the sources.
     *
     * @param sources the sources value
     */

    public void setSources(List<EventReleaseSource> sources) {
        this.sources = sources;
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
