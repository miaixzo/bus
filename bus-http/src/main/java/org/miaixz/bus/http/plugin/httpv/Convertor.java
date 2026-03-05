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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * An interface for message conversion, handling serialization and deserialization of objects to and from different data
 * formats.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Convertor {

    /**
     * Returns the media type of the messages handled by this converter.
     *
     * @return The media type string.
     */
    String contentType();

    /**
     * Parses an input stream into a {@link CoverWapper}.
     *
     * @param in      The input stream.
     * @param charset The character set.
     * @return A {@link CoverWapper} instance.
     */
    CoverWapper toMapper(InputStream in, Charset charset);

    /**
     * Parses an input stream into a {@link CoverArray}.
     *
     * @param in      The input stream.
     * @param charset The character set.
     * @return A {@link CoverArray} instance.
     */
    CoverArray toArray(InputStream in, Charset charset);

    /**
     * Serializes a Java object into a byte array.
     *
     * @param object  The Java object.
     * @param charset The character set.
     * @return A byte array.
     */
    byte[] serialize(Object object, Charset charset);

    /**
     * Serializes a Java object into a byte array with a specified date format.
     *
     * @param object     The Java object.
     * @param dateFormat The date format string.
     * @param charset    The character set.
     * @return A byte array.
     */
    byte[] serialize(Object object, String dateFormat, Charset charset);

    /**
     * Parses an input stream into a Java bean of the specified type.
     *
     * @param <T>     The target generic type.
     * @param type    The target class type.
     * @param in      The input stream.
     * @param charset The character set.
     * @return A Java bean instance.
     */
    <T> T toBean(Class<T> type, InputStream in, Charset charset);

    /**
     * Parses an input stream into a list of Java objects of the specified type.
     *
     * @param <T>     The target generic type.
     * @param type    The target class type.
     * @param in      The input stream.
     * @param charset The character set.
     * @return A list of Java objects.
     */
    <T> List<T> toList(Class<T> type, InputStream in, Charset charset);

    /**
     * A form converter that can be used to automatically serialize form parameters.
     */
    class FormConvertor implements Convertor {

        private final Convertor convertor;

        public FormConvertor(Convertor convertor) {
            this.convertor = convertor;
        }

        @Override
        public String contentType() {
            return MediaType.APPLICATION_FORM_URLENCODED;
        }

        @Override
        public CoverWapper toMapper(InputStream in, Charset charset) {
            return convertor.toMapper(in, charset);
        }

        @Override
        public CoverArray toArray(InputStream in, Charset charset) {
            return convertor.toArray(in, charset);
        }

        @Override
        public byte[] serialize(Object object, Charset charset) {
            return serialize(object, null, charset);
        }

        @Override
        public byte[] serialize(Object object, String dateFormat, Charset charset) {
            byte[] data = convertor.serialize(object, dateFormat, charset);
            CoverWapper coverWapper = convertor.toMapper(new ByteArrayInputStream(data), charset);
            StringBuilder sb = new StringBuilder();
            for (String key : coverWapper.keySet()) {
                sb.append(key).append(Symbol.C_EQUAL).append(coverWapper.getString(key)).append(Symbol.C_AND);
            }
            if (sb.length() > 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString().getBytes(charset);
        }

        @Override
        public <T> T toBean(Class<T> type, InputStream in, Charset charset) {
            return convertor.toBean(type, in, charset);
        }

        @Override
        public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
            return convertor.toList(type, in, charset);
        }

    }

}
