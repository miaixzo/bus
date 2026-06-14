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
package org.miaixz.bus.image.nimble.opencv;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

/**
 * Represents the MetadataParser type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MetadataParser {

    /**
     * Creates a new instance.
     */
    private MetadataParser() {
        // No initialization required.
    }

    /**
     * Parses the exif parse metadata.
     *
     * @param metadataList  the metadata list.
     * @param metadataTypes the metadata types.
     * @return the operation result.
     */
    public static List<String> parseExifParseMetadata(List<Mat> metadataList, MatOfInt metadataTypes) {
        if (metadataList == null || metadataTypes == null || metadataTypes.empty()) {
            return List.of();
        }

        int[] typesArray = metadataTypes.toArray();
        if (metadataList.size() != typesArray.length || typesArray[typesArray.length - 1] != 1000) {
            return List.of();
        }

        Mat metadata = metadataList.get(typesArray.length - 1);

        if (metadata.empty()) {
            return List.of();
        }

        int numTags = metadata.rows();
        var result = new ArrayList<String>(numTags);
        for (int i = 0; i < numTags; i++) {
            result.add(parseTagRow(metadata.row(i)));
        }
        return result;
    }

    /**
     * Parses a single metadata row into text.
     *
     * @param row the metadata row.
     * @return the parsed tag text.
     */
    private static String parseTagRow(Mat row) {
        if (row.empty()) {
            return "";
        }
        int byteCount = (int) row.elemSize() * row.cols() * row.channels();
        var tagBytes = new byte[byteCount];
        row.get(0, 0, tagBytes);
        return new String(tagBytes, StandardCharsets.UTF_8).trim();
    }

}
