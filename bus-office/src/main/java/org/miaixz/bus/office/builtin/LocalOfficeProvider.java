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
package org.miaixz.bus.office.builtin;

import java.io.File;
import java.io.InputStream;

/**
 * Default implementation of a document converter. This implementation uses the provided office manager to perform
 * document conversions. The provided office manager must be started for this converter to be used.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LocalOfficeProvider extends AbstractProvider {

    /**
     * Description inherited from parent class or interface.
     *
     * @param source the source file to convert
     * @return null (not implemented in this version)
     */
    @Override
    public Object convert(File source) {
        return null;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param source the input stream to convert
     * @return null (not implemented in this version)
     */
    @Override
    public Object convert(InputStream source) {
        return null;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param source      the input stream to convert
     * @param closeStream whether to close the stream after conversion
     * @return null (not implemented in this version)
     */
    @Override
    public Object convert(InputStream source, boolean closeStream) {
        return null;
    }

}
