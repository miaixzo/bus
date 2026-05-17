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
import java.util.*;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

/**
 * Represents the MetadataParser type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MetadataParser {

    /**
     * Creates a new instance.
     */
    private MetadataParser() {
    }

    /**
     * Parses the exif parse metadata.
     *
     * @param metadataList  the metadata list.
     * @param metadataTypes the metadata types.
     * @return the operation result.
     */
    public static List<String> parseExifParseMetadata(List<Mat> metadataList, MatOfInt metadataTypes) {
        List<String> result = new ArrayList<>();

        if (metadataList == null || metadataTypes == null || metadataTypes.empty()) {
            return result;
        }

        int[] typesArray = metadataTypes.toArray();
        if (metadataList.size() != typesArray.length || typesArray[typesArray.length - 1] != 1000) {
            return result;
        }
        int lastIndex = typesArray.length - 1;
        Mat metadata = metadataList.get(lastIndex);

        if (metadata.empty()) {
            return result;
        }

        // The metadata Mat contains rows of tag data
        int numTags = metadata.rows();
        for (int i = 0; i < numTags; i++) {
            Mat row = metadata.row(i);
            if (row.empty()) {
                result.add("");
            } else {
                int bytesPerElement = (int) (row.elemSize());
                int totalElements = row.cols() * row.channels();
                byte[] tagBytes = new byte[bytesPerElement * totalElements];

                row.get(0, 0, tagBytes);
                String tagValue = new String(tagBytes, StandardCharsets.UTF_8).trim();
                result.add(tagValue);
            }
        }
        return result;
    }

    // // public static Map<Integer, String> parseMetadata(List<Mat> metadataList, MatOfInt
    // metadataTypes) {
    // Map<Integer, String> metadataMap = new LinkedHashMap<>();
    //
    // if (metadataList == null || metadataTypes == null || metadataTypes.empty()) {
    // return metadataMap;
    // }
    //
    // int[] typesArray = metadataTypes.toArray();
    // if (metadataList.size() != typesArray.length) {
    // return metadataMap;
    // }
    //
    // for (int i = 0; i < metadataList.size(); i++) {
    // Mat metadata = metadataList.get(i);
    // int metadataType = typesArray[i];
    //
    // if (metadata.empty()) {
    // continue;
    // }
    //
    // switch (metadataType) {
    // case Imgcodecs.IMAGE_METADATA_XMP:
    // String xmpData = extractMetadata(metadata);
    // if (!xmpData.isEmpty()) {
    // metadataMap.put(Imgcodecs.IMAGE_METADATA_XMP, xmpData);
    // }
    // break;
    // case Imgcodecs.IMAGE_METADATA_ICCP:
    // String iccpData = extractMetadata(metadata);
    // if (!iccpData.isEmpty()) {
    // metadataMap.put(Imgcodecs.IMAGE_METADATA_ICCP, iccpData);
    // }
    // break;
    // case Imgcodecs.IMAGE_METADATA_CICP:
    // String cicpData = extractMetadata(metadata);
    // if (!cicpData.isEmpty()) {
    // metadataMap.put(Imgcodecs.IMAGE_METADATA_CICP, cicpData);
    // }
    // break;
    // case Imgcodecs.IMAGE_METADATA_UNKNOWN:
    // default:
    // String rawData = extractMetadata(metadata);
    // if (!rawData.isEmpty()) {
    // metadataMap.put(metadataType, rawData);
    // }
    // break;
    // }
    // }
    //
    // return metadataMap;
    // }
    //
    // // public static String extractMetadata(Mat metadata) {
    // byte[] bytes = new byte[(int) metadata.total()];
    // metadata.get(0, 0, bytes);
    //
    // return new String(bytes, StandardCharsets.UTF_8);
    // }

}
