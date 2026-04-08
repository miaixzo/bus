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

import java.io.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;

/**
 * Shared archive models and low-level binary archive access helpers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Archive {

    private Archive() {

    }

    /**
     * Archive manifest persisted for a handle directory.
     *
     * @param handleId    handle identifier
     * @param createdAt   creation timestamp in milliseconds
     * @param expiresAt   expiration timestamp in milliseconds
     * @param previewRows preview row limit stored with the handle
     * @param segments    archived segments
     */
    public record Manifest(String handleId, long createdAt, long expiresAt, int previewRows, List<Segment> segments)
            implements Serializable {
    }

    /**
     * Archive segment metadata.
     *
     * @param segmentIndex  zero-based segment index
     * @param segmentName   logical segment name
     * @param totalRecords  total record count
     * @param dataFileName  segment data file name
     * @param indexFileName segment index file name
     * @param attributes    optional segment attributes
     */
    public record Segment(int segmentIndex, String segmentName, long totalRecords, String dataFileName,
            String indexFileName, Map<String, String> attributes) implements Serializable {
    }

    /**
     * Archive slice loaded from a segment.
     *
     * @param segmentIndex segment index
     * @param segmentName  segment name
     * @param totalRecords total records in segment
     * @param pageNo       page number starting from 1
     * @param pageSize     page size
     * @param attributes   optional segment attributes
     * @param items        page items
     */
    public record Slice(int segmentIndex, String segmentName, long totalRecords, int pageNo, int pageSize,
            Map<String, String> attributes, List<Record> items) {
    }

    /**
     * Single archive record.
     *
     * @param recordIndex logical record index
     * @param payload     serialized payload
     */
    public record Record(long recordIndex, byte[] payload) {

    }

    /**
     * Binary archive writer used to persist segment data and the manifest.
     */
    public static class Writer implements AutoCloseable {

        /**
         * Root directory containing the manifest and segment files.
         */
        private final File rootDir;

        /**
         * External handle identifier associated with the archive.
         */
        private final String handleId;

        /**
         * Archive creation timestamp in milliseconds.
         */
        private final long createdAt;

        /**
         * Archive expiration timestamp in milliseconds.
         */
        private final long expiresAt;

        /**
         * Preview row limit stored in the manifest.
         */
        private final int previewRows;

        /**
         * Whether {@link #finish()} has already been called.
         */
        private boolean finished;

        /**
         * Mutable segment states keyed by segment index.
         */
        private final Map<Integer, SegmentState> states = new LinkedHashMap<>();

        /**
         * Creates an archive writer without a preview row limit.
         *
         * @param rootDir   root output directory
         * @param handleId  handle identifier
         * @param expiresAt expiration timestamp in milliseconds
         */
        public Writer(final File rootDir, final String handleId, final long expiresAt) {
            this(rootDir, handleId, expiresAt, 0);
        }

        /**
         * Creates an archive writer.
         *
         * @param rootDir     root output directory
         * @param handleId    handle identifier
         * @param expiresAt   expiration timestamp in milliseconds
         * @param previewRows preview row limit stored with the archive
         */
        public Writer(final File rootDir, final String handleId, final long expiresAt, final int previewRows) {
            this.rootDir = Assert.notNull(rootDir, "rootDir must not be null");
            this.handleId = Assert.notBlank(handleId, "handleId must not be blank");
            this.createdAt = System.currentTimeMillis();
            this.expiresAt = expiresAt;
            this.previewRows = Math.max(previewRows, 0);
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }
        }

        /**
         * Appends a single record to a segment.
         *
         * @param segmentIndex segment index
         * @param segmentName  segment name
         * @param recordIndex  logical record index in the segment
         * @param payload      serialized payload bytes
         * @throws IOException if writing fails
         */
        public void append(
                final int segmentIndex,
                final String segmentName,
                final long recordIndex,
                final byte[] payload) throws IOException {
            append(segmentIndex, segmentName, recordIndex, payload, Map.of());
        }

        /**
         * Appends a single record to a segment.
         *
         * @param segmentIndex segment index
         * @param segmentName  segment name
         * @param recordIndex  logical record index in the segment
         * @param payload      serialized payload bytes
         * @param attributes   segment attributes stored in the manifest
         * @throws IOException if writing fails
         */
        public void append(
                final int segmentIndex,
                final String segmentName,
                final long recordIndex,
                final byte[] payload,
                final Map<String, String> attributes) throws IOException {
            ensureWritable();
            final SegmentState state = resolveState(segmentIndex, segmentName, attributes);
            state.offsets.add((long) state.outputStream.size());
            state.outputStream.writeLong(recordIndex);
            final byte[] safePayload = null == payload ? new byte[0] : payload;
            state.outputStream.writeInt(safePayload.length);
            state.outputStream.write(safePayload);
            state.totalRecords++;
        }

        /**
         * Appends multiple records to a segment.
         *
         * @param segmentIndex     segment index
         * @param segmentName      segment name
         * @param startRecordIndex first logical record index in the segment
         * @param payloads         serialized payload bytes
         * @throws IOException if writing fails
         */
        public void appendAll(
                final int segmentIndex,
                final String segmentName,
                final long startRecordIndex,
                final List<byte[]> payloads) throws IOException {
            appendAll(segmentIndex, segmentName, startRecordIndex, payloads, Map.of());
        }

        /**
         * Appends multiple records to a segment.
         *
         * @param segmentIndex     segment index
         * @param segmentName      segment name
         * @param startRecordIndex first logical record index in the segment
         * @param payloads         serialized payload bytes
         * @param attributes       segment attributes stored in the manifest
         * @throws IOException if writing fails
         */
        public void appendAll(
                final int segmentIndex,
                final String segmentName,
                final long startRecordIndex,
                final List<byte[]> payloads,
                final Map<String, String> attributes) throws IOException {
            if (null == payloads || payloads.isEmpty()) {
                return;
            }
            long recordIndex = startRecordIndex;
            for (byte[] payload : payloads) {
                append(segmentIndex, segmentName, recordIndex++, payload, attributes);
            }
        }

        /**
         * Finalizes all segment files and writes the archive manifest.
         *
         * @return written manifest
         * @throws IOException if writing fails
         */
        public Manifest finish() throws IOException {
            ensureWritable();
            this.finished = true;
            final List<Segment> segments = new ArrayList<>();
            for (SegmentState state : this.states.values()) {
                state.outputStream.flush();
                state.outputStream.close();
                try (DataOutputStream indexStream = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(state.indexFile)))) {
                    indexStream.writeInt(state.offsets.size());
                    for (Long offset : state.offsets) {
                        indexStream.writeLong(null == offset ? 0L : offset);
                    }
                }
                segments.add(
                        new Segment(state.segmentIndex, state.segmentName, state.totalRecords, state.dataFile.getName(),
                                state.indexFile.getName(), state.attributes));
            }
            final Manifest manifest = new Manifest(this.handleId, this.createdAt, this.expiresAt, this.previewRows,
                    segments);
            try (ObjectOutputStream outputStream = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(new File(this.rootDir, "manifest.bin"))))) {
                outputStream.writeObject(manifest);
            }
            return manifest;
        }

        @Override
        public void close() throws IOException {
            for (SegmentState state : this.states.values()) {
                state.outputStream.close();
            }
        }

        /**
         * Ensures the writer can still accept new records.
         */
        private void ensureWritable() {
            if (this.finished) {
                throw new IllegalStateException("Archive.Writer has already been finished");
            }
        }

        /**
         * Resolves or creates mutable state for a segment.
         *
         * @param segmentIndex segment index
         * @param segmentName  segment name
         * @param attributes   segment attributes
         * @return mutable segment state
         * @throws IOException if opening the segment files fails
         */
        private SegmentState resolveState(
                final int segmentIndex,
                final String segmentName,
                final Map<String, String> attributes) throws IOException {
            SegmentState state = this.states.get(segmentIndex);
            if (null != state) {
                return state;
            }
            final String baseName = "segment-" + segmentIndex;
            final File dataFile = new File(this.rootDir, baseName + ".bin");
            final File indexFile = new File(this.rootDir, baseName + ".idx");
            state = new SegmentState(segmentIndex, null == segmentName ? "" : segmentName, dataFile, indexFile,
                    new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dataFile))), new ArrayList<>(),
                    Collections.unmodifiableMap(new LinkedHashMap<>(null == attributes ? Map.of() : attributes)), 0L);
            this.states.put(segmentIndex, state);
            return state;
        }

        /**
         * Mutable state for a segment being written.
         */
        private static final class SegmentState {

            /**
             * Zero-based segment index.
             */
            private final int segmentIndex;

            /**
             * Logical segment name.
             */
            private final String segmentName;

            /**
             * Segment payload file.
             */
            private final File dataFile;

            /**
             * Segment offset index file.
             */
            private final File indexFile;

            /**
             * Stream used to write segment payload data.
             */
            private final DataOutputStream outputStream;

            /**
             * Ordered payload offsets in the segment data file.
             */
            private final List<Long> offsets;

            /**
             * Immutable segment attributes stored in the manifest.
             */
            private final Map<String, String> attributes;

            /**
             * Number of records written to the segment.
             */
            private long totalRecords;

            /**
             * Creates mutable state for one segment writer.
             *
             * @param segmentIndex segment index
             * @param segmentName  segment name
             * @param dataFile     segment payload file
             * @param indexFile    segment offset index file
             * @param outputStream segment payload output stream
             * @param offsets      recorded payload offsets
             * @param attributes   immutable segment attributes
             * @param totalRecords current record count
             */
            private SegmentState(final int segmentIndex, final String segmentName, final File dataFile,
                    final File indexFile, final DataOutputStream outputStream, final List<Long> offsets,
                    final Map<String, String> attributes, final long totalRecords) {
                this.segmentIndex = segmentIndex;
                this.segmentName = segmentName;
                this.dataFile = dataFile;
                this.indexFile = indexFile;
                this.outputStream = outputStream;
                this.offsets = offsets;
                this.attributes = attributes;
                this.totalRecords = totalRecords;
            }
        }
    }

    /**
     * Binary archive reader used to load the manifest and slices from disk.
     */
    public static class Reader {

        /**
         * Reads the archive manifest from the supplied root directory.
         *
         * @param rootDir archive root directory
         * @return archive manifest
         * @throws IOException            if reading fails
         * @throws ClassNotFoundException if the manifest type cannot be resolved
         */
        public Manifest readManifest(final File rootDir) throws IOException, ClassNotFoundException {
            try (ObjectInputStream inputStream = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(new File(rootDir, "manifest.bin"))))) {
                return (Manifest) inputStream.readObject();
            }
        }

        /**
         * Reads one slice from a segment.
         *
         * @param rootDir      archive root directory
         * @param segmentIndex segment index
         * @param pageNo       page number starting from {@code 1}
         * @param pageSize     page size
         * @return archive slice
         * @throws IOException            if reading fails
         * @throws ClassNotFoundException if the manifest type cannot be resolved
         */
        public Slice readPage(final File rootDir, final int segmentIndex, final int pageNo, final int pageSize)
                throws IOException, ClassNotFoundException {
            final Manifest manifest = readManifest(rootDir);
            final Segment segment = manifest.segments().stream().filter(item -> item.segmentIndex() == segmentIndex)
                    .findFirst().orElseThrow(() -> new IOException("segment not found: " + segmentIndex));
            final int safePageNo = Math.max(pageNo, 1);
            final int safePageSize = Math.max(pageSize, 1);
            final long fromIndex = (long) (safePageNo - 1) * safePageSize;
            if (fromIndex >= segment.totalRecords()) {
                return new Slice(segment.segmentIndex(), segment.segmentName(), segment.totalRecords(), safePageNo,
                        safePageSize, segment.attributes(), List.of());
            }
            final int endExclusive = (int) Math.min(segment.totalRecords(), fromIndex + safePageSize);
            final long[] offsets = readOffsets(new File(rootDir, segment.indexFileName()));
            final List<Record> items = new ArrayList<>(Math.max(0, endExclusive - (int) fromIndex));
            try (RandomAccessFile dataFile = new RandomAccessFile(new File(rootDir, segment.dataFileName()), "r")) {
                for (int index = (int) fromIndex; index < endExclusive; index++) {
                    dataFile.seek(offsets[index]);
                    long recordIndex = dataFile.readLong();
                    int payloadLength = dataFile.readInt();
                    byte[] payload = new byte[Math.max(payloadLength, 0)];
                    if (payloadLength > 0) {
                        dataFile.readFully(payload);
                    }
                    items.add(new Record(recordIndex, payload));
                }
            }
            return new Slice(segment.segmentIndex(), segment.segmentName(), segment.totalRecords(), safePageNo,
                    safePageSize, segment.attributes(), items);
        }

        /**
         * Reads the ordered payload offsets from a segment index file.
         *
         * @param indexFile segment index file
         * @return ordered payload offsets
         * @throws IOException if reading fails
         */
        private long[] readOffsets(final File indexFile) throws IOException {
            try (DataInputStream inputStream = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(indexFile)))) {
                int size = inputStream.readInt();
                long[] offsets = new long[size];
                for (int index = 0; index < size; index++) {
                    offsets[index] = inputStream.readLong();
                }
                return offsets;
            }
        }
    }

}
