/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http.cache;

import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;

import java.io.File;
import java.io.IOException;
import java.net.CacheResponse;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * 一个由Android提供的类，这样它就可以继续支持带有
 * 统计信息的{@link java.net.ResponseCache}
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public class ResponseCache extends java.net.ResponseCache {

    private final Cache delegate;

    private ResponseCache(Cache delegate) {
        this.delegate = delegate;
    }

    public static ResponseCache create(File directory, long maxSize) {
        Cache cache = new Cache(directory, maxSize);
        return new ResponseCache(cache);
    }

    public boolean isEquivalent(File directory, long maxSize) {
        Cache installedCache = getCache();
        return (installedCache.directory().equals(directory)
                && installedCache.maxSize() == maxSize
                && !installedCache.isClosed());
    }

    public Cache getCache() {
        return delegate;
    }

    @Override
    public CacheResponse get(URI uri, String requestMethod,
                             Map<String, List<String>> requestHeaders) throws IOException {
        Request request = NetApiConvert.createRequest(uri, requestMethod, requestHeaders);
        Response response = delegate.internalCache.get(request);
        if (response == null) {
            return null;
        }
        return NetApiConvert.createJavaCacheResponse(response);
    }

    @Override
    public java.net.CacheRequest put(URI uri, URLConnection urlConnection) throws IOException {
        Response response = NetApiConvert.createResponseForCachePut(uri, urlConnection);
        if (response == null) {
            return null;
        }
        CacheRequest cacheRequest =
                delegate.internalCache.put(response);
        if (cacheRequest == null) {
            return null;
        }
        return NetApiConvert.createJavaCacheRequest(cacheRequest);
    }

    public long size() throws IOException {
        return delegate.size();
    }

    public long maxSize() {
        return delegate.maxSize();
    }

    public void flush() throws IOException {
        delegate.flush();
    }

    public int getNetworkCount() {
        return delegate.networkCount();
    }

    public int getHitCount() {
        return delegate.hitCount();
    }

    public int getRequestCount() {
        return delegate.requestCount();
    }

    public void close() throws IOException {
        delegate.close();
    }

    public void delete() throws IOException {
        delegate.delete();
    }

}