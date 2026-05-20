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

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The abstract minimal epic class.
 *
 * @param <E> the concrete epic model type
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class AbstractMinimalEpic<E extends AbstractMinimalEpic<E>> implements Serializable {

    /**
     * Constructs a new AbstractMinimalEpic instance.
     */
    public AbstractMinimalEpic() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852235180659L;

    private Long id;
    private Long iid;
    private Long groupId;
    private Long parentId;
    private String title;
    private String reference;
    private String url;

    /**
     * Sets the title and returns this minimal epic model.
     *
     * @param title the epic title
     * @return this minimal epic model
     */
    public E withTitle(String title) {
        this.title = title;
        return (E) (this);
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
