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

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.data.id.ID;

/**
 * Access point for creating, describing, fetching, and releasing archived bundle data.
 *
 * @param <T> item type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Broker<T> {

    /**
     * Creates an archived bundle for the supplied segments.
     *
     * @param segments    ordered segment list
     * @param previewRows preview row limit
     * @param ttlMillis   time to live in milliseconds
     * @return created bundle descriptor, or {@code null} when no segment data is available
     */
    Bundle.Paged<T> create(List<Bundle.Segment<T>> segments, int previewRows, long ttlMillis);

    /**
     * Resolves archived bundle metadata.
     *
     * @param handleId handle identifier
     * @return bundle descriptor, or {@code null} when the handle cannot be resolved
     */
    Bundle.Paged<T> describe(String handleId);

    /**
     * Fetches one page from the target segment.
     *
     * @param handleId     handle identifier
     * @param segmentIndex segment index
     * @param pageNo       page number starting from {@code 1}
     * @param pageSize     page size
     * @return paged result
     */
    Bundle.Paged<T> fetch(String handleId, int segmentIndex, int pageNo, int pageSize);

    /**
     * Releases archived bundle resources.
     *
     * @param handleId handle identifier
     * @return {@code true} if the handle directory no longer exists
     */
    boolean release(String handleId);

    /**
     * Removes expired archived bundles.
     *
     * @return number of removed handles
     */
    int clear();

    /**
     * File-backed {@link Broker} implementation.
     *
     * @param <T> item type
     */
    class FileBroker<T> implements Broker<T> {

        /**
         * Bundle codec used to convert items to and from archive payloads.
         */
        private final Bundle.Codec<T> codec;

        /**
         * Archive reader used to load persisted manifests and slices.
         */
        private final Archive.Reader reader = new Archive.Reader();

        /**
         * Creates a file-backed broker.
         *
         * @param codec item codec
         */
        public FileBroker(final Bundle.Codec<T> codec) {
            this.codec = codec;
        }

        /**
         * Opens a new streaming archive session.
         *
         * @param previewRows preview row limit
         * @param ttlMillis   handle time to live in milliseconds
         * @return opened session
         */
        public Session<T> open(final int previewRows, final long ttlMillis) {
            final String handleId = ID.objectId();
            final File handleDir = resolveHandleDir(handleId);
            final long expiresAt = Instant.now().plusMillis(Math.max(ttlMillis, 0L)).toEpochMilli();
            try {
                Archive.Writer writer = new Archive.Writer(handleDir, handleId, expiresAt, previewRows);
                return new Session<>(handleId, handleDir, expiresAt, Math.max(previewRows, 0), writer,
                        new LinkedHashMap<>(), 0L, 0L, false);
            } catch (Exception e) {
                deleteRecursively(handleDir);
                throw new IllegalStateException("Failed to open archived bundle session", e);
            }
        }

        /**
         * Appends a single item to the target segment.
         *
         * @param session      streaming session
         * @param segmentIndex segment index
         * @param segmentName  segment name
         * @param attributes   segment attributes
         * @param item         item to append
         */
        public void append(
                final Session<T> session,
                final int segmentIndex,
                final String segmentName,
                final Map<String, String> attributes,
                final T item) {
            if (item == null) {
                return;
            }
            try {
                ensureWritable(session);
                SessionSegmentState segmentState = resolveSegmentState(session, segmentIndex, segmentName, attributes);
                byte[] payload = this.codec.encode(item);
                session.writer().append(segmentIndex, segmentName, segmentState.totalItems(), payload, attributes);
                segmentState.increment(1L, payload != null ? payload.length : 0L);
                session.increment(1L, payload != null ? payload.length : 0L);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to append archived bundle item", e);
            }
        }

        /**
         * Appends multiple items to the target segment.
         *
         * @param session      streaming session
         * @param segmentIndex segment index
         * @param segmentName  segment name
         * @param attributes   segment attributes
         * @param items        items to append
         */
        public void appendAll(
                final Session<T> session,
                final int segmentIndex,
                final String segmentName,
                final Map<String, String> attributes,
                final List<T> items) {
            if (items == null || items.isEmpty()) {
                return;
            }
            try {
                ensureWritable(session);
                SessionSegmentState segmentState = resolveSegmentState(session, segmentIndex, segmentName, attributes);
                List<byte[]> payloads = new ArrayList<>(items.size());
                long writtenBytes = 0L;
                for (T item : items) {
                    byte[] payload = this.codec.encode(item);
                    payloads.add(payload);
                    writtenBytes += payload != null ? payload.length : 0L;
                }
                session.writer().appendAll(segmentIndex, segmentName, segmentState.totalItems(), payloads, attributes);
                segmentState.increment(items.size(), writtenBytes);
                session.increment(items.size(), writtenBytes);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to append archived bundle items", e);
            }
        }

        /**
         * Finishes the current session and returns the handle descriptor.
         *
         * @param session streaming session
         * @return handle descriptor
         */
        public Bundle.Paged<T> finish(final Session<T> session) {
            try {
                ensureWritable(session);
                Archive.Manifest manifest = session.writer().finish();
                session.markFinished();
                return Bundle.Paged.<T>builder().handleId(session.handleId()).segmentCount(manifest.segments().size())
                        .totalItems(session.writtenItems()).expiresAt(manifest.expiresAt())
                        .previewRows(manifest.previewRows()).total(session.writtenItems()).items(List.of()).build();
            } catch (Exception e) {
                abort(session);
                throw new IllegalStateException("Failed to finish archived bundle session", e);
            }
        }

        /**
         * Aborts the session and removes temporary files.
         *
         * @param session streaming session
         */
        public void abort(final Session<T> session) {
            if (session == null) {
                return;
            }
            try {
                if (session.writer() != null) {
                    session.writer().close();
                }
            } catch (Exception ignored) {
                // ignore close failure during abort
            }
            session.markFinished();
            deleteRecursively(session.handleDir());
        }

        /**
         * Creates an archived bundle in the local file system.
         *
         * @param segments    ordered segment list
         * @param previewRows preview row limit
         * @param ttlMillis   time to live in milliseconds
         * @return created bundle descriptor, or {@code null} when no segment data is available
         */
        @Override
        public Bundle.Paged<T> create(
                final List<Bundle.Segment<T>> segments,
                final int previewRows,
                final long ttlMillis) {
            if (null == segments || segments.isEmpty()) {
                return null;
            }
            final Session<T> session = open(previewRows, ttlMillis);
            try {
                for (Bundle.Segment<T> segment : segments) {
                    if (null == segment || null == segment.items() || segment.items().isEmpty()) {
                        continue;
                    }
                    appendAll(
                            session,
                            segment.segmentIndex(),
                            segment.segmentName(),
                            segment.attributes(),
                            segment.items());
                }
                return finish(session);
            } catch (Exception e) {
                abort(session);
                throw new IllegalStateException("Failed to create archived bundle", e);
            }
        }

        /**
         * Resolves archived bundle metadata from the local file system.
         *
         * @param handleId handle identifier
         * @return bundle descriptor, or {@code null} when the handle cannot be resolved
         */
        @Override
        public Bundle.Paged<T> describe(final String handleId) {
            if (null == handleId || handleId.isBlank()) {
                return null;
            }
            try {
                Archive.Manifest manifest = this.reader.readManifest(resolveHandleDir(handleId));
                long totalItems = manifest.segments().stream().mapToLong(item -> item.totalRecords()).sum();
                return Bundle.Paged.<T>builder().handleId(manifest.handleId()).segmentCount(manifest.segments().size())
                        .totalItems(totalItems).expiresAt(manifest.expiresAt()).previewRows(manifest.previewRows())
                        .total(totalItems).items(List.of()).build();
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Fetches one page from the target segment in the local file system.
         *
         * @param handleId     handle identifier
         * @param segmentIndex segment index
         * @param pageNo       page number starting from {@code 1}
         * @param pageSize     page size
         * @return paged result
         */
        @Override
        public Bundle.Paged<T> fetch(
                final String handleId,
                final int segmentIndex,
                final int pageNo,
                final int pageSize) {
            try {
                Archive.Slice slice = this.reader.readPage(resolveHandleDir(handleId), segmentIndex, pageNo, pageSize);
                List<T> items = new ArrayList<>(slice.items().size());
                for (Archive.Record record : slice.items()) {
                    items.add(this.codec.decode(record.payload()));
                }
                return Bundle.Paged.<T>builder().handleId(handleId).segmentIndex(slice.segmentIndex())
                        .segmentName(slice.segmentName()).pageNo(slice.pageNo()).pageSize(slice.pageSize())
                        .total(slice.totalRecords()).attributes(slice.attributes()).items(items).build();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to fetch archived bundle page", e);
            }
        }

        /**
         * Releases archived bundle resources from the local file system.
         *
         * @param handleId handle identifier
         * @return {@code true} if the handle directory no longer exists
         */
        @Override
        public boolean release(final String handleId) {
            final File handleDir = resolveHandleDir(handleId);
            deleteRecursively(handleDir);
            return !handleDir.exists();
        }

        /**
         * Removes expired archived bundles from the local file system.
         *
         * @return number of removed handles
         */
        @Override
        public int clear() {
            int removed = 0;
            long now = System.currentTimeMillis();
            File rootDir = resolveRootDir();
            File[] dirs = rootDir.listFiles(File::isDirectory);
            if (null == dirs) {
                return 0;
            }
            for (File dir : dirs) {
                try {
                    Archive.Manifest manifest = this.reader.readManifest(dir);
                    if (manifest.expiresAt() > 0 && manifest.expiresAt() < now) {
                        deleteRecursively(dir);
                        removed++;
                    }
                } catch (Exception ignored) {
                    // ignore broken handle and keep scanning
                }
            }
            return removed;
        }

        /**
         * Resolves the archive root directory.
         *
         * @return archive root directory
         */
        private File resolveRootDir() {
            File rootDir = new File(System.getProperty("java.io.tmpdir"), "page-store");
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }
            return rootDir;
        }

        /**
         * Resolves the handle directory inside the archive root directory.
         *
         * @param handleId handle identifier
         * @return handle directory
         */
        private File resolveHandleDir(final String handleId) {
            return new File(resolveRootDir(), handleId);
        }

        /**
         * Deletes a directory tree on a best-effort basis.
         *
         * @param path root path to delete
         */
        private void deleteRecursively(final File path) {
            if (null == path || !path.exists()) {
                return;
            }
            try {
                Files.walk(path.toPath()).sorted((left, right) -> right.getNameCount() - left.getNameCount())
                        .forEach(current -> {
                            try {
                                Files.deleteIfExists(current);
                            } catch (Exception ignored) {
                                // ignore best effort cleanup
                            }
                        });
            } catch (Exception ignored) {
                // ignore best effort cleanup
            }
        }

        /**
         * Ensures that the session can still accept writes.
         *
         * @param session streaming session
         */
        private void ensureWritable(final Session<T> session) {
            if (session == null) {
                throw new IllegalArgumentException("Session must not be null");
            }
            if (session.finished()) {
                throw new IllegalStateException("Broker session has already been finished");
            }
        }

        /**
         * Resolves or creates mutable state for the target segment.
         *
         * @param session      streaming session
         * @param segmentIndex segment index
         * @param segmentName  segment name
         * @param attributes   segment attributes
         * @return mutable segment state
         */
        private SessionSegmentState resolveSegmentState(
                final Session<T> session,
                final int segmentIndex,
                final String segmentName,
                final Map<String, String> attributes) {
            return session.segments().computeIfAbsent(
                    segmentIndex,
                    index -> new SessionSegmentState(segmentIndex, segmentName != null ? segmentName : "",
                            attributes != null ? Map.copyOf(attributes) : Map.of()));
        }

        /**
         * Streaming archive session.
         *
         * @param <T> item type
         */
        public static final class Session<T> {

            /**
             * Handle identifier.
             */
            private final String handleId;

            /**
             * Handle directory.
             */
            private final File handleDir;

            /**
             * Expiration timestamp.
             */
            private final long expiresAt;

            /**
             * Preview row limit.
             */
            private final int previewRows;

            /**
             * Archive writer.
             */
            private final Archive.Writer writer;

            /**
             * Mutable segment states.
             */
            private final Map<Integer, SessionSegmentState> segments;

            /**
             * Written item count.
             */
            private long writtenItems;

            /**
             * Written payload bytes.
             */
            private long writtenBytes;

            /**
             * Whether the session has been finished.
             */
            private boolean finished;

            /**
             * Creates a streaming session.
             *
             * @param handleId     handle identifier
             * @param handleDir    handle directory
             * @param expiresAt    expiration timestamp
             * @param previewRows  preview row limit
             * @param writer       archive writer
             * @param segments     mutable segment states
             * @param writtenItems written item count
             * @param writtenBytes written payload bytes
             * @param finished     completion flag
             */
            private Session(final String handleId, final File handleDir, final long expiresAt, final int previewRows,
                    final Archive.Writer writer, final Map<Integer, SessionSegmentState> segments,
                    final long writtenItems, final long writtenBytes, final boolean finished) {
                this.handleId = handleId;
                this.handleDir = handleDir;
                this.expiresAt = expiresAt;
                this.previewRows = previewRows;
                this.writer = writer;
                this.segments = segments;
                this.writtenItems = writtenItems;
                this.writtenBytes = writtenBytes;
                this.finished = finished;
            }

            /**
             * Returns the handle identifier.
             *
             * @return handle identifier
             */
            public String handleId() {
                return this.handleId;
            }

            /**
             * Returns the handle directory.
             *
             * @return handle directory
             */
            public File handleDir() {
                return this.handleDir;
            }

            /**
             * Returns the expiration timestamp.
             *
             * @return expiration timestamp
             */
            public long expiresAt() {
                return this.expiresAt;
            }

            /**
             * Returns the preview row limit.
             *
             * @return preview row limit
             */
            public int previewRows() {
                return this.previewRows;
            }

            /**
             * Returns the archive writer.
             *
             * @return archive writer
             */
            public Archive.Writer writer() {
                return this.writer;
            }

            /**
             * Returns mutable segment states.
             *
             * @return segment states
             */
            public Map<Integer, SessionSegmentState> segments() {
                return this.segments;
            }

            /**
             * Returns the written item count.
             *
             * @return written item count
             */
            public long writtenItems() {
                return this.writtenItems;
            }

            /**
             * Returns the written payload bytes.
             *
             * @return written payload bytes
             */
            public long writtenBytes() {
                return this.writtenBytes;
            }

            /**
             * Returns whether the session has been finished.
             *
             * @return {@code true} when finished
             */
            public boolean finished() {
                return this.finished;
            }

            /**
             * Increments write statistics.
             *
             * @param items written item count delta
             * @param bytes written payload byte delta
             */
            private void increment(final long items, final long bytes) {
                this.writtenItems += Math.max(items, 0L);
                this.writtenBytes += Math.max(bytes, 0L);
            }

            /**
             * Marks the session as finished.
             */
            private void markFinished() {
                this.finished = true;
            }
        }

        /**
         * Mutable session segment state.
         */
        private static final class SessionSegmentState {

            /**
             * Segment index.
             */
            private final int segmentIndex;

            /**
             * Segment name.
             */
            private final String segmentName;

            /**
             * Segment attributes.
             */
            private final Map<String, String> attributes;

            /**
             * Written item count.
             */
            private long totalItems;

            /**
             * Written payload bytes.
             */
            private long totalBytes;

            /**
             * Creates segment state.
             *
             * @param segmentIndex segment index
             * @param segmentName  segment name
             * @param attributes   segment attributes
             */
            private SessionSegmentState(final int segmentIndex, final String segmentName,
                    final Map<String, String> attributes) {
                this.segmentIndex = segmentIndex;
                this.segmentName = segmentName;
                this.attributes = attributes;
            }

            /**
             * Returns the written item count.
             *
             * @return written item count
             */
            private long totalItems() {
                return this.totalItems;
            }

            /**
             * Increments segment statistics.
             *
             * @param items item count delta
             * @param bytes payload byte delta
             */
            private void increment(final long items, final long bytes) {
                this.totalItems += Math.max(items, 0L);
                this.totalBytes += Math.max(bytes, 0L);
            }
        }
    }

}
