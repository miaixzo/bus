/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.core.net.Header;
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
 * 缓存HTTP和HTTPS对文件系统的响应，以便可以重用它们，从而节省时间和带宽.
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

    public final InternalCache internalCache = new InternalCache() {

        @Override
        public Response get(Request request) {
            return Cache.this.get(request);
        }

        @Override
        public CacheRequest put(Response response) {
            return Cache.this.put(response);
        }

        @Override
        public void remove(Request request) throws IOException {
            Cache.this.remove(request);
        }

        @Override
        public void update(Response cached, Response network) {
            Cache.this.update(cached, network);
        }

        @Override
        public void trackConditionalCacheHit() {
            Cache.this.trackConditionalCacheHit();
        }

        @Override
        public void trackResponse(CacheStrategy cacheStrategy) {
            Cache.this.trackResponse(cacheStrategy);
        }
    };

    /**
     * 在{@code directory}中创建最多{@code maxSize}字节的缓存
     *
     * @param directory 目录
     * @param maxSize   缓存的最大大小(以字节为单位)
     */
    public Cache(File directory, long maxSize) {
        this(directory, maxSize, DiskLruCache.DiskFile.SYSTEM);
    }

    public Cache(File directory, long maxSize, DiskLruCache.DiskFile diskFile) {
        this.cache = DiskLruCache.create(diskFile, directory, VERSION, ENTRY_COUNT, maxSize);
    }

    public static String key(UnoUrl url) {
        return ByteString.encodeUtf8(url.toString()).md5().hex();
    }

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
            // 放弃，因为缓存无法读取
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

    CacheRequest put(Response response) {
        String requestMethod = response.request().method();

        if (HTTP.invalidatesCache(response.request().method())) {
            try {
                remove(response.request());
            } catch (IOException ignored) {
                // 无法写入缓存
            }
            return null;
        }
        if (!HTTP.GET.equals(requestMethod)) {
            // 不要缓存非get响应。从技术上讲，我们可以缓存HEAD请求和POST请求，但是这样做的复杂性很高，好处很少
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

    void remove(Request request) throws IOException {
        cache.remove(key(request.url()));
    }

    void update(Response cached, Response network) {
        Entry entry = new Entry(network);
        DiskLruCache.Snapshot snapshot = ((CacheResponseBody) cached.body()).snapshot;
        DiskLruCache.Editor editor = null;
        try {
            // 如果快照不是当前的，则返回null
            editor = snapshot.edit(); // Returns null if snapshot is not current.
            if (editor != null) {
                entry.writeTo(editor);
                editor.commit();
            }
        } catch (IOException e) {
            abortQuietly(editor);
        }
    }

    private void abortQuietly(DiskLruCache.Editor editor) {
        // 放弃，因为缓存无法写入
        try {
            if (null != editor) {
                editor.abort();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * 始化缓存。这将包括从存储器中读取日志文件并构建必要的内存缓存信息
     * 注意，如果应用程序选择不调用此方法来初始化缓存。默认情况下，将在第一次使用缓存时执行延迟初始化
     *
     * @throws IOException 初始化异常
     */
    public void initialize() throws IOException {
        cache.initialize();
    }

    /**
     * 关闭缓存并删除其所有存储值。这将删除缓存目录中的所有文件，包括没有由缓存创建的文件
     *
     * @throws IOException 删除异常
     */
    public void delete() throws IOException {
        cache.delete();
    }

    /**
     * 删除缓存中存储的所有值。缓存中的写操作将正常完成，但不会存储相应的响应
     *
     * @throws IOException 清除异常
     */
    public void evictAll() throws IOException {
        cache.evictAll();
    }


    /**
     * 在此缓存中的url上返回一个迭代器,该迭代器支持{@linkplain Iterator#remove}。
     * 从迭代器中删除URL将从缓存中删除相应的响应。使用此来清除选定的响应
     *
     * @return 迭代器
     * @throws IOException 异常
     */
    public Iterator<String> urls() throws IOException {
        return new Iterator<>() {
            final Iterator<DiskLruCache.Snapshot> delegate = cache.snapshots();

            String nextUrl;
            boolean canRemove;

            @Override
            public boolean hasNext() {
                if (null != nextUrl) {
                    return true;
                }

                canRemove = false;
                // 删除()在错误的内容
                while (delegate.hasNext()) {
                    try (DiskLruCache.Snapshot snapshot = delegate.next()) {
                        BufferSource metadata = IoKit.buffer(snapshot.getSource(ENTRY_METADATA));
                        nextUrl = metadata.readUtf8LineStrict();
                        return true;
                    } catch (IOException ignored) {
                        // 无法读取此快照的元数据;可能是因为主机文件系统已经消失了!跳过它
                    }
                }

                return false;
            }

            @Override
            public String next() {
                if (!hasNext()) throw new NoSuchElementException();
                String result = nextUrl;
                nextUrl = null;
                canRemove = true;
                return result;
            }

            @Override
            public void remove() {
                if (!canRemove) throw new IllegalStateException("remove() before next()");
                delegate.remove();
            }
        };
    }

    public synchronized int writeAbortCount() {
        return writeAbortCount;
    }

    public synchronized int writeSuccessCount() {
        return writeSuccessCount;
    }

    public long size() throws IOException {
        return cache.size();
    }

    public long maxSize() {
        return cache.getMaxSize();
    }

    @Override
    public void flush() throws IOException {
        cache.flush();
    }

    @Override
    public void close() throws IOException {
        cache.close();
    }

    public File directory() {
        return cache.getDirectory();
    }

    public boolean isClosed() {
        return cache.isClosed();
    }

    synchronized void trackResponse(CacheStrategy cacheStrategy) {
        requestCount++;

        if (null != cacheStrategy.networkRequest) {
            // 如果这是一个条件请求，我们将增加hitCount如果/当它命中。
            networkCount++;
        } else if (null != cacheStrategy.cacheResponse) {
            // 此响应使用缓存而不是网络。这就是缓存命中
            hitCount++;
        }
    }

    synchronized void trackConditionalCacheHit() {
        hitCount++;
    }

    public synchronized int networkCount() {
        return networkCount;
    }

    public synchronized int hitCount() {
        return hitCount;
    }

    public synchronized int requestCount() {
        return requestCount;
    }

    private static class Entry {
        /**
         * 合成响应标头:请求发送时的本地时间
         */
        private static final String SENT_MILLIS = Platform.get().getPrefix() + "-Sent-Millis";

        /**
         * 合成响应标头:接收到响应的本地时间
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
         * 从输入流中读取项。一个典型的案例:
         * <pre>{@code
         *   http://google.com/foo
         *   GET
         *   2
         *   Accept-Language: fr-CA
         *   Accept-Charset: UTF-8
         *   HTTP/1.1 200 OK
         *   3
         *   Content-Type: image/png
         *   Content-Length: 100
         *   Cache-Control: max-age=600
         * }</pre>
         *
         * <p>HTTPS文件是这样的:
         * <pre>{@code
         *   https://google.com/foo
         *   GET
         *   2
         *   Accept-Language: fr-CA
         *   Accept-Charset: UTF-8
         *   HTTP/1.1 200 OK
         *   3
         *   Content-Type: image/png
         *   Content-Length: 100
         *   Cache-Control: max-age=600
         *
         *   AES_256_WITH_MD5
         *   2
         *   base64-encoded peerCertificate[0]
         *   base64-encoded peerCertificate[1]
         *   -1
         *   TLSv1.2
         * }</pre>
         *
         * @param in 输入流
         * @throws IOException 异常
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
                sentRequestMillis = null != sendRequestMillisString
                        ? Long.parseLong(sendRequestMillisString)
                        : 0L;
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
                    TlsVersion tlsVersion = !source.exhausted()
                            ? TlsVersion.forJavaName(source.readUtf8LineStrict())
                            : TlsVersion.SSLv3;
                    handshake = Handshake.get(tlsVersion, cipherSuite, peerCertificates, localCertificates);
                } else {
                    handshake = null;
                }
            } finally {
                in.close();
            }
        }

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

        public void writeTo(DiskLruCache.Editor editor) throws IOException {
            BufferSink sink = IoKit.buffer(editor.newSink(ENTRY_METADATA));

            sink.writeUtf8(url)
                    .writeByte(Symbol.C_LF);
            sink.writeUtf8(requestMethod)
                    .writeByte(Symbol.C_LF);
            sink.writeDecimalLong(varyHeaders.size())
                    .writeByte(Symbol.C_LF);
            for (int i = 0, size = varyHeaders.size(); i < size; i++) {
                sink.writeUtf8(varyHeaders.name(i))
                        .writeUtf8(": ")
                        .writeUtf8(varyHeaders.value(i))
                        .writeByte(Symbol.C_LF);
            }

            sink.writeUtf8(new StatusLine(protocol, code, message).toString())
                    .writeByte(Symbol.C_LF);
            sink.writeDecimalLong(responseHeaders.size() + 2)
                    .writeByte(Symbol.C_LF);
            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                sink.writeUtf8(responseHeaders.name(i))
                        .writeUtf8(": ")
                        .writeUtf8(responseHeaders.value(i))
                        .writeByte(Symbol.C_LF);
            }
            sink.writeUtf8(SENT_MILLIS)
                    .writeUtf8(": ")
                    .writeDecimalLong(sentRequestMillis)
                    .writeByte(Symbol.C_LF);
            sink.writeUtf8(RECEIVED_MILLIS)
                    .writeUtf8(": ")
                    .writeDecimalLong(receivedResponseMillis)
                    .writeByte(Symbol.C_LF);

            if (isHttps()) {
                sink.writeByte(Symbol.C_LF);
                sink.writeUtf8(handshake.cipherSuite().javaName())
                        .writeByte(Symbol.C_LF);
                writeCertList(sink, handshake.peerCertificates());
                writeCertList(sink, handshake.localCertificates());
                sink.writeUtf8(handshake.tlsVersion().javaName()).writeByte(Symbol.C_LF);
            }
            sink.close();
        }

        private boolean isHttps() {
            return url.startsWith(HTTP.HTTPS_PREFIX);
        }

        private List<Certificate> readCertificateList(BufferSource source) throws IOException {
            int length = readInt(source);
            if (length == -1) return Collections.emptyList();

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

        private void writeCertList(BufferSink sink, List<Certificate> certificates)
                throws IOException {
            try {
                sink.writeDecimalLong(certificates.size())
                        .writeByte(Symbol.C_LF);
                for (int i = 0, size = certificates.size(); i < size; i++) {
                    byte[] bytes = certificates.get(i).getEncoded();
                    String line = ByteString.of(bytes).base64();
                    sink.writeUtf8(line)
                            .writeByte(Symbol.C_LF);
                }
            } catch (CertificateEncodingException e) {
                throw new IOException(e.getMessage());
            }
        }

        public boolean matches(Request request, Response response) {
            return url.equals(request.url().toString())
                    && requestMethod.equals(request.method())
                    && Headers.varyMatches(response, varyHeaders, request);
        }

        public Response response(DiskLruCache.Snapshot snapshot) {
            String mediaType = responseHeaders.get(Header.CONTENT_TYPE);
            String length = responseHeaders.get(Header.CONTENT_LENGTH);
            Request request = new Request.Builder()
                    .url(url)
                    .method(requestMethod, null)
                    .headers(varyHeaders)
                    .build();
            return new Response.Builder()
                    .request(request)
                    .protocol(protocol)
                    .code(code)
                    .message(message)
                    .headers(responseHeaders)
                    .body(new CacheResponseBody(snapshot, mediaType, length))
                    .handshake(handshake)
                    .sentRequestAtMillis(sentRequestMillis)
                    .receivedResponseAtMillis(receivedResponseMillis)
                    .build();
        }
    }

    private static class CacheResponseBody extends ResponseBody {

        final DiskLruCache.Snapshot snapshot;
        private final BufferSource bodySource;
        private final String mediaType;
        private final String length;

        CacheResponseBody(final DiskLruCache.Snapshot snapshot,
                          String mediaType, String length) {
            this.snapshot = snapshot;
            this.mediaType = mediaType;
            this.length = length;

            Source source = snapshot.getSource(ENTRY_BODY);
            bodySource = IoKit.buffer(new AssignSource(source) {
                @Override
                public void close() throws IOException {
                    snapshot.close();
                    super.close();
                }
            });
        }

        @Override
        public MediaType mediaType() {
            return null != mediaType ? MediaType.valueOf(mediaType) : null;
        }

        @Override
        public long length() {
            try {
                return null != length ? Long.parseLong(length) : -1;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        @Override
        public BufferSource source() {
            return bodySource;
        }
    }

    private class CacheRequestImpl implements CacheRequest {

        private final DiskLruCache.Editor editor;
        boolean done;
        private Sink cacheOut;
        private Sink body;

        CacheRequestImpl(final DiskLruCache.Editor editor) {
            this.editor = editor;
            this.cacheOut = editor.newSink(ENTRY_BODY);
            this.body = new AssignSink(cacheOut) {
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

        @Override
        public Sink body() {
            return body;
        }
    }

}
