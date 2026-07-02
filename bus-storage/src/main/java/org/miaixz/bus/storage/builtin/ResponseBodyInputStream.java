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
package org.miaixz.bus.storage.builtin;

import java.io.FilterInputStream;
import java.io.IOException;

import org.miaixz.bus.http.Response;

/**
 * Input stream that keeps the response open until callers close the stream.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ResponseBodyInputStream extends FilterInputStream {

    /**
     * The response backing this stream.
     */
    private final Response response;

    /**
     * Constructs a response body stream.
     *
     * @param response The response containing the body stream.
     */
    public ResponseBodyInputStream(final Response response) {
        super(response.body().byteStream());
        this.response = response;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            response.close();
        }
    }

}
