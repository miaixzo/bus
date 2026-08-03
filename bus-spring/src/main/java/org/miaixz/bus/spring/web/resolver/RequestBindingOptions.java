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
package org.miaixz.bus.spring.web.resolver;

/**
 * Immutable and deliberately non-configurable request-object binding limits.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RequestBindingOptions {

    /**
     * Initializes the immutable binding limits used by the request-object argument resolver.
     */
    public RequestBindingOptions() {
        // No initialization required.
    }

    /**
     * Default collection auto-growth limit for request binding.
     */
    private static final int AUTO_GROW_COLLECTION_LIMIT = 256;

    /**
     * Exposes the maximum collection size Spring binding may auto-grow.
     *
     * @return the auto grow collection limit
     */
    public int getAutoGrowCollectionLimit() {
        return AUTO_GROW_COLLECTION_LIMIT;
    }

    /**
     * Indicates whether request fields without writable target properties are ignored.
     *
     * @return whether ignore unknown fields
     */
    public boolean isIgnoreUnknownFields() {
        return false;
    }

    /**
     * Indicates whether empty multipart files participate in data binding.
     *
     * @return whether bind empty multipart files
     */
    public boolean isBindEmptyMultipartFiles() {
        return false;
    }

}
