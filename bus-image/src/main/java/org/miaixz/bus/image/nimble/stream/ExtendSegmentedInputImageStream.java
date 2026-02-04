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
package org.miaixz.bus.image.nimble.stream;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public record ExtendSegmentedInputImageStream(

        Path path, long[] segmentPositions, int[] segmentLengths, ImageDescriptor imageDescriptor) {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExtendSegmentedInputImageStream that = (ExtendSegmentedInputImageStream) o;
        return Objects.equals(path, that.path) && Arrays.equals(segmentPositions, that.segmentPositions)
                && Arrays.equals(segmentLengths, that.segmentLengths)
                && Objects.equals(imageDescriptor, that.imageDescriptor);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(path, imageDescriptor);
        result = 31 * result + Arrays.hashCode(segmentPositions);
        result = 31 * result + Arrays.hashCode(segmentLengths);
        return result;
    }

    @Override
    public String toString() {
        return "ExtendSegmentedInputImageStream{" + "path=" + path + '}';
    }

}
