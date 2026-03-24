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
package org.miaixz.bus.image.galaxy.media;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class MultipartParser {

    private final String boundary;

    public MultipartParser(String boundary) {
        this.boundary = boundary;
    }

    public void parse(InputStream in, Handler handler) throws IOException {
        new MultipartInputStream(in, "--" + boundary).skipAll(); // skip preamble
        for (int i = 1;; i++) {
            int ch1 = in.read();
            int ch2 = in.read();
            if ((ch1 | ch2) < 0)
                throw new EOFException();

            if (ch1 == '-' && ch2 == '-')
                break;

            if (ch1 != '\r' || ch2 != '\n')
                throw new IOException("missing CR/LF after boundary");

            MultipartInputStream mis = new MultipartInputStream(in, "\r\n--" + boundary);
            handler.bodyPart(i, mis);
            mis.skipAll();
        }
    }

    public interface Handler {

        void bodyPart(int partNumber, MultipartInputStream in) throws IOException;
    }

}
