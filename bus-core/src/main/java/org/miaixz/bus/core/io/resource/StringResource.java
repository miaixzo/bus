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
package org.miaixz.bus.core.io.resource;

import java.io.Serial;

import org.miaixz.bus.core.lang.Charset;

/**
 * String resource, treating a string as a resource. This class extends {@link CharSequenceResource} to specifically
 * handle {@link String} objects as resources.
 *
 * @author Kimi Liu
 * @see CharSequenceResource
 * @since Java 17+
 */
public class StringResource extends CharSequenceResource {

    @Serial
    private static final long serialVersionUID = 2852232676563L;

    /**
     * Constructs a {@code StringResource} with the given string data, using UTF-8 encoding.
     *
     * @param data The string data to be used as the resource.
     */
    public StringResource(final String data) {
        super(data, null);
    }

    /**
     * Constructs a {@code StringResource} with the given string data and resource name, using UTF-8 encoding.
     *
     * @param data The string data to be used as the resource.
     * @param name The name of the resource.
     */
    public StringResource(final String data, final String name) {
        super(data, name, Charset.UTF_8);
    }

    /**
     * Constructs a {@code StringResource} with the given string data, resource name, and character set.
     *
     * @param data    The string data to be used as the resource.
     * @param name    The name of the resource.
     * @param charset The {@link java.nio.charset.Charset} to use for encoding the string data.
     */
    public StringResource(final String data, final String name, final java.nio.charset.Charset charset) {
        super(data, name, charset);
    }

}
