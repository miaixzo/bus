/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.offers;

import org.aoju.bus.http.Call;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.accord.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * Listener for metrics events. Extend this class to monitor the quantity, size, and duration of
 * your application's HTTP calls.
 *
 * <p>All start/connect/acquire events will eventually receive a matching end/release event,
 * either successful (non-null parameters), or failed (non-null throwable).  The first core
 * parameters of each event pair are used to link the event in case of concurrent or repeated
 * events e.g. dnsStart(call, domainName) -&gt; dnsEnd(call, domainName, inetAddressList).
 *
 * <p>Nesting is as follows
 * <ul>
 * <li>call -&gt; (dns -&gt; connect -&gt; secure connect)* -&gt; request events</li>
 * <li>call -&gt; (connection acquire/release)*</li>
 * </ul>
 *
 * <p>Request events are ordered:
 * requestHeaders -&gt; requestBody -&gt; responseHeaders -&gt; responseBody
 *
 * <p>Since connections may be reused, the dns and connect events may not be present for a call,
 * or may be repeated in case of failure retries, even concurrently in case of happy eyeballs type
 * scenarios. A redirect cross entity, or to use https may cause additional connection and request
 * events.
 *
 * <p>All event methods must execute fast, without external locking, cannot throw exceptions,
 * attempt to mutate the event parameters, or be reentrant back into the client.
 * Any IO - writing to files or network should be done asynchronously.
 *
 * @author Kimi Liu
 * @version 5.0.2
 * @since JDK 1.8+
 */
public abstract class EventListener {

    public static final EventListener NONE = new EventListener() {

    };

    public static EventListener.Factory factory(final EventListener listener) {
        return new EventListener.Factory() {
            public EventListener create(Call call) {
                return listener;
            }
        };
    }

    public void callStart(Call call) {

    }


    public void dnsStart(Call call, String domainName) {
    }

    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
    }

    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    }

    public void secureConnectStart(Call call) {
    }

    public void secureConnectEnd(Call call, Handshake handshake) {
    }

    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy,
                           Protocol protocol) {
    }

    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy,
                              Protocol protocol, IOException ioe) {
    }

    public void connectionAcquired(Call call, Connection connection) {
    }

    public void connectionReleased(Call call, Connection connection) {
    }

    public void requestHeadersStart(Call call) {
    }

    public void requestHeadersEnd(Call call, Request request) {
    }

    public void requestBodyStart(Call call) {
    }

    public void requestBodyEnd(Call call, long byteCount) {
    }

    public void responseHeadersStart(Call call) {
    }

    public void responseHeadersEnd(Call call, Response response) {
    }

    public void responseBodyStart(Call call) {
    }

    public void responseBodyEnd(Call call, long byteCount) {
    }

    public void callEnd(Call call) {
    }

    public void callFailed(Call call, IOException ioe) {
    }

    public interface Factory {

        EventListener create(Call call);
    }
}
