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
package org.aoju.bus.http.metric;

import org.aoju.bus.http.NewCall;
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
 * @version 5.3.6
 * @since JDK 1.8+
 */
public abstract class EventListener {

    public static final EventListener NONE = new EventListener() {

    };

    public static EventListener.Factory factory(final EventListener listener) {
        return new EventListener.Factory() {
            public EventListener create(NewCall call) {
                return listener;
            }
        };
    }

    public void callStart(NewCall call) {

    }


    public void dnsStart(NewCall call, String domainName) {
    }

    public void dnsEnd(NewCall call, String domainName, List<InetAddress> inetAddressList) {
    }

    public void connectStart(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy) {
    }

    public void secureConnectStart(NewCall call) {
    }

    public void secureConnectEnd(NewCall call, Handshake handshake) {
    }

    public void connectEnd(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy,
                           Protocol protocol) {
    }

    public void connectFailed(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy,
                              Protocol protocol, IOException ioe) {
    }

    public void connectionAcquired(NewCall call, Connection connection) {
    }

    public void connectionReleased(NewCall call, Connection connection) {
    }

    public void requestHeadersStart(NewCall call) {
    }

    public void requestHeadersEnd(NewCall call, Request request) {
    }

    public void requestBodyStart(NewCall call) {
    }

    public void requestBodyEnd(NewCall call, long byteCount) {
    }

    public void responseHeadersStart(NewCall call) {
    }

    public void responseHeadersEnd(NewCall call, Response response) {
    }

    public void responseBodyStart(NewCall call) {
    }

    public void responseBodyEnd(NewCall call, long byteCount) {
    }

    public void callEnd(NewCall call) {
    }

    public void callFailed(NewCall call, IOException ioe) {
    }

    public interface Factory {

        EventListener create(NewCall call);
    }
}
