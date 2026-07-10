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
package org.miaixz.bus.fabric.cache;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.codec.stream.StreamSink;

/**
 * Disk-backed protocol-neutral cache store using DiskLruCache editor and snapshot storage.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DiskStore implements CacheStore {

    /**
     * Cache version.
     */
    private static final int VERSION = 20260706;

    /**
     * Metadata entry index.
     */
    private static final int ENTRY_METADATA = 0;

    /**
     * Body entry index.
     */
    private static final int ENTRY_BODY = 1;

    /**
     * Entry value count.
     */
    private static final int ENTRY_COUNT = 2;

    /**
     * Streaming copy buffer size.
     */
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * Maximum body size read through a known-length byte array path.
     */
    private static final long SMALL_BODY_THRESHOLD = 256L * 1024L;

    /**
     * Directory.
     */
    private final Path directory;

    /**
     * Disk LRU cache.
     */
    private final DiskLruCache cache;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Creates a store.
     *
     * @param directory directory
     * @param maxSize   max size
     */
    private DiskStore(final Path directory, final long maxSize) {
        this.directory = directory;
        this.cache = DiskLruCache
                .create(DiskLruCache.DiskFile.SYSTEM, directory.toFile(), VERSION, ENTRY_COUNT, maxSize);
        this.state = new AtomicReference<>(Status.OPENED);
    }

    /**
     * Opens a store.
     *
     * @param directory directory
     * @param maxSize   max size
     * @return store
     */
    public static DiskStore open(final Path directory, final long maxSize) {
        if (directory == null || maxSize <= 0) {
            throw new ValidateException("Disk cache directory must be non-null and max size positive");
        }
        final DiskStore store = new DiskStore(directory.toAbsolutePath(), maxSize);
        try {
            Files.createDirectories(store.directory);
            store.cache.initialize();
            return store;
        } catch (final IOException | RuntimeException e) {
            try {
                store.close();
            } catch (final RuntimeException ignored) {
                // Preserve the original open failure.
            }
            if (e instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new InternalException("Unable to open disk cache", e);
        }
    }

    /**
     * Initializes the underlying journal.
     */
    public void initialize() {
        ensureOpen();
        try {
            cache.initialize();
        } catch (final IOException e) {
            throw new InternalException("Unable to initialize disk cache", e);
        }
    }

    /**
     * Returns the cache directory.
     *
     * @return directory
     */
    public Path directory() {
        return directory;
    }

    /**
     * Returns current stored byte size.
     *
     * @return size
     */
    public long size() {
        ensureOpen();
        try {
            return cache.size();
        } catch (final IOException e) {
            throw new InternalException("Unable to read disk cache size", e);
        }
    }

    /**
     * Returns maximum stored byte size.
     *
     * @return max size
     */
    public long maxSize() {
        ensureOpen();
        return cache.getMaxSize();
    }

    /**
     * Gets a cached entry.
     *
     * @param key key
     * @return entry or null
     */
    @Override
    public CacheEntry get(final String key) {
        ensureOpen();
        final String checked = validateKey(key);
        try {
            final DiskLruCache.Snapshot snapshot = cache.get(name(checked));
            return snapshot == null ? null : read(snapshot);
        } catch (final IOException e) {
            throw new InternalException("Unable to read disk cache", e);
        }
    }

    /**
     * Stores an entry.
     *
     * @param key   key
     * @param entry entry
     */
    @Override
    public void put(final String key, final CacheEntry entry) {
        try (CacheWriter writer = writer(key, entry)) {
            writer.body().write(entry.payload());
            writer.commit();
        }
    }

    /**
     * Opens a streaming writer for one-shot bodies.
     *
     * @param key   key
     * @param entry entry metadata
     * @return writer
     */
    @Override
    public CacheWriter writer(final String key, final CacheEntry entry) {
        ensureOpen();
        final String checked = validateKey(key);
        if (entry == null) {
            throw new ValidateException("Cache entry must not be null");
        }
        DiskLruCache.Editor editor = null;
        try {
            editor = cache.edit(name(checked));
            if (editor == null) {
                return null;
            }
            writeMeta(editor.newSink(ENTRY_METADATA), checked, entry);
            return new DiskWriter(editor, new EntrySink(editor.newSink(ENTRY_BODY)));
        } catch (final IOException | RuntimeException e) {
            abortQuietly(editor);
            if (e instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new InternalException("Unable to open disk cache writer", e);
        }
    }

    /**
     * Removes an entry.
     *
     * @param key key
     */
    @Override
    public void remove(final String key) {
        ensureOpen();
        try {
            cache.remove(name(validateKey(key)));
        } catch (final IOException e) {
            throw new InternalException("Unable to remove disk cache", e);
        }
    }

    /**
     * Returns key snapshot.
     *
     * @return keys
     */
    @Override
    public Iterator<String> keys() {
        ensureOpen();
        try {
            final ArrayList<String> keys = new ArrayList<>();
            final Iterator<DiskLruCache.Snapshot> snapshots = cache.snapshots();
            while (snapshots.hasNext()) {
                try (DiskLruCache.Snapshot snapshot = snapshots.next()) {
                    keys.add(readMeta(snapshot.getSource(ENTRY_METADATA)).key());
                }
            }
            return keys.iterator();
        } catch (final IOException e) {
            throw new InternalException("Unable to list disk cache keys", e);
        }
    }

    /**
     * Trims entries by LRU order.
     */
    public void trim() {
        flush();
    }

    /**
     * Evicts all entries while keeping the cache open.
     */
    public void evictAll() {
        ensureOpen();
        try {
            cache.evictAll();
        } catch (final IOException e) {
            throw new InternalException("Unable to evict disk cache", e);
        }
    }

    /**
     * Closes and deletes all cache files.
     */
    public synchronized void delete() {
        final Status current = state.get();
        if (current == Status.CLOSED) {
            return;
        }
        state.set(Status.CLOSING);
        try {
            cache.delete();
            state.set(Status.CLOSED);
        } catch (final IOException e) {
            state.set(Status.CLOSED);
            throw new InternalException("Unable to delete disk cache", e);
        }
    }

    /**
     * Flushes journal state.
     */
    public void flush() {
        ensureOpen();
        try {
            cache.flush();
        } catch (final IOException e) {
            throw new InternalException("Unable to flush disk cache", e);
        }
    }

    /**
     * Closes this store.
     */
    @Override
    public synchronized void close() {
        final Status current = state.get();
        if (current == Status.CLOSED) {
            return;
        }
        if (!current.canTransit(Status.CLOSING)) {
            throw new StatefulException("Disk cache cannot close from state " + current);
        }
        state.set(Status.CLOSING);
        try {
            cache.close();
            state.set(Status.CLOSED);
        } catch (final IOException e) {
            state.set(Status.CLOSED);
            throw new InternalException("Unable to close disk cache", e);
        }
    }

    /**
     * Reads entry.
     *
     * @param snapshot cache snapshot
     * @return entry
     * @throws IOException when reading fails
     */
    private CacheEntry read(final DiskLruCache.Snapshot snapshot) throws IOException {
        final Metadata metadata = readMeta(snapshot.getSource(ENTRY_METADATA));
        final Payload payload = new SourcePayload(IoKit.buffer(snapshot.getSource(ENTRY_BODY)), snapshot,
                snapshot.getLength(ENTRY_BODY));
        return CacheEntry.of(metadata.metadata(), payload);
    }

    /**
     * Writes metadata.
     *
     * @param target target sink
     * @param key    key
     * @param entry  entry
     * @throws IOException when writing fails
     */
    private static void writeMeta(final Sink target, final String key, final CacheEntry entry) throws IOException {
        try (BufferSink sink = IoKit.buffer(target)) {
            sink.writeUtf8(key).writeByte(Symbol.C_LF);
            writeHeaders(sink, entry.metadata());
        }
    }

    /**
     * Writes headers.
     *
     * @param sink    sink
     * @param headers headers
     * @throws IOException when writing fails
     */
    private static void writeHeaders(final BufferSink sink, final Headers headers) throws IOException {
        final int count = headers.size();
        sink.writeDecimalLong(count).writeByte(Symbol.C_LF);
        for (int i = 0; i < count; i++) {
            sink.writeUtf8(headers.name(i)).writeByte(Symbol.C_LF);
            sink.writeUtf8(headers.value(i)).writeByte(Symbol.C_LF);
        }
    }

    /**
     * Reads headers.
     *
     * @param source source
     * @return headers
     * @throws IOException when reading fails
     */
    private static Headers readHeaders(final BufferSource source) throws IOException {
        final Headers.Builder builder = Headers.builder();
        final int count = readInt(source);
        for (int i = 0; i < count; i++) {
            builder.add(source.readUtf8LineStrict(), source.readUtf8LineStrict());
        }
        return builder.build();
    }

    /**
     * Reads metadata.
     *
     * @param source source
     * @return metadata
     * @throws IOException when loading fails
     */
    private static Metadata readMeta(final Source source) throws IOException {
        try (BufferSource input = IoKit.buffer(source)) {
            final String key = input.readUtf8LineStrict();
            return new Metadata(key, readHeaders(input));
        }
    }

    /**
     * Reads an integer line.
     *
     * @param source source
     * @return integer
     * @throws IOException when reading fails
     */
    private static int readInt(final BufferSource source) throws IOException {
        final long value = source.readDecimalLong();
        final String suffix = source.readUtf8LineStrict();
        if (value < 0 || value > Integer.MAX_VALUE || !suffix.isEmpty()) {
            throw new IOException("expected an int but was " + value + suffix);
        }
        return (int) value;
    }

    /**
     * Ensures store is open.
     */
    private void ensureOpen() {
        if (state.get().terminal()) {
            throw new StatefulException("Disk cache is closed");
        }
    }

    /**
     * Validates key.
     *
     * @param key key
     * @return key
     */
    private static String validateKey(final String key) {
        if (StringKit.isBlank(key) || StringKit.containsAny(key, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Cache key must be non-blank and single-line");
        }
        return key;
    }

    /**
     * Returns disk-safe file name for key.
     *
     * @param key key
     * @return name
     */
    private static String name(final String key) {
        try {
            final byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(validateKey(key).getBytes(StandardCharsets.UTF_8));
            final StringBuilder builder = new StringBuilder(digest.length * 2);
            for (final byte value : digest) {
                builder.append(Character.forDigit((value >>> 4) & 0xf, 16));
                builder.append(Character.forDigit(value & 0xf, 16));
            }
            return builder.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException("MD5 digest is unavailable", e);
        }
    }

    /**
     * Aborts an editor quietly.
     *
     * @param editor editor
     */
    private static void abortQuietly(final DiskLruCache.Editor editor) {
        if (editor == null) {
            return;
        }
        try {
            editor.abort();
        } catch (final IOException ignored) {
            // Best-effort abort keeps the original failure.
        }
    }

    /**
     * Cache metadata.
     *
     * @param key      key
     * @param metadata metadata
     */
    private record Metadata(String key, Headers metadata) {

    }

    /**
     * Stream sink backed by a buffered sink.
     */
    private static final class EntrySink implements StreamSink {

        /**
         * Sink.
         */
        private final Sink sink;

        /**
         * Reusable buffer.
         */
        private final Buffer buffer;

        /**
         * Written byte count.
         */
        private long written;

        /**
         * Closed flag.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a sink adapter.
         *
         * @param sink sink
         */
        private EntrySink(final Sink sink) {
            this.sink = sink;
            this.buffer = new Buffer();
        }

        /**
         * Writes the source buffer.
         *
         * @param source source buffer
         */
        @Override
        public void write(final ByteBuffer source) {
            if (source == null) {
                throw new ValidateException("Source buffer must not be null");
            }
            ensureOpen();
            final int count = source.remaining();
            try {
                if (source.hasArray()) {
                    buffer.write(source.array(), source.arrayOffset() + source.position(), count);
                    source.position(source.limit());
                    sink.write(buffer, count);
                    written += count;
                    return;
                }
                final byte[] bytes = new byte[Math.min(Math.max(count, 1), BUFFER_SIZE)];
                while (source.hasRemaining()) {
                    final int current = Math.min(source.remaining(), bytes.length);
                    source.get(bytes, 0, current);
                    buffer.write(bytes, 0, current);
                    sink.write(buffer, current);
                    written += current;
                }
            } catch (final IOException e) {
                throw new InternalException("Unable to write cache body", e);
            }
        }

        /**
         * Writes the payload.
         *
         * @param payload payload
         */
        @Override
        public void write(final Payload payload) {
            if (payload == null) {
                throw new ValidateException("Payload must not be null");
            }
            ensureOpen();
            try (Source input = IoKit.source(payload.stream())) {
                long read = input.read(buffer, BUFFER_SIZE);
                while (read != -1L) {
                    sink.write(buffer, read);
                    written += read;
                    read = input.read(buffer, BUFFER_SIZE);
                }
            } catch (final IOException e) {
                throw new InternalException("Unable to write cache payload", e);
            }
        }

        /**
         * Writes a source buffer directly to the buffered sink.
         *
         * @param source    source buffer
         * @param byteCount byte count
         * @throws IOException when writing fails
         */
        private void write(final Buffer source, final long byteCount) throws IOException {
            ensureOpen();
            sink.write(source, byteCount);
            written += byteCount;
        }

        /**
         * Returns written bytes.
         *
         * @return written bytes
         */
        @Override
        public long written() {
            return written;
        }

        /**
         * Flushes the sink.
         */
        @Override
        public void flush() {
            ensureOpen();
            try {
                sink.flush();
            } catch (final IOException e) {
                throw new InternalException("Unable to flush cache body", e);
            }
        }

        /**
         * Closes the sink.
         */
        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                sink.close();
            } catch (final IOException e) {
                throw new InternalException("Unable to close cache body", e);
            }
        }

        /**
         * Ensures this sink is open.
         */
        private void ensureOpen() {
            if (closed.get()) {
                throw new StatefulException("Cache body sink is closed");
            }
        }

    }

    /**
     * Streaming disk cache writer.
     */
    private static final class DiskWriter implements CacheWriter {

        /**
         * Editor.
         */
        private final DiskLruCache.Editor editor;

        /**
         * Body sink.
         */
        private final EntrySink body;

        /**
         * Finished flag.
         */
        private final AtomicBoolean finished = new AtomicBoolean();

        /**
         * Creates a writer.
         *
         * @param editor editor
         * @param body   body sink
         */
        private DiskWriter(final DiskLruCache.Editor editor, final EntrySink body) {
            this.editor = editor;
            this.body = body;
        }

        /**
         * Returns stream sink.
         *
         * @return stream sink
         */
        @Override
        public StreamSink body() {
            if (finished.get()) {
                throw new StatefulException("Disk cache writer is closed");
            }
            return body;
        }

        /**
         * Writes a bus-core buffer directly to the cache sink.
         *
         * @param source    source buffer
         * @param byteCount byte count
         * @throws IOException when writing fails
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            if (finished.get()) {
                throw new StatefulException("Disk cache writer is closed");
            }
            body.write(source, byteCount);
        }

        /**
         * Commits part files into the cache.
         */
        @Override
        public void commit() {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            try {
                body.close();
                editor.commit();
            } catch (final IOException e) {
                throw new InternalException("Unable to commit disk cache writer", e);
            }
        }

        /**
         * Aborts part files.
         */
        @Override
        public void abort() {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            try {
                body.close();
            } catch (final RuntimeException ignored) {
                // Best-effort abort keeps the original stream failure.
            }
            abortQuietly(editor);
        }

    }

    /**
     * Payload backed by a cache snapshot source.
     */
    private static final class SourcePayload implements Payload, AutoCloseable {

        /**
         * Body source.
         */
        private final BufferSource source;

        /**
         * Snapshot.
         */
        private final DiskLruCache.Snapshot snapshot;

        /**
         * Length.
         */
        private final long length;

        /**
         * Opened flag.
         */
        private final AtomicBoolean opened = new AtomicBoolean();

        /**
         * Creates a source payload.
         *
         * @param source   body source
         * @param snapshot snapshot
         * @param length   length
         */
        private SourcePayload(final BufferSource source, final DiskLruCache.Snapshot snapshot, final long length) {
            this.source = source;
            this.snapshot = snapshot;
            this.length = length;
        }

        /**
         * Returns body length.
         *
         * @return body length
         */
        @Override
        public long length() {
            return length;
        }

        /**
         * Opens the source stream once.
         *
         * @return input stream
         */
        @Override
        public InputStream stream() {
            if (!opened.compareAndSet(false, true)) {
                throw new StatefulException("Disk cache payload can only be opened once");
            }
            return source.inputStream();
        }

        /**
         * Reads all body bytes.
         *
         * @return bytes
         */
        @Override
        public byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads all body bytes with an explicit materialize threshold.
         *
         * @param maxBytes maximum bytes to materialize
         * @return bytes
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            try {
                return Payload.materialize(this, maxBytes, "DiskStore.SourcePayload.bytes(long)");
            } finally {
                try {
                    close();
                } catch (final IOException e) {
                    throw new InternalException("Unable to close disk cache payload", e);
                }
            }
        }

        /**
         * Reads body text.
         *
         * @param charset charset
         * @return text
         */
        @Override
        public String text(final java.nio.charset.Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public String text(final java.nio.charset.Charset charset, final long maxBytes) {
            if (charset == null) {
                throw new ValidateException("Charset must not be null");
            }
            return new String(bytes(maxBytes), charset);
        }

        /**
         * Reads the known-length body without first buffering the whole source.
         *
         * @return bytes
         * @throws IOException when reading fails
         */
        private byte[] readKnownLength() throws IOException {
            final int size = Math.toIntExact(length);
            final byte[] bytes = new byte[size];
            int offset = 0;
            while (offset < bytes.length) {
                final int read = source.read(bytes, offset, bytes.length - offset);
                if (read == -1) {
                    throw new IOException("Unexpected end of disk cache payload");
                }
                offset += read;
            }
            return bytes;
        }

        /**
         * Returns whether this payload can be opened more than once.
         *
         * @return false
         */
        @Override
        public boolean repeatable() {
            return false;
        }

        /**
         * Closes source and snapshot.
         */
        @Override
        public void close() throws IOException {
            try {
                source.close();
            } finally {
                snapshot.close();
            }
        }

    }

}
