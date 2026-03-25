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
package org.miaixz.bus.auth.nimble.jd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * JD (Jingdong) authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum JdScope implements AuthorizeScope {

    /**
     * Basic authorization. The meaning of {@code scope} is subject to {@code description}.
     */
    SNSAPI_BASE("snsapi_base", "Basic authorization", true);

    /**
     * The scope string as defined by JD.
     */
    private final String scope;
    /**
     * A description of what the scope grants access to.
     */
    private final String description;
    /**
     * Indicates if this scope is enabled by default.
     */
    private final boolean isDefault;

}
