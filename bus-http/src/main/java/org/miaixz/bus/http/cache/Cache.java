/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.cache;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.AssignSink;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.AssignSource;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.UnoUrl;
import org.miaixz.bus.http.accord.platform.Platform;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.http.StatusLine;
import org.miaixz.bus.http.secure.CipherSuite;
import org.miaixz.bus.http.socket.Handshake;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;

/**
 * Caches HTTP and HTTPS responses to the filesystem so they can be reused, saving time and bandwidth.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Cache implements Closeable, Flushable {

    private static final int VERSION = 201105;
    private static final int ENTRY_METADATA = 0;
    private static final int ENTRY_BODY = 1;
    private static final int ENTRY_COUNT = 2;
    final DiskLruCache cache;
    int writeSuccessCount;
    int writeAbortCount;
    private int networkCount;
    private int hitCount;
    private int requestCount;
    /**
     * The internal cache implementation for use by the HTTP client.
     */
    public final InternalCache internalCache = new InternalCache() {

        /**
         * Retrieves a cached response for the given request.
         *
         * @param request The request to retrieve a cached response for.
         * @return The cached response, or null if not found.
         */
        @Override
        public Response get(Request request) {
            return Cache.this.get(request);
        }

        /**
         * Stores a response in the cache.
         *
         * @param response The response to store.
         * @return A cache request for writing the response body.
         */
        @Override
        public CacheRequest put(Response response) {
            return Cache.this.put(response);
        }

        /**
         * Removes a cached response for the given request.
         *
         * @param request The request to remove from the cache.
         * @throws IOException if an I/O error occurs.
         */
        @Override
        public void remove(Request request) throws IOException {
            Cache.this.remove(request);
        }

        /**
         * Updates a cached response with new network response metadata.
         *
         * @param cached  The cached response to update.
         * @param network The network response with new metadata.
         */
        @Override
        public void update(Response cached, Response network) {
            Cache.this.update(cached, network);
        }

        /**
         * Tracks a conditional cache hit.
         */
        @Override
        public void trackConditionalCacheHit() {
            Cache.this.trackConditionalCacheHit();
        }

        /**
         * Tracks the response cache strategy.
         *
         * @param cacheStrategy The cache strategy used.
         */
        @Override
        public void trackResponse(CacheStrategy cacheStrategy) {
            Cache.this.trackResponse(cacheStrategy);
        }
    };

    /**
     * Creates a cache in the specified {@code directory} with a maximum size of {@code maxSize} bytes.
     *
     * @param directory The directory to store the cache in.
     * @param maxSize   The maximum size of the cache in bytes.
     */
    public Cache(File directory, long maxSize) {
        this(directory, maxSize, DiskLruCache.DiskFile.SYSTEM);
    }

    /**
     * Creates a cache with the specified directory, max size, and disk file system.
     *
     * @param directory The directory to store the cache in.
     * @param maxSize   The maximum size of the cache in bytes.
     * @param diskFile  The disk file system to use.
     */
    public Cache(File directory, long maxSize, DiskLruCache.DiskFile diskFile) {
        this.cache = DiskLruCache.create(diskFile, directory, VERSION, ENTRY_COUNT, maxSize);
    }

    /**
     * Generates a unique and safe key for the cache from a URL.
     *
     * @param url The URL to generate a key for.
     * @return The cache key.
     */
    public static String key(UnoUrl url) {
        return ByteString.encodeUtf8(url.toString()).md5().hex();
    }

    /**
     * Reads an integer from a buffered source.
     *
     * @param source The buffered source to read from.
     * @return The integer value.
     * @throws IOException if an I/O error occurs or the format is invalid.
     */
    static int readInt(BufferSource source) throws IOException {
        try {
            long result = source.readDecimalLong();
            String line = source.readUtf8LineStrict();
            if (result < 0 || result > Integer.MAX_VALUE || !line.isEmpty()) {
                throw new IOException("expected an int but was \"" + result + line + "\"");
            }
            return (int) result;
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Retrieves a response from the cache for a given request.
     *
     * @param request The request to get the cached response for.
     * @return The cached response, or null if not found or invalid.
     */
    Response get(Request request) {
        String key = key(request.url());
        DiskLruCache.Snapshot snapshot;
        Entry entry;
        try {
            snapshot = cache.get(key);
            if (null == snapshot) {
                return null;
            }
        } catch (IOException e) {
            // Give up because the cache cannot be read.
            return null;
        }

        try {
            entry = new Entry(snapshot.getSource(ENTRY_METADATA));
        } catch (IOException e) {
            IoKit.close(snapshot);
            return null;
        }

        Response response = entry.response(snapshot);

        if (!entry.matches(request, response)) {
            IoKit.close(response.body());
            return null;
        }

        return response;
    }

    /**
     * Stores a response in the cache and returns a {@link CacheRequest} to write the body.
     *
     * @param response The response to store.
     * @return A {@link CacheRequest} to write the response body, or null if the response cannot be cached.
     */
    CacheRequest put(Response response) {
        String requestMethod = response.request().method();

        if (HTTP.invalidatesCache(response.request().method())) {
            try {
                remove(response.request());
            } catch (IOException ignored) {
                // Unable to write to cache.
            }
            return null;
        }
        if (!HTTP.GET.equals(requestMethod)) {
            // Don't cache non-GET responses. We could technically cache HEAD requests and POST requests that
            // have a 200 OK response, but the complexity of doing so is high and the benefit is low.
            return null;
        }

        if (Headers.hasVaryAll(response)) {
            return null;
        }

        Entry entry = new Entry(response);
        DiskLruCache.Editor editor = null;
        try {
            editor = cache.edit(key(response.request().url()));
            if (null == editor) {
                return null;
            }
            entry.writeTo(editor);
            return new CacheRequestImpl(editor);
        } catch (IOException e) {
            abortQuietly(editor);
            return null;
        }
    }

    /**
     * Removes an entry from the cache for a given request.
     *
     * @param request The request to remove from the cache.
     * @throws IOException if an I/O error occurs.
     */
    void remove(Request request) throws IOException {
        cache.remove(key(request.url()));
    }

    /**
     * Updates a stale cached entry with a new network response.
     *
     * @param cached  The stale cached response.
     * @param network The new network response.
     */
    void update(Response cached, Response network) {
        Entry entry = new Entry(network);
        DiskLruCache.Snapshot snapshot = ((CacheResponseBody) cached.body()).snapshot;
        DiskLruCache.Editor editor = null;
        try {
            editor = snapshot.edit(); // Returns null if snapshot is not current.
            if (editor != null) {
                entry.writeTo(editor);
                editor.commit();
            }
        } catch (IOException e) {
            abortQuietly(editor);
        }
    }

    /**
     * Safely aborts a cache edit.
     *
     * @param editor The editor to abort.
     */
    private void abortQuietly(DiskLruCache.Editor editor) {
        // Give up because the cache cannot be written.
        try {
            if (null != editor) {
                editor.abort();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Initializes the cache. This will include reading the journal file from storage and building up the necessary
     * in-memory cache information. Note that if the application chooses not to call this method to initialize the
     * cache, it will be initialized lazily on the first use of the cache.
     *
     * @throws IOException if an I/O error occurs during initialization.
     */
    public void initialize() throws IOException {
        cache.initialize();
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete all files in the cache directory,
     * including files that were not created by the cache.
     *
     * @throws IOException if an I/O error occurs during deletion.
     */
    public void delete() throws IOException {
        cache.delete();
    }

    /**
     * Deletes all values stored in the cache. Writes to the cache will still complete normally, but the corresponding
     * responses will not be stored.
     *
     * @throws IOException if an I/O error occurs during eviction.
     */
    public void evictAll() throws IOException {
        cache.evictAll();
    }

    /**
     * Returns an iterator over the URLs in this cache. This iterator supports {@linkplain Iterator#remove}. Removing a
     * URL from the iterator will remove the corresponding response from the cache. Use this to clear selected
     * responses.
     *
     * @return An iterator over the URLs in the cache.
     * @throws IOException if an I/O error occurs.
     */
    public Iterator<String> urls() throws IOException {
        return new Iterator<>() {

            final Iterator<DiskLruCache.Snapshot> delegate = cache.snapshots();
            String nextUrl;
            boolean canRemove;

            /**
             * Returns true if there are more URLs to iterate.
             *
             * @return true if there are more URLs.
             */
            @Override
            public boolean hasNext() {
                if (null != nextUrl)
                    return true;

                canRemove = false; // Prevent remove() on bogus content.
                while (delegate.hasNext()) {
                    try (DiskLruCache.Snapshot snapshot = delegate.next()) {
                        BufferSource metadata = IoKit.buffer(snapshot.getSource(ENTRY_METADATA));
                        nextUrl = metadata.readUtf8LineStrict();
                        return true;
                    } catch (IOException ignored) {
                        // Could not read the metadata for this snapshot; possibly because the host filesystem has
                        // disappeared! Skip it.
                    }
                }

                return false;
            }

            /**
             * Returns the next URL in the iteration.
             *
             * @return The next URL.
             */
            @Override
            public String next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                String result = nextUrl;
                nextUrl = null;
                canRemove = true;
                return result;
            }

            /**
             * Removes the current URL from the cache.
             */
            @Override
            public void remove() {
                if (!canRemove)
                    throw new IllegalStateException("remove() before next()");
                delegate.remove();
            }
        };
    }

    /**
     * Returns the number of writes to the cache that were aborted.
     *
     * @return The number of aborted writes.
     */
    public synchronized int writeAbortCount() {
        return writeAbortCount;
    }

    /**
     * Returns the number of writes to the cache that were successful.
     *
     * @return The number of successful writes.
     */
    public synchronized int writeSuccessCount() {
        return writeSuccessCount;
    }

    /**
     * Returns the current size of the cache in bytes.
     *
     * @return The current size of the cache.
     * @throws IOException if an I/O error occurs.
     */
    public long size() throws IOException {
        return cache.size();
    }

    /**
     * Returns the maximum size of the cache in bytes.
     *
     * @return The maximum size of the cache.
     */
    public long maxSize() {
        return cache.getMaxSize();
    }

    /**
     * Flushes the cache to disk.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        cache.flush();
    }

    /**
     * Closes the cache.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        cache.close();
    }

    /**
     * Returns the directory where the cache is stored.
     *
     * @return The cache directory.
     */
    public File directory() {
        return cache.getDirectory();
    }

    /**
     * Returns whether the cache is closed.
     *
     * @return {@code true} if the cache is closed.
     */
    public boolean isClosed() {
        return cache.isClosed();
    }

    /**
     * Tracks a response, updating cache statistics.
     *
     * @param cacheStrategy The cache strategy used for the response.
     */
    synchronized void trackResponse(CacheStrategy cacheStrategy) {
        requestCount++;

        if (null != cacheStrategy.networkRequest) {
            // If this is a conditional request, we'll increment hitCount if/when it hits.
            networkCount++;
        } else if (null != cacheStrategy.cacheResponse) {
            // This response uses the cache and not the network. That's a cache hit.
            hitCount++;
        }
    }

    /**
     * Tracks a conditional cache hit.
     */
    synchronized void trackConditionalCacheHit() {
        hitCount++;
    }

    /**
     * Returns the number of network requests made.
     *
     * @return The network request count.
     */
    public synchronized int networkCount() {
        return networkCount;
    }

    /**
     * Returns the number of cache hits.
     *
     * @return The cache hit count.
     */
    public synchronized int hitCount() {
        return hitCount;
    }

    /**
     * Returns the total number of requests made.
     *
     * @return The total request count.
     */
    public synchronized int requestCount() {
        return requestCount;
    }

    /**
     * An immutable snapshot of the metadata of a cached response.
     */
    private static final class Entry {

        /**
         * Synthetic response header: the local time when the request was sent.
         */
        private static final String SENT_MILLIS = Platform.get().getPrefix() + "-Sent-Millis";

        /**
         * Synthetic response header: the local time when the response was received.
         */
        private static final String RECEIVED_MILLIS = Platform.get().getPrefix() + "-Received-Millis";

        private final String url;
        private final Headers varyHeaders;
        private final String requestMethod;
        private final Protocol protocol;
        private final int code;
        private final String message;
        private final Headers responseHeaders;
        private final Handshake handshake;
        private final long sentRequestMillis;
        private final long receivedResponseMillis;

        /**
         * Reads an entry from an input stream.
         *
         * @param in The input source.
         * @throws IOException if an I/O error occurs.
         */
        Entry(Source in) throws IOException {
            try {
                BufferSource source = IoKit.buffer(in);
                url = source.readUtf8LineStrict();
                requestMethod = source.readUtf8LineStrict();
                Headers.Builder varyHeadersBuilder = new Headers.Builder();
                int varyRequestHeaderLineCount = readInt(source);
                for (int i = 0; i < varyRequestHeaderLineCount; i++) {
                    varyHeadersBuilder.addLenient(source.readUtf8LineStrict());
                }
                varyHeaders = varyHeadersBuilder.build();

                StatusLine statusLine = StatusLine.parse(source.readUtf8LineStrict());
                protocol = statusLine.protocol;
                code = statusLine.code;
                message = statusLine.message;
                Headers.Builder responseHeadersBuilder = new Headers.Builder();
                int responseHeaderLineCount = readInt(source);
                for (int i = 0; i < responseHeaderLineCount; i++) {
                    responseHeadersBuilder.addLenient(source.readUtf8LineStrict());
                }
                String sendRequestMillisString = responseHeadersBuilder.get(SENT_MILLIS);
                String receivedResponseMillisString = responseHeadersBuilder.get(RECEIVED_MILLIS);
                responseHeadersBuilder.removeAll(SENT_MILLIS);
                responseHeadersBuilder.removeAll(RECEIVED_MILLIS);
                sentRequestMillis = null != sendRequestMillisString ? Long.parseLong(sendRequestMillisString) : 0L;
                receivedResponseMillis = null != receivedResponseMillisString
                        ? Long.parseLong(receivedResponseMillisString)
                        : 0L;
                responseHeaders = responseHeadersBuilder.build();

                if (isHttps()) {
                    String blank = source.readUtf8LineStrict();
                    if (blank.length() > 0) {
                        throw new IOException("expected \"\" but was \"" + blank + "\"");
                    }
                    String cipherSuiteString = source.readUtf8LineStrict();
                    CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);
                    List<Certificate> peerCertificates = readCertificateList(source);
                    List<Certificate> localCertificates = readCertificateList(source);
                    TlsVersion tlsVersion = !source.exhausted() ? TlsVersion.forJavaName(source.readUtf8LineStrict())
                            : TlsVersion.SSLv3;
                    handshake = Handshake.get(tlsVersion, cipherSuite, peerCertificates, localCertificates);
                } else {
                    handshake = null;
                }
            } finally {
                in.close();
            }
        }

        /**
         * Creates a new entry from a response.
         *
         * @param response The response to create an entry from.
         */
        Entry(Response response) {
            this.url = response.request().url().toString();
            this.varyHeaders = Headers.varyHeaders(response);
            this.requestMethod = response.request().method();
            this.protocol = response.protocol();
            this.code = response.code();
            this.message = response.message();
            this.responseHeaders = response.headers();
            this.handshake = response.handshake();
            this.sentRequestMillis = response.sentRequestAtMillis();
            this.receivedResponseMillis = response.receivedResponseAtMillis();
        }

        /**
         * Writes this entry to a cache editor.
         *
         * @param editor The editor to write to.
         * @throws IOException if an I/O error occurs.
         */
        public void writeTo(DiskLruCache.Editor editor) throws IOException {
            BufferSink sink = IoKit.buffer(editor.newSink(ENTRY_METADATA));

            sink.writeUtf8(url).writeByte(Symbol.C_LF);
            sink.writeUtf8(requestMethod).writeByte(Symbol.C_LF);
            sink.writeDecimalLong(varyHeaders.size()).writeByte(Symbol.C_LF);
            for (int i = 0, size = varyHeaders.size(); i < size; i++) {
                sink.writeUtf8(varyHeaders.name(i)).writeUtf8(": ").writeUtf8(varyHeaders.value(i))
                        .writeByte(Symbol.C_LF);
            }

            sink.writeUtf8(new StatusLine(protocol, code, message).toString()).writeByte(Symbol.C_LF);
            sink.writeDecimalLong(responseHeaders.size() + 2).writeByte(Symbol.C_LF);
            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                sink.writeUtf8(responseHeaders.name(i)).writeUtf8(": ").writeUtf8(responseHeaders.value(i))
                        .writeByte(Symbol.C_LF);
            }
            sink.writeUtf8(SENT_MILLIS).writeUtf8(": ").writeDecimalLong(sentRequestMillis).writeByte(Symbol.C_LF);
            sink.writeUtf8(RECEIVED_MILLIS).writeUtf8(": ").writeDecimalLong(receivedResponseMillis)
                    .writeByte(Symbol.C_LF);

            if (isHttps()) {
                sink.writeByte(Symbol.C_LF);
                sink.writeUtf8(handshake.cipherSuite().javaName()).writeByte(Symbol.C_LF);
                writeCertList(sink, handshake.peerCertificates());
                writeCertList(sink, handshake.localCertificates());
                sink.writeUtf8(handshake.tlsVersion().javaName()).writeByte(Symbol.C_LF);
            }
            sink.close();
        }

        /**
         * Returns whether this entry is for an HTTPS response.
         *
         * @return {@code true} if the entry is for an HTTPS response.
         */
        private boolean isHttps() {
            return url.startsWith(Protocol.HTTPS_PREFIX);
        }

        /**
         * Reads a list of certificates from a buffered source.
         *
         * @param source The source to read from.
         * @return A list of certificates.
         * @throws IOException if an I/O error occurs.
         */
        private List<Certificate> readCertificateList(BufferSource source) throws IOException {
            int length = readInt(source);
            if (length == -1)
                return Collections.emptyList(); // Empty list.

            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                List<Certificate> result = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    String line = source.readUtf8LineStrict();
                    Buffer bytes = new Buffer();
                    bytes.write(ByteString.decodeBase64(line));
                    result.add(certificateFactory.generateCertificate(bytes.inputStream()));
                }
                return result;
            } catch (CertificateException e) {
                throw new IOException(e.getMessage());
            }
        }

        /**
         * Writes a list of certificates to a buffered sink.
         *
         * @param sink         The sink to write to.
         * @param certificates The list of certificates.
         * @throws IOException if an I/O error occurs.
         */
        private void writeCertList(BufferSink sink, List<Certificate> certificates) throws IOException {
            try {
                sink.writeDecimalLong(certificates.size()).writeByte(Symbol.C_LF);
                for (int i = 0, size = certificates.size(); i < size; i++) {
                    byte[] bytes = certificates.get(i).getEncoded();
                    String line = ByteString.of(bytes).base64();
                    sink.writeUtf8(line).writeByte(Symbol.C_LF);
                }
            } catch (CertificateEncodingException e) {
                throw new IOException(e.getMessage());
            }
        }

        /**
         * Returns whether this cache entry matches the given request.
         *
         * @param request  The request to match.
         * @param response The response to match.
         * @return {@code true} if the entry matches the request.
         */
        public boolean matches(Request request, Response response) {
            return url.equals(request.url().toString()) && requestMethod.equals(request.method())
                    && Headers.varyMatches(response, varyHeaders, request);
        }

        /**
         * Creates a response from this entry and a snapshot.
         *
         * @param snapshot The snapshot to create the response from.
         * @return The response.
         */
        public Response response(DiskLruCache.Snapshot snapshot) {
            String contentType = responseHeaders.get("Content-Type");
            String contentLength = responseHeaders.get("Content-Length");
            Request cacheRequest = new Request.Builder().url(url).method(requestMethod, null).headers(varyHeaders)
                    .build();
            return new Response.Builder().request(cacheRequest).protocol(protocol).code(code).message(message)
                    .headers(responseHeaders).body(new CacheResponseBody(snapshot, contentType, contentLength))
                    .handshake(handshake).sentRequestAtMillis(sentRequestMillis)
                    .receivedResponseAtMillis(receivedResponseMillis).build();
        }
    }

    /**
     * A response body sourced from the cache.
     */
    private static class CacheResponseBody extends ResponseBody {

        final DiskLruCache.Snapshot snapshot;
        private final BufferSource bodySource;
        private final String contentType;
        private final String contentLength;

        /**
         * Constructs a new cache response body.
         *
         * @param snapshot      The snapshot of the cache entry.
         * @param contentType   The content type.
         * @param contentLength The content length.
         */
        CacheResponseBody(final DiskLruCache.Snapshot snapshot, String contentType, String contentLength) {
            this.snapshot = snapshot;
            this.contentType = contentType;
            this.contentLength = contentLength;

            Source source = snapshot.getSource(ENTRY_BODY);
            bodySource = IoKit.buffer(new AssignSource(source) {

                /**
                 * Closes the snapshot and the underlying source.
                 *
                 * @throws IOException if an I/O error occurs.
                 */
                @Override
                public void close() throws IOException {
                    snapshot.close();
                    super.close();
                }
            });
        }

        /**
         * Returns the content type of the response body.
         *
         * @return The media type, or null if unknown.
         */
        @Override
        public MediaType contentType() {
            return null != contentType ? MediaType.valueOf(contentType) : null;
        }

        /**
         * Returns the content length of the response body.
         *
         * @return The content length in bytes, or -1 if unknown.
         */
        @Override
        public long contentLength() {
            try {
                return null != contentLength ? Long.parseLong(contentLength) : -1;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        /**
         * Returns the buffered source for reading the response body.
         *
         * @return The buffered source.
         */
        @Override
        public BufferSource source() {
            return bodySource;
        }
    }

    /**
     * A cache request that writes to the cache.
     */
    private final class CacheRequestImpl implements CacheRequest {

        private final DiskLruCache.Editor editor;
        private Sink cacheOut;
        private boolean done;
        private Sink body;

        /**
         * Constructs a new cache request.
         *
         * @param editor The editor for the cache entry.
         */
        CacheRequestImpl(final DiskLruCache.Editor editor) {
            this.editor = editor;
            this.cacheOut = editor.newSink(ENTRY_BODY);
            this.body = new AssignSink(cacheOut) {

                /**
                 * Commits the cache entry when the body is closed.
                 *
                 * @throws IOException if an I/O error occurs.
                 */
                @Override
                public void close() throws IOException {
                    synchronized (Cache.this) {
                        if (done) {
                            return;
                        }
                        done = true;
                        writeSuccessCount++;
                    }
                    super.close();
                    editor.commit();
                }
            };
        }

        /**
         * Aborts the cache request and discards any data written so far.
         */
        @Override
        public void abort() {
            synchronized (Cache.this) {
                if (done) {
                    return;
                }
                done = true;
                writeAbortCount++;
            }
            IoKit.close(cacheOut);
            try {
                editor.abort();
            } catch (IOException ignored) {
            }
        }

        /**
         * Returns the sink for writing the response body.
         *
         * @return The sink.
         */
        @Override
        public Sink body() {
            return body;
        }
    }

}
