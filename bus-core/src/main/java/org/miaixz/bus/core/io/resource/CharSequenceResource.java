/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.resource;

import java.io.Serial;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;

/**
 * {@link CharSequence} resource, treating a character sequence as a resource. This class provides an implementation of
 * the {@link Resource} interface for resources represented by a {@link CharSequence}, such as a {@link String} or
 * {@link StringBuilder}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharSequenceResource extends BytesResource {

    @Serial
    private static final long serialVersionUID = 2852230576091L;

    /**
     * The name of the character set used for encoding. Since {@link java.nio.charset.Charset} is not serializable, its
     * name is stored instead.
     */
    private final String charsetName;

    /**
     * Constructs a {@code CharSequenceResource} with the given character sequence data, using UTF-8 encoding.
     *
     * @param data The character sequence data to be used as the resource.
     */
    public CharSequenceResource(final CharSequence data) {
        this(data, null);
    }

    /**
     * Constructs a {@code CharSequenceResource} with the given character sequence data and resource name, using UTF-8
     * encoding.
     *
     * @param data The character sequence data to be used as the resource.
     * @param name The name of the resource.
     */
    public CharSequenceResource(final CharSequence data, final String name) {
        this(data, name, Charset.UTF_8);
    }

    /**
     * Constructs a {@code CharSequenceResource} with the given character sequence data, resource name, and character
     * set.
     *
     * @param data    The character sequence data to be used as the resource.
     * @param name    The name of the resource.
     * @param charset The {@link java.nio.charset.Charset} to use for encoding the character sequence data.
     */
    public CharSequenceResource(final CharSequence data, final String name, final java.nio.charset.Charset charset) {
        super(ByteKit.toBytes(data, charset), name);
        this.charsetName = charset.name();
    }

    /**
     * Reads the content of the resource as a string using the character set specified during construction.
     *
     * @return The content of the resource as a string.
     */
    public String readString() {
        return readString(getCharset());
    }

    /**
     * Retrieves the name of the character set used for this resource.
     *
     * @return The name of the character set.
     */
    public String getCharsetName() {
        return this.charsetName;
    }

    /**
     * Retrieves the {@link java.nio.charset.Charset} object used for this resource.
     *
     * @return The {@link java.nio.charset.Charset} object.
     */
    public java.nio.charset.Charset getCharset() {
        return Charset.charset(this.charsetName);
    }

}
