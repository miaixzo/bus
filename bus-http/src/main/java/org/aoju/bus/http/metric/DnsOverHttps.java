/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.metric;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.*;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.http.bodys.ResponseBody;
import org.aoju.bus.http.cache.CacheControl;
import org.aoju.bus.http.metric.suffix.SuffixDatabase;
import org.aoju.bus.logger.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * DNS over HTTPS实施
 *
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
 */
public class DnsOverHttps implements DnsX {

    public static final int TYPE_A = 0x0001;
    public static final int TYPE_AAAA = 0x001c;
    public static final MediaType DNS_MESSAGE = MediaType.valueOf("application/dns-message");
    public static final int MAX_RESPONSE_SIZE = Normal._64 * Normal._1024;
    private static final byte SERVFAIL = 2;
    private static final byte NXDOMAIN = 3;
    private final Httpd client;
    private final UnoUrl url;
    private final boolean includeIPv6;
    private final boolean post;
    private final boolean resolvePrivateAddresses;
    private final boolean resolvePublicAddresses;

    DnsOverHttps(Builder builder) {
        if (null == builder.client) {
            throw new NullPointerException("client not set");
        }
        if (null == builder.url) {
            throw new NullPointerException("url not set");
        }

        this.url = builder.url;
        this.includeIPv6 = builder.includeIPv6;
        this.post = builder.post;
        this.resolvePrivateAddresses = builder.resolvePrivateAddresses;
        this.resolvePublicAddresses = builder.resolvePublicAddresses;
        this.client = builder.client.newBuilder().dns(buildBootstrapClient(builder)).build();
    }

    private static DnsX buildBootstrapClient(Builder builder) {
        List<InetAddress> hosts = builder.bootstrapDnsHosts;

        if (null != hosts) {
            return new BootstrapDns(builder.url.host(), hosts);
        } else {
            return builder.systemDns;
        }
    }

    public static ByteString encodeQuery(String host, int type) {
        Buffer buf = new Buffer();

        buf.writeShort(0);
        buf.writeShort(Normal._256);
        buf.writeShort(1);
        buf.writeShort(0);
        buf.writeShort(0);
        buf.writeShort(0);

        Buffer nameBuf = new Buffer();
        final String[] labels = host.split("\\.");
        for (String label : labels) {
            long utf8ByteCount = size(label, 0, label.length());
            if (utf8ByteCount != label.length()) {
                throw new IllegalArgumentException("non-ascii hostname: " + host);
            }
            nameBuf.writeByte((byte) utf8ByteCount);
            nameBuf.writeUtf8(label);
        }
        nameBuf.writeByte(0);

        nameBuf.copyTo(buf, 0, nameBuf.size());
        buf.writeShort(type);
        buf.writeShort(1);

        return buf.readByteString();
    }

    public static List<InetAddress> decodeAnswers(String hostname, ByteString byteString)
            throws Exception {
        List<InetAddress> result = new ArrayList<>();

        Buffer buf = new Buffer();
        buf.write(byteString);
        buf.readShort();

        final int flags = buf.readShort() & 0xffff;
        if (flags >> 15 == 0) {
            throw new IllegalArgumentException("not a response");
        }

        byte responseCode = (byte) (flags & 0xf);

        if (responseCode == NXDOMAIN) {
            throw new UnknownHostException(hostname + ": NXDOMAIN");
        } else if (responseCode == SERVFAIL) {
            throw new UnknownHostException(hostname + ": SERVFAIL");
        }

        final int questionCount = buf.readShort() & 0xffff;
        final int answerCount = buf.readShort() & 0xffff;
        buf.readShort();
        buf.readShort();

        for (int i = 0; i < questionCount; i++) {
            skipName(buf);
            buf.readShort();
            buf.readShort();
        }

        for (int i = 0; i < answerCount; i++) {
            skipName(buf);

            int type = buf.readShort() & 0xffff;
            buf.readShort();
            final int length = buf.readShort() & 0xffff;

            if (type == TYPE_A || type == TYPE_AAAA) {
                byte[] bytes = new byte[length];
                buf.read(bytes);
                result.add(InetAddress.getByAddress(bytes));
            } else {
                buf.skip(length);
            }
        }

        return result;
    }

