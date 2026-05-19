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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The diff ref class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DiffRef implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852251659905L;

    private String baseSha;
    private String headSha;
    private String startSha;

    /**
     * Constructs a new {@code DiffRef} instance.
     */

    public DiffRef() {
        // No initialization required.
    }

    /**
     * Returns the base sha.
     *
     * @return the result
     */

    public String getBaseSha() {
        return baseSha;
    }

    /**
     * Sets the base sha.
     *
     * @param baseSha the base sha value
     */

    public void setBaseSha(final String baseSha) {
        this.baseSha = baseSha;
    }

    /**
     * Returns the head sha.
     *
     * @return the result
     */

    public String getHeadSha() {
        return headSha;
    }

    /**
     * Sets the head sha.
     *
     * @param headSha the head sha value
     */

    public void setHeadSha(final String headSha) {
        this.headSha = headSha;
    }

    /**
     * Returns the start sha.
     *
     * @return the result
     */

    public String getStartSha() {
        return startSha;
    }

    /**
     * Sets the start sha.
     *
     * @param startSha the start sha value
     */

    public void setStartSha(final String startSha) {
        this.startSha = startSha;
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
