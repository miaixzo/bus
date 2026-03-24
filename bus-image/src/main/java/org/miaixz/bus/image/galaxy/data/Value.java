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
package org.miaixz.bus.image.galaxy.data;

import java.io.IOException;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Value {

    Value NULL = new Value() {

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return vr == VR.SQ && encOpts.undefEmptySequenceLength ? -1 : 0;
        }

        @Override
        public void writeTo(ImageOutputStream dos, VR vr) {
        }

        @Override
        public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return vr == VR.SQ && encOpts.undefEmptySequenceLength ? 8 : 0;
        }

        @Override
        public String toString() {
            return Normal.EMPTY;
        }

        @Override
        public byte[] toBytes(VR vr, boolean bigEndian) {
            return new byte[] {};
        }
    };

    boolean isEmpty();

    byte[] toBytes(VR vr, boolean bigEndian) throws IOException;

    void writeTo(ImageOutputStream out, VR vr) throws IOException;

    int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr);

    int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr);

}
