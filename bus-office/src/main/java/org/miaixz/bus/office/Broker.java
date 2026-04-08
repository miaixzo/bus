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
import java.util.List;

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
            final String handleId = ID.objectId();
            final File handleDir = resolveHandleDir(handleId);
            final long expiresAt = Instant.now().plusMillis(Math.max(ttlMillis, 0L)).toEpochMilli();
            try (Archive.Writer writer = new Archive.Writer(handleDir, handleId, expiresAt, previewRows)) {
                long totalItems = 0L;
                for (Bundle.Segment<T> segment : segments) {
                    if (null == segment || null == segment.items() || segment.items().isEmpty()) {
                        continue;
                    }
                    long recordIndex = 0L;
                    for (T item : segment.items()) {
                        writer.append(
                                segment.segmentIndex(),
                                segment.segmentName(),
                                recordIndex++,
                                this.codec.encode(item),
                                segment.attributes());
                    }
                    totalItems += segment.items().size();
                }
                Archive.Manifest manifest = writer.finish();
                return Bundle.Paged.<T>builder().handleId(handleId).segmentCount(manifest.segments().size())
                        .totalItems(totalItems).expiresAt(manifest.expiresAt()).previewRows(manifest.previewRows())
                        .total(totalItems).items(List.of()).build();
            } catch (Exception e) {
                deleteRecursively(handleDir);
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
    }

}
