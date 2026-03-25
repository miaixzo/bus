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
package org.miaixz.bus.office.builtin;

import java.io.File;
import java.io.InputStream;

/**
 * Default implementation of a document converter. This implementation uses the provided office manager to perform
 * document conversions. The provided office manager must be started for this converter to be used.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LocalOfficeProvider extends AbstractProvider {

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param source the source file to convert
     * @return null (not implemented in this version)
     */
    @Override
    public Object convert(File source) {
        throw new UnsupportedOperationException("Local office conversion is not implemented yet");
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param source the input stream to convert
     * @return null (not implemented in this version)
     */
    @Override
    public Object convert(InputStream source) {
        throw new UnsupportedOperationException("Local office conversion is not implemented yet");
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param source      the input stream to convert
     * @param closeStream whether to close the stream after conversion
     * @return null (not implemented in this version)
     */
    @Override
    public Object convert(InputStream source, boolean closeStream) {
        throw new UnsupportedOperationException("Local office conversion is not implemented yet");
    }

}
