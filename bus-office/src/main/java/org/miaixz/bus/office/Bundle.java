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
package org.miaixz.bus.office;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shared bundle models used to describe segment input, codecs, and paged output.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Bundle {

    private Bundle() {

    }

    /**
     * Generic codec used to convert items to and from archive payloads.
     *
     * @param <T> item type
     */
    public interface Codec<T> {

        /**
         * Encodes an item.
         *
         * @param value item value
         * @return serialized bytes
         * @throws Exception if encoding fails
         */
        byte[] encode(T value) throws Exception;

        /**
         * Decodes an item.
         *
         * @param payload serialized bytes
         * @return decoded item
         * @throws Exception if decoding fails
         */
        T decode(byte[] payload) throws Exception;
    }

    /**
     * Segment input used to create an archived bundle.
     *
     * @param <T>          item type
     * @param segmentIndex segment index
     * @param segmentName  segment name
     * @param attributes   optional segment attributes
     * @param items        ordered items
     */
    public record Segment<T>(int segmentIndex, String segmentName, Map<String, String> attributes, List<T> items) {

    }

    /**
     * Generic paged bundle payload.
     *
     * @param <T> item type
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Paged<T> {

        /**
         * Bundle handle identifier.
         */
        private String handleId;

        /**
         * Segment index.
         */
        private int segmentIndex;

        /**
         * Logical segment name.
         */
        private String segmentName;

        /**
         * Total number of segments in the archive.
         */
        private int segmentCount;

        /**
         * Total number of items in the archive.
         */
        private long totalItems;

        /**
         * Handle expiration timestamp.
         */
        private long expiresAt;

        /**
         * Preview row limit.
         */
        private int previewRows;

        /**
         * Current page number.
         */
        private int pageNo;

        /**
         * Current page size.
         */
        private int pageSize;

        /**
         * Total items available in the current segment.
         */
        private long total;

        /**
         * Segment attributes.
         */
        private Map<String, String> attributes;

        /**
         * Current page items.
         */
        private List<T> items;

        /**
         * Checks whether the current page contains items.
         *
         * @return {@code true} if items are present
         */
        public boolean hasItems() {
            return items != null && !items.isEmpty();
        }
    }

}