    private static void skipName(Buffer in) throws EOFException {
        int length = in.readByte();

        if (length < 0) {
            in.skip(1);
        } else {
            while (length > 0) {
                in.skip(length);
                length = in.readByte();
            }
        }
    }

    public static long size(String string, int beginIndex, int endIndex) {
        if (null == string) throw new IllegalArgumentException("string == null");
        if (beginIndex < 0) throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException(
                    "endIndex > string.length: " + endIndex + " > " + string.length());
        }

        long result = 0;
        for (int i = beginIndex; i < endIndex; ) {
            int c = string.charAt(i);

            if (c < 0x80) {
                result++;
                i++;

            } else if (c < 0x800) {
                result += 2;
                i++;

            } else if (c < 0xd800 || c > 0xdfff) {
                result += 3;
                i++;

            } else {
                int low = i + 1 < endIndex ? string.charAt(i + 1) : 0;
                if (c > 0xdbff || low < 0xdc00 || low > 0xdfff) {
                    result++;
                    i++;

                } else {
                    result += 4;
                    i += 2;
                }
            }
        }

        return result;
    }

    static boolean isPrivateHost(String host) {
        return null == SuffixDatabase.get().getEffectiveTldPlusOne(host);
    }

    public UnoUrl url() {
        return url;
    }

    public boolean post() {
        return post;
    }

    public boolean includeIPv6() {
        return includeIPv6;
    }

    public Httpd client() {
        return client;
    }

    public boolean resolvePrivateAddresses() {
        return resolvePrivateAddresses;
    }

    public boolean resolvePublicAddresses() {
        return resolvePublicAddresses;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (!resolvePrivateAddresses || !resolvePublicAddresses) {
            boolean privateHost = isPrivateHost(hostname);

            if (privateHost && !resolvePrivateAddresses) {
                throw new UnknownHostException("private hosts not resolved");
            }

            if (!privateHost && !resolvePublicAddresses) {
                throw new UnknownHostException("public hosts not resolved");
            }
        }

        return lookupHttps(hostname);
    }

    private List<InetAddress> lookupHttps(String hostname) throws UnknownHostException {
        List<NewCall> networkRequests = new ArrayList<>(2);
        List<Exception> failures = new ArrayList<>(2);
        List<InetAddress> results = new ArrayList<>(5);

        buildRequest(hostname, networkRequests, results, failures, TYPE_A);

        if (includeIPv6) {
            buildRequest(hostname, networkRequests, results, failures, TYPE_AAAA);
        }

        executeRequests(hostname, networkRequests, results, failures);

        if (!results.isEmpty()) {
            return results;
        }

        return throwBestFailure(hostname, failures);
    }

    private void buildRequest(String hostname, List<NewCall> networkRequests, List<InetAddress> results,
                              List<Exception> failures, int type) {
        Request request = buildRequest(hostname, type);
        Response response = getCacheOnlyResponse(request);

        if (null != response) {
            processResponse(response, hostname, results, failures);
        } else {
            networkRequests.add(client.newCall(request));
        }
    }

    private void executeRequests(final String hostname, List<NewCall> networkRequests,
                                 final List<InetAddress> responses, final List<Exception> failures) {
        final CountDownLatch latch = new CountDownLatch(networkRequests.size());

        for (NewCall call : networkRequests) {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(NewCall call, IOException e) {
                    synchronized (failures) {
                        failures.add(e);
                    }
                    latch.countDown();
                }

                @Override
                public void onResponse(NewCall call, Response response) {
                    processResponse(response, hostname, responses, failures);
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            failures.add(e);
        }
    }

    private void processResponse(Response response, String hostname, List<InetAddress> results,
                                 List<Exception> failures) {
        try {
            List<InetAddress> addresses = readResponse(hostname, response);
            synchronized (results) {
                results.addAll(addresses);
            }
        } catch (Exception e) {
            synchronized (failures) {
                failures.add(e);
            }
        }
    }

    private List<InetAddress> throwBestFailure(String hostname, List<Exception> failures)
            throws UnknownHostException {
        if (failures.size() == 0) {
            throw new UnknownHostException(hostname);
        }

        Exception failure = failures.get(0);

        if (failure instanceof UnknownHostException) {
            throw (UnknownHostException) failure;
        }

        UnknownHostException unknownHostException = new UnknownHostException(hostname);
        unknownHostException.initCause(failure);

        for (int i = 1; i < failures.size(); i++) {
            org.aoju.bus.http.Builder.addSuppressedIfPossible(unknownHostException, failures.get(i));
        }

        throw unknownHostException;
    }

    private Response getCacheOnlyResponse(Request request) {
        if (!post && null != client.cache()) {
            try {
                Request cacheRequest = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();

                Response cacheResponse = client.newCall(cacheRequest).execute();

                if (cacheResponse.code() != 504) {
                    return cacheResponse;
                }
            } catch (IOException ioe) {
            }
        }

        return null;
    }

    private List<InetAddress> readResponse(String hostname, Response response) throws Exception {
        if (null == response.cacheResponse() && response.protocol() != Protocol.HTTP_2) {
            Logger.debug("Incorrect protocol: " + response.protocol(), null);
        }

        try {
            if (!response.isSuccessful()) {
                throw new IOException("response: " + response.code() + Symbol.SPACE + response.message());
            }

            ResponseBody body = response.body();

            if (body.contentLength() > MAX_RESPONSE_SIZE) {
                throw new IOException("response size exceeds limit ("
                        + MAX_RESPONSE_SIZE
                        + " bytes): "
                        + body.contentLength()
                        + " bytes");
            }

            ByteString responseBytes = body.source().readByteString();

            return decodeAnswers(hostname, responseBytes);
        } finally {
            response.close();
        }
    }

    private Request buildRequest(String hostname, int type) {
        Request.Builder requestBuilder = new Request.Builder().header(Header.ACCEPT, DNS_MESSAGE.toString());

        ByteString query = encodeQuery(hostname, type);

        if (post) {
            requestBuilder = requestBuilder.url(url).post(RequestBody.create(DNS_MESSAGE, query));
        } else {
            String encoded = query.base64Url().replace(Symbol.EQUAL, Normal.EMPTY);
            UnoUrl requestUrl = url.newBuilder().addQueryParameter("dns", encoded).build();

            requestBuilder = requestBuilder.url(requestUrl);
        }

        return requestBuilder.build();
    }

    public static final class Builder {

        Httpd client = null;

        UnoUrl url = null;
        boolean includeIPv6 = true;
        boolean post = false;
        DnsX systemDns = DnsX.SYSTEM;

        List<InetAddress> bootstrapDnsHosts = null;
        boolean resolvePrivateAddresses = false;
        boolean resolvePublicAddresses = true;

        public Builder() {
        }

        public DnsOverHttps build() {
            return new DnsOverHttps(this);
        }

        public Builder client(Httpd client) {
            this.client = client;
            return this;
        }

        public Builder url(UnoUrl url) {
            this.url = url;
            return this;
        }

        public Builder includeIPv6(boolean includeIPv6) {
            this.includeIPv6 = includeIPv6;
            return this;
        }

        public Builder post(boolean post) {
            this.post = post;
            return this;
        }

        public Builder resolvePrivateAddresses(boolean resolvePrivateAddresses) {
            this.resolvePrivateAddresses = resolvePrivateAddresses;
            return this;
        }

        public Builder resolvePublicAddresses(boolean resolvePublicAddresses) {
            this.resolvePublicAddresses = resolvePublicAddresses;
            return this;
        }

        public Builder bootstrapDnsHosts(List<InetAddress> bootstrapDnsHosts) {
            this.bootstrapDnsHosts = bootstrapDnsHosts;
            return this;
        }

        public Builder bootstrapDnsHosts(InetAddress... bootstrapDnsHosts) {
            return bootstrapDnsHosts(Arrays.asList(bootstrapDnsHosts));
        }

        public Builder systemDns(DnsX systemDns) {
            this.systemDns = systemDns;
            return this;
        }
    }

}